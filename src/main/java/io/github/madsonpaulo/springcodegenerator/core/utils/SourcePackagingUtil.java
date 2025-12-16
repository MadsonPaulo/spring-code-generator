package io.github.madsonpaulo.springcodegenerator.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

import io.github.madsonpaulo.springcodegenerator.core.dto.GeneratedJavaSourceDto;

public final class SourcePackagingUtil {

	private SourcePackagingUtil() {
		// utility class
	}

	/**
	 * Generates the final binary payload for the generated Java sources.
	 * <ul>
	 * <li>If only one source is provided, returns the .java content directly.</li>
	 * <li>If multiple sources are provided, returns a ZIP archive.</li>
	 * </ul>
	 */
	public static ByteArrayResource generatePayload(List<GeneratedJavaSourceDto> generatedSources) throws IOException {
		if (generatedSources == null || generatedSources.isEmpty()) {
			throw new IllegalArgumentException("No generated Java sources were provided.");
		}

		if (generatedSources.size() == 1) {
			return new ByteArrayResource(generatedSources.get(0).getJavaSourceCode().getBytes(StandardCharsets.UTF_8));
		}

		return generateZipArchive(generatedSources);
	}

	/**
	 * Determines the output file name based on the generated sources. Returns
	 * either a .java or .zip file name.
	 */
	public static String resolveOutputFileName(List<GeneratedJavaSourceDto> generatedSources, String defaultName) {

		if (generatedSources == null || generatedSources.isEmpty()) {
			throw new IllegalArgumentException("No generated Java sources were provided.");
		}

		return generatedSources.size() == 1 ? "%s.java".formatted(generatedSources.get(0).getJavaName())
				: "%s.zip".formatted(defaultName);
	}

	/**
	 * Generates a ZIP archive containing all generated Java source files.
	 */
	private static ByteArrayResource generateZipArchive(List<GeneratedJavaSourceDto> generatedSources)
			throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
			for (GeneratedJavaSourceDto source : generatedSources) {
				String filePath = resolveJavaFilePath(source);
				addFileToZip(zipOutputStream, filePath, source.getJavaSourceCode());
			}
		}

		return new ByteArrayResource(outputStream.toByteArray());
	}

	/**
	 * Resolves the relative file path of a generated Java source. Example:
	 * com/example/Foo.java Foo.java
	 */
	private static String resolveJavaFilePath(GeneratedJavaSourceDto source) {
		if (StringUtils.hasText(source.getPackageName())) {
			return "%s/%s.java".formatted(source.getPackageName(), source.getJavaName());
		}

		return "%s.java".formatted(source.getJavaName());
	}

	/**
	 * Adds a single file entry to the ZIP output stream.
	 */
	private static void addFileToZip(ZipOutputStream zipOutputStream, String filePath, String content)
			throws IOException {
		ZipEntry zipEntry = new ZipEntry(filePath);
		zipOutputStream.putNextEntry(zipEntry);

		zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
		zipOutputStream.closeEntry();
	}

}
