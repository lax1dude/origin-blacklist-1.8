package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.EaglerXBungee;
import net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist.OriginBlacklistConfigAdapter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

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
public class OriginBlacklistConfigBungee implements OriginBlacklistConfigAdapter {

	public static OriginBlacklistConfigBungee loadConfig(File dataDir) throws IOException {
		if(!dataDir.isDirectory() && !dataDir.mkdirs()) {
			throw new IOException("Could not create directory: " + dataDir.getAbsolutePath());
		}
		File configFile = new File(dataDir, "config.yml");
		if(!configFile.exists()) {
			try(InputStream defaultConf = OriginBlacklistConfigBungee.class.getResourceAsStream("../default_config.yml")) {
				try(OutputStream os = new FileOutputStream(configFile)) {
					byte[] copyBuffer = new byte[1024];
					int i;
					while((i = defaultConf.read(copyBuffer)) != -1) {
						os.write(copyBuffer, 0, i);
					}
				}
			}
		}
		Configuration conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
		return new OriginBlacklistConfigBungee(dataDir, conf);
	}

	private final File dataDir;
	private final Configuration conf;

	private OriginBlacklistConfigBungee(File dataDir, Configuration conf) {
		this.dataDir = dataDir;
		this.conf = conf;
	}

	@Override
	public File getLocalBlacklistFile() {
		return new File(dataDir, "origin_blacklist.txt");
	}

	@Override
	public String getKickMessage() {
		return conf.getString("origin_blacklist_kick_message", "End of stream");
	}

	@Override
	public boolean getBlockClientsWithNoOriginHeader() {
		return conf.getBoolean("origin_blacklist_block_missing_origin_header", false);
	}

	@Override
	public String getSubscriptionDownloadUserAgent() {
		return "Mozilla/5.0 EaglerXBungee/" + EaglerXBungee.getEagler().getDescription().getVersion();
	}

	@Override
	public Collection<String> getBlacklistURLs() {
		boolean enableSubscribe = conf.getBoolean("enable_web_origin_blacklist", false);
		if(!enableSubscribe) {
			return null;
		}
		return (Collection<String>)conf.getList("origin_blacklist_subscriptions", new ArrayList<String>());
	}

	@Override
	public boolean shouldBlacklistOfflineDownload() {
		return conf.getBoolean("origin_blacklist_block_offline_download", false);
	}

	@Override
	public boolean shouldBlacklistReplits() {
		return conf.getBoolean("origin_blacklist_block_replit_clients", false);
	}

	@Override
	public boolean isSimpleWhitelistEnabled() {
		return conf.getBoolean("origin_blacklist_use_simple_whitelist", false);
	}

	@Override
	public Collection<String> getBlacklistSimpleWhitelist() {
		return (Collection<String>)conf.getList("origin_blacklist_simple_whitelist", new ArrayList<String>());
	}

}
