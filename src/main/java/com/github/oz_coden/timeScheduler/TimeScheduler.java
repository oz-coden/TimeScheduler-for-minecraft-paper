package com.github.oz_coden.timeScheduler;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class TimeScheduler extends JavaPlugin {

    private static TimeScheduler plugin;
    public static TimeScheduler getPlugin() {
        return plugin;
    }

    private boolean timeSignal = true;
    public boolean getTimeSignal() {
        return timeSignal;
    }
    public void setTimeSignal(boolean timeSignal) {
        this.timeSignal = timeSignal;
        FileConfiguration config = getConfig();
        config.set("timesignal", timeSignal);
    }

    private final List<ScheduledTask> taskList = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        int defaultVersion = 2;
        int serverVersion = getConfig().getInt("version", 0);
        if (serverVersion < defaultVersion) {
            getConfig().options().copyDefaults(true);
            getConfig().set("version", defaultVersion);
            saveConfig();
            getLogger().info("Update detected...\ntimescheduler\\config.yml updated.");
        }

        LangManager.load(plugin);
        setUpSchedulerAndTimeSignal();
        Objects.requireNonNull(getCommand("schedule")).setExecutor(new ScheduleCommand());
        Objects.requireNonNull(getCommand("timesignal")).setExecutor(new TimeSignalCommand());

        Bukkit.getConsoleSender().sendMessage(LangManager.get(CommandType.PLUGIN, MessageType.MESSAGE, "plugin.enabled"));
    }

    @Override
    public void onDisable() {
        saveConfig();
        Bukkit.getConsoleSender().sendMessage(LangManager.get(CommandType.PLUGIN, MessageType.MESSAGE, "plugin.disabled"));
    }

    private void setUpSchedulerAndTimeSignal() {
        FileConfiguration config = getConfig();
        loadTasks();
        startMainLoop();
        timeSignal = config.getBoolean("timesignal");
    }

    public void addTask(ScheduledTask task) {
        taskList.add(task);
        saveTasks();
    }

    public List<ScheduledTask> getTasks() {
        return this.taskList;
    }

    private void saveTasks() {
        FileConfiguration config = getConfig();
        config.set("tasks", null);

        for (ScheduledTask task : taskList) {
            String path = "tasks." + task.getId().toString();
            config.set(path + ".register", task.getRegister());
            config.set(path + ".target", task.getTarget());
            config.set(path + ".type", task.getType().toString());
            config.set(path + ".targetTime", task.getTargetTime());
            config.set(path + ".message", task.getMessage());
        }
        saveConfig();
    }

    private void loadTasks() {
        taskList.clear();
        FileConfiguration config = getConfig();
        if (!config.contains("tasks")) return;

        Set<String> keys = config.getConfigurationSection("tasks").getKeys(false);
        for (String keyStr : keys) {
            UUID id = UUID.fromString(keyStr);
            String register = config.getString("tasks." + keyStr + ".register");
            String target = config.getString("tasks." + keyStr + ".target");
            ScheduleType type = config.getString("tasks." + keyStr + ".type").equalsIgnoreCase("REAL") ? ScheduleType.REAL : ScheduleType.IN_GAME;
            long targetTime = config.getLong("tasks." + keyStr + ".targetTime");
            String message = config.getString("tasks." + keyStr + ".message");

            taskList.add(new ScheduledTask(id, register, target, type, targetTime, message));
        }
    }

    private void startMainLoop() {
        new BukkitRunnable() {
            private int lastHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            @Override
            public void run() {
                long currentRealTime = System.currentTimeMillis();
                long currentInGameTime = Bukkit.getWorlds().get(0).getFullTime();

                boolean isUpdated = false;
                Iterator<ScheduledTask> iterator = taskList.iterator();
                while (iterator.hasNext()) {
                    ScheduledTask task = iterator.next();

                    boolean shouldExecute = false;
                    if (task.getType().equals(ScheduleType.REAL) && currentRealTime >= task.getTargetTime()) {
                        shouldExecute = true;
                    } else if (task.getType().equals(ScheduleType.IN_GAME) && currentInGameTime >= task.getTargetTime()) {
                        shouldExecute = true;
                    }

                    if (shouldExecute) {
                        switch (task.getTarget()) {
                            case "@server":
                                Bukkit.getConsoleSender().sendMessage(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.EXECUTED, task.getMessage()));
                                break;
                            case "@a":
                                Bukkit.broadcast(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.EXECUTED, task.getMessage()));
                                break;
                            default:
                                Player target = getServer().getPlayer(task.getTarget());
                                if (target != null) {
                                    target.sendMessage(LangManager.getWithCustom(CommandType.SCHEDULER, MessageType.EXECUTED, task.getMessage()));
                                }
                        }
                        iterator.remove();
                        isUpdated = true;
                    }
                }

                if (isUpdated) {
                    saveTasks();
                }

                if (timeSignal) {
                    Calendar now = Calendar.getInstance();
                    int currentHour = now.get(Calendar.HOUR_OF_DAY);
                    if (currentHour != lastHour) {
                        TextReplacementConfig c = TextReplacementConfig.builder().matchLiteral("%h").replacement(String.valueOf(currentHour)).build();
                        Bukkit.broadcast(LangManager.get(CommandType.TIMESIGNAL, MessageType.EXECUTED, "timesignal-command.notification-message").replaceText(c));
                        lastHour = currentHour;
                    }

                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }
}
