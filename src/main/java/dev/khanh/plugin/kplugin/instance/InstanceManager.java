package dev.khanh.plugin.kplugin.instance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code InstanceManager} class provides a centralized mechanism for managing
 * singleton instances of various classes. It uses a concurrent hash map to store and retrieve
 * instances by their class type. This ensures that only one instance of each class type
 * is managed by the system.
 */
public class InstanceManager {

    /**
     * A thread-safe map holding instances of various classes, keyed by their {@link Class} objects.
     * This map is used to store singleton instances.
     */
    private static final Map<Class<?>, Object> INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * Removes all instances from the {@code INSTANCE_MAP}, effectively clearing
     * all managed singleton instances.
     */
    public static void clearInstances() {
        INSTANCE_MAP.clear();
    }

    /**
     * Registers a singleton instance of the specified class.
     *
     * @param <T>       the type of the class
     * @param clazz     the class of the instance to be registered
     * @param instance  the instance to be registered
     * @return the registered instance
     * @throws NullPointerException if the specified class or instance is {@code null}
     */
    public static <T> T registerInstance(Class<? extends T> clazz, T instance) {
        INSTANCE_MAP.put(clazz, instance);
        return instance;
    }

    /**
     * Retrieves the singleton instance of the specified class, if it exists.
     *
     * @param <T>   the type of the class
     * @param clazz the class of the instance to be retrieved
     * @return the instance of the specified class, or {@code null} if no such instance exists
     */
    @Nullable
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = INSTANCE_MAP.get(clazz);
        if (instance == null) {
            return null;
        }
        return clazz.cast(instance);
    }


    /**
     * Retrieves the singleton instance of the specified class, or throws an exception if it doesn't exist.
     *
     * @param <T>   the type of the class
     * @param clazz the class of the instance to be retrieved
     * @return the instance of the specified class
     * @throws NoSuchElementException  if no instance of the specified class exists
     */
    @NotNull
    public static <T> T getInstanceOrElseThrow(Class<T> clazz) {
        Object instance = INSTANCE_MAP.get(clazz);
        if (instance == null) {
            throw new NoSuchElementException("No such instance: " + clazz.getName());
        }
        return clazz.cast(instance);
    }

    /**
     * Removes the singleton instance of the specified class, if it exists.
     *
     * @param <T>   the type of the class
     * @param clazz the class of the instance to be removed
     */
    public static <T> void removeInstance(Class<T> clazz) {
        INSTANCE_MAP.remove(clazz);
    }
}
