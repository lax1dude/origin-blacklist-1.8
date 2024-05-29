package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.api.EaglerXVelocityAPIHelper;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.command.EaglerCommand;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.server.EaglerPlayerData;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.OriginBlacklistPluginVelocity;

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
public class CommandDomainBlock extends EaglerCommand {

	public CommandDomainBlock() {
		super("block-domain", "eaglercraft.command.blockdomain");
	}

	@Override
	public void execute(CommandSource p0, String[] p1) {
		if (p1.length < 1) {
			p0.sendMessage(Component.text("Please follow this command by a username", NamedTextColor.RED));
			return;
		}
		final Player user = OriginBlacklistPluginVelocity.proxy().getPlayer(p1[0]).orElse(null);
		if (user == null) {
			p0.sendMessage(Component.text("That user is not online", NamedTextColor.RED));
		}else {
			EaglerPlayerData eagPlayer = EaglerXVelocityAPIHelper.getEaglerHandle(user);
			if(eagPlayer != null) {
				String o = OriginBlacklist.removeProtocolFromOrigin(eagPlayer.getOrigin());
				if(o != null) {
					if("null".equals(o)) {
						p0.sendMessage(Component.text("That user is on an offline download", NamedTextColor.RED));
					}else {
						OriginBlacklist bl = OriginBlacklistPluginVelocity.getPlugin().list;
						bl.addLocal(o);
						for(Player p : OriginBlacklistPluginVelocity.proxy().getAllPlayers()) {
							eagPlayer = EaglerXVelocityAPIHelper.getEaglerHandle(p);
							if(eagPlayer != null) {
								String oo = OriginBlacklist.removeProtocolFromOrigin(eagPlayer.getOrigin());
								if(oo != null) {
									if(bl.test(oo)) {
										String msg = bl.getKickMessage();
										if(msg != null) {
											p.disconnect(Component.text(msg));
										}else {
											p.disconnect(Component.translatable("disconnect.endOfStream"));
										}
									}
								}
							}
						}
						p0.sendMessage(Component.text("Domain of ", NamedTextColor.RED)
								.append(Component.text(p1[0], NamedTextColor.WHITE))
								.append(Component.text(" is ", NamedTextColor.RED))
								.append(Component.text(o, NamedTextColor.WHITE)));
						p0.sendMessage(Component.text("It was added to the local block list.", NamedTextColor.RED));
					}
				}else {
					p0.sendMessage(Component.text("Domain of " + p1[0] + " is unknown (desktop runtime?)", NamedTextColor.RED));
				}
			}else {
				p0.sendMessage(Component.text("That user is not using an Eaglercraft client", NamedTextColor.RED));
			}
		}
	}

}
