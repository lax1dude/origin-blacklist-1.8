package net.lax1dude.eaglercraft.v1_8.plugin.origin_blacklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
public class OriginBlacklist {

	private final OriginBlacklistLoggerAdapter logger;
	private String kickMessage = null;
	private boolean blockClientsWithNoOriginHeader = false;
	public final Collection<Pattern> regexBlacklist = new ArrayList();
	public final Collection<Pattern> regexLocalBlacklist = new ArrayList();
	public final Collection<Pattern> regexBlacklistReplit = new ArrayList();
	public final Collection<String> simpleWhitelist = new ArrayList();
	private File localBlacklist = null;
	private String subscriptionDownloadUserAgent = null;
	private Collection<String> blacklistSubscriptions = null;
	private boolean blockOfflineDownload = false;
	private boolean blockAllReplits = false;
	private boolean localWhitelistMode = false;
	private boolean simpleWhitelistMode = false;
	private final HashSet<String> brokenURLs = new HashSet();
	private final HashSet<String> brokenRegex = new HashSet();

	public static final HashSet<String> regexBlacklistReplitInternalStrings = new HashSet();
	public static final Collection<Pattern> regexBlacklistReplitInternal = new ArrayList();
	
	static {
		regexBlacklistReplitInternalStrings.add(".*repl(it)?\\..{1,5}$");
		for(String s : regexBlacklistReplitInternalStrings) {
			regexBlacklistReplitInternal.add(Pattern.compile(s));
		}
	}

	private int updateRate = 15 * 60 * 1000;
	private long lastLocalUpdate = 0l;
	private long lastUpdate = 0;
	
	public OriginBlacklist(OriginBlacklistLoggerAdapter log) {
		logger = log;
	}
	
	public String getKickMessage() {
		return kickMessage;
	}
	
	public boolean getBlockClientsWithNoOriginHeader() {
		return blockClientsWithNoOriginHeader;
	}
	
	public boolean test(String origin) {
		synchronized(regexBlacklist) {
			if(blockOfflineDownload && origin.equalsIgnoreCase("null")) {
				return true;
			}
			if(simpleWhitelistMode) {
				for(String st : simpleWhitelist) {
					if(origin.equalsIgnoreCase(st)) {
						return false;
					}
				}
			}
			if(localWhitelistMode || simpleWhitelistMode) {
				if(!blockOfflineDownload && origin.equalsIgnoreCase("null")) {
					return false;
				}
				for(Pattern m : regexLocalBlacklist) {
					if(m.matcher(origin).matches()) {
						return false;
					}
				}
				return true;
			}else {
				if(blockAllReplits) {
					for(Pattern m : regexBlacklistReplitInternal) {
						if(m.matcher(origin).matches()) {
							return true;
						}
					}
					for(Pattern m : regexBlacklistReplit) {
						if(m.matcher(origin).matches()) {
							return true;
						}
					}
				}
				for(Pattern m : regexBlacklist) {
					if(m.matcher(origin).matches()) {
						return true;
					}
				}
				for(Pattern m : regexLocalBlacklist) {
					if(m.matcher(origin).matches()) {
						return true;
					}
				}
				return false;
			}
		}
	}
	
	public void init(OriginBlacklistConfigAdapter cfg) {
		synchronized(regexBlacklist) {
			brokenURLs.clear();
			brokenRegex.clear();
			regexBlacklist.clear();
			regexLocalBlacklist.clear();
			regexBlacklistReplit.clear();
			simpleWhitelist.clear();
			localBlacklist = cfg.getLocalBlacklistFile();
			kickMessage = cfg.getKickMessage();
			blockClientsWithNoOriginHeader = cfg.getBlockClientsWithNoOriginHeader();
			subscriptionDownloadUserAgent = cfg.getSubscriptionDownloadUserAgent();
			blacklistSubscriptions = cfg.getBlacklistURLs();
			blockOfflineDownload = cfg.shouldBlacklistOfflineDownload();
			blockAllReplits = cfg.shouldBlacklistReplits();
			simpleWhitelistMode = cfg.isSimpleWhitelistEnabled();
			simpleWhitelist.addAll(cfg.getBlacklistSimpleWhitelist());
			lastLocalUpdate = 0l;
			lastUpdate = System.currentTimeMillis() - updateRate - 1000l;
			update();
		}
	}
	
	public void update() {
		long ct = System.currentTimeMillis();
		if((int)(ct - lastUpdate) > updateRate) {
			lastUpdate = ct;
			synchronized(regexBlacklist) {
				if(blacklistSubscriptions != null) {
					ArrayList<Pattern> newBlacklist = new ArrayList();
					ArrayList<Pattern> newReplitBlacklist = new ArrayList();
					HashSet<String> newBlacklistSet = new HashSet();
					newBlacklistSet.addAll(regexBlacklistReplitInternalStrings);
					for(String str : blacklistSubscriptions) {
						try {
							URL u;
							try {
								u = new URL(str);
							}catch(MalformedURLException e) {
								if(brokenURLs.add(str)) {
									logger.error("The blacklist subscription URL '" + str + "' is invalid");
								}
								continue;
							}
							URLConnection cc = u.openConnection();
							if(cc instanceof HttpURLConnection) {
								HttpURLConnection ccc = (HttpURLConnection)cc;
								ccc.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml");
								ccc.setRequestProperty("User-Agent", subscriptionDownloadUserAgent);
							}
							cc.connect();
							try(BufferedReader is = new BufferedReader(new InputStreamReader(cc.getInputStream()))) {
								String firstLine = is.readLine();
								if(firstLine == null) {
									is.close();
									throw new IOException("Could not read line");
								}
								firstLine = firstLine.trim();
								if(!firstLine.startsWith("#") || !firstLine.substring(1).trim().toLowerCase().startsWith("eaglercraft domain blacklist")) {
									throw new IOException("File does not contain a list of domains");
								}
								String ss;
								while((ss = is.readLine()) != null) {
									if((ss = ss.trim()).length() > 0) {
										if(ss.startsWith("#")) {
											ss = ss.substring(1).trim();
											if(ss.startsWith("replit-wildcard:")) {
												ss = ss.substring(16).trim();
												if(newBlacklistSet.add(ss)) {
													try {
														newReplitBlacklist.add(Pattern.compile(ss));
													}catch(PatternSyntaxException shit) {
														if(brokenRegex.add(ss)) {
															logger.error("the blacklist replit wildcard regex '" + ss + "' is invalid");
															continue;
														}
													}
													brokenRegex.remove(ss);
												}
											}
											continue;
										}
										if(newBlacklistSet.add(ss)) {
											try {
												newBlacklist.add(Pattern.compile(ss));
											}catch(PatternSyntaxException shit) {
												if(brokenRegex.add(ss)) {
													logger.error("the blacklist regex '" + ss + "' is invalid");
													continue;
												}
											}
											brokenRegex.remove(ss);
										}
									}
								}
							}
							brokenURLs.remove(str);
						}catch(Throwable t) {
							if(brokenURLs.add(str)) {
								logger.error("the blacklist subscription URL '" + str + "' is invalid");
							}
							t.printStackTrace();
						}
					}
					if(!newBlacklist.isEmpty()) {
						regexBlacklist.clear();
						regexBlacklist.addAll(newBlacklist);
					}
					if(!newReplitBlacklist.isEmpty()) {
						regexBlacklistReplit.clear();
						regexBlacklistReplit.addAll(newReplitBlacklist);
					}
				}else {
					brokenURLs.clear();
					brokenRegex.clear();
					regexBlacklist.clear();
					lastLocalUpdate = 0l;
				}
			}
		}
		if(localBlacklist.exists()) {
			long lastLocalEdit = localBlacklist.lastModified();
			if(lastLocalEdit != lastLocalUpdate) {
				lastLocalUpdate = lastLocalEdit;
				synchronized(regexBlacklist) {
					try(BufferedReader is = new BufferedReader(new FileReader(localBlacklist))) {
						regexLocalBlacklist.clear();
						localWhitelistMode = false;
						boolean foundWhitelistStatement = false;
						String ss;
						while((ss = is.readLine()) != null) {
							try {
								if((ss = ss.trim()).length() > 0) {
									if(!ss.startsWith("#")) {
										regexLocalBlacklist.add(Pattern.compile(ss));
									}else {
										String st = ss.substring(1).trim();
										if(st.startsWith("whitelistMode:")) {
											foundWhitelistStatement = true;
											String str = st.substring(14).trim().toLowerCase();
											localWhitelistMode = str.equals("true") || str.equals("on") || str.equals("1");
										}
									}
								}
							}catch(PatternSyntaxException shit) {
								logger.error("the local " + (localWhitelistMode ? "whitelist" : "blacklist") + " regex '" + ss + "' is invalid");
							}
						}
						is.close();
						if(!foundWhitelistStatement) {
							List<String> newLines = new ArrayList();
							newLines.add("#whitelistMode: false");
							newLines.add("");
							try(BufferedReader is2 = new BufferedReader(new FileReader(localBlacklist))) {
								while((ss = is2.readLine()) != null) {
									newLines.add(ss);
								}
							}
							try(PrintWriter os = new PrintWriter(new FileWriter(localBlacklist))) {
								for(String str : newLines) {
									os.println(str);
								}
							}
							lastLocalUpdate = localBlacklist.lastModified();
						}
						logger.info("Reloaded '" + localBlacklist.getName() + "'.");
					}catch(IOException ex) {
						regexLocalBlacklist.clear();
						logger.error("failed to read local " + (localWhitelistMode ? "whitelist" : "blacklist") + " file '" + localBlacklist.getName() + "'");
						ex.printStackTrace();
					}
				}
			}
		}else {
			synchronized(regexBlacklist) {
				if(!regexLocalBlacklist.isEmpty()) {
					logger.warn("the blacklist file '" + localBlacklist.getName() + "' has been deleted");
				}
				regexLocalBlacklist.clear();
			}
		}
	}

	public void addLocal(String o) {
		String p = "^" + Pattern.quote(o.trim()) + "$";
		ArrayList<String> lines = new ArrayList();
		if(localBlacklist.exists()) {
			try(BufferedReader is = new BufferedReader(new FileReader(localBlacklist))) {
				String ss;
				while((ss = is.readLine()) != null) {
					if((ss = ss.trim()).length() > 0) {
						lines.add(ss);
					}
				}
			}catch(IOException ex) {
				// ?
			}
		}
		if(lines.isEmpty()) {
			lines.add("#whitelist false");
			lines.add("");
		}
		if(!lines.contains(p)) {
			lines.add(p);
			try(PrintWriter os = new PrintWriter(new FileWriter(localBlacklist))) {
				for(String s : lines) {
					os.println(s);
				}
				lastLocalUpdate = 0l;
				update();
			}catch(IOException ex) {
				// ?
			}
		}
	}

	public boolean removeLocal(String o) {
		String p = "^" + Pattern.quote(o.trim()) + "$";
		ArrayList<String> lines = new ArrayList();
		if(localBlacklist.exists()) {
			try(BufferedReader is = new BufferedReader(new FileReader(localBlacklist))) {
				String ss;
				while((ss = is.readLine()) != null) {
					if((ss = ss.trim()).length() > 0) {
						lines.add(ss);
					}
				}
			}catch(IOException ex) {
				// ?
			}
		}
		if(lines.contains(p)) {
			lines.remove(p);
			try {
				try(PrintWriter os = new PrintWriter(new FileWriter(localBlacklist))) {
					for(String s : lines) {
						os.println(s);
					}
				}
				lastLocalUpdate = 0l;
				update();
				return true;
			}catch(IOException ex) {
				logger.error("Failed to save '" + localBlacklist.getName() + "'");
				ex.printStackTrace();
			}
		}
		return false;
	}

}
