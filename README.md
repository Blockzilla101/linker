## Linker Plugin

Links a player to discord and gives them admin status based on a role <br>
#### Configuration

A file named `linker_plugin.json` should be placed on the config folder of the server <br>
Config values:<br>
```jsonc
{
    "databasePath": "./config/players.db",
    "botPrefix": "!",
    "botChannelId": "<Channel id where the bot will be listening>",
    "botServerId": "<Server for the bot>",
    "botToken": "<Bot token>",
    "adminPassword": "<What needs to typed in chat to get admin status with re connecting [may get removed]>",
    "adminRoleId": "<Role to be had to get admin>",
    "sessionMaxTries": 3, //<How many times a wrong unlink/link key can be entered>
    "sessionTimeout": 20 // <How long will a link/unlink session last [in seconds]>
}
```
