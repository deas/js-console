package de.fme.jsconsole;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.ClassPathStoreResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScriptResources extends BaseProcessorExtension implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private List<String> scanPattern;

    public void setScanPattern(List<String> scanPattern) {
        this.scanPattern = scanPattern;
    }

    public String[] getScriptResources() {
        List<String> resNames = new ArrayList<String>();
        ClassPathStoreResourceResolver cpsResolver = new ClassPathStoreResourceResolver(getApplicationContext());
        try {
            if (this.scanPattern != null) {
                changed:
                for (String pat : scanPattern) {
                    Resource[] resources = cpsResolver.getResources(pat);
                    for (Resource res : resources) {
                        resNames.add(res.getURL().toString());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resNames.toArray(new String[resNames.size()]);

    }

    public String getResourceContent(String url) throws Exception {
        try (InputStream is = new URL(url).openStream()) {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
