package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.EaglerXVelocityVersion;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklistConfigAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklistLoggerAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.command.CommandDomainBlock;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.command.CommandDomainBlockDomain;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.velocity.command.CommandDomainUnblock;

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
@Plugin(
		id = OriginBlacklistPluginVersion.ID,
		name = OriginBlacklistPluginVersion.NAME,
		description = OriginBlacklistPluginVersion.DESCRIPTION,
		version = OriginBlacklistPluginVersion.VERSION,
		authors = {
			OriginBlacklistPluginVersion.AUTHOR
		},
		dependencies = {
			@Dependency(
				id = EaglerXVelocityVersion.PLUGIN_ID,
				optional = false
			)
		}
)
public class OriginBlacklistPluginVelocity {

	private static OriginBlacklistPluginVelocity instance = null;
	private final ProxyServer proxy;
	private final Logger logger;
	private final Path dataDirAsPath;
	private final File dataDir;

	public final OriginBlacklist list;

	private Timer updateOriginBlacklistTimer = null;

	@Inject
	public OriginBlacklistPluginVelocity(ProxyServer proxyIn, Logger loggerIn, @DataDirectory Path dataDirIn) {
		instance = this;
		proxy = proxyIn;
		logger = loggerIn;
		dataDirAsPath = dataDirIn;
		dataDir = dataDirIn.toFile();
		list = new OriginBlacklist(new OriginBlacklistLoggerAdapter() {
			@Override
			public void warn(String msg) {
				OriginBlacklistPluginVelocity.this.logger.warn(msg);
			}
			
			@Override
			public void info(String msg) {
				OriginBlacklistPluginVelocity.this.logger.info(msg);
			}
			
			@Override
			public void error(String msg) {
				OriginBlacklistPluginVelocity.this.logger.error(msg);
			}
		});
	}

	private void reloadConfig() {
		OriginBlacklistConfigAdapter cfg;
		try {
			cfg = OriginBlacklistConfigVelocity.loadConfig(dataDir);
		}catch(IOException ex) {
			throw new RuntimeException("Could not load origin blacklist config file!", ex);
		}
		list.init(cfg);
	}

	@Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
		reloadConfig();
		if(updateOriginBlacklistTimer == null) {
			updateOriginBlacklistTimer = new Timer("EaglerXBungee: Origin Blacklist Updater");
			updateOriginBlacklistTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						list.update();
					}catch(Throwable t) {
						OriginBlacklistPluginVelocity.this.logger.error("Could not update origin blacklist!", t);
					}
				}
			}, 0, 6000l);
		}
		proxy.getEventManager().register(this, new OriginBlacklistListenerVelocity(this));
		CommandRegisterHelper.register(this, new CommandDomainBlock());
		CommandRegisterHelper.register(this, new CommandDomainBlockDomain());
		CommandRegisterHelper.register(this, new CommandDomainUnblock());
	}

	@Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
		if(updateOriginBlacklistTimer != null) {
			updateOriginBlacklistTimer.cancel();
			updateOriginBlacklistTimer = null;
		}
	}

	public ProxyServer getProxy() {
		return proxy;
	}

	public Logger getLogger() {
		return logger;
	}

	public static OriginBlacklistPluginVelocity getPlugin() {
		return instance;
	}

	public static ProxyServer proxy() {
		return instance.proxy;
	}

	public static Logger logger() {
		return instance.logger;
	}
}
