package net.mrquba.seclosia;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.List;

public class CommandUtils {
    private static final SeclosiaUtilsConfig conf = new SeclosiaUtilsConfig();
    public static  <T, U> void saveConfig(File file, List<Pair<T, U>> lp, List<String> ctr){
        conf.saveConfig(file, lp, ctr);
        lp.clear();
        ctr.clear();
    }
    public static <PT1, PT2, L extends List<Pair<PT1, PT2>> > void registration(CommandDispatcher<ServerCommandSource> dispatcher, L list, Registration<PT1, PT2> function){
        for (Pair<PT1, PT2> m : list) {
            function.register(dispatcher, m.getLeft(), m.getRight());
        }
    }
    public static <PT1, PT2, L extends List<Pair<PT1, PT2>> > void init(CommandDispatcher<ServerCommandSource> dispatcher, L list, File file, Registration<PT1, PT2> function, List<String> toRemove){
        registration(dispatcher, list, function);
        saveConfig(file, list, toRemove);
    }
    public static  void onRegisterEvent(CommandDispatcher<ServerCommandSource> dispatcher, List<Pair<String, Vec3i>> list, File file, Registration<String, Vec3i> function, List<String> toRemove, boolean locations){
        list = conf.loadConfig(file, locations);
        CommandUtils.init(dispatcher, list, file, function, toRemove);
        list.clear();
    }
    public static void onRegisterEvent(CommandDispatcher<ServerCommandSource> dispatcher, List<Pair<String, String>> list, File file, Registration<String, String> function, List<String> toRemove){
                list = conf.loadConfig(file);
        CommandUtils.init(dispatcher, list, file, function, toRemove);
        list.clear();
    }

    @FunctionalInterface
    interface Registration<T, U>{
        void register(CommandDispatcher<ServerCommandSource> dispatcher, T t, U u);
    }
}
