package linker;


import arc.*;
import arc.backend.headless.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;

import java.awt.*;
import java.time.*;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;

import static arc.util.Log.format;

public class Config {
    public static Database db;
    public static Bot bot;
    public static LinkSession session;

    public static String BotPrefix;
    public static String DatabasePath;
    public static String BotChannelId;
    public static String BotServerId;
    public static String BotToken;


    public static String AdminPassword;
    public static String AdminRoleId;

    public static float SessionTimeout;
    public static float SessionMaxTries;

    public static class EmbedColors {
        public static Color Error = new Color(245, 183, 177);
        public static Color Started = new Color(174, 214, 241);
        public static Color Success = new Color(171, 235, 198);
        public static Color Warn = new Color(247, 220, 111);
        public static Color Info = new Color(215, 189, 226);
    }

    private static final String[] tags = {"\u001b[34m\u001b[1m[D]", "\u001b[32m\u001b[1m[I]", "\u001b[33m\u001b[1m[W]", "\u001b[31m\u001b[1m[E]", ""};
    private static final DateTimeFormatter dataTime = DateTimeFormatter.ofPattern("MM-dd-yyyy | HH:mm:ss");

    public static void load(String path) throws FileNotFoundException {
        if (!Fi.get(path).exists()) {
            throw new FileNotFoundException("Linker config file was not found");
        }

        Log.setLogger((level, text) -> {
            String result = "\u001b[36m[" + dataTime.format(LocalDateTime.now()) + "] " + "[Linker]\u001b[0m" + format(tags[level.ordinal()] + " " + text + "\u001b[0m");
            System.out.println(result);
        });

        Log.info("Loading config from " + path);

        ObjectMap<String, Object> data = new Json().fromJson(ObjectMap.class, Fi.get(path).read());

        DatabasePath = (String) data.get("databasePath");

        BotPrefix = (String) data.get("botPrefix");
        BotChannelId = (String) data.get("botChannelId");
        BotServerId = (String) data.get("botServerId");
        BotToken = (String) data.get("botToken");

        AdminPassword = (String) data.get("adminPassword");
        AdminRoleId = (String) data.get("adminRoleId");

        SessionMaxTries = (float)data.get("sessionMaxTries");
        SessionTimeout = (float)data.get("sessionTimeout");
    }

}
