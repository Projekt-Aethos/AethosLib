package de.ethos.ethoslib.packages;


import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;


public class PackageLoader<P extends JavaPlugin,T extends Package> extends URLClassLoader  {
    private final P plugin;

    public PackageLoader(P plugin) {
        super(new URL[]{}, PackageLoader.class.getClassLoader());
        this.plugin = plugin;
        final File dir = new File(plugin.getDataFolder(), "package");
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.toPath().endsWith(".jar")){
                try {
                    addURL(file.toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<T> load(){
        return Arrays.stream(getURLs()).map(this::load).filter(Objects::nonNull).toList();
    }
    public T load(URL url){
        try {
            final JarFile jar = new JarFile(new File(url.getFile()));
            final InputStream in = jar.getInputStream(jar.getEntry("package.yml"));
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            final Class<T> clazz = (Class<T>) loadClass(config.getString("main"));
            final String name = config.getString("name");
            final T pack = clazz.getConstructor().newInstance();
            assert name != null;
            pack.init(plugin,name);
            return pack;
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
