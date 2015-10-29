package com.jx.tw319qrc.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.jx.tw319qrc.K;
import com.jx.tw319qrc.R;

public class TW319StoreItem extends TW319LocationItem implements Serializable {

	/**
	 * serialVersionUID is generated automatically.
	 */
	private static final long serialVersionUID = -2757654219080840874L;
	public class LatLng {
		public LatLng(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public double latitude;
		public double longitude;
	}
	private String category = null;
	private String icon = null;
	private String telephone = null;
	private String address = null;
	private LatLng coordinates = null;

	public TW319StoreItem(TW319Location container) {
		super(container);
	}

	public TW319StoreItem(TW319Location container, String id, String name,
			String url) {
		super(container, id, name, url);
	}

	public TW319StoreItem(TW319Location container, String id, String name,
			String url, String category, String icon, String telephone,
			String address) {
		super(container, id, name, url);
		this.category = category;
		this.icon = icon;
		this.telephone = telephone;
		this.address = address;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LatLng getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double latitude, double longitude) {
		if (this.coordinates == null) {
			this.coordinates = new LatLng(latitude, longitude);
		} else {
			this.coordinates.latitude = latitude;
			this.coordinates.longitude = longitude;
		}
	}

	public void fromJson(JSONObject jsonObject) {
		id = jsonObject.optString(K.jsonId, "");
		name = jsonObject.optString(K.jsonName, "");
		url = jsonObject.optString(K.jsonUrl, "");
		category = jsonObject.optString(K.jsonCategory, "");
		icon = jsonObject.optString(K.jsonIcon, "");
		telephone = jsonObject.optString(K.jsonTelephone, "");
		address = jsonObject.optString(K.jsonAddress, "");
		if(jsonObject.has(K.jsonCoordinates)) {
			JSONObject joCoordinates = jsonObject.optJSONObject(K.jsonCoordinates);
			double latitude = joCoordinates.optDouble(K.jsonLatitude, 0);
			double longitude = joCoordinates.optDouble(K.jsonLongitude, 0);
			setCoordinates(latitude, longitude);
		}
	}

	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(K.jsonId, id);
			jsonObject.put(K.jsonName, name);
			jsonObject.put(K.jsonUrl, url);
			jsonObject.put(K.jsonCategory, category);
			jsonObject.put(K.jsonIcon, icon);
			jsonObject.put(K.jsonTelephone, telephone);
			jsonObject.put(K.jsonAddress, address);
			if(coordinates != null) {
				JSONObject joCoordinates = new JSONObject();
				joCoordinates.put(K.jsonLatitude, coordinates.latitude);
				joCoordinates.put(K.jsonLongitude, coordinates.longitude);
				jsonObject.put(K.jsonCoordinates, joCoordinates);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public int getIconId() {
		int id = R.drawable.icon_x;
		if (icon != null) {
			if (icon.contains("icon_1"))
				id = R.drawable.icon_1;
			else if (icon.contains("icon_2"))
				id = R.drawable.icon_2;
			else if (icon.contains("icon_3"))
				id = R.drawable.icon_3;
			else if (icon.contains("icon_4"))
				id = R.drawable.icon_4;
			else if (icon.contains("icon_5"))
				id = R.drawable.icon_5;
			else if (icon.contains("icon_6"))
				id = R.drawable.icon_6;
		}
		return id;
	}
}
