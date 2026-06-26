package com.github.oz_coden.timeScheduler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class TimeSignalCommand  implements TabExecutor {

    private final TimeScheduler plugin = TimeScheduler.getPlugin();

    public TimeSignalCommand() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            String str = LangManager.getString("timesignal-command.current-signal-mode").replace("%bool", String.valueOf(plugin.getTimeSignal()));
            sender.sendMessage(LangManager.getWithCustom(CommandType.TIMESIGNAL, MessageType.COMMAND, str));
        } else {
            if (args[0].equalsIgnoreCase("true")) {
                plugin.setTimeSignal(true);
            } else if (args[0].equalsIgnoreCase("false")) {
                plugin.setTimeSignal(false);
            } else if (args[0].equalsIgnoreCase("toggle")) {
                plugin.setTimeSignal(!plugin.getTimeSignal());
            } else {
                sender.sendMessage(LangManager.get(CommandType.TIMESIGNAL, MessageType.COMMAND, "timesignal-command.need-boolean", sender));
                return false;
            }

            String str = LangManager.getString("timesignal-command.set-new-signal-mode").replace("%bool", String.valueOf(plugin.getTimeSignal()));
            sender.sendMessage(LangManager.getWithCustom(CommandType.TIMESIGNAL, MessageType.COMMAND, str));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("true", "false", "toggle").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
