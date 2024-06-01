package net.mrquba.seclosia;

import net.minecraft.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class SeclosiaUtilsConfigUtils {

    // changes String to Integer
    public static int StringBuilderToInt(StringBuilder x_sb){
    return Integer.parseInt(x_sb.toString());
}

    public static void removeKeys(Properties prop, List<String> ctr){
        for(String k : ctr){
            prop.remove(k);
        }
    }
    public static <T, U> boolean isPairNull(T k, U v) { return  k == null && v == null; }

    public static void createEmptyPropertiesFile(String path) {
        try {
            File file = new File(path);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties(File file){
        Properties prop = new Properties();
        try (FileReader reader = new FileReader(file)) {
            prop.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  prop;
    }

    // merges two lists into one list of pairs
    public static <T, U> List<Pair<T, U>> zipLists(List<T> list1, List<U> list2) {
        Iterator<T> iterator1 = list1.iterator();
        Iterator<U> iterator2 = list2.iterator();
        List<Pair<T, U>> resultSet = new LinkedList<>();

        while (iterator1.hasNext() && iterator2.hasNext()) {
            T element1 = iterator1.next();
            U element2 = iterator2.next();
            resultSet.add(new Pair<>(element1, element2));
        }

        return resultSet;
    }
}

