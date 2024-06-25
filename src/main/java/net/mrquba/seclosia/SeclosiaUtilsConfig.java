package net.mrquba.seclosia;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class SeclosiaUtilsConfig {
    public static final File LOCATIONS_FILE = new File("config/seclosia_locations.properties");
    public static final File COMMANDS_FILE = new File("config/seclosia_commands.properties");
    public static final File MACROS_FILE = new File("config/seclosia_macros.properties");
    private static Properties properties = new Properties();

    // Takes File as input and returns Set<Pair<String, String>>
    // (add boolean to use File, Set<Pair<String, Vec3i>>, boolean as arguments instead)
    // Pre-defined files SeclosiaUtilsConfig::COMMANDS_FILE, SeclosiaUtilsConfig::LOCATIONS_FILE
    public List<Pair<String, String>> loadConfig(File file) {
        // variable declarations
        List<Pair<String, String>> conf = new LinkedList<>();
        List<String> keyRing = new LinkedList<>();
        List<String> values = new LinkedList<>();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                keyRing.add(key);
                values.add(value);

            }
            conf = ConfigUtils.zipLists(keyRing, values);
        } catch (IOException e) {
            ConfigUtils.createEmptyPropertiesFile("config/seclosia_commands.properties");
        }
        properties.clear();
        properties = new Properties();
        keyRing.clear();
        values.clear();
        return conf;
    }

    // Takes File and boolean as input and returns Set<Pair<String, String>>
    // (boolean is used to differentiate between two functions, this one is used for locations)
    // Pre-defined files SeclosiaUtilsConfig::COMMANDS_FILE, SeclosiaUtilsConfig::LOCATIONS_FILE
    public List<Pair<String, Vec3i>> loadConfig(File file, boolean locations) {

        // variable declarations
        List<Pair<String, Vec3i>> conf;
        List<String> keyRing = new LinkedList<>();
        List<Vec3i> values = new LinkedList<>();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            keyRing = new LinkedList<>(properties.stringPropertyNames());
            Pattern pattern = Pattern.compile("Vec3i\\{x=(-?\\d+), y=(-?\\d+), z=(-?\\d+)}");
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                Matcher matcher = pattern.matcher(value);
                if (matcher.matches()) {
                    int x = Integer.parseInt(matcher.group(1));
                    int y = Integer.parseInt(matcher.group(2));
                    int z = Integer.parseInt(matcher.group(3));
                    values.add(new Vec3i(x, y, z));
                }
            }

        } catch (IOException e) {
                ConfigUtils.createEmptyPropertiesFile("config/seclosia_locations.properties");
        }
        conf = ConfigUtils.zipLists(keyRing, values);
        properties.clear();
        properties = new Properties();
        keyRing.clear();
        values.clear();
        return conf;
    }
    public <T, U> void  saveConfig(File file, List<Pair<T, U>> s, List<String> ctr) {
        properties = ConfigUtils.getProperties(file);
        if(ctr != null){
            ConfigUtils.removeKeys(properties, ctr);
        }
        try (FileWriter writer = new FileWriter(file)) {
            for (Pair<T, U> p : s) {
                T key = p.getLeft();
                U value = p.getRight();
                    if (!ConfigUtils.isPairNull(key, value)) {
                        properties.setProperty(key.toString(), value.toString());
                    }
            }
            properties.store(writer, "");
            properties.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}