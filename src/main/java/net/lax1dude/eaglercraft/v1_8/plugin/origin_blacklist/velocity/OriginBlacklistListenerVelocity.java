package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;

import net.kyori.adventure.text.Component;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.api.EaglerXVelocityAPIHelper;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.server.EaglerPlayerData;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;

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
public class OriginBlacklistListenerVelocity {

	private final OriginBlacklistPluginVelocity plugin;

	public OriginBlacklistListenerVelocity(OriginBlacklistPluginVelocity plugin) {
		this.plugin = plugin;
	}

	@Subscribe(order = PostOrder.FIRST)
	public void handleLoginEvent(LoginEvent evt) {
		EaglerPlayerData eaglerCon = EaglerXVelocityAPIHelper.getEaglerHandle(evt.getPlayer());
		if(eaglerCon != null) {
			String origin = eaglerCon.getOrigin();
			OriginBlacklist blacklist = plugin.list;
			boolean shouldKick = true;
			try {
				shouldKick = origin == null ? blacklist.getBlockClientsWithNoOriginHeader() : blacklist.test(OriginBlacklist.removeProtocolFromOrigin(origin));
			}catch(Throwable t) {
				plugin.getLogger().error("Failed to check origin blacklist for: " + origin, t);
			}
			if(shouldKick) {
				plugin.getLogger().info("Disconnecting a player who joined from blacklisted origin: " + origin);
				String msg = blacklist.getKickMessage();
				if(msg != null) {
					evt.setResult(ComponentResult.denied(Component.text(msg)));
				}else {
					evt.setResult(ComponentResult.denied(Component.translatable("disconnect.endOfStream")));
				}
			}
		}
	}

}
