package com.jx.tw319qrc.data;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jx.tw319qrc.K;

public class TW319County extends TW319Location {

	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = 2072730470308801838L;

	public TW319County() {
		super();
	}

	private void validatePathCounties() {
		File pathCounties = new File(getPathCounties());
		if (!pathCounties.isDirectory()) {
			pathCounties.mkdirs();
		}
	}

	protected boolean isFileExist() {
		File theFile = new File(getFileName());
		return theFile.isFile();
	}

	protected String getFileName() {
		validatePathCounties();
		return getPathCounties() + getId() + FILE_EXTENSION;
	}

	private void saveVillages() {
		saveToFile(getFileName());
	}

	private void getVillagesFromFile() {
		String jsonString = loadFromFile(getFileName());
		fromJsonString(jsonString);
	}

	private boolean getVillageOnLine() {
		boolean ret = false;
		String countyUrl = getUrlPrefixOfCounty() + getId();
		try {
			String html = new TW319HttpTask().execute(countyUrl).get(
					K.timeoutHttpRequest, TimeUnit.MILLISECONDS);
			if (html.length() > 0) {
				Document doc = Jsoup.parse(html);
				Elements elems = doc
						.select("table[class=city-box bgLG] tr td a");
				if (elems.size() > 0) {
					clearLocationItems();
					for (Element e : elems) {
						String href = e.attr("href");
						String name = e.text();
						int idIndex = href.lastIndexOf("/");
						String id = href.substring(idIndex + 1);
						String url = TW319Location.getUrlBase() + href;
						addLocationItem(id, name, url);
					}
					saveVillages();
					ret = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public boolean isItemDataCached(String id) {
		String filename = getPathVillages() + id + FILE_EXTENSION;
		File file = new File(filename);
		boolean fileExists = file.isFile();
		return fileExists;
	}

	public void getVillages(String countyId) {
		setId(countyId);
		clearLocationItems();
		if (isFileExist()) {
			getVillagesFromFile();
		} else {
			getVillageOnLine();
		}
	}

	public void deleteFile() {
		File file = new File(getFileName());
		file.delete();
	}

	public void reload() {
		String countyId = getId();
		clearLocationItems();
		if (!getVillageOnLine()) {
			getVillages(countyId);
		}
	}
}
