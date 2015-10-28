package com.jx.tw319qrc.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jx.tw319qrc.K;

public class TW319Location implements Serializable {
	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = -459074273176372671L;
	private static String urlBase = null;
	private static String urlPrefixOfCounty = null;
	private static String urlPrefixOfVillage = null;
	private static String urlPrefixOfStoreCode = null;
	private static String urlPrefixOfStoreDetail = null;
	private static String urlCheckinByCounty = null;
	private static String pathTW319QRC = null;
	protected final static String PATH_DATA = "data/";
	protected final static String PATH_USER = "user/";
	protected final static String PATH_COUNTIES = "counties/";
	protected final static String PATH_VILLAGES = "villages/";
	protected final static String PATH_STORES = "stores/";
	protected final static String FILE_STORES = "stores.json";
	protected final static String FILE_TOKEN = "token.json";
	protected final static String FILE_CHECKIN = "checkin.json";
	protected final static String FILE_EXTENSION = ".json";

	private String id = null;
	private ArrayList<TW319LocationItem> items = null;
	private static HashSet<String> visitedStores = null;

	public TW319Location() {
		super();
		items = new ArrayList<TW319LocationItem>();
	}

	public static String getUrlBase() {
		return urlBase;
	}

	public static void setUrlBase(String url) {
		urlBase = url;
	}

	public static String getUrlPrefixOfCounty() {
		return urlPrefixOfCounty;
	}

	public static void setUrlPrefixOfCounty(String urlPath) {
		urlPrefixOfCounty = urlBase + urlPath;
	}

	public static String getUrlPrefixOfVillage() {
		return urlPrefixOfVillage;
	}

	public static void setUrlPrefixOfVillage(String urlPath) {
		urlPrefixOfVillage = urlBase + urlPath;
	}

	public static String getUrlPrefixOfStoreCode() {
		return urlPrefixOfStoreCode;
	}

	public static void setUrlPrefixOfStoreDetail(String urlPath) {
		urlPrefixOfStoreDetail = urlBase + urlPath;
	}

	public static String getUrlPrefixOfStoreDetail() {
		return urlPrefixOfStoreDetail;
	}

	public static void setUrlPrefixOfStoreCode(String urlPath) {
		urlPrefixOfStoreCode = urlBase + urlPath;
	}

	public static String getUrlCheckinByCounty() {
		return urlCheckinByCounty;
	}

	public static void setUrlCheckinByCounty(String urlPath) {
		urlCheckinByCounty = urlBase + urlPath;
	}

	public static String getPathTW319QRC() {
		return pathTW319QRC;
	}

	public static void setPathTW319QRC(String path) {
		if(!path.substring(path.length()-1).equals("/")) {
			path += "/";
		}
		pathTW319QRC = path;
	}

	public static String getPathToken() {
		return getPathTW319QRC() + PATH_USER + FILE_TOKEN;
	}

	public static String getPathCheckin() {
		return getPathTW319QRC() + PATH_USER + FILE_CHECKIN;
	}

	public static String getPathCounties() {
		return getPathTW319QRC() + PATH_DATA + PATH_COUNTIES;
	}

	public static String getPathVillages() {
		return getPathTW319QRC() + PATH_DATA + PATH_VILLAGES;
	}

	public static String getPathStores() {
		return getPathTW319QRC() + PATH_DATA + PATH_STORES;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<TW319LocationItem> getLocationItems() {
		return items;
	}

	public void clearLocationItems() {
		items.clear();
	}

	public void addLocationItem(String id, String name, String url) {
		TW319LocationItem item = new TW319LocationItem(this, id, name, url);
		addLocationItem(item);
	}

	public void addLocationItem(TW319LocationItem item) {
		items.add(item);
	}

	public TW319LocationItem getLocationItem(int index) {
		if (items != null && index < items.size())
			return items.get(index);
		else
			return null;
	}

	public TW319LocationItem getLocationItem(String id) {
		for (TW319LocationItem item : items) {
			if (item.getId().equals(id))
				return item;
		}
		return null;
	}

	public static String loadFromFile(String fileName) {
		String jsonString = "";
		FileInputStream is = null;
		try {
			is = new FileInputStream(fileName);
			FileChannel fc = is.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			jsonString = Charset.defaultCharset().decode(bb).toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (is != null)
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	public void saveToFile(String fileName) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(fileName);
			String out = toJsonString();
			os.write(out.getBytes());
			if (os != null)
				os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isItemDataCached(String id) {
		String filename = getPathCounties() + id + FILE_EXTENSION;
		File file = new File(filename);
		boolean fileExists = file.isFile();
		return fileExists;
	}

	public static boolean isStoreVisited(String id) {
		return (visitedStores != null) ? visitedStores.contains(id) : false;
	}

	public static void loadVisitedStoresFromFile() {
		visitedStores = new HashSet<String>();
		/*
		 * sample content of checkin.json
         * {
         *  "status": "1",
         *  "stores_visited_by_district": [
         *    {
         *      "stores_visited": 2,
         *      "name": "county_name",
         *      "county_id": 1,
         *      "stores": [
         *        {
         *          "time_collected": "2015/01/01 12:34",
         *          "name": "store_name",
         *          "store_id": 1111
         *        },
         *        {
         *          "time_collected": "2015/10/14 23:45",
         *          "name": "store_name",
         *          "store_id": 2222
         *        }
         *      ]
         *    }
         *  ],
         *  "status_msg": ""
         * }
		 */
		String jsCheckin = loadFromFile(getPathCheckin());
		try {
			JSONObject joCheckin = new JSONObject(jsCheckin);
			JSONArray joStoresVisitedByDistrict = joCheckin.optJSONArray(K.jsonStoresVisitedByDistrict);
			for (int i = 0; i < joStoresVisitedByDistrict.length(); i++) {
				JSONObject joDistrict = joStoresVisitedByDistrict.getJSONObject(i);
				JSONArray joStores = joDistrict.optJSONArray(K.jsonStores);
				for (int j = 0; j < joStores.length(); j++) {
					JSONObject joStore = joStores.getJSONObject(j);
					String store_id = joStore.optString(K.jsonStoreId);
					visitedStores.add(store_id);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void updateVisitedStores() {
        /*
         * the visited stores list is retrieved from one of the following url:
         *   http://www.319.com.tw/cwApp/checkin/list/bytime?token=yourtoken
         *   http://www.319.com.tw/cwApp/checkin/list/bycategory?token=yourtoken
         *   http://www.319.com.tw/cwApp/checkin/list/bycounty?token=yourtoken
         */
		/*
		 * sample content of token.json
		 * {
		 * 	"email": "myname@foo.bar.com",
		 * 	"token": "0123456789012345678901234567890123456789012345%23ABCD0123CDEF%23aBcD0123eFgH4567iJkL8901mNoP2345"
		 * } 
		 */
		File fileToken = new File(getPathToken());
		if(fileToken.isFile()) {
			String token = null;
			String jsToken = loadFromFile(fileToken.getPath());
			try {
				JSONObject joToken = new JSONObject(jsToken);
				token = joToken.optString(K.jsonUserToken);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(token != null) {
				try {
					String urlCheckinByCounty = getUrlCheckinByCounty();
					urlCheckinByCounty = String.format("%s?token=%s", urlCheckinByCounty, token);
					String jsCheckin = new TW319HttpTask().execute(urlCheckinByCounty).get(
							K.timeoutHttpRequest, TimeUnit.MILLISECONDS);
					if (jsCheckin.length() > 0) {
						OutputStream os = new FileOutputStream(getPathCheckin());
						if (os != null) {
							os.write(jsCheckin.getBytes());
							os.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void fromJsonString(String jsonString) {
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.optJSONArray(K.jsonItems);
			for (int i = 0; i < jsonArray.length(); i++) {
				TW319LocationItem item = new TW319LocationItem(this);
				item.fromJson(jsonArray.getJSONObject(i));
				addLocationItem(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String toJsonString() {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (TW319LocationItem item : items) {
			jsonArray.put(item.toJson());
		}
		try {
			jsonObject.put(K.jsonItems, jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String jsonString = jsonObject.toString();
		return jsonString;
	}
}
