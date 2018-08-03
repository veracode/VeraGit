package com.veracode.git.utils.types;

public class VeracodeGitAppSettings {

	public String name;
	public String key;
	public String url;
	public String app_id;
	public boolean no_key;

	public VeracodeGitAppSettings() {

	}

	public VeracodeGitAppSettings(String name, String app_id, String url, String key, boolean no_key) {
		this.name = name;
		this.key = key;
		this.url = url;
		this.app_id = app_id;
		this.no_key = no_key;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getKey() {
		return key;
	}

	public String getAppId() {
		return app_id;
	}

	public boolean isPublic() {
		return no_key;
	}

}
