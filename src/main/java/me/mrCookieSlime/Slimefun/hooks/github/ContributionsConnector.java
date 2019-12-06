package me.mrCookieSlime.Slimefun.hooks.github;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;

public class ContributionsConnector extends GitHubConnector {

	private static final Pattern nameFormat = Pattern.compile("[\\w_]+");

	// GitHub Bots that do not count as Contributors
	// (includes "invalid-email-address" because it is an invalid contributor)
	private static final List<String> blacklist = Arrays.asList(
		"invalid-email-address",
		"renovate-bot",
		"ImgBotApp",
		"TheBusyBot",
		"imgbot"
	);

	// Matches a GitHub name with a Minecraft name.
	private static final Map<String, String> aliases = new HashMap<>();

	static {
		aliases.put("WalshyDev", "HumanRightsAct");
		aliases.put("J3fftw1", "_lagpc_");
		aliases.put("ajan-12", "ajan_12");
		aliases.put("LinoxGH", "ajan_12");
	}
	
	private final String prefix;
	private final String repository;
	private final String role;
	
	public ContributionsConnector(String prefix, String repository, String role) {
		this.prefix = prefix;
		this.repository = repository;
		this.role = role;
	}
	
	@Override
	public void onSuccess(JsonElement element) {
		computeContributors(element.getAsJsonArray());
	}
	
	@Override
	public String getRepository() {
		return repository;
	}
	
	@Override
	public String getFileName() {
		return prefix + "_contributors";
	}

	@Override
	public String getURLSuffix() {
		return "/contributors?per_page=100";
	}

	private void computeContributors(JsonArray array) {
	    for (int i = 0; i < array.size(); i++) {
	    	JsonObject object = array.get(i).getAsJsonObject();

	    	String name = object.get("login").getAsString();
	    	int commits = object.get("contributions").getAsInt();
	    	String profile = object.get("html_url").getAsString();

	    	final String alias = aliases.getOrDefault(name, name);

	    	if (nameFormat.matcher(name).matches() && !blacklist.contains(name)) {
	    		Contributor contributor = SlimefunPlugin.getUtilities().contributors.computeIfAbsent(
	    				alias,
						key -> new Contributor(alias, profile)
				);
	    		contributor.setContribution(role, commits);
	    	}
	    }
	}
}