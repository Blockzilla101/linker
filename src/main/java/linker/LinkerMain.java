package linker;

import arc.*;
import arc.struct.Array;
import arc.util.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.plugin.Plugin;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static mindustry.Vars.*;

public class LinkerMain extends Plugin {

    public LinkerMain() throws FileNotFoundException {
        Config.load(Core.settings.getDataDirectory().path() + "/linker_config.json");

        loadFileLogger();

        Config.db = new Database(Config.DatabasePath);
        Config.session = new LinkSession();
        Config.bot = new Bot();

        Events.on(PlayerConnect.class, event -> {
            PlayerData data = Config.db.getData(event.player.uuid);
            if (data == null) {
                Array<String> names = new Array<>();
                names.add(event.player.name);
                Config.db.addData(
                    new PlayerData(
                        event.player.uuid,
                        null,
                        names,
                        false
                    )
                );
                return;
            }

            if (!data.names.contains(event.player.name)) data.names.add(event.player.name);

            if (data.discordId != null) {
                if (Config.bot.hasRole(data.discordId, Config.AdminRoleId)) {
                    event.player.isAdmin = true;
                }
            }
        });

        Events.on(ServerLoadEvent.class, event -> netServer.admins.addChatFilter((player, text) -> {
            if (text.equals(Config.AdminPassword)) {
                PlayerData data = Config.db.getData(player.uuid);
                if (data.discordId != null) {
                    if (Config.bot.hasRole(data.discordId, Config.AdminRoleId)) {
                        data.isAdmin = true;
                        Config.db.update(data);
                        player.isAdmin = true;
                    } else {
                        player.sendMessage("You don't have the required role on discord");
                    }
                } else {
                    player.sendMessage("Link you're discord first");
                }
            }
            return "hi";
        }));
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.register("confirm", "<Key>", "Confirm an on-going link session", (args, player) -> {
            if (!Config.session.isSessionActive()) {
                ((Player) player).sendMessage("No session is active");
                return;
            }

            if (!((Player) player).uuid.equals(Config.session.getPlayer().uuid)) {
                ((Player) player).sendMessage("Only " + Config.session.getPlayer().name + " can confirm the session");
                return;
            }
            try {
                Config.session.confirm(args[0]);
            } catch (IllegalStateException e) {
                ((Player) player).sendMessage("No session is active");
            } catch (IllegalArgumentException e) {
                ((Player) player).sendMessage("Invalid key");
            }
        });

        handler.register("decline", "<Key>", "Decline an on-going link session", (args, player) -> {
            if (!Config.session.isSessionActive()) {
                ((Player) player).sendMessage("No session is active");
                return;
            }

            if (!((Player) player).uuid.equals(Config.session.getPlayer().uuid)) {
                ((Player) player).sendMessage("Only " + Config.session.getPlayer().name + " can confirm the session");
                return;
            }
            try {
                Config.session.decline(args[0]);
            } catch (IllegalStateException e) {
                ((Player) player).sendMessage("No session is active");
            } catch (IllegalArgumentException e) {
                ((Player) player).sendMessage("Invalid key");
            }
        });

        handler.register("linkstatus", "Displays you're linked discord account", (args, player) -> {
            PlayerData data = Config.db.getData(((Player)player).uuid);
            if (data.discordId != null) {
                if (Config.bot.api.getServerById(Config.BotServerId).isPresent()) {
                    Server server = Config.bot.api.getServerById(Config.BotServerId).get();
                    if (server.getMemberById(data.discordId).isPresent()) {
                        User user = server.getMemberById(data.discordId).get();
                        ((Player)player).sendMessage("You're linked to " + user.getDiscriminatedName());
                    } else {
                        CompletableFuture<User> task = Config.bot.api.getUserById(data.discordId);
                        try {
                            task.wait();
                            if (task.isDone()) {
                                ((Player) player).sendMessage("You're linked account " + task.get().getDiscriminatedName() + " is not on " + server.getName());
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            ((Player)player).sendMessage("Failed to get account");
                        }
                    }
                }
            } else {
                ((Player)player).sendMessage("You have no linked discord account");
            }
        });
    }
}
