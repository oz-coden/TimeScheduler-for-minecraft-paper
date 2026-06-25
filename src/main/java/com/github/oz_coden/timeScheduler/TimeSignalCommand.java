package com.github.oz_coden.timeScheduler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class TimeSignalCommand  implements TabExecutor {

    private final TimeScheduler plugin = TimeScheduler.getPlugin();

    public TimeSignalCommand() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("[TIMESIGNAL] 時報は" + plugin.getTimeSignal() + "に設定されています。");
            return true;
        } else {
            if (args[0].equalsIgnoreCase("true")) {
                plugin.setTimeSignal(true);
                sender.sendMessage("[TIMESIGNAL] 時報はtrueに設定されました。");
                return true;
            } else if (args[0].equalsIgnoreCase("false")) {
                plugin.setTimeSignal(false);
                sender.sendMessage("[TIMESIGNAL] 時報はfalseに設定されました。");
                return true;
            } else if (args[0].equalsIgnoreCase("toggle")) {
                plugin.setTimeSignal(!plugin.getTimeSignal());
                sender.sendMessage("[TIMESIGNAL] 時報は" + plugin.getTimeSignal() + "に設定されました。");
                return true;
            } else {
                sender.sendMessage("[TIMESIGNAL] trueかfalseを指定してください。");
                return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("true", "false", "toggle").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
