package com.github.oz_coden.timeScheduler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    private static final Map<CommandType, String> commandPrefixMap = Map.ofEntries(Map.entry(CommandType.PLUGIN, "plugin.prefix"), Map.entry(CommandType.SCHEDULER, "schedule-command.prefix"), Map.entry(CommandType.TIMESIGNAL, "timesignal-command.prefix"));
    private static final Map<MessageType, NamedTextColor> messageColorMap = Map.ofEntries(Map.entry(MessageType.MESSAGE, NamedTextColor.WHITE), Map.entry(MessageType.COMMAND, NamedTextColor.GREEN), Map.entry(MessageType.ERROR, NamedTextColor.RED), Map.entry(MessageType.EXECUTED, NamedTextColor.GOLD));
    private static final Map<String, FileConfiguration> langMap = new HashMap<>();
    private static String defaultLang;

    public static void load(TimeScheduler plugin) {
        langMap.clear();
        defaultLang = plugin.getConfig().getString("server-lang", "en_us");

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            boolean b = langFolder.mkdirs();
            if (!b) {
                plugin.getLogger().info("[TIMESCHEDULER] Can't make the \"timescheduler\\lang\" directory!");
            }
        }

        saveDefaultLang(plugin, "ja_jp");
        saveDefaultLang(plugin, "en_us");

        File[] files = langFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".yml")) {
                    String langName = file.getName().replace(".yml", "").toLowerCase();
                    langMap.put(langName, YamlConfiguration.loadConfiguration(file));
                }
            }
        }
    }

    private static void saveDefaultLang(TimeScheduler plugin, String langName) {
        File file = new File(plugin.getDataFolder(), "lang/" + langName + ".yml");
        if (!file.exists() && plugin.getResource("lang/" + langName + ".yml") != null) {
            plugin.saveResource("lang/" + langName + ".yml", false);
        }
    }

    public static TextComponent get(CommandType commandType, MessageType messageType, String key) {
        String locale = defaultLang;

        FileConfiguration config = langMap.getOrDefault(locale, langMap.get(defaultLang));

        NamedTextColor color = messageColorMap.get(messageType);
        String prefix = config.getString(commandPrefixMap.get(commandType), "[UNKNOWN]");

        return Component.text(prefix, color).append(Component.text(" " + config.getString(key, key),NamedTextColor.WHITE));
    }

    public static TextComponent get(CommandType commandType, MessageType messageType, String key, CommandSender sender) {
        String locale = defaultLang;

        if (sender instanceof Player) {
            locale = ((Player) sender).locale().toString().toLowerCase();
        }

        FileConfiguration config = langMap.getOrDefault(locale, langMap.get(defaultLang));

        NamedTextColor color = messageColorMap.get(messageType);
        String prefix = config.getString(commandPrefixMap.get(commandType), "[UNKNOWN]");

        return Component.text(prefix, color).append(Component.text(" " + config.getString(key, key),NamedTextColor.WHITE));
    }

    public static String getString(String key) {
        String locale = defaultLang;

        FileConfiguration config = langMap.getOrDefault(locale, langMap.get(defaultLang));

        return config.getString(key, key);
    }

    public static String getString(String key, CommandSender sender) {
        String locale = defaultLang;

        if (sender instanceof Player) {
            locale = ((Player) sender).locale().toString().toLowerCase();
        }

        FileConfiguration config = langMap.getOrDefault(locale, langMap.get(defaultLang));

        return config.getString(key, key);
    }

    public static TextComponent getWithCustom(CommandType commandType, MessageType messageType, String customMessage) {
        String locale = defaultLang;

        FileConfiguration config = langMap.getOrDefault(locale, langMap.get(defaultLang));

        NamedTextColor color = messageColorMap.get(messageType);
        String prefix = config.getString(commandPrefixMap.get(commandType), "[UNKNOWN]");

        return Component.text(prefix, color).append(Component.text(" " + customMessage ,NamedTextColor.WHITE));
    }

}

