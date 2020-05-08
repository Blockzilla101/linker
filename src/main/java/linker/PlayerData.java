package linker;

import arc.struct.Array;

public class PlayerData {
    public String uuid;
    public String discordId;

    public Array<String> names;

    public boolean isAdmin;

    public PlayerData(String uuid, String discordId, Array<String> names, boolean isAdmin){
        this.uuid = uuid;
        this.discordId = discordId;

        this.names = names;

        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid='" + uuid + '\'' +
                ", discordId='" + discordId + '\'' +
                ", names=" + names +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
