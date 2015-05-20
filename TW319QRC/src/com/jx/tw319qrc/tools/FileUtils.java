package com.jx.tw319qrc.tools;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an
	 * actual file.
	 * 
	 * @param file
	 *            the file object to be checked if is a symbolic link.
	 * @return true if the file is a Symbolic Link
	 * @throws IOException
	 */
	public static boolean isSymlink(File file) throws IOException {
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}
}
