package de.aethos.lib.blocks;


import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BlockType {
    String value();

    Class<? extends JavaPlugin> plugin();

    @SuppressWarnings("rawtypes") Class<? extends CustomBlockFactory> factory();


    interface Register {
        Map<String, CustomBlockFactory<?>> STRING_FACTORY_MAP = new ConcurrentHashMap<>();

        static void register(Class<? extends CustomBlock> cl) throws ReflectiveOperationException {
            Preconditions.checkArgument(cl.isAnnotationPresent(BlockType.class));
            final BlockType type = Objects.requireNonNull(cl.getAnnotation(BlockType.class));
            final CustomBlockFactory<?> factory = STRING_FACTORY_MAP.getOrDefault(type.value(), type.factory().getConstructor().newInstance());
            STRING_FACTORY_MAP.put(type.value(), factory);
        }

        @SuppressWarnings("unchecked")
        static <T extends CustomBlock> CustomBlockFactory<T> getFactory(Class<T> cl) {
            Preconditions.checkArgument(cl.isAnnotationPresent(BlockType.class));
            final BlockType type = Objects.requireNonNull(cl.getAnnotation(BlockType.class));
            try {

                return (CustomBlockFactory<T>) STRING_FACTORY_MAP.getOrDefault(type.value(), type.factory().getConstructor().newInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }

        static JavaPlugin getPlugin(BlockType type) {
            return JavaPlugin.getPlugin(type.plugin());
        }


    }


}
