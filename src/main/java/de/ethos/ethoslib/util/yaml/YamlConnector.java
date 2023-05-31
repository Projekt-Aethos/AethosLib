package de.ethos.ethoslib.util.yaml;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class YamlConnector {
    private final JavaPlugin plugin;

    private File file;

    public YamlConnector(JavaPlugin plugin, String... dirs) throws Exception {
        this.plugin = plugin;
        File file = plugin.getDataFolder();
        for (String dir : dirs) {
            if (!file.isDirectory()) {
                throw new Exception("No directory with path " + file.toPath() + " found");
            }
            file = new File(file.toPath() + System.getProperty("file.separator") + dir);
        }
    }

    public YamlConnector(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        if (file.isDirectory()) {
            this.file = file;
        } else {
            this.file = plugin.getDataFolder();
        }
    }

    public void save(Object object, String filename) throws IOException {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        FileWriter writer = new FileWriter(filename + ".yaml");
        yaml.dump(object, writer);
        writer.close();
    }

    public Object load(String filename) throws IOException {
        FileInputStream input = null;
        try {
            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            input = new FileInputStream(filename + ".yaml");
            return yaml.load(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

}
