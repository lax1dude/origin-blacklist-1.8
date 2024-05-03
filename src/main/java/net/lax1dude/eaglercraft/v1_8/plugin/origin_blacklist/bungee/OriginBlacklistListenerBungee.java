package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.bungee;

import java.util.logging.Level;

import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.server.EaglerInitialHandler;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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
public class OriginBlacklistListenerBungee implements Listener {

	private final OriginBlacklistPluginBungee plugin;

	public OriginBlacklistListenerBungee(OriginBlacklistPluginBungee plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleLoginEvent(LoginEvent evt) {
		if(evt.getConnection() instanceof EaglerInitialHandler) {
			EaglerInitialHandler eaglerCon = (EaglerInitialHandler)evt.getConnection();
			String origin = eaglerCon.getOrigin();
			OriginBlacklist blacklist = plugin.list;
			boolean shouldKick = true;
			try {
				shouldKick = (origin == null && blacklist.getBlockClientsWithNoOriginHeader()) || blacklist.test(origin);
			}catch(Throwable t) {
				plugin.getLogger().log(Level.SEVERE, "Failed to check origin blacklist for: " + origin, t);
			}
			if(shouldKick) {
				plugin.getLogger().info("Disconnecting a player who joined from blacklisted origin: " + origin);
				evt.setCancelled(true);
				String msg = blacklist.getKickMessage();
				if(msg != null) {
					evt.setReason(new TextComponent());
				}
			}
		}
	}

}
