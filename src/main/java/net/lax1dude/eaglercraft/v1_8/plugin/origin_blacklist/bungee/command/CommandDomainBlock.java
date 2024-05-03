package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.bungee.command;

import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.server.EaglerInitialHandler;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.bungee.OriginBlacklistPluginBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class CommandDomainBlock extends Command {

	public CommandDomainBlock() {
		super("block-domain", "eaglercraft.command.blockdomain");
	}

	@Override
	public void execute(CommandSender p0, String[] p1) {
		if (p1.length < 1) {
			p0.sendMessage(new TextComponent(ChatColor.RED + "Please follow this command by a username"));
			return;
		}
		final ProxiedPlayer user = ProxyServer.getInstance().getPlayer(p1[0]);
		if (user == null) {
			p0.sendMessage(new TextComponent(ChatColor.RED + "That user is not online"));
		}else {
			if(user.getPendingConnection() instanceof EaglerInitialHandler) {
				Object o = ((EaglerInitialHandler)user.getPendingConnection()).getOrigin();
				if(o != null) {
					if("null".equals(o)) {
						p0.sendMessage(new TextComponent(ChatColor.RED + "That user is on an offline download"));
					}else {
						OriginBlacklist bl = OriginBlacklistPluginBungee.getPlugin().list;
						bl.addLocal((String)o);
						p0.sendMessage(new TextComponent(ChatColor.RED + "Domain of " + ChatColor.WHITE + p1[0] + ChatColor.RED + " is " + ChatColor.WHITE + o));
						p0.sendMessage(new TextComponent(ChatColor.RED + "It was added to the local block list."));
						user.disconnect(new TextComponent(bl.getKickMessage()));
					}
				}else {
					p0.sendMessage(new TextComponent(ChatColor.RED + "Domain of " + p1[0] + " is unknown (desktop runtime?)"));
				}
			}else {
				p0.sendMessage(new TextComponent(ChatColor.RED + "That user is not using an Eaglercraft client"));
			}
		}
	}

}
