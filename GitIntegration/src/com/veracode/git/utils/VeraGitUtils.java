package com.veracode.git.utils;

import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;

public class GitCloneAndScan {
	/**
	 * Clones a repoistory from Github and submits it to Veracode for scanning.
	 * 
	 * @param url
	 *            the URL of the .git repository on Github
	 * @param oauth_token
	 *            Github OAuth key
	 * @param app_id
	 *            the id of the application on Veracode
	 * @param v_api_id
	 *            Veracode API key
	 * @param v_api_secret
	 *            Veracode API secret
	 * @return true if scan successfully submitted, false if not
	 */
	public static boolean cloneAndScan(String url, String oauth_token, String app_id, String v_api_id,
			String v_api_secret) {

		if (!clone(url, oauth_token, app_id)) {
			return false;
		}

		return scan(app_id, v_api_id, v_api_secret);
	}

	/**
	 * Clones a repoistory from Github and submits it to Veracode for scanning.
	 * 
	 * @param url
	 *            the URL of the .git repository on Github
	 * @param app_id
	 *            the id of the application on Veracode
	 * @param v_api_id
	 *            Veracode API key
	 * @param v_api_secret
	 *            Veracode API secret
	 * @return true if scan successfully submitted, false if not
	 */
	public static boolean cloneAndScan(String url, String app_id, String v_api_id, String v_api_secret) {
		if (!clone(url, null, app_id)) {
			return false;
		}

		return scan(app_id, v_api_id, v_api_secret);
	}

	/**
	 * Clones a repository from Github to the local machine
	 * 
	 * @param url
	 *            the URL of the .git repository on Github
	 * @param ouath_token
	 *            Github OAuth key
	 * @param app_id
	 *            the id of the application on Veracode
	 * @return true if the clone was successfully, false if not
	 */
	private static boolean clone(String url, String ouath_token, String app_id) {
		CloneCommand c_command = Git.cloneRepository();
		c_command.setURI(url);
		if (ouath_token != null) {
			c_command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(ouath_token, ""));
		}
		c_command.setDirectory(VeraGitUtils.getCodePath(app_id));

		try {
			c_command.call();
		} catch (GitAPIException e) {
			System.out.println("Git clone failed");
			VeraGitUtils.deleteDirectory(VeraGitUtils.getAppPath(app_id));
			return false;
		}

		return true;
	}

	/**
	 * Submits a local Git repository to Veracode for scanning
	 * 
	 * @param app_id
	 *            the id of the application on Veracode
	 * @param v_api_id
	 *            Veracode API key
	 * @param v_api_secret
	 *            Veracode API secret
	 * @return true if the scan was successfully initiated, false if not
	 */
	private static boolean scan(String app_id, String v_api_id, String v_api_secret) {
		VeraGitUtils.compress(VeraGitUtils.getCodePath(app_id).getAbsolutePath());
		UploadAPIWrapper upload_wrap = new UploadAPIWrapper();
		upload_wrap.setUpApiCredentials(v_api_id, v_api_secret);
		try {
			upload_wrap.uploadFile(app_id, VeraGitUtils.getZipPath(app_id).getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Upload to Veracode failed");
			VeraGitUtils.deleteDirectory(VeraGitUtils.getAppPath(app_id));
			return false;
		}

		try {
			upload_wrap.beginPreScan(app_id, null, "true");
		} catch (IOException e) {
			System.out.println("Scan failed to initiate");
			VeraGitUtils.deleteDirectory(VeraGitUtils.getAppPath(app_id));
			return false;
		}
		VeraGitUtils.deleteDirectory(VeraGitUtils.getAppPath(app_id));
		System.out.println("Scan successfully initiated");
		return true;

	}

}
