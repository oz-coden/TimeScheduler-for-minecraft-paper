package com.github.oz_coden.timeScheduler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        setUpSchedulerAndTimeSignal();
        Objects.requireNonNull(getCommand("schedule")).setExecutor(new ScheduleCommand());
        Objects.requireNonNull(getCommand("timesignal")).setExecutor(new TimeSignalCommand());

        getLogger().info("Enabling TimeScheduler...");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("Disabling TimeScheduler...");
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
                                getLogger().info( "[SCHEDULER] " + task.getMessage());
                                break;
                            case "@a":
                                Component msg = Component.text("[SCHEDULER] ", NamedTextColor.GOLD)
                                        .append(Component.text(task.getMessage(), NamedTextColor.WHITE));
                                Bukkit.broadcast(msg);
                                break;
                            default:
                                Player target = getServer().getPlayer(task.getTarget());
                                if (target != null) {
                                    target.sendMessage("[SCHEDULER] " + task.getMessage());
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
                        Component timeMsg = Component.text("[TIMESIGNAL] 現在時刻は ", NamedTextColor.AQUA)
                                .append(Component.text(currentHour + "時", NamedTextColor.YELLOW))
                                .append(Component.text(" です。", NamedTextColor.AQUA));
                        Bukkit.broadcast(timeMsg);
                        lastHour = currentHour;
                    }

                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }
}
