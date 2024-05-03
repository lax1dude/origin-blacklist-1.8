package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.bungee;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklist;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklistConfigAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklistLoggerAdapter;
import net.md_5.bungee.api.plugin.Plugin;

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
public class OriginBlacklistPluginBungee extends Plugin {

	private static OriginBlacklistPluginBungee instance = null;

	public final OriginBlacklist list;

	private Timer updateOriginBlacklistTimer = null;

	public OriginBlacklistPluginBungee() {
		instance = this;
		list = new OriginBlacklist(new OriginBlacklistLoggerAdapter() {
			@Override
			public void warn(String msg) {
				OriginBlacklistPluginBungee.this.getLogger().warning(msg);
			}
			
			@Override
			public void info(String msg) {
				OriginBlacklistPluginBungee.this.getLogger().info(msg);
			}
			
			@Override
			public void error(String msg) {
				OriginBlacklistPluginBungee.this.getLogger().severe(msg);
			}
		});
	}

	@Override
	public void onLoad() {
		reloadConfig();
	}

	private void reloadConfig() {
		OriginBlacklistConfigAdapter cfg;
		try {
			cfg = OriginBlacklistConfigBungee.loadConfig(getDataFolder());
		}catch(IOException ex) {
			throw new RuntimeException("Could not load origin blacklist config file!", ex);
		}
		list.init(cfg);
	}

	@Override
	public void onEnable() {
		if(updateOriginBlacklistTimer == null) {
			updateOriginBlacklistTimer = new Timer("EaglerXBungee: Origin Blacklist Updater");
			updateOriginBlacklistTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						list.update();
					}catch(Throwable t) {
						OriginBlacklistPluginBungee.this.getLogger().log(Level.SEVERE, "Could not update origin blacklist!", t);
					}
				}
			}, 0, 6000l);
		}
		getProxy().getPluginManager().registerListener(this, new OriginBlacklistListenerBungee(this));
	}

	@Override
	public void onDisable() {
		if(updateOriginBlacklistTimer != null) {
			updateOriginBlacklistTimer.cancel();
			updateOriginBlacklistTimer = null;
		}
		getProxy().getPluginManager().unregisterListeners(this);
	}

	public static OriginBlacklistPluginBungee getPlugin() {
		return instance;
	}

	public static Logger logger() {
		return instance.getLogger();
	}
}
