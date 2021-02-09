# vShop3.0
## Check out the plugin on Spigot: [vShop Remastered](https://www.spigotmc.org/resources/vshop-remastered.62508/)

A virtual shop, accessible through commands. Fast and efficient.

I use this project as a way to implement new concepts or technologies that I learn. I started this project when I was 15 and have been adding to it ever since.

Right now, I'm working on a companion website that would allow a user to perform all the actions a player could perform in-game. I'll put a link to the repo once I start some work on it.

**Dependencies:**
  - [IDLogger](https://github.com/arif-banai/IDLogger)
  - [Vault](https://www.spigotmc.org/resources/vault.34315/)

This plugin allows players to buy and sell from each other using only commands to make buying and selling on your 
economy server quick and efficient!

The plugin uses a MySQL or SQLite database to store Offers and Transactions. You can configure the database type you 
wish to use in the config.yml.

By default, all offers made by players are broadcast to every player on the server. 
This can also be configured in the config.yml

Please reference this website (linked) for Minecraft 1.13 item names: https://minecraftitemids.com/

**Changelog**

- **2/9/2021**
    - Added JUnit tests for adding offers and finding the amount of offers for some item
    - Abstracted query implementation from QueryManager using Queries and AsyncQueries 
    - Updated javadoc in various places
    - Created custom exceptions
    - Code cleanup and refactoring
    - Updated maven dependencies and plugins
    - Updated vShop version to 2.5