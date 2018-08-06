package com.veracode.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.veracode.git.utils.GitCloneAndScan;
import com.veracode.git.utils.VeraGitUtils;
import com.veracode.git.utils.types.VeracodeGitAppSettings;
import com.veracode.git.utils.types.VeracodeGitSettings;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class VeracodeGit {

	private static VeracodeGitSettings manageSettings(String[] args) {
		File vera_git_folder = VeraGitUtils.getSettingsPath();

		VeracodeGitSettings settings = null;

		if (!vera_git_folder.exists()) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("No configuration file exists. Please follow the steps to create one");
			System.out.println("Enter Veracode API id:");
			String id = scanner.nextLine();
			System.out.println("Enter Veracode API secret key:");
			String secret = scanner.nextLine();
			scanner.close();

			settings = new VeracodeGitSettings(id, secret);
			try {
				YamlWriter writer = new YamlWriter(new FileWriter(VeraGitUtils.getSettingsPath()));
				writer.write(settings);
				writer.close();
			} catch (IOException e) {
				System.out.println("Failed to save Veracode profile");
				System.exit(1);
			}
		} else {

			YamlReader reader = null;
			try {
				reader = new YamlReader(new FileReader(VeraGitUtils.getSettingsPath()));

			} catch (FileNotFoundException e1) {
				System.out.println("Veracode settings file doesn't exist");
				System.exit(1);
			}
			try {
				settings = reader.read(VeracodeGitSettings.class);
				reader.close();
			} catch (YamlException e) {
				System.out.println("Failed to load Veracode settings file due to a formatting issue");
				System.exit(1);
			} catch (IOException e) {
				System.out.println("Failed to load Veracode settings file");
				System.exit(1);
			}

		}
		return settings;
	}

	public static boolean executeScanSubparser(Namespace ns, VeracodeGitSettings settings,
			VeracodeGitAppSettings app_settings) {
		if (ns.getString("app") != null) {

			YamlReader reader = null;
			String app_name = VeraGitUtils.formatArgsString(ns.getString("app"));

			try {
				reader = new YamlReader(new FileReader(VeraGitUtils.getAppSettingsPath(app_name)));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				app_settings = reader.read(VeracodeGitAppSettings.class);
			} catch (YamlException e) {
				e.printStackTrace();
			}
			boolean no_key = app_settings.isPublic();
			if (no_key)
				GitCloneAndScan.cloneAndScan(app_settings.getUrl(), app_settings.getAppId(), settings.getApiID(),
						settings.getApiSecretKey());
			else
				GitCloneAndScan.cloneAndScan(app_settings.getUrl(), app_settings.getKey(), app_settings.getAppId(),
						settings.getApiID(), settings.getApiSecretKey());

		} else if (ns.getString("url") != null) {
			String key = VeraGitUtils.formatArgsString(ns.getString("key"));
			boolean no_key = ns.getBoolean("public");
			String app_id = VeraGitUtils.formatArgsString(ns.getString("id"));
			String url = VeraGitUtils.formatArgsString(ns.getString("url"));

			if (key == null && !no_key) {
				System.out.println("A key is required when scanning using .git url or use option --no-key");
				System.exit(1);
			}
			if (app_id == null) {
				System.out.println("An id for the application on Veracode is required when scanning using URL");
				System.exit(1);
			}
			if (no_key)
				return GitCloneAndScan.cloneAndScan(url, app_id, settings.getApiID(), settings.getApiSecretKey());
			else
				return GitCloneAndScan.cloneAndScan(url, key, app_id, settings.getApiID(), settings.getApiSecretKey());
		} else
			return false;
		return true;
	}

	public static boolean executeCreateSubparser(Namespace ns, VeracodeGitSettings settings,
			VeracodeGitAppSettings app_settings) {
		if (ns.getString("name") != null) {
			String app_name = VeraGitUtils.formatArgsString(ns.getString("name"));
			String key = VeraGitUtils.formatArgsString(ns.getString("key"));
			String id = VeraGitUtils.formatArgsString(ns.getString("id"));
			String url = VeraGitUtils.formatArgsString(ns.getString("url"));
			boolean no_key = ns.getBoolean("public");
			File file = VeraGitUtils.getAppSettingsPath(app_name);
			if (file.exists()) {
				System.out.println("Profile already created for this app");
				System.exit(1);
			}

			app_settings = new VeracodeGitAppSettings(app_name, id, url, key, no_key);
			YamlWriter writer = null;
			try {
				writer = new YamlWriter(new FileWriter(VeraGitUtils.getAppSettingsPath(app_name)));
			} catch (IOException e1) {
				System.out.println("Failed to save application profile.");
				System.exit(1);
			}
			try {
				writer.write(app_settings);
				writer.close();
			} catch (YamlException e) {
				System.out.println("Failed to save application profile.");
				System.exit(1);
			}
			System.out.println("Profile successfully created");
		} else
			return false;
		return true;
	}

	public static void main(String[] args) {

		VeracodeGitSettings settings = manageSettings(args);

		if (settings == null) {
			System.out.println("Scan initilization failed");
			System.exit(1);
		}

		VeracodeGitAppSettings app_settings = null;

		ArgumentParser parser = ArgumentParsers.newArgumentParser("VeracodeGit").defaultHelp(true)
				.description("Upload and Scan from Git repository");

		Subparsers subparser = parser.addSubparsers().help("sub-command help");

		Subparser sub_scan = subparser.addParser("scan");

		sub_scan.addArgument("--app").nargs(1);

		sub_scan.addArgument("--url").nargs(1);
		sub_scan.addArgument("--id").nargs(1);
		sub_scan.addArgument("--key").nargs(1);
		sub_scan.addArgument("--public").action(Arguments.storeTrue());

		Subparser sub_create = subparser.addParser("create");

		sub_create.addArgument("--url").required(true);
		sub_create.addArgument("--key");
		sub_create.addArgument("--name").required(true);
		sub_create.addArgument("--id").required(true);
		sub_create.addArgument("--public").action(Arguments.storeTrue());

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
			if (ns != null) {

				executeCreateSubparser(ns, settings, app_settings);
				if (ns.get("app") == null)
					executeScanSubparser(ns, settings, app_settings);
			}
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

	}

}
