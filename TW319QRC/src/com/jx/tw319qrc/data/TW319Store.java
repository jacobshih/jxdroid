package com.jx.tw319qrc.data;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.jx.tw319qrc.K;
import com.jx.tw319qrc.data.TW319StoreItem.LatLng;

import android.util.Log;

public class TW319Store extends TW319Location implements Serializable {

	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = -8661664291096050128L;

	private JSONObject dictStores = null;

	public TW319Store() {
		initialize();
	}

	public void initialize() {
		dictStores = new JSONObject();
		if (isFileExist()) {
			getStoresFromFile();
		}
		Log.i("jacob_shih", ""+dictStores.toString());
	}

	private void validatePathStores() {
		File pathStores = new File(getPathStores());
		if (!pathStores.isDirectory()) {
			pathStores.mkdirs();
		}
	}

	protected boolean isFileExist() {
		File theFile = new File(getFileName());
		return theFile.isFile();
	}

	protected String getFileName() {
		validatePathStores();
		return getPathStores() + FILE_STORES;
	}

	private void getStoresFromFile() {
		String jsonString = loadFromFile(getFileName());
		fromJsonString(jsonString);
	}

	private void getStoreOnLine(TW319StoreItem item) {
		String storeUrl = TW319Location.getUrlPrefixOfStoreDetail()+item.getId();
		try {
			String html = new TW319HttpTask().execute(storeUrl).get(
					K.timeoutHttpRequest, TimeUnit.MILLISECONDS);
			if (html.length() > 0) {
				Document doc = Jsoup.parse(html);
				/*
				 * elems contains image url of the static map of google
				 * map for the store.
				 * for example, below a image url for store:
				 *
				 *   http://maps.google.com/maps/api/staticmap?center=24.719441,120.915833&markers=24.719441,120.915833&zoom=13&size=250x163&sensor=false
				 *
				 * and what we interested in is the geographic coordinate
				 * of the store. the coordinate is one of the pairs in
				 * query string, in the case, the value of 'markers'.
				 */
				Elements elems = doc.select("img[class=cityMap]");
				if (elems.size() > 0) {
					String mapImgSrc = elems.attr("src");
					URL url = new URL(mapImgSrc);
					String query = url.getQuery();
					if ( -1 != query.indexOf("markers")) {
						String [] pairs = query.split("&");
						for (String pair : pairs) {
							String [] s = pair.split("=");
							if (s[0].equalsIgnoreCase("markers")) {
								String[] loc = s[1].split(",");
								double latitude = Double.parseDouble(loc[0]);
							    double longitude = Double.parseDouble(loc[1]);
							    item.setCoordinates(latitude, longitude);
							    dictStores.put(item.getId(), item.toJson());
							    saveAllStores();
							    Log.i("jacob_shih", "### "+dictStores.toString());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveAllStores() {
		saveToFile(getFileName());
	}

	public boolean isCoordinatesAvailable(TW319StoreItem item) {
		return dictStores.has(item.getId());
	}

	public LatLng getCoordinates(TW319StoreItem item) {
		Log.i("jacob_shih", ""+item.getId()+" "+item.getCategory()+" "+item.getTelephone()+" "+item.getIcon()+" "+item.getName()+" "+item.getAddress());
		if(!isCoordinatesAvailable(item)) {
			Log.i("jacob_shih", ">>> getStoreOnLine: "+item.getId());
			getStoreOnLine(item);
		} else {
			JSONObject joStore = dictStores.optJSONObject(item.getId());
			item.fromJson(joStore);
		}
		return item.getCoordinates();
	}

	public void fromJsonString(String jsonString) {
		try {
			dictStores = new JSONObject(jsonString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String toJsonString() {
		return dictStores.toString();
	}

}
