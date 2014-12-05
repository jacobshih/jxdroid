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
		File pathCounties = new File(getPathTW319QRC() + PATH_COUNTIES);
		if (!pathCounties.isDirectory()) {
			pathCounties.mkdirs();
		}
	}

	protected String getFileName() {
		validatePathCounties();
		return getPathTW319QRC() + PATH_COUNTIES + getId() + FILE_EXTENSION;
	}

	private void saveVillages() {
		saveToFile(getFileName());
	}

	private void getVillagesFromFile() {
		String jsonString = loadFromFile(getFileName());
		fromJsonString(jsonString);
	}

	private void getVillageOnLine() {
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
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isItemDataCached(String id) {
		String filename = getPathTW319QRC() + PATH_VILLAGES + id
				+ FILE_EXTENSION;
		File file = new File(filename);
		boolean fileExists = file.isFile();
		return fileExists;
	}

	public void getVillages(String countyId) {
		setId(countyId);
		File fileCounty = new File(getFileName());
		boolean fileCountyExists = fileCounty.isFile();
		clearLocationItems();
		if (fileCountyExists) {
			getVillagesFromFile();
		} else {
			getVillageOnLine();
		}
	}
}
