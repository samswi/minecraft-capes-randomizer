# Minecraft capes randomizer
Fabric mod for people who can't choose their favorite minecraft cape

This very simple fabric mod utilizes [Mojang API](https://minecraft.wiki/w/Mojang_API) to fetch capes the logged in player owns, and then equips a random one every time player disconnects from a server.

<img src="https://github.com/user-attachments/assets/2ee2cd31-9b71-44d2-b629-f4f20f231327" alt="An animated gif showing 2 minecraft instances. One on the right shows player relogging into a LAN world, and one on the left observing how the cape changes on every relog." style="width: 10000px;"/>

You can choose which capes will be included in randomizer pool by opening the "Select favorite capes" screen via mod menu or by using `/caperandomizer set_favorites` command. In this screen you can also select the default cape, which will automatically equip when you close the game.