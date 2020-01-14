package com.toocms.tab.toolkit;

import java.lang.reflect.Method;

/**
 * Author：Zero
 * Date：2020/1/13 15:32
 *
 * @version v1.0
 */
public class ClassUtils {

    public static Object invoke(final Object obj, final String methodName, final Class[] classes, final Object[] objects) {
        try {
            Method method = getMethod(obj.getClass(), methodName, classes);
            method.setAccessible(true);
            return method.invoke(obj, objects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class clazz, String methodName, final Class[] classes) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName, classes);
                }
            }
        }
        return method;
    }
}
