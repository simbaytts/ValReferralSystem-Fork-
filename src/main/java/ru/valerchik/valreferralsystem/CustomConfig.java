package ru.valerchik.valreferralsystem;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomConfig {
   private final File file;
   private FileConfiguration configuration;

   public CustomConfig(File file) {
      this.file = file;
      this.configuration = YamlConfiguration.loadConfiguration(file);
   }

   public FileConfiguration getConfiguration() {
      return this.configuration;
   }

   public void reload() {
      this.configuration = YamlConfiguration.loadConfiguration(this.file);
   }

   public void save() {
      try {
         this.configuration.save(this.file);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void save(File file) {
      try {
         this.configuration.save(file);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public String getString(String path) {
      return this.configuration.getString(path);
   }

   public void set(String path, Object value) {
      this.configuration.set(path, value);
   }
}
