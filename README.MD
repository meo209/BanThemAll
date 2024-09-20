# BanThemAll
Very Simple minecraft anti-cheat for fabric
<br>
This project was inspired by [Inertia Anticheat](https://modrinth.com/mod/inertiaanticheat).

## How does it work?

### Client-Side:

On initialization, the client scans the mods folder, computes the SHA-256 hash for each .jar file, and combines it with the mod’s file name.
The client sends this list to the server upon joining.

### Server-Side
The server listens for incoming mod lists from clients.
Upon receiving a list, the server verifies that each hash in the list matches a pre-approved list in the server's configuration.
If all mod hashes are valid, the player is allowed to stay. If not, the player is disconnected with a message.

## How do I whitelist mods?
Just add the JAR file to config/banthemall-mods. (Server-Side)
**You need to put in the EXACT same file the client uses.**

## Is this foolproof?
Nope. It's really easy to bypass, however, this project was intended to be useful against casual cheaters.