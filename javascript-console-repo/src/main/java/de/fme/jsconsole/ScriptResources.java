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
import java.util.Arrays;
import java.util.List;

public class ScriptResources extends BaseProcessorExtension implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private String[] urlIncludes;
    private String[] scanPattern;

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
                } catch (Exception e) {
                    throw new RuntimeException(e);
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