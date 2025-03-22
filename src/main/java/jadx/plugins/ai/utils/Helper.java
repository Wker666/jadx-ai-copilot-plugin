package jadx.plugins.ai.utils;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Helper {

	public static String extractClassName(String input) {
		if (input == null || input.isEmpty()) {
			throw new IllegalArgumentException("Input cannot be null or empty");
		}
		String[] parts = input.split("\\.");
		if (parts.length < 3) {
			throw new IllegalArgumentException("Input should have at least three parts separated by dots");
		}
		String className = parts[parts.length - 2];
		String methodName = parts[parts.length - 1];
		return className + "." + methodName;
	}

	public static  int calculateNodeWidth(String label) {
		Font font = new Font("Dialog", Font.PLAIN, 12);
		FontMetrics metrics = new JLabel().getFontMetrics(font);
		int padding = 20;
		return metrics.stringWidth(label) + padding;
	}


	public static String removeLeadingWhitespace(String text) {
		String[] lines = text.split("\n");
		int minLeadingSpaces = Arrays.stream(lines)
				.filter(line -> !line.trim().isEmpty())
				.mapToInt(line -> line.indexOf(line.trim()))
				.min()
				.orElse(0);
		StringBuilder result = new StringBuilder();
		Arrays.stream(lines).forEach(line -> {
			if (!line.trim().isEmpty()) {
				result.append(line.substring(minLeadingSpaces)).append("\n");
			} else {
				result.append("\n");
			}
		});
		return result.toString();
	}


	public static void writeResourceToTargetFile(String resourceFilePath, String targetFilePath) throws IOException {
		InputStream resourceStream = Helper.class.getClassLoader().getResourceAsStream(resourceFilePath);
		if (resourceStream == null) {
			throw new FileNotFoundException("not found sources: " + resourceFilePath);
		}
		Path targetPath = Paths.get(targetFilePath);
		Path targetDirPath = targetPath.getParent();
		if (targetDirPath != null && !Files.exists(targetDirPath)) {
			Files.createDirectories(targetDirPath);
		}
		try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = resourceStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} finally {
			resourceStream.close();
		}
	}
}
