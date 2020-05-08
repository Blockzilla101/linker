package linker;

import arc.struct.Array;
import arc.util.serialization.Json;
import arc.util.Log;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class Database {
    private Connection conn;
    private final Json json = new Json();

    public Database(String path) {
        connect(path);
    }

    public PlayerData getData(String uuid) {
        try {
            PreparedStatement statement = conn.prepareStatement("" +
                    "SELECT \n" +
                    "   uuid,\n" +
                    "   discordId,\n" +
                    "   names,\n" +
                    "   isAdmin\n" +
                    "FROM\n" +
                    "   players\n" +
                    "WHERE\n" +
                    "   uuid='"+uuid+"'");

            try (ResultSet result = statement.executeQuery()) {
                if(result.next()) {
                    return new PlayerData(
                            result.getString("uuid"),
                            result.getString("discordId"),
                            (Array<String>) json.fromJson(Array.class, result.getString("names")),
                            result.getBoolean("isAdmin")
                    );
                }
            } catch (SQLException e) {
                Log.err(e);
            }

        } catch (SQLException e) {
            Log.err("Error while getting player data", e);
        }
        return null;
    }

    public boolean update(PlayerData data) {
        try {
            PreparedStatement statement = conn.prepareStatement("" +
                    "UPDATE players" +
                    "   SET " +
                    "       uuid," +
                    "       discordId," +
                    "       names," +
                    "       isAdmin" +
                    "WHERE" +
                    "   uuid = ?");

            statement.setString(1, data.uuid);
            statement.setString(2, data.discordId);
            statement.setString(3, json.toJson(data.names));
            statement.setBoolean(5, data.isAdmin);

            statement.setString(7, data.uuid);

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            Log.err("Error while updating player data", e);
        }
        return false;
    }

    public void addData(PlayerData data) {
        try {
            PreparedStatement statement = conn.prepareStatement("" +
                    "INSERT INTO players " +
                    "   VALUES(?, ?, ?, ?)");

            statement.setString(1, data.uuid);
            statement.setString(2, data.discordId);
            statement.setString(3, json.toJson(data.names));
            statement.setBoolean(5, data.isAdmin);

            statement.executeUpdate();
            Log.info("Added " + data + " to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect(String databasePath) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            PreparedStatement statement = con.prepareStatement(""
                    + "CREATE TABLE IF NOT EXISTS 'players' (\n"
                    + "uuid text PRIMARY KEY NOT NULL,\n"
                    + "discordId text,\n"
                    + "names text NOT NULL,\n"
                    + "isAdmin integer NOT NULL,\n"
                    + ")");

            statement.execute();
            this.conn = con;

            Log.info("Connected to " + databasePath);
        } catch (SQLException | ClassNotFoundException e) {
            Log.err("Error while connecting to " + databasePath, e);
        }
    }
}