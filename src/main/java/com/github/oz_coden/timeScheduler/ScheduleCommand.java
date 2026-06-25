package com.github.oz_coden.timeScheduler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        ScheduleCommandType commandType = null;
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
                    sender.sendMessage(Component.text("[SCHEDULER] 不明なモードです： " + args[0], NamedTextColor.RED));
                    return false;
            }
        }
        if (args.length >= 2) {
            commandTarget = args[1];
        } else {
            commandTarget = null;
        }
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "real":
                    scheduleType = ScheduleType.REAL;
                    break;
                case "game":
                    scheduleType = ScheduleType.IN_GAME;
                    break;
                case "all":
                    scheduleType = ScheduleType.NONE;
                    break;
                default:
                    scheduleType = null;
            }
        } else {
            scheduleType = null;
        }
        if (args.length >= 4) {
            Boolean isTick = null;
            switch (scheduleType) {
                case REAL:
                    isTick = false;
                    break;
                case IN_GAME:
                    isTick = true;
                    break;
            }
            time = (isTick == null ? 0 : parseTime(isTick, args[3]));
        }
        if (args.length >= 5) {
            message = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        }

        if (args.length == 0 || (args.length < 2 && commandType == ScheduleCommandType.REMOVE) || (args.length < 5 && commandType == ScheduleCommandType.SET)) {
            sender.sendMessage(Component.text("[SCHEDULER] 引数が足りません: " + label, NamedTextColor.RED));
            return false;
        }

        boolean isSenderPlayer = (sender instanceof Player);
        String senderName = sender.getName().toLowerCase();
        List<ScheduledTask> tasks;
        List<ScheduledTask> list;

        switch (commandType) {
            case GET:
                tasks = plugin.getTasks();
                if (tasks.isEmpty()) {
                    sender.sendMessage(Component.text("[SCHEDULER GET] 現在設定されているスケジュールはありません。", NamedTextColor.GREEN));
                    return true;
                }
                if (commandTarget != null && !sender.isOp() && !commandTarget.equalsIgnoreCase(senderName)) {
                    sender.sendMessage(Component.text("[SCHEDULER GET] 管理者権限がない場合、他人のスケジュールを見ることはできません。", NamedTextColor.RED));
                    return true;
                }

                list = tasks.stream()
                        .filter(task -> (commandTarget == null || task.getRegister().equalsIgnoreCase(senderName) || commandTarget.equalsIgnoreCase("@a") || ((sender.isOp() || sender.getName().equalsIgnoreCase("CONSOLE".toLowerCase())) && commandTarget.equals("@server") && task.getTarget().equalsIgnoreCase("@server")) || ((sender.isOp() || senderName.equalsIgnoreCase(commandTarget)) && task.getTarget().equalsIgnoreCase(commandTarget))))
                        .filter(task -> (scheduleType == null || scheduleType == ScheduleType.NONE || (scheduleType == ScheduleType.REAL && task.getType().equals(ScheduleType.REAL)) || (scheduleType == ScheduleType.IN_GAME && task.getType().equals(ScheduleType.IN_GAME))))
                        .toList();

                if (list.isEmpty()) {
                    sender.sendMessage(Component.text("[SCHEDULER GET] 条件に合致したスケジュールはありません。", NamedTextColor.GREEN));
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
                        readableTime = "ワールド時間: " + (currentTime - item.getTargetTime()) + " Tick後 (" + item.getTargetTime() + ")";
                    }

                    sender.sendMessage(Component.text("[SCHEDULER GET] [" + item.getType().toString().toUpperCase() + "] To: " + item.getTarget() + ", Message: " + item.getMessage() + ", At: " + readableTime + ", ID:" + item.getId(), NamedTextColor.GREEN));
                }
                return true;
            case REMOVE:
                tasks = plugin.getTasks();

                for (ScheduledTask task : tasks) {
                    if (task.getId().toString().equalsIgnoreCase(commandTarget)) {
                        if (sender.isOp() || task.getRegister().equalsIgnoreCase(senderName) || task.getTarget().equalsIgnoreCase(senderName)) {
                            sender.sendMessage(Component.text("[SCHEDULER REMOVE] スケジュールを削除しました。", NamedTextColor.GREEN));
                            tasks.remove(task);
                            return true;
                        } else {
                            sender.sendMessage(Component.text("[SCHEDULER REMOVE] 管理者権限がない場合、削除できません。", NamedTextColor.RED));
                            return true;
                        }
                    }
                }

                sender.sendMessage(Component.text("[SCHEDULER REMOVE] 当てはまるスケジュールIDを持つスケジュールが見つかりませんでした。", NamedTextColor.RED));
                return true;
            case SET:
                if (time < 0) {
                    sender.sendMessage(Component.text("[SCHEDULER SET] 指定する時間は正の整数である必要があります。", NamedTextColor.RED));
                    return true;
                }

                long targetTime = time;
                long left = time;
                int days;
                int hours;
                int minutes;
                int seconds;
                int ticks;
                switch (scheduleType) {
                    case REAL:
                        days = (int) Math.floor((double) left / (24L * 60L * 60L * 1000L));
                        left = left - days * (24L * 60L * 60L * 1000L);
                        hours = (int) Math.floor((double) left / (60L * 60L * 1000L));
                        left = left - hours * (60L * 60L * 1000L);
                        minutes = (int) Math.floor((double) left / (60L * 1000L));
                        left = left - minutes * (60L * 1000L);
                        seconds = (int) Math.floor((double) left / (1000L));
                        left = left - seconds * (1000L);
                        ticks = (int) Math.floor((double) left / (50L));

                        time += System.currentTimeMillis();
                        sender.sendMessage(Component.text("[SCHEDULER SET] 現実時間で " + days + "日" + hours + "時間" + minutes + "分" + seconds + "秒後にスケジュールを設定しました。", NamedTextColor.GREEN));
                        break;
                    case IN_GAME:
                        days = (int) Math.floor((double) left / (24000L));
                        left = left - days * (24000L);

                        long currentTime = (sender instanceof Player)
                                ? ((Player) sender).getWorld().getFullTime()
                                : plugin.getServer().getWorlds().get(0).getFullTime();

                        time += currentTime;
                        sender.sendMessage(Component.text("[SCHEDULER SET] ゲーム内時間で " + days + " 日と" + left+ "TICK後にスケジュールを設定しました。", NamedTextColor.GREEN));
                        break;
                    default:
                        sender.sendMessage(Component.text("[SCHEDULER SET] スケジュールタイプには、'real' または 'game' を指定してください。", NamedTextColor.RED));
                        return false;
                }

                ScheduledTask task = new ScheduledTask(senderName.equalsIgnoreCase("CONSOLE".toLowerCase()) ? "@sender" : senderName.replace("CONSOLE".toLowerCase(), "@server"), scheduleType, commandTarget, time, message);
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
                ;

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