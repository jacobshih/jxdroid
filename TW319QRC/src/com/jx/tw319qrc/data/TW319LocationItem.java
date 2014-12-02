package com.jx.tw319qrc.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.jx.tw319qrc.K;

public class TW319LocationItem implements Serializable {
	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = -4274000937938757726L;
	protected TW319Location mLocation = null;
	protected String id = null;
	protected String name = null;
	protected String url = null;

	public TW319LocationItem(TW319Location location) {
		super();
		setLocation(location);
	}

	public TW319LocationItem(TW319Location location, String id, String name,
			String url) {
		super();
		setLocation(location);
		setId(id);
		setName(name);
		setUrl(url);
	}

	public TW319Location getLocation() {
		return mLocation;
	}

	public void setLocation(TW319Location location) {
		this.mLocation = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isDataCached() {
		return getLocation().isItemDataCached(getId());
	}

	public void fromJson(JSONObject jsonObject) {
		id = jsonObject.optString(K.jsonId, "");
		name = jsonObject.optString(K.jsonName, "");
		url = jsonObject.optString(K.jsonUrl, "");
	}

	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(K.jsonId, id);
			jsonObject.put(K.jsonName, name);
			jsonObject.put(K.jsonUrl, url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
}
