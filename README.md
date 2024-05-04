# Origin Blacklist for EaglercraftXBungee 1.8

### This plugin can be used to add the "Origin Blacklist" feature from Eaglercraft 1.5.2 to you EaglercraftXBungee 1.8 server. Fully compatible with old 1.5.2 config files.

## Features

- **Block players from using specific websites to play on your server**
- **Block players from using offline downloads to play on your server**
- **Block players from using replit clients to play on your server**
- **Use regular expressions to match multiple domains by wildcard**
- **Whitelist to only allow players to join using specific websites**
- **Commands to block/unblock a player's website automatically**
- **Automatically load additional lists of blocked websites via URL**

## Installation

Place the "**[OriginBlacklist.jar](https://raw.githubusercontent.com/lax1dude/origin-blacklist-1.8/main/OriginBlacklist.jar)**" file in your BungeeCord server's plugins folder and restart the server, config files will be generated in "plugins/OriginBlacklist".

## Commands

- `/block-domain <username>` Automatically add the domain a player is using to the local origin blacklist and kick all players currently using that domain. Permission: `eaglercraft.command.blockdomain`

- `/block-domain-name <domain>` Automatically add a domain to the local origin blacklist and kick all players currently using that domain. Permission: `eaglercraft.command.blockdomainname`

- `/unblock-domain <domain>` Automatically remove a domain to the local origin blacklist. Permission: `eaglercraft.command.unblockdomain`

## config.yml

- `origin_blacklist_kick_message:` The message to display when kicking players
- `origin_blacklist_block_missing_origin_header:` Allows you to block the desktop runtime
- `origin_blacklist_block_offline_download:` Allows you to block offline downloads
- `origin_blacklist_block_replit_clients:` Allows you to block replit clients
- `enable_web_origin_blacklist:` Enables downloading origin blacklist from a list of URLs
- `origin_blacklist_subscriptions:` URLs to download origin blacklist entries from
- `origin_blacklist_use_simple_whitelist:` Enables simple whitelist mode
- `origin_blacklist_simple_whitelist:` List of allowed domains for simple whitelist mode
