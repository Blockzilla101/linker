package linker;

import org.javacord.api.*;
import org.javacord.api.entity.message.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import static mindustry.Vars.*;

import java.util.Arrays;

public class Bot{
    public final DiscordApi api;

    public Bot() {
        api = new DiscordApiBuilder().setToken(Config.BotToken).login().join();

        api.addMessageCreateListener(event -> {
            Message msg = event.getMessage();
            if (msg.getContent().startsWith(Config.BotPrefix) && msg.getChannel().getIdAsString().equals(Config.BotChannelId)) {
                String[] text = msg.getContent().split("[ ]?");

                String command = text[0];
                String[] args = { };

                if (text.length != 1) {
                    args = Arrays.copyOfRange(text, 1, text.length);
                }
                String[] a = args;
                new Thread(() -> parseCommand(command, a, msg)).start();
            }
        });
    }

    private void parseCommand(String command, String[] args, Message msg) {
        switch (command) {
            case "link":
                link(args, msg);
                break;

            case "unlink":
                unlink(args, msg);
                break;

            case "cancel":
                unlink(args, msg);
                break;

            default:
                help(msg);
        }
    }

    public boolean hasRole(String userId, String roleId) {
        if (api.getServerById(Config.BotServerId).isPresent()) {
            Server server = api.getServerById(Config.BotServerId).get();
            if (server.getMemberById(userId).isPresent()) {
                User user = server.getMemberById(userId).get();
                if (server.getRoleById(roleId).isPresent()) {
                    Role role = server.getRoleById(roleId).get();
                    return user.getRoles(server).contains(role);
                }
            }
        }
        return false;
    }

    private void help(Message msg) {
        msg.getChannel().sendMessage("Help");
    }

    private void cancel(String[] args, Message msg) {
        if (!Config.session.isSessionActive()) {
            msg.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("There no session active")
            );
            return;
        }

        if (!Config.session.getDiscordUser().getIdAsString().equals(msg.getAuthor().asUser().get().getIdAsString())) {
            msg.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Only " + Config.session.getDiscordUser().getDiscriminatedName() + " can cancel the " + (Config.session.isUnlinkSession() ? "unlink" : "link"))
            );
            return;
        }

        try {
            Config.session.cancel();
        } catch (IllegalStateException e) {
            msg.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Error")
                .setDescription("There no session active")
            );
        }
    }

    private void unlink(String[] args, Message msg) {
        if (args.length == 0) {
            msg.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Unlink help")
                .setDescription("Unlinks a player from discord account")
                .addField("Usage", Config.BotPrefix + "unlink <ign name>")
            );
            return;
        }

        String ign = args[0];

        for (int i = 0; i < playerGroup.size(); i++) {
            if (playerGroup.all().get(i).name.equals(ign)) {
                PlayerData data = Config.db.getData(playerGroup.all().get(i).uuid);
                if (data != null && data.discordId != null) {
                    Config.session.init(msg, msg.getAuthor().asUser().get(), player, true);
                    return;
                }

                msg.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Unlink Failed")
                    .setDescription(player.name + " was not linked to a discord account")
                );

                return;
            }
        }

        msg.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Unlink Failed")
                .setDescription(ign + " was not found on the server, make sure the player is online")
        );
    }

    private void link(String[] args, Message msg) {
        if (args.length == 0) {
            msg.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Link help")
                .setDescription("Links a player to a discord account")
                .addField("Usage", Config.BotPrefix + "link <ign name>"));
            return;
        }

        String ign = args[0];

        for (int i = 0; i < playerGroup.size(); i++) {
            if (playerGroup.all().get(i).name.equals(ign)) {
                Config.session.init(msg, msg.getAuthor().asUser().get(), playerGroup.all().get(i));
                return;
            }
        }

        msg.getChannel().sendMessage(new EmbedBuilder()
            .setTitle("Link Failed")
            .setDescription(ign + " was not found on the server, make sure the player is online")
        );
    }
}
