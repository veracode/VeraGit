package com.veracode.git.utils;

import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.veracode.apiwrapper.wrappers.UploadAPIWrapper;

public class GitCloneAndScan {

	public static boolean cloneAndScan(String url, String key, String app_id, String v_api_id, String v_api_secret) {

		if (!clone(url, key, app_id))
			return false;

		return scan(app_id, v_api_id, v_api_secret);

	}

	public static boolean cloneAndScan(String url, String app_id, String v_api_id, String v_api_secret) {
		if (!clone(url, null, app_id))
			return false;
		return scan(app_id, v_api_id, v_api_secret);

	}

	private static boolean clone(String url, String oath_token, String app_id) {
		CloneCommand c_command = Git.cloneRepository();
		c_command.setURI(url);
		if (oath_token != null)
			c_command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(oath_token, ""));
		c_command.setDirectory(VeraGitUtils.getCodePath(app_id));

		try {
			c_command.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			System.out.println("Git clone failed");
			VeraGitUtils.deleteDirectory(VeraGitUtils.getAppPath(app_id));
			return false;
		}

		return true;
	}

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
			upload_wrap.beginPreScan(app_id);
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
