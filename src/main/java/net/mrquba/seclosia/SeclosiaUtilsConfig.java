package net.mrquba.seclosia;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class SeclosiaUtilsConfig {
    public static final File LOCATIONS_FILE = new File("config/seclosia_locations.properties");
    public static final File COMMANDS_FILE = new File("config/seclosia_commands.properties");
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
            conf = zipLists(keyRing, values);
        } catch (IOException e) {
            createEmptyPropertiesFile("config/seclosia_commands.properties");
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
            createEmptyPropertiesFile("config/seclosia_locations.properties");
        }
        conf = zipLists(keyRing, values);
        properties.clear();
        properties = new Properties();
        keyRing.clear();
        values.clear();
        return conf;
    }
    public static void createEmptyPropertiesFile(String path) {
        try {
            File file = new File(path);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // deletes given string from StringBuilder
    private void delete(String str2rem, StringBuilder sb){
        sb.delete(sb.indexOf(str2rem), sb.indexOf(str2rem) + str2rem.length() - 1);
    }
    // changes String to Integer
    private int StringToInt(StringBuilder x_sb){
        return Integer.parseInt(x_sb.toString());
    }

    StringBuilder x_s = new StringBuilder();
    private int AppendChars(int nextChar){
        if((char) nextChar != ','){
            x_s.append((char) nextChar);
        }
        else {
            return StringToInt(x_s);
        }
        return 23;
    }

    public <T, U> void  saveConfig(File file, List<Pair<T, U>> s) {
        try (FileReader reader = new FileReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (Pair<T, U> p : s) {
                T key = p.getLeft();
                U value = p.getRight();
                if (!properties.containsKey(key) && key != null && value != null) {
                    properties.setProperty(key.toString(), value.toString());
                }
            }
            properties.store(writer, "");
            properties.clear();
            properties = new Properties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Takes as argument File, Set<Pair<String, Vec3i>>, boolean
    // (boolean is used to differentiate between functions)
    // and appends to file (without repetition) string values (key, value)

    // merges two lists into one list of pairs
    public static <T, U> List<Pair<T, U>> zipLists(List<T> set1, List<U> set2) {
        Iterator<T> iterator1 = set1.iterator();
        Iterator<U> iterator2 = set2.iterator();
        List<Pair<T, U>> resultSet = new LinkedList<>();

        while (iterator1.hasNext() && iterator2.hasNext()) {
            T element1 = iterator1.next();
            U element2 = iterator2.next();
            resultSet.add(new Pair<>(element1, element2));
        }

        return resultSet;
    }
    // checks if String contains specific character
    public boolean contains(String str, char find){
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == find) {
                return true;
            }
        }
        return false;
    }
    public boolean contains(String str, String find){
        return str.contains(find);
    }
}