package com.veracode.git.utils.types;

public class VeracodeGitSettings {

	public String veracode_api_id;

	public String veracode_secret_key;

	public VeracodeGitSettings() {

	}

	public VeracodeGitSettings(String veracode_api_id, String veracode_secret_key) {
		this.veracode_api_id = veracode_api_id;
		this.veracode_secret_key = veracode_secret_key;
	}

	public String getApiID() {
		return veracode_api_id;
	}

	public String getApiSecretKey() {
		return this.veracode_secret_key;
	}

}
