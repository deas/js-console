package de.fme.jsconsole;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.ClassPathStoreResourceResolver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptResources extends BaseProcessorExtension implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ScriptResources.class);
    private ApplicationContext applicationContext;
    private String[] urlIncludes;
    private String[] scanPattern;
    private boolean ignoreIncludeEx;
    private String[] repoXPaths;


    public void setRepoXPaths(String[] repoXPaths) {
        this.repoXPaths = repoXPaths;
    }

    public String[] getRepoXPaths() {
        return repoXPaths;
    }

    public void saveResource(String url, String content) throws Exception {
        logger.debug("Save {}", url);
        try  (PrintStream ps = new PrintStream(new FileOutputStream(Paths.get(new URL(url).toURI()).toFile()))) {
            ps.print(content);
        }
    }

    public void setIgnoreIncludeEx(boolean ignoreIncludeEx) {
        this.ignoreIncludeEx = ignoreIncludeEx;
    }

    public void setScanPattern(String[] scanPattern) {
        this.scanPattern = scanPattern;
    }

    public void setUrlIncludes(String[] urlIncludes) {
        this.urlIncludes = urlIncludes;
    }

    public String[] getAllResources() {
        List<String> allNames = new ArrayList<String>();
        allNames.addAll(Arrays.asList(getResources(this.scanPattern)));
        allNames.addAll(Arrays.asList(getUrlIncludes(this.urlIncludes)));
        return allNames.toArray(new String[allNames.size()]);
    }

    public String[] getResources() {
        return getResources(this.scanPattern);
    }

    public String[] getResources(String[] patterns) {
        List<String> resNames = new ArrayList<String>();
        ClassPathStoreResourceResolver cpsResolver = new ClassPathStoreResourceResolver(getApplicationContext());
        try {
            if (patterns != null) {
                for (String pat : patterns) {
                    Resource[] resources = cpsResolver.getResources(pat);
                    for (Resource res : resources) {
                        resNames.add(res.getURL().toString());
                    }
                }
            }
        } catch (IOException e) {
            new RuntimeException(e);
        }
        return resNames.toArray(new String[resNames.size()]);

    }

    public String getResourceContent(String urls) throws IOException {
        URLConnection connection = new URL(urls).openConnection();
        try (InputStream is = connection.getInputStream()) {
            if  (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                int statusCode = httpConn.getResponseCode();
                logger.warn("Got response code {} for {}", statusCode, urls);
                // TODO Should propagate to Client
                throw new IOException("Invalid status " + statusCode + " for " + urls);

            }
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : null;
        }
    }

    public String[] getUrlIncludes() {
        return getUrlIncludes(this.urlIncludes);
    }

    public String[] getUrlIncludes(String[] includes) {
        List<String> incNames = new ArrayList<String>();
        if (includes != null) {
            for (String url : includes) {
                try {
                    String[] urls = getResourceContent(url).split("\\r?\\n");
                    incNames.addAll(Arrays.asList(urls));
                } catch (IOException e) {
                    if (this.ignoreIncludeEx) {
                        logger.warn("Error loading include from {} : {}", url, e.getMessage());
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return incNames.toArray(new String[incNames.size()]);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
