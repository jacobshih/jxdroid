package com.jx.tw319qrc.tools;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	/**
	 * compress a directory recursively to a zip file.
	 * 
	 * @param directory
	 *            a directory to be compressed.
	 * @param zipFile
	 *            a zip file of the compressed directory.
	 * @throws IOException
	 */
	static public void zip(String directory, String zipFile) throws IOException {
		zip(new File(directory), new File(zipFile));
	}

	/**
	 * compress a directory recursively to a zip file.
	 * 
	 * @param directory
	 *            a directory to be compressed.
	 * @param zipFile
	 *            a zip file of the compressed directory.
	 * @throws IOException
	 */
	static public void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = null;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
			zout.close();
		} finally {
			if (res != null)
				res.close();
		}
		out.close();
	}

	/**
	 * uncompress the zip file to the specified directory.
	 * 
	 * @param zipFile
	 *            a zip file to be uncompressed.
	 * @param directory
	 *            the directory to store the uncompressed files and sub
	 *            directory.
	 * @throws IOException
	 */
	static public void unzip(String zipFile, String directory)
			throws IOException {
		unzip(new File(zipFile), new File(directory));
	}

	/**
	 * uncompress the zip file to the specified directory.
	 * 
	 * @param zipFile
	 *            a zip file to be uncompressed.
	 * @param directory
	 *            the directory to store the uncompressed files and sub
	 *            directory.
	 * @throws IOException
	 */
	static public void unzip(File zipfile, File directory) throws IOException {
		ZipFile zfile = new ZipFile(zipfile);
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				InputStream in = zfile.getInputStream(entry);
				try {
					copy(in, file);
				} finally {
					in.close();
				}
			}
		}
		zfile.close();
	}

	/**
	 * copy file.
	 * 
	 * @param in
	 *            source file.
	 * @param out
	 *            destination file.
	 * @throws IOException
	 */
	static private void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	/**
	 * copy file.
	 * 
	 * @param in
	 *            source file.
	 * @param out
	 *            destination file.
	 * @throws IOException
	 */
	static private void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	/**
	 * copy file.
	 * 
	 * @param in
	 *            source file.
	 * @param out
	 *            destination file.
	 * @throws IOException
	 */
	static private void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}
}
