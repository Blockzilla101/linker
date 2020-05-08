package linker;

import arc.util.Log;

import mindustry.entities.type.Player;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LinkSession {
    private boolean isActive;
    private boolean isUnlink;

    private Player player;
    private User discordUser;

    private int tryNum;

    private String key;
    private Message waitMsg;

    private final Timer timer = new Timer();
    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (!isActive) throw new IllegalStateException("No session is active");

            player.sendMessage(((isUnlink) ? "Unlink" : "Link") + " session timed out");

            if (waitMsg != null) {
                waitMsg.edit(new EmbedBuilder()
                    .setTitle(((isUnlink) ? "Unlink" : "Link") + " Timed out")
                    .setDescription(((isUnlink) ? "Unlink" : "Link") + " session timed out")
                );
            }

            reset();
        }
    };

    public LinkSession() { }

    public void init(Message msg, User discordUser, Player player) throws IllegalStateException {
        init(msg, discordUser, player, false);
    }

    public void init(Message msg, User discordUser, Player player, boolean unlink) throws IllegalStateException {
        if (isActive) throw new IllegalStateException("Cannot create a session while one is active");

        this.discordUser = discordUser;
        this.player = player;

        this.isUnlink = unlink;

        this.key = Long.toHexString(new Random().nextLong());

        try {
            CompletableFuture<Message> m = msg.getChannel().sendMessage(new EmbedBuilder()
                .setTitle(((isUnlink) ? "Unlink" : "Link") + " Started")
                .setDescription("Type /confirm [Key] to confirm, /decline [Key] to deny "+((isUnlink) ? "unlink" : "link")+" in-game")
                .addField("Key: ", key, true)
            );
            m.wait();
            if (m.isDone()) waitMsg = m.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.err("Could not get last sent Message", e);
        }

        player.sendMessage(msg.getAuthor().getDiscriminatedName() + " has started a "+((isUnlink) ? "unlink" : "link")+" session, type /confirm [Key] to confirm, /deny [Key] to deny");

        timer.schedule(task, (long) 1000 * 15);
        Log.info(((isUnlink) ? "Unlink" : "Link") + " session started by " + msg.getAuthor().getDisplayName() + " for " + player.name);
    }

    public void confirm(String key) throws IllegalStateException, IllegalArgumentException {
        if (!this.key.equals(key)) {
            tryNum++;

            if(tryNum >= Config.SessionMaxTries - 1) {
                InvalidKey();
                return;
            }
            throw new IllegalArgumentException("Invalid key");
        }

        if (!isActive) throw new IllegalStateException("No session is active");

        PlayerData data = Config.db.getData(player.uuid);
        data.discordId = ((isUnlink) ? null : discordUser.getIdAsString());
        data.isAdmin = false;
        Config.db.update(data);

        player.isAdmin = false;

        if (waitMsg != null) {
            waitMsg.edit(new EmbedBuilder()
                .setTitle(((isUnlink) ? "Unlink" : "Link") + " Complete")
                .setDescription("Your account is now "+((isUnlink) ? "unlinked from " : "linked to ") + player.name)
            );
        }

        player.sendMessage(((isUnlink) ? "Unlink from " : "Link to ") + discordUser.getDiscriminatedName() + " has been successfully completed");
        reset();
    }

    public void decline(String key) throws IllegalStateException, IllegalArgumentException{
        if (!this.key.equals(key)) {
            tryNum++;

            if(tryNum >= Config.SessionMaxTries - 1) {
                InvalidKey();
                return;
            }
            throw new IllegalArgumentException("Invalid key");
        }
        if (!isActive) throw new IllegalStateException("No session is active");

        if (waitMsg != null) {
            waitMsg.edit(new EmbedBuilder()
                .setTitle(((isUnlink) ? "Unlink" : "Link") +" denied")
                .setDescription(((isUnlink) ? "unlink" : "link") + " was denied by " + player.name)
            );
        }
        reset();
    }

    public void cancel() throws IllegalStateException {
        if (!isActive) throw new IllegalStateException("No session is active");

        player.sendMessage(discordUser.getDiscriminatedName() + " has cancelled the " + ((isUnlink) ? "unlink" : "link"));
        waitMsg.edit(new EmbedBuilder()
            .setTitle(((isUnlink) ? "Unlink" : "Link") + " cancelled")
            .setDescription(((isUnlink) ? "Unlink" : "Link") + " has been cancelled")
        );
        reset();
    }

    public void InvalidKey() throws IllegalStateException {
        if (!isActive) throw new IllegalStateException("No session is active");

        waitMsg.edit(new EmbedBuilder()
            .setTitle(((isUnlink) ? "Unlink" : "Link") + " Failed")
            .setDescription("An invalid key was specified more then " + Config.SessionMaxTries)
        );
        reset();
    }

    public boolean isUnlinkSession() { return isUnlink; }
    public boolean isSessionActive() { return isActive; }
    public Player getPlayer() { return player; }
    public User getDiscordUser() { return discordUser; }
    public Message getWaitMsg() { return waitMsg; }

    private void reset() {
        this.player = null;
        this.discordUser = null;
        this.waitMsg = null;

        this.isActive = false;

        this.key = null;
        this.tryNum = 0;
        this.isActive = false;
    }
}
