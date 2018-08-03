package com.veracode.git.utils;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VeraGitUtils {

	public static File getPath() {
		return new File(System.getProperty("user.home") + "/VeraGit");
	}

	public static File getSettingsPath() {
		return new File(getPath() + "/veragit.yaml");
	}

	public static File getAppSettingsPath(String app_name) {
		return new File(getPath() + "/" + app_name + ".yaml");

	}

	public static File getAppPath(String app_name) {
		return new File(getPath().getAbsolutePath() + "/repos/" + app_name);
	}

	public static File getCodePath(String app_name) {
		return new File(getAppPath(app_name).getPath() + "/code/");
	}

	public static File getZipPath(String app_name) {
		return new File(getAppPath(app_name).getAbsolutePath() + "/code.zip");
	}

	public static void deleteDirectory(File file) {
		Path dir = Paths.get(file.getAbsolutePath());
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc == null) {
						Files.delete(dir);
						return CONTINUE;
					} else {
						throw exc;
					}
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String formatArgsString(String s) {
		if (s == null)
			return null;
		return s.replace("[", "").replace("]", "");
	}

	public static void compress(String dirPath) {
		Path sourceDir = Paths.get(dirPath);
		String zipFileName = dirPath.concat(".zip");
		try {
			ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
			Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					try {
						Path targetFile = sourceDir.relativize(file);
						outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
						byte[] bytes = Files.readAllBytes(file);
						outputStream.write(bytes, 0, bytes.length);
						outputStream.closeEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
