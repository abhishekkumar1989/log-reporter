package pw.server.logreporter.api.annotation;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import pw.server.logreporter.util.NullChecker;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {

    private Map<String, Map<String, String>> urlRegistry = new HashMap<String, Map<String, String>>();
    private final Pattern VERSION_PREFIX = Pattern.compile("/\\*/([^/].*)");

    @Override
    protected void detectHandlers() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for URL mappings in application context: " + getApplicationContext());
        }
        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
        beanNames = getBeansSortedByVersion(beanNames);
        // Take any bean name that we can determine URLs for.
        for (String beanName : beanNames) {
            String[] urls = determineUrlsForHandler(beanName);
            if (!ObjectUtils.isEmpty(urls)) {
                // URL paths found: Let's consider it a handler.
                registerHandler(urls, beanName);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rejected bean name '" + beanName + "': no URL paths identified");
                }
            }
        }
    }

    private String[] getBeansSortedByVersion(String[] beanNames) {
        List<String> list = Arrays.asList(beanNames);
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String clazz1 = getBeanClassName(o1);
                String clazz2 = getBeanClassName(o2);

                APIVersion v1 = getBeanVersion(o1);
                APIVersion v2 = getBeanVersion(o2);

                if (v1 == null || v2 == null || !clazz1.equals(clazz2)) {
                    return clazz1.compareTo(clazz2);
                }

                return ((Float) v1.value()).compareTo((Float) v2.value());
            }
        });
        return list.toArray(new String[]{});
    }

    protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
        Assert.notNull(urlPaths, "URL path array must not be null");

        List<String> urlPathsAsList = Arrays.asList(urlPaths);
        Collection<String> urls = new HashSet<String>(urlPathsAsList);
        APIVersion version = getBeanVersion(beanName);
        if (NullChecker.isNotNull(version)) {
            urls = getStrippedUrls(urls);
            registerMissingUrls(version, beanName, urls);
            updateUrlRegistry(beanName, urls);
            urls = getVersionedUrls(urlPathsAsList, version.value());
            urls.addAll(getNonVersionedUrls(urlPathsAsList));
        }

        registerIndividualUrls(urls, beanName);
    }

    private String getVersionedUrl(String url, float value) {
        return "/v" + value + "/" + url;
    }

    private Collection<String> getVersionedUrls(Collection<String> urlPaths, float value) {
        Collection<String> urlsWithVersion = getStrippedUrls(urlPaths);
        Collection<String> urlsWithoutVersion = getNonVersionedUrls(urlPaths);

        Collection<String> versionedUrls = new HashSet<String>();
        for (String url : urlsWithVersion) {
            versionedUrls.add(getVersionedUrl(url, value));
        }
        versionedUrls.addAll(urlsWithoutVersion);
        return versionedUrls;
    }

    private Collection<String> getNonVersionedUrls(Collection<String> urlPaths) {
        Set<String> nonVersionedUrls = new HashSet<String>();

        for (String urlPath : urlPaths) {
            Matcher matcher = VERSION_PREFIX.matcher(urlPath);
            if (!matcher.matches()) {
                nonVersionedUrls.add(urlPath);
            }
        }
        return nonVersionedUrls;

    }


    private Collection<String> getStrippedUrls(Collection<String> urlsPaths) {

        Set<String> strippedUrls = new HashSet<String>();

        for (String urlPath : urlsPaths) {
            Matcher matcher = VERSION_PREFIX.matcher(urlPath);
            if (matcher.matches()) {
                strippedUrls.add(matcher.group(1));
            }
        }
        return strippedUrls;
    }

    private void registerIndividualUrls(Collection<String> urls, String beanName) {
        for (String url : urls) {
            registerHandler(url, beanName);
        }
    }

    private String getBeanClassName(String beanName) {
        Object bean = getApplicationContext().getBean(beanName);
        return ClassUtils.getShortName(bean.getClass());
    }

    private APIVersion getBeanVersion(String beanName) {
        Object bean = getApplicationContext().getBean(beanName);
        for (Annotation annotation : bean.getClass().getAnnotations()) {
            if (APIVersion.class.isInstance(annotation)) {
                return (APIVersion) annotation;
            }
        }
        return null;
    }

    private void updateUrlRegistry(String beanName, Collection<String> strippedUrls) {
        String beanClassName = getBeanClassName(beanName);
        Map<String, String> apiToBeanMap = urlRegistry.get(beanClassName);

        if (apiToBeanMap == null) {
            apiToBeanMap = new HashMap<String, String>();
            urlRegistry.put(beanClassName, apiToBeanMap);
        }

        for (String url : strippedUrls) {
            apiToBeanMap.put(url, beanName);
        }
    }

    private void registerMissingUrls(APIVersion version, String beanName, Collection<String> urls) {

        Map<String, String> apiToBeanMap = urlRegistry.get(getBeanClassName(beanName));

        if (NullChecker.isNull(apiToBeanMap)) {
            return;
        }

        for (Map.Entry<String, String> entry : apiToBeanMap.entrySet()) {
            String url = entry.getKey();

            //ignore the APIs which are already added for the latest version
            if (urls.contains(url)) {
                continue;
            }

            String oldBeanName = entry.getValue();

            //register the new version of the API to the old bean itself
            registerHandler(getVersionedUrl(url, version.value()), oldBeanName);
        }

    }
}
