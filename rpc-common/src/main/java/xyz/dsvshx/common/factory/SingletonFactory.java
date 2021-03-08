package xyz.dsvshx.common.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhonghua
 * Created on 2021-03-06
 */
public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new HashMap<>();

    private SingletonFactory() {
    }

    /**
     * 双重检验锁
     */
    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();
        Object instance = OBJECT_MAP.get(key);
        if (instance != null) {
            return c.cast(instance);
        }
        synchronized (SingletonFactory.class) {
            instance = OBJECT_MAP.get(key);
            if (instance == null) {
                try {
                    instance = c.getDeclaredConstructor().newInstance();
                    OBJECT_MAP.put(key, instance);
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return c.cast(instance);
    }
}

