package com.jx.tw319qrc.data;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jx.tw319qrc.K;

public class TW319Village extends TW319Location implements Serializable {

	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = 7896032400118583456L;

	public TW319Village() {
		super();
	}

	private void validatePathVillages() {
		File pathVillages = new File(getPathVillages());
		if (!pathVillages.isDirectory()) {
			pathVillages.mkdirs();
		}
	}

	protected String getFileName() {
		validatePathVillages();
		return getPathVillages() + getId() + FILE_EXTENSION;
	}

	private void saveStores() {
		saveToFile(getFileName());
	}

	public void fromJsonString(String jsonString) {
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.optJSONArray(K.jsonItems);
			for (int i = 0; i < jsonArray.length(); i++) {
				TW319StoreItem item = new TW319StoreItem(this);
				item.fromJson(jsonArray.getJSONObject(i));
				addLocationItem(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getStoresFromFile() {
		String jsonString = loadFromFile(getFileName());
		fromJsonString(jsonString);
	}

	private void getStoresOnLine() {
		String villageUrl = getUrlPrefixOfVillage() + getId();
		String html;
		try {
			html = new TW319HttpTask().execute(villageUrl).get(
					K.timeoutHttpRequest, TimeUnit.MILLISECONDS);
			if (html.length() > 0) {
				Document doc = Jsoup.parse(html);
				/*
				 * elems contains information of the stores. traverse elems to
				 * retrieve the information of each store.
				 */
				Elements elems = doc.select("div[id=tab2] table tr");
				if (elems.size() > 0) {
					for (Element e : elems) {
						/*
						 * tds contains information of the store, as described
						 * below, 0, the category icon (<img src="..."
						 * title="...">). 1, name and link of the store (<a
						 * href="...">). 2, telephone of the store. 3, address
						 * of the store.
						 */
						Elements tds = e.select("td");
						Element elemCategory = tds.get(0).child(0);
						Element elemStore = tds.get(1).child(0);
						Element elemTelephone = tds.get(2);
						Element elemAddress = tds.get(3);
						String href = elemStore.attr("href");
						int idIndex = href.lastIndexOf("/");
						String storeId = href.substring(idIndex + 1);
						String storeUrl = getUrlPrefixOfStoreCode() + storeId;
						String storeCategory = elemCategory.attr("title");
						String storeIcon = elemCategory.attr("src");
						String storeName = elemStore.text();
						String storeTelephone = elemTelephone.text();
						String storeAddress = elemAddress.text();
						TW319StoreItem item = new TW319StoreItem(this, storeId,
								storeName, storeUrl, storeCategory, storeIcon,
								storeTelephone, storeAddress);
						addLocationItem(item);
					}
					saveStores();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getStores(String villageId) {
		setId(villageId);
		File fileVillage = new File(getFileName());
		boolean fileVillageExists = fileVillage.isFile();
		clearLocationItems();
		if (fileVillageExists) {
			getStoresFromFile();
		} else {
			getStoresOnLine();
		}
	}

	public void deleteFile() {
		File file = new File(getFileName());
		file.delete();
	}

	public void reload() {
		deleteFile();
		getStores(getId());
	}
}
