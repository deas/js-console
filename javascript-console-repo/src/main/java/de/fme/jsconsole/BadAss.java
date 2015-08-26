package de.fme.jsconsole;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by deas on 18.08.15.
 */
public class BadAss extends BaseScopableProcessorExtension {
    private static final Logger logger = LoggerFactory.getLogger(BadAss.class);
    private boolean  enabled;
    private boolean adminOnly;
    private AuthorityService authorityService;
    private ContentService contentService;

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    void securityCheck() {
        if (!enabled || (adminOnly && !authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser()))) {
            throw new RuntimeException("You are not allowed to use this stuff");
        }
    }

    File createTempFile(NodeRef nodeRef) throws IOException {
        return createTempFile(contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream());
    }


    File createTempFile(InputStream inputStream) throws IOException {
        File file = File.createTempFile("jsexec", ".tmp");
        try (InputStream is = inputStream;
             OutputStream os = new FileOutputStream(file);
        ) {
            IOUtils.copy(is, os);
        }
        return file;

    }

    public void copyFromFile(String filename, NodeRef nodeRef) throws Exception {
        securityCheck();
        logger.info("Copy {} to {}", nodeRef, filename);
        try (InputStream is = new FileInputStream(filename);
             OutputStream os = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).getContentOutputStream()) {
            IOUtils.copy(is, os);
        }
    }

    public void copyToFile(String filename, NodeRef nodeRef) throws Exception {
        securityCheck();
        logger.info("Copy {} to {}", nodeRef, filename);
        try (InputStream is = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
             OutputStream os = new FileOutputStream(filename)) {
            IOUtils.copy(is, os);
        }
    }

    public boolean deleteFile(String filename) throws Exception {
        securityCheck();
        logger.info("Delete {}", filename);
        return new File(filename).delete();
    }

    public Scriptable exec(String[] args, Long timeoutMs, NodeRef nodeRef) throws Exception {
        securityCheck();
        File file = nodeRef != null ? createTempFile(nodeRef) : null;
        RuntimeExec rtExec = new RuntimeExec();
        String[] fullArgs;
        if (file == null) {
            fullArgs = args;
        } else {
            List<String> l = new ArrayList<String>();
            l.addAll(Arrays.asList(args));
            l.add(file.getAbsolutePath());
            fullArgs = l.toArray(new String[l.size()]);
        }
        HashMap<String, String[]> commandAndArgs = new HashMap<String, String[]>();
        logger.info("Execute: {}", StringUtils.join(fullArgs, " "));
        commandAndArgs.put("*", fullArgs);
        rtExec.setCommandsAndArguments(commandAndArgs);
        rtExec.setWaitForCompletion(true);
        Map<String, String> defaultProperties = Collections.emptyMap();
        RuntimeExec.ExecutionResult result = rtExec.execute(defaultProperties, timeoutMs != null ? timeoutMs : -1);
        Context ctx = Context.getCurrentContext();
        Scriptable object = ctx.newObject(getScope());
        object.put("exitValue", object, result.getExitValue());
        Object stdOut = null;
        if (result.getExitValue() == 0) {
            try {
                stdOut = new JsonParser(ctx, getScope()).parseValue(result.getStdOut());
            } catch (Exception e) {
                logger.warn("Error parsing output from executable : " + e.getMessage());
            }
        }
        if (stdOut == null) {
            stdOut = result.getStdOut();
        }
        object.put("stdOut", object, stdOut);
        object.put("stdErr", object, result.getStdErr());
        if (file != null) {
            file.delete();
        }
        return object;
    }

    class JsonParser {

        private Context cx;
        private Scriptable scope;

        private int pos;
        private int length;
        private String src;

        public JsonParser(Context cx, Scriptable scope) {
            this.cx = cx;
            this.scope = scope;
        }

        public synchronized Object parseValue(String json) throws ParseException {
            if (json == null) {
                throw new ParseException("Input string may not be null");
            }
            pos = 0;
            length = json.length();
            src = json;
            Object value = readValue();
            consumeWhitespace();
            if (pos < length) {
                throw new ParseException("Expected end of stream at char " + pos);
            }
            return value;
        }

        private Object readValue() throws ParseException {
            consumeWhitespace();
            while (pos < length) {
                char c = src.charAt(pos++);
                switch (c) {
                    case '{':
                        return readObject();
                    case '[':
                        return readArray();
                    case 't':
                        return readTrue();
                    case 'f':
                        return readFalse();
                    case '"':
                        return readString();
                    case 'n':
                        return readNull();
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0':
                    case '-':
                        return readNumber(c);
                    default:
                        throw new ParseException("Unexpected token: " + c);
                }
            }
            throw new ParseException("Empty JSON string");
        }

        private Object readObject() throws ParseException {
            consumeWhitespace();
            Scriptable object = cx.newObject(scope);
            // handle empty object literal case early
            if (pos < length && src.charAt(pos) == '}') {
                pos += 1;
                return object;
            }
            String id;
            Object value;
            boolean needsComma = false;
            while (pos < length) {
                char c = src.charAt(pos++);
                switch (c) {
                    case '}':
                        if (!needsComma) {
                            throw new ParseException("Unexpected comma in object literal");
                        }
                        return object;
                    case ',':
                        if (!needsComma) {
                            throw new ParseException("Unexpected comma in object literal");
                        }
                        needsComma = false;
                        break;
                    case '"':
                        if (needsComma) {
                            throw new ParseException("Missing comma in object literal");
                        }
                        id = readString();
                        consume(':');
                        value = readValue();

                        long index = /*ScriptRuntime.*/indexFromString(id);
                        if (index < 0) {
                            object.put(id, object, value);
                        } else {
                            object.put((int) index, object, value);
                        }

                        needsComma = true;
                        break;
                    default:
                        throw new ParseException("Unexpected token in object literal");
                }
                consumeWhitespace();
            }
            throw new ParseException("Unterminated object literal");
        }

        public long indexFromString(String str) {
            // The length of the decimal string representation of
            //  Integer.MAX_VALUE, 2147483647
            final int MAX_VALUE_LENGTH = 10;

            int len = str.length();
            if (len > 0) {
                int i = 0;
                boolean negate = false;
                int c = str.charAt(0);
                if (c == '-') {
                    if (len > 1) {
                        c = str.charAt(1);
                        if (c == '0') return -1L; // "-0" is not an index
                        i = 1;
                        negate = true;
                    }
                }
                c -= '0';
                if (0 <= c && c <= 9
                        && len <= (negate ? MAX_VALUE_LENGTH + 1 : MAX_VALUE_LENGTH)) {
                    // Use negative numbers to accumulate index to handle
                    // Integer.MIN_VALUE that is greater by 1 in absolute value
                    // then Integer.MAX_VALUE
                    int index = -c;
                    int oldIndex = 0;
                    i++;
                    if (index != 0) {
                        // Note that 00, 01, 000 etc. are not indexes
                        while (i != len && 0 <= (c = str.charAt(i) - '0') && c <= 9) {
                            oldIndex = index;
                            index = 10 * index - c;
                            i++;
                        }
                    }
                    // Make sure all characters were consumed and that it couldn't
                    // have overflowed.
                    if (i == len &&
                            (oldIndex > (Integer.MIN_VALUE / 10) ||
                                    (oldIndex == (Integer.MIN_VALUE / 10) &&
                                            c <= (negate ? -(Integer.MIN_VALUE % 10)
                                                    : (Integer.MAX_VALUE % 10))))) {
                        return 0xFFFFFFFFL & (negate ? index : -index);
                    }
                }
            }
            return -1L;
        }

        private Object readArray() throws ParseException {
            consumeWhitespace();
            // handle empty array literal case early
            if (pos < length && src.charAt(pos) == ']') {
                pos += 1;
                return cx.newArray(scope, 0);
            }
            List<Object> list = new ArrayList<Object>();
            boolean needsComma = false;
            while (pos < length) {
                char c = src.charAt(pos);
                switch (c) {
                    case ']':
                        if (!needsComma) {
                            throw new ParseException("Unexpected comma in array literal");
                        }
                        pos += 1;
                        return cx.newArray(scope, list.toArray());
                    case ',':
                        if (!needsComma) {
                            throw new ParseException("Unexpected comma in array literal");
                        }
                        needsComma = false;
                        pos += 1;
                        break;
                    default:
                        if (needsComma) {
                            throw new ParseException("Missing comma in array literal");
                        }
                        list.add(readValue());
                        needsComma = true;
                }
                consumeWhitespace();
            }
            throw new ParseException("Unterminated array literal");
        }

        private String readString() throws ParseException {
        /*
         * Optimization: if the source contains no escaped characters, create the
         * string directly from the source text.
         */
            int stringStart = pos;
            while (pos < length) {
                char c = src.charAt(pos++);
                if (c <= '\u001F') {
                    throw new ParseException("String contains control character");
                } else if (c == '\\') {
                    break;
                } else if (c == '"') {
                    return src.substring(stringStart, pos - 1);
                }
            }

        /*
         * Slow case: string contains escaped characters.  Copy a maximal sequence
         * of unescaped characters into a temporary buffer, then an escaped
         * character, and repeat until the entire string is consumed.
         */
            StringBuilder b = new StringBuilder();
            while (pos < length) {
                assert src.charAt(pos - 1) == '\\';
                b.append(src, stringStart, pos - 1);
                if (pos >= length) {
                    throw new ParseException("Unterminated string");
                }
                char c = src.charAt(pos++);
                switch (c) {
                    case '"':
                        b.append('"');
                        break;
                    case '\\':
                        b.append('\\');
                        break;
                    case '/':
                        b.append('/');
                        break;
                    case 'b':
                        b.append('\b');
                        break;
                    case 'f':
                        b.append('\f');
                        break;
                    case 'n':
                        b.append('\n');
                        break;
                    case 'r':
                        b.append('\r');
                        break;
                    case 't':
                        b.append('\t');
                        break;
                    case 'u':
                        if (length - pos < 5) {
                            throw new ParseException("Invalid character code: \\u" + src.substring(pos));
                        }
                        int code = fromHex(src.charAt(pos + 0)) << 12
                                | fromHex(src.charAt(pos + 1)) << 8
                                | fromHex(src.charAt(pos + 2)) << 4
                                | fromHex(src.charAt(pos + 3));
                        if (code < 0) {
                            throw new ParseException("Invalid character code: " + src.substring(pos, pos + 4));
                        }
                        pos += 4;
                        b.append((char) code);
                        break;
                    default:
                        throw new ParseException("Unexpected character in string: '\\" + c + "'");
                }
                stringStart = pos;
                while (pos < length) {
                    c = src.charAt(pos++);
                    if (c <= '\u001F') {
                        throw new ParseException("String contains control character");
                    } else if (c == '\\') {
                        break;
                    } else if (c == '"') {
                        b.append(src, stringStart, pos - 1);
                        return b.toString();
                    }
                }
            }
            throw new ParseException("Unterminated string literal");
        }

        private int fromHex(char c) {
            return c >= '0' && c <= '9' ? c - '0'
                    : c >= 'A' && c <= 'F' ? c - 'A' + 10
                    : c >= 'a' && c <= 'f' ? c - 'a' + 10
                    : -1;
        }

        private Number readNumber(char c) throws ParseException {
            assert c == '-' || (c >= '0' && c <= '9');
            final int numberStart = pos - 1;
            if (c == '-') {
                c = nextOrNumberError(numberStart);
                if (!(c >= '0' && c <= '9')) {
                    throw numberError(numberStart, pos);
                }
            }
            if (c != '0') {
                readDigits();
            }
            // read optional fraction part
            if (pos < length) {
                c = src.charAt(pos);
                if (c == '.') {
                    pos += 1;
                    c = nextOrNumberError(numberStart);
                    if (!(c >= '0' && c <= '9')) {
                        throw numberError(numberStart, pos);
                    }
                    readDigits();
                }
            }
            // read optional exponent part
            if (pos < length) {
                c = src.charAt(pos);
                if (c == 'e' || c == 'E') {
                    pos += 1;
                    c = nextOrNumberError(numberStart);
                    if (c == '-' || c == '+') {
                        c = nextOrNumberError(numberStart);
                    }
                    if (!(c >= '0' && c <= '9')) {
                        throw numberError(numberStart, pos);
                    }
                    readDigits();
                }
            }
            String num = src.substring(numberStart, pos);
            final double dval = Double.parseDouble(num);
            final int ival = (int) dval;
            if (ival == dval) {
                return Integer.valueOf(ival);
            } else {
                return Double.valueOf(dval);
            }
        }

        private ParseException numberError(int start, int end) {
            return new ParseException("Unsupported number format: " + src.substring(start, end));
        }

        private char nextOrNumberError(int numberStart) throws ParseException {
            if (pos >= length) {
                throw numberError(numberStart, length);
            }
            return src.charAt(pos++);
        }

        private void readDigits() {
            for (; pos < length; ++pos) {
                char c = src.charAt(pos);
                if (!(c >= '0' && c <= '9')) {
                    break;
                }
            }
        }

        private Boolean readTrue() throws ParseException {
            if (length - pos < 3
                    || src.charAt(pos) != 'r'
                    || src.charAt(pos + 1) != 'u'
                    || src.charAt(pos + 2) != 'e') {
                throw new ParseException("Unexpected token: t");
            }
            pos += 3;
            return Boolean.TRUE;
        }

        private Boolean readFalse() throws ParseException {
            if (length - pos < 4
                    || src.charAt(pos) != 'a'
                    || src.charAt(pos + 1) != 'l'
                    || src.charAt(pos + 2) != 's'
                    || src.charAt(pos + 3) != 'e') {
                throw new ParseException("Unexpected token: f");
            }
            pos += 4;
            return Boolean.FALSE;
        }

        private Object readNull() throws ParseException {
            if (length - pos < 3
                    || src.charAt(pos) != 'u'
                    || src.charAt(pos + 1) != 'l'
                    || src.charAt(pos + 2) != 'l') {
                throw new ParseException("Unexpected token: n");
            }
            pos += 3;
            return null;
        }

        private void consumeWhitespace() {
            while (pos < length) {
                char c = src.charAt(pos);
                switch (c) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        pos += 1;
                        break;
                    default:
                        return;
                }
            }
        }

        private void consume(char token) throws ParseException {
            consumeWhitespace();
            if (pos >= length) {
                throw new ParseException("Expected " + token + " but reached end of stream");
            }
            char c = src.charAt(pos++);
            if (c == token) {
                return;
            } else {
                throw new ParseException("Expected " + token + " found " + c);
            }
        }

        class ParseException extends Exception {

            static final long serialVersionUID = 4804542791749920772L;

            ParseException(String message) {
                super(message);
            }

            ParseException(Exception cause) {
                super(cause);
            }
        }

    }

}
