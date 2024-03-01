package net.mrquba.seclosia;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Character.isDigit;

public class SeclosiaUtilsConfig {
    public static final File LOCATIONS_FILE = new File("config/seclosia_locations.properties");
    public static final File COMMANDS_FILE = new File("config/seclosia_commands.properties");
    private final Properties properties = new Properties();

    // Takes File as input and returns Set<Pair<String, String>>
    // (add boolean to use File, Set<Pair<String, Vec3i>>, boolean as arguments instead)
    // Pre-defined files SeclosiaUtilsConfig::COMMANDS_FILE, SeclosiaUtilsConfig::LOCATIONS_FILE
    public List<Pair<String, String>> loadConfig(File file) {

        List<Pair<String, String>> conf = new LinkedList<>();
        int nextChar;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        List<String> keyRing = new LinkedList<>();
        List<String> values = new LinkedList<>();
        boolean readAnotherKey = true;
        boolean readValue = false;
        try (FileReader reader = new FileReader(file)) {

            while ((nextChar = reader.read()) != -1) {
                if ((char) nextChar == '=') {
                    if(contains(String.valueOf(key), '#')){
                        key = new StringBuilder("");
                        readAnotherKey = false;
                        readValue = true;
                        continue;
                    }
                    while(contains(String.valueOf(key), '\\')){
                        int ind = key.indexOf(String.valueOf('\\'));
                        key = new StringBuilder(key.substring(0, ind) + key.substring(ind + 1));
                    }
                    keyRing.add(key.toString());
                    key = new StringBuilder("");
                    readAnotherKey = false;
                    readValue = true;
                    continue;
                }
                if(readValue && (char) nextChar == (char) 10){
                    while(contains(String.valueOf(value), (char) 13)){
                       int ind = value.indexOf(String.valueOf((char) 13));
                        value = new StringBuilder(value.substring(0, ind) + value.substring(ind + 1));
                    }
                   while(contains(String.valueOf(value), (char) 10)){
                        int ind = value.indexOf(String.valueOf((char) 10));
                       value = new StringBuilder(value.substring(0, ind) + value.substring(ind + 1));
                    }
                    if(!value.toString().equals("dd")) {
                        values.add(value.toString());
                    }
                    value = new StringBuilder("");
                        readAnotherKey = true;
                        readValue = false;
                        continue;
                }
                if(readValue)value.append((char) nextChar);
                if(readAnotherKey)key.append((char) nextChar);
            }
            conf = zipLists(keyRing, values);
        } catch (IOException e) {

            e.printStackTrace();

        }
        return conf;
    }

    // Takes File and boolean as input and returns Set<Pair<String, String>>
    // (boolean is used to differentiate between two functions, this one is used for locations)
    // Pre-defined files SeclosiaUtilsConfig::COMMANDS_FILE, SeclosiaUtilsConfig::LOCATIONS_FILE
    public List<Pair<String, Vec3i>> loadConfig(File file, boolean locations) {

        List<Pair<String, Vec3i>> conf = new LinkedList<>();
        int nextChar;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        int x = 23,y = 23,z = 23;
        List<String> keyRing = new LinkedList<>();
        List<Vec3i> values = new LinkedList<>();
        boolean readAnotherKey = true;
        boolean readValue = false;
        try (FileReader reader = new FileReader(file)) {

            while ((nextChar = reader.read()) != -1) {
                if ((char) nextChar == '=') {
                    if(contains(String.valueOf(key), '#')){
                        key = new StringBuilder("");
                        readAnotherKey = false;
                        readValue = true;
                        continue;
                    }
                    while(contains(String.valueOf(key), '\\')){
                        int ind = key.indexOf(String.valueOf('\\'));
                        key = new StringBuilder(key.substring(0, ind) + key.substring(ind + 1));
                    }
                    keyRing.add(key.toString());
                    key = new StringBuilder("");
                    readAnotherKey = false;
                    readValue = true;
                    continue;
                }
                if(readValue && (char) nextChar == (char) 10){
                    while(contains(String.valueOf(value), (char) 13)){
                        int ind = value.indexOf(String.valueOf((char) 13));
                        value = new StringBuilder(value.substring(0, ind) + value.substring(ind + 1));
                    }
                    while(contains(String.valueOf(value), (char) 10)){
                        int ind = value.indexOf(String.valueOf((char) 10));
                        value = new StringBuilder(value.substring(0, ind) + value.substring(ind + 1));
                    }
                    value.delete(value.indexOf("Vec3i{x\\=") + 1, "Vec3i{x\\=".length() - 1);
                    value.delete(value.indexOf("z\\=") + 1,"z\\=".length() - 1);
                    value.delete(value.indexOf("y\\=") + 1,"y\\=".length() -1);
                    value.delete(value.indexOf("}") + 1,"}".length() - 1);
                    while(contains(String.valueOf(value), ' ')){
                        value.delete(value.indexOf(" ")," ".length());
                    }
                    if(x == 23){
                        StringBuilder x_s = new StringBuilder();
                        if((char) nextChar != ','){
                            x_s.append((char) nextChar);
                        }
                        else {
                            x = StringToInt(x_s);
                        }
                    }
                    else if(y == 23){
                        StringBuilder y_s = new StringBuilder();
                        if((char) nextChar != ','){
                            y_s.append((char) nextChar);
                        }
                        else {
                            y = StringToInt(y_s);
                        }
                    }
                    else if(z == 23){
                        StringBuilder z_s = new StringBuilder();
                        if((char) nextChar != ','){
                            z_s.append((char) nextChar);
                        }
                        else {
                            z = StringToInt(z_s);
                        }
                    }
                    Vec3i new_value = new Vec3i(x,y,z);
                    if(!value.toString().equals("dd")) {
                        values.add(new_value);
                    }
                    value = new StringBuilder("");
                    readAnotherKey = true;
                    readValue = false;
                    continue;
                }
                if(readValue && isDigit((char) nextChar))value.append((char) nextChar);
                if(readAnotherKey)key.append((char) nextChar);
            }
            conf = zipLists(keyRing, values);
        } catch (IOException e) {

            e.printStackTrace();

        }

        return conf;
    }
    private int StringToInt(StringBuilder x_sb){
        return Integer.parseInt(x_sb.toString());
    }
    // Takes as argument File and Set<Pair<String, String>>
    // (add boolean to use File, Set<Pair<String, Vec3i>>, boolean as arguments instead)
    // and appends to file (without repetition) string values (key, value)
    public void saveConfig(File file, List<Pair<String, String>> s) {
        try (FileReader reader = new FileReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (Pair<String, String> p : s) {
                String key = p.getLeft();
                String value = p.getRight();
                if (!properties.containsKey(key) && key != null && value != null) {
                    properties.setProperty(key, value);
                }
            }
            properties.store(writer, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Takes as argument File, Set<Pair<String, Vec3i>>, boolean
    // (boolean is used to differentiate between functions)
    // and appends to file (without repetition) string values (key, value)
    public void saveConfig(File file, List<Pair<String, Vec3i>> s, boolean locations) {
        try (FileReader reader = new FileReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (Pair<String, Vec3i> p : s) {
                String key = p.getLeft();
                Vec3i value = p.getRight();
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, String.valueOf(value));
                }
            }
            properties.store(writer, "Seclosia commands shortcuts");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Vec3i{x\=1, y\=1, z\=1}
    private static Vec3i fromString(String str) {
        assert str != null;
        String[] parts = str.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        return new Vec3i(x, y, z);
    }
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
    public boolean contains(String str, char find){
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == find) {
                return true;
            }
        }
        return false;
    }
}