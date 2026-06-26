package com.github.oz_coden.timeScheduler;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ScheduleCommand implements TabExecutor {

    private final TimeScheduler plugin = TimeScheduler.getPlugin();

    public ScheduleCommand() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // \schedule get <target> <schedule-type>
        // \schedule set <target> <schedule-type> <time> <messages>
        // \schedule remove <id>
        ScheduleCommandType commandType;
        String commandTarget;
        ScheduleType scheduleType;
        Long time = null;
        String message = null;

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "get":
                    commandType = ScheduleCommandType.GET;
                    break;
                case "set":
                    commandType = ScheduleCommandType.SET;
                    break;
                case "remove":
                    commandType = ScheduleCommandType.REMOVE;
                    break;
                default:
                    TextReplacementConfig c = TextReplacementConfig.builder().matchLiteral("%mode").replacement(args[0]).build();
                    sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.unknown-mode", sender).replaceText(c));
                    return false;
            }
        } else {
            TextReplacementConfig c = TextReplacementConfig.builder().matchLiteral("%label").replacement(label).build();
            sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.not-enough-args", sender).replaceText(c));
            return false;
        }
        if ((args.length < 2 && commandType == ScheduleCommandType.REMOVE) || (args.length < 5 && commandType == ScheduleCommandType.SET)) {
            TextReplacementConfig c = TextReplacementConfig.builder().matchLiteral("%label").replacement(label).build();
            sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.not-enough-args", sender).replaceText(c));
            return false;
        }
        if (args.length >= 2) {
            commandTarget = args[1];
        } else {
            commandTarget = null;
        }
        if (args.length >= 3) {
            scheduleType = switch (args[2].toLowerCase()) {
                case "real" -> ScheduleType.REAL;
                case "game" -> ScheduleType.IN_GAME;
                case "all" -> ScheduleType.NONE;
                default -> null;
            };
        } else {
            scheduleType = null;
        }
        if (args.length >= 4) {
            Boolean isTick = switch (Objects.requireNonNull(scheduleType)) {
                case REAL -> false;
                case IN_GAME -> true;
                default -> null;
            };
            time = (isTick == null ? 0 : parseTime(isTick, args[3]));
        }
        if (args.length >= 5) {
            message = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        }



        String senderName = sender.getName().toLowerCase();
        List<ScheduledTask> tasks;
        List<ScheduledTask> list;

        switch (commandType) {
            case GET:
                tasks = plugin.getTasks();
                if (tasks.isEmpty()) {
                    sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.COMMAND, "schedule-command.no-schedule-set", sender));
                    return true;
                }
                if (commandTarget != null && !sender.isOp() && !commandTarget.equalsIgnoreCase(senderName)) {
                    sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.need-op-permission", sender));
                    return true;
                }

                list = tasks.stream()
                        .filter(task -> (commandTarget == null || task.getRegister().equalsIgnoreCase(senderName) || commandTarget.equalsIgnoreCase("@a") || ((sender.isOp() || sender.getName().equalsIgnoreCase("CONSOLE".toLowerCase())) && commandTarget.equals("@server") && task.getTarget().equalsIgnoreCase("@server")) || ((sender.isOp() || senderName.equalsIgnoreCase(commandTarget)) && task.getTarget().equalsIgnoreCase(commandTarget))))
                        .filter(task -> (scheduleType == null || scheduleType == ScheduleType.NONE || (scheduleType == ScheduleType.REAL && task.getType().equals(ScheduleType.REAL)) || (scheduleType == ScheduleType.IN_GAME && task.getType().equals(ScheduleType.IN_GAME))))
                        .toList();

                if (list.isEmpty()) {
                    sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.COMMAND, "schedule-command.no-match-schedule", sender));
                    return true;
                }

                for (ScheduledTask item : list) {

                    String readableTime;
                    if (item.getType() == ScheduleType.REAL) {
                        ZonedDateTime dateTime = Instant.ofEpochMilli(item.getTargetTime()).atZone(ZoneId.systemDefault());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        readableTime = dateTime.format(formatter);
                    } else {

                        long currentTime = (sender instanceof Player)
                                ? ((Player) sender).getWorld().getFullTime()
                                : plugin.getServer().getWorlds().get(0).getFullTime();
                        readableTime = LangManager.getString("schedule-command.world-time", sender).replace("%tick", String.valueOf(item.getTargetTime() - currentTime)).replace("%time", String.valueOf(item.getTargetTime()));
                    }
                    String str = LangManager.getString("schedule-command.get-match-schedule", sender).replace("%type", item.getType().toString().toUpperCase()).replace("%target", item.getTarget()).replace("%message", item.getMessage()).replace("%time", readableTime).replace("%id", item.getId().toString());
                    sender.sendMessage(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.COMMAND, str));
                }
                return true;
            case REMOVE:
                tasks = plugin.getTasks();

                for (ScheduledTask task : tasks) {
                    if (task.getId().toString().equalsIgnoreCase(commandTarget)) {
                        if (sender.isOp() || task.getRegister().equalsIgnoreCase(senderName) || task.getTarget().equalsIgnoreCase(senderName)) {
                            sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.COMMAND, "schedule-command.remove-schedule", sender));
                            tasks.remove(task);
                        } else {
                            sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.need-op-permission", sender));
                        }
                        return true;
                    }
                }

                sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.not-found-schedule-id", sender));
                return true;
            case SET:
                if (time < 0) {
                    sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.need-positive-int", sender));
                    return true;
                }

                long targetTime = time;
                long left = time;
                int days;
                int hours;
                int minutes;
                int seconds;
                switch (scheduleType) {
                    case REAL:
                        days = (int) Math.floor((double) left / (24L * 60L * 60L * 1000L));
                        left = left - days * (24L * 60L * 60L * 1000L);
                        hours = (int) Math.floor((double) left / (60L * 60L * 1000L));
                        left = left - hours * (60L * 60L * 1000L);
                        minutes = (int) Math.floor((double) left / (60L * 1000L));
                        left = left - minutes * (60L * 1000L);
                        seconds = (int) Math.floor((double) left / (1000L));

                        targetTime += System.currentTimeMillis();
                        String str_real = LangManager.getString("schedule-command.set-schedule-real", sender).replace("%days", String.valueOf(days)).replace("%hours", String.valueOf(hours)).replace("%minutes", String.valueOf(minutes)).replace("%seconds", String.valueOf(seconds));
                        sender.sendMessage(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.COMMAND, str_real));
                        break;
                    case IN_GAME:
                        days = (int) Math.floor((double) left / (24000L));
                        left = left - days * (24000L);

                        long currentTime = (sender instanceof Player)
                                ? ((Player) sender).getWorld().getFullTime()
                                : plugin.getServer().getWorlds().get(0).getFullTime();

                        targetTime += currentTime;
                        String str_game = LangManager.getString("schedule-command.set-schedule-game", sender).replace("%days", String.valueOf(days)).replace("%ticks", String.valueOf(left));
                        sender.sendMessage(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.COMMAND, str_game));
                        break;
                    default:

                        sender.sendMessage(LangManager.get(CommandType.SCHEDULER, MessageType.ERROR, "schedule-command.need-correct-type", sender));
                        return false;
                }

                ScheduledTask task = new ScheduledTask(senderName.equalsIgnoreCase("CONSOLE".toLowerCase()) ? "@server" : senderName, scheduleType, commandTarget, targetTime, message);
                plugin.addTask(task);
                return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("get", "set", "remove").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                List<ScheduledTask> tasks = plugin.getTasks();
                return tasks.stream().filter(task -> sender.isOp() || task.getRegister().equalsIgnoreCase(sender.getName()) || task.getTarget().equalsIgnoreCase(sender.getName())).map(task -> task.getId().toString()).filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }

            if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
                Stream<String> customTargets = Stream.of("@a", "@server");
                Stream<String> onlinePlayers = org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName);

                return Stream.concat(customTargets, onlinePlayers).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }
        } else if (args.length == 3 && !args[0].equalsIgnoreCase("remove")) {
            if (args[0].equalsIgnoreCase("get")) {
                return Stream.of("real", "game", "all").filter(s -> s.startsWith(args[2].toLowerCase())).toList();
            } else if (args[0].equalsIgnoreCase("set")) {
                return Stream.of("real", "game").filter(s -> s.startsWith(args[2].toLowerCase())).toList();
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            String timeInput = args[3].toLowerCase();

            if (timeInput.isEmpty()) {
                if (args[2].equalsIgnoreCase("real")) {
                    return List.of("1d", "1h", "10m", "60s");
                } else {
                    return List.of("1d", "1000t");
                }
            }

            if (timeInput.matches(".*\\d$")) {
                List<String> units = List.of("d", "h", "m", "s", "t");


                return units.stream().map(unit -> timeInput + unit).filter(suggestion -> {
                    String lastUnit = suggestion.substring(suggestion.length() - 1);
                    return !timeInput.contains(lastUnit);
                }).toList();
            }
        }
        return List.of();
    }

    public long parseTime(Boolean isTick, String s) {
        long total = 0L;
        Matcher m = Pattern.compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?(?:(\\d+)t)?").matcher(s);

        if (m.matches()) {
            if (m.group(1) != null) {total += Long.parseLong(m.group(1)) * (isTick ? 24000L : 24 * 60 * 60 * 1000L);}
            if (m.group(2) != null) {total += Long.parseLong(m.group(2)) * (isTick ? 1000L : 60 * 60 * 1000L);}
            if (m.group(3) != null) {total += Long.parseLong(m.group(3)) * (isTick ? 100L : 60 * 1000L);}
            if (m.group(4) != null) {total += Long.parseLong(m.group(4)) * (isTick ? 10L : 1000L);}
            if (m.group(5) != null) {total += Long.parseLong(m.group(5)) * (isTick ? 1 : 50L);}
        }

        if (total == 0L && s.matches("\\d+")) {
            total += Long.parseLong(s) * (isTick ? 1 : 60 * 1000L);
        }

        return total;
    }
}