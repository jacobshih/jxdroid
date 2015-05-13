package com.jx.tw319qrc.tools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class FileUtils {

	/**
	 * Resolve the file path from a uri.
	 * 
	 * @param context
	 *            the context of the application.
	 * @param uri
	 *            the uri of the file or content.
	 * @return the storage path of the file.
	 * @throws URISyntaxException
	 */
	public static String getPath(Context context, Uri uri)
			throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			final String columnName = "_data";
			String[] projection = { columnName };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(columnName);
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {

			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

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
