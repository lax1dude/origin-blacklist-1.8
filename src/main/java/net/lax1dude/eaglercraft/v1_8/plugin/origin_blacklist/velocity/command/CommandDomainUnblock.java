package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.command;

import com.velocitypowered.api.command.CommandSource;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.command.EaglerCommand;
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
public class CommandDomainUnblock extends EaglerCommand {

	public CommandDomainUnblock() {
		super("unblock-domain", "eaglercraft.command.unblockdomain", "unblock-domain-name");
	}

	@Override
	public void execute(CommandSource p0, String[] p1) {
		if (p1.length < 1) {
			p0.sendMessage(Component.text("Please follow this command by a domain", NamedTextColor.RED));
			return;
		}
		OriginBlacklist bl = OriginBlacklistPluginVelocity.getPlugin().list;
		if(bl.removeLocal(p1[0])) {
			p0.sendMessage(Component.text("The domain '" + p1[0] + "' was removed from the local block list", NamedTextColor.GREEN));
		}else {
			p0.sendMessage(Component.text("The domain was not removed, is it on the block list? Check '" + bl.getLocalBlacklist().getName() + "' in your plugin directory", NamedTextColor.RED));
		}
	}

}
