package com.toocms.tab.toolkit;

/**
 * SystemUtils
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-15
 */
public class SystemUtils {

    public static final String JAVA_VERSION = getSystemProperty("java.version");

    public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");

    public static final boolean IS_JAVA_1_6 = getJavaVersionMatches("1.6");

    public static final int DEFAULT_THREAD_POOL_SIZE = getDefaultThreadPoolSize();

    public static int getDefaultThreadPoolSize() {
        return getDefaultThreadPoolSize(8);
    }

    public static int getDefaultThreadPoolSize(int max) {
        int availableProcessors = 2 * Runtime.getRuntime().availableProcessors() + 1;
        return availableProcessors > max ? max : availableProcessors;
    }

    private static boolean getJavaVersionMatches(final String versionPrefix) {
        return isJavaVersionMatch(JAVA_SPECIFICATION_VERSION, versionPrefix);
    }

    static boolean isJavaVersionMatch(final String version, final String versionPrefix) {
        if (version == null) {
            return false;
        }
        return version.startsWith(versionPrefix);
    }

    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            System.err.println("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }
}
