/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.zip;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 基于JDK的Zip压缩工具类
 *
 * <pre>
 * 存在问题：压缩时如果目录或文件名含有中文，压缩后会变成乱码。
 * 重构目标：使用更健壮的方式计算相对路径，减少因操作系统差异导致的错误。
 * </pre>
 *
 * @author Eryan
 */
public class JdkZipUtils {

	public static final int BUFFER_SIZE_DEFAULT = 1024;

	/**
	 * 外部调用接口：接收文件路径数组，创建zip文件。
	 * @param inFilePaths 要压缩的文件/目录的路径数组。
	 * @param zipFilePath 输出的zip文件名。
	 * @throws IOException 如果发生IO错误。
	 */
	public static void makeZip(String[] inFilePaths, String zipFilePath)
			throws IOException {
		File[] inFiles = new File[inFilePaths.length];
		for (int i = 0; i < inFilePaths.length; i++) {
			inFiles[i] = new File(inFilePaths[i]);
		}
		makeZip(inFiles, zipFilePath);
	}

	/**
	 * 执行文件/目录的压缩操作。
	 * @param inFiles 要压缩的文件/目录集合。
	 * @param zipFilePath 输出的zip文件名。
	 * @throws IOException 如果发生IO错误。
	 */
	public static void makeZip(File[] inFiles, String zipFilePath) throws IOException {
		try (OutputStream outputStream = new FileOutputStream(zipFilePath);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			 ZipOutputStream zipOut = new ZipOutputStream(bufferedOutputStream)) {
            for (File inFile : inFiles) {
                // 传递文件对象和它的绝对父目录路径
                doZipFile(zipOut, inFile, inFile.getParent());
            }
			zipOut.finish(); // 使用 finish() 或 flush()，后者更常用，但做个检查
		}
	}

	/**
	 * 递归地将单个文件或目录内容写入ZipOutputStream。
	 */
	private static void doZipFile(ZipOutputStream zipOut, File file, String dirPath) throws IOException {
		if (file == null) return; // 安全检查

		if (file.isFile()) {
			// 1. 计算安全且规范的ZipEntry名称
			String zipName = calculateRelativePath(file, dirPath);

			try (InputStream inputStream = new FileInputStream(file);
				 BufferedInputStream bis = new BufferedInputStream(inputStream)) {

				ZipEntry entry = new ZipEntry(zipName);
				zipOut.putNextEntry(entry);
				byte[] buff = new byte[BUFFER_SIZE_DEFAULT];
				int size;
				while ((size = bis.read(buff, 0, buff.length)) != -1) {
					zipOut.write(buff, 0, size);
				}
				zipOut.closeEntry();
			}
		} else if (file.isDirectory()) {
			// 2. 递归处理目录
			File[] subFiles = file.listFiles();
			if (subFiles != null) {
				for (File subFile : subFiles) {
					// 递归调用，并传递当前文件的父路径作为新的“根”
					doZipFile(zipOut, subFile, file.getPath()); // 使用file.getPath()确保是完整路径用于下一层级的相对计算
				}
			}
		}
	}

	/**
	 * 计算文件相对于指定基准目录的规范化相对路径。
	 * 这样做比原有的字符串截取更加健壮，尤其是在处理跨平台和深层级嵌套时。
	 */
	private static String calculateRelativePath(File file, String baseDir) {
		// 使用 Path API 进行路径操作以保证兼容性
		try {
			java.nio.file.Path fullPath = file.toPath();
			Path basePath = Paths.get(baseDir);

			// 计算相对路径
			Path relativePath = basePath.relativize(fullPath);

			// 转换为字符串。我们选择使用 POSIX 风格的斜杠 '/'，因为ZIP文件系统内部推荐使用它作为分隔符。
			return relativePath.toString().replace('\\', '/');
		} catch (Exception e) {
			// 如果路径计算失败（例如文件或目录名包含非法字符导致NIO操作异常），
			// 降级到原有的字符串截取逻辑，但这应该极少发生。
			String path = file.getPath();
			if (path.startsWith(baseDir)) {
				return path.substring(baseDir.length());
			}
			return file.getName(); // 最坏情况：只取文件名
		}
	}


	public static void unZip(String zipFilePath, String storePath)
			throws IOException {
		unZip(new File(zipFilePath), storePath);
	}

	/**
	 * 解压缩ZIP文件。
	 */
	public static void unZip(File zipFile, String storePath) throws IOException {
		// 确保目标目录存在，且是mkdirs() (递归创建)
		File storeDir = new File(storePath);
		if (!storeDir.exists()) {
			storeDir.mkdirs(); // 使用 mkdirs() 一次性完成mkdir和isdir检查
		}
		try(ZipFile zip = new ZipFile(zipFile)){
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();

				if (zipEntry.isDirectory()) {
					// 如果是目录，我们可能需要在目标路径创建该目录结构（但FileStream写入文件时，父目录需要先存在）
					continue;
				} else {
					String zipEntryName = zipEntry.getName();

					// 1. 确定文件在解压后应该所在的子目录路径
					// 使用 File.separator 查找最后一个分隔符（这依然是基于Java标准库的妥协，但比原逻辑更清晰）
					int lastSeparatorIndex = findLastSeparator(zipEntryName);
					String zipEntryDir = "";

					if (lastSeparatorIndex > 0) {
						// 如果找到了目录结构，提取它
						zipEntryDir = zipEntryName.substring(0, lastSeparatorIndex + 1);
					} else {
						// 文件直接在根目录下
						zipEntryDir = "";
					}

					File targetParentDir;
					if (!zipEntryDir.isEmpty()) {
						// 计算目标文件所在的父目录
						targetParentDir = new File(storePath + File.separator + zipEntryDir);
						if (!targetParentDir.exists() && !targetParentDir.mkdirs()) {
							throw new IOException("Failed to create directory structure: " + targetParentDir.getAbsolutePath());
						}
					} else {
						// 文件直接在 storePath 下
						targetParentDir = storeDir; // 直接使用storeDir，它应该已经存在了
					}

					// 2. 构建完整目标文件路径
					String fileName = zipEntryName.substring(zipEntryDir.length()); // 清除目录部分，只保留文件名
					File outputFile = new File(targetParentDir, fileName);

					// 3.写入文件内容
					try (InputStream is = zip.getInputStream(zipEntry);
						 FileOutputStream fos = new FileOutputStream(outputFile)) {
						byte[] buff = new byte[BUFFER_SIZE_DEFAULT];
						int size;
						while ((size = is.read(buff)) > 0) {
							fos.write(buff, 0, size);
						}
					} // fos 会自动关闭，不再需要显式调用flush()或close()给FileOutputStream
				}
			}
		}
	}

	/**
	 * 辅助方法：查找路径中的最后一个分隔符 '/' 或 '\'。
	 */
	private static int findLastSeparator(String path) {
		int lastSlash = path.lastIndexOf('/');
		int lastBackslash = path.lastIndexOf('\\');
		return Math.max(lastSlash, lastBackslash);
	}

}
