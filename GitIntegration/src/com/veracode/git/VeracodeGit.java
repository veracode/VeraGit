package com.veracode.git;

import java.io.File;
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

	/**
	 * 
	 * @param args
	 *            args from the command line
	 * @return a VeracodeGitSettings filled with information from args
	 */
	private static VeracodeGitSettings manageSettings(String[] args) {
		File vera_git_folder = VeraGitUtils.getSettingsPath();

		VeracodeGitSettings settings = null;
		String id = null;
		String secret = null;
		if (!vera_git_folder.exists()) {
			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("No configuration file exists. Please follow the steps to create one.");
				System.out.println("Enter Veracode API id:");
				id = scanner.nextLine().trim();
				System.out.println("Enter Veracode API secret key:");
				secret = scanner.nextLine().trim();
			}

			settings = new VeracodeGitSettings(id, secret);

			try (FileWriter f_writer = new FileWriter(VeraGitUtils.getSettingsPath())) {
				YamlWriter writer = new YamlWriter(new FileWriter(VeraGitUtils.getSettingsPath()));
				writer.write(settings);
				writer.close();
			} catch (IOException e) {
				System.out.println("Failed to save Veracode profile");
				System.exit(1);
			}

		} else {

			try (FileReader f_reader = new FileReader(VeraGitUtils.getSettingsPath())) {
				YamlReader reader = new YamlReader(f_reader);
				try {
					settings = reader.read(VeracodeGitSettings.class);
					reader.close();
				} catch (YamlException e) {
					System.out.println("Failed to load Veracode settings file due to a formatting issue");
					System.exit(1);
				} catch (IOException e) {
					System.out.println("Failed to load Veracode settings file");
					System.exit(1);
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("Veracode settings file doesn't exist");
				System.exit(1);
			}
		}
		return settings;
	}

	/**
	 * Executes a scan
	 * 
	 * @param ns
	 *            a Namespace from Yamlbeans
	 * @param settings
	 *            VeracodeGitSettings
	 * @param app_settings
	 *            VeracodeGitAppSettings
	 * @return true if the scan was successfully executed, false otherwise
	 */
	public static boolean executeScanSubparser(Namespace ns, VeracodeGitSettings settings,
			VeracodeGitAppSettings app_settings) {
		if (ns.getString("app") != null) {
			executeAppScan(ns, settings, app_settings);
		} else if (ns.getString("url") != null) {
			return executeUrlScan(ns, settings);
		} else
			return false;
		return true;
	}

	/**
	 * Executes a scan based on an application profile
	 * 
	 * @param ns
	 *            a Namespace from Yamlbeans
	 * @param settings
	 *            VeracodeGitSettings
	 * @param app_settings
	 *            VeracodeGitAppSettings
	 * @return true if the scan was successfully executed, false otherwise
	 */
	private static boolean executeAppScan(Namespace ns, VeracodeGitSettings settings,
			VeracodeGitAppSettings app_settings) {
		String app_name = VeraGitUtils.formatArgsString(ns.getString("app"));

		try (FileReader f_reader = new FileReader(VeraGitUtils.getAppSettingsPath(app_name))) {
			YamlReader reader = new YamlReader(f_reader);
			app_settings = reader.read(VeracodeGitAppSettings.class);
		} catch (IOException e) {
			System.out.println("Failed to load app profile");
			System.exit(1);
		}
		boolean no_key = app_settings.isPublic();
		if (no_key) {
			return GitCloneAndScan.cloneAndScan(app_settings.getUrl(), app_settings.getAppId(), settings.getApiID(),
					settings.getApiSecretKey());
		} else {
			return GitCloneAndScan.cloneAndScan(app_settings.getUrl(), app_settings.getKey(), app_settings.getAppId(),
					settings.getApiID(), settings.getApiSecretKey());
		}
	}

	/**
	 * Executes a scan based on a URL
	 * 
	 * @param ns
	 *            a Namespace from Yamlbeans
	 * @param settings
	 *            VeracodeGitSettings
	 * @return true if the scan was successfully executed, false otherwise
	 */
	private static boolean executeUrlScan(Namespace ns, VeracodeGitSettings settings) {
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
		if (no_key) {
			return GitCloneAndScan.cloneAndScan(url, app_id, settings.getApiID(), settings.getApiSecretKey());
		} else {
			return GitCloneAndScan.cloneAndScan(url, key, app_id, settings.getApiID(), settings.getApiSecretKey());
		}
	}

	/**
	 * Creates an application profile
	 * 
	 * @param ns
	 *            a Namespace from YamlBeans
	 * @param settings
	 *            VeracodeGitSettings
	 * @param app_settings
	 *            VeracodeGitAppSettings
	 */
	public static void executeCreateSubparser(Namespace ns, VeracodeGitSettings settings,
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
			try (FileWriter f_writer = new FileWriter(VeraGitUtils.getAppSettingsPath(app_name))) {
				YamlWriter writer = new YamlWriter(new FileWriter(VeraGitUtils.getAppSettingsPath(app_name)));
				writer.write(app_settings);
				writer.close();
			} catch (IOException e) {
				System.out.println("Failed to save application profile.");
				System.exit(1);

			}
			System.out.println("Profile successfully created");
		}
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
				if (ns.get("app") == null) {
					executeScanSubparser(ns, settings, app_settings);
				}
			} else {
				System.out.println("Failed to parse arguments");
			}
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

	}

}
