package linker;


import arc.*;
import arc.backend.headless.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;

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

    public static Integer SessionMaxTries;

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

        ObjectMap<String, String> data = new Json().fromJson(ObjectMap.class, Fi.get(path).read());

        DatabasePath = data.get("databasePath");

        BotPrefix = data.get("botPrefix");
        BotChannelId = data.get("botChannelId");
        BotServerId = data.get("botServerId");
        BotToken = data.get("botToken");

        AdminPassword = data.get("adminPassword");
        AdminRoleId = data.get("adminRoleId");

        SessionMaxTries = Integer.parseInt(data.get("sessionMaxTries"));
    }

}
