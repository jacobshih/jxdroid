package com.jx.tw319qrc.ui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jx.tw319qrc.K;
import com.jx.tw319qrc.R;
import com.jx.tw319qrc.data.TW319HttpTask;
import com.jx.tw319qrc.data.TW319Location;
import com.jx.tw319qrc.data.TW319Store;
import com.jx.tw319qrc.data.TW319StoreItem;
import com.jx.tw319qrc.data.TW319StoreItem.LatLng;
import com.jx.tw319qrc.data.TW319Village;

public class TW319QRCVillageActivity extends Activity implements LocationListener {

	private Context mContext = null;
	private SwipeRefreshLayout swipeRefreshLayout =null;
	private ListView listViewStores = null;
	private LinearLayout layoutStoreDetail = null;
	private LinearLayout layoutStoreDescription = null;
	private ImageView imageViewQRCode = null;
	private ImageView imageViewStoreIcon = null;
	private TextView textViewStoreName = null;
	private TextView textViewStoreAddress = null;
	private TextView textViewStoreTelphone = null;
	private TextView textViewDistance = null;
	private TW319Village village = null;
	private TW319StoreItem storeItem = null;
	protected static TW319Store store = null;
	private LocationManager locationManager = null;
	private Location location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_village);
		initialize();
		loadListViewStores();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_tw319qrc, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean ret = super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.itemClearData).setVisible(false);
		menu.findItem(R.id.itemData).setVisible(false);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.itemAbout:
			showAbout();
			return true;
		case R.id.itemUserUpdateVisitedStores:
			TW319Location.updateVisitedStores();
			TW319Location.loadVisitedStoresFromFile();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initialize() {
		LayoutInflater inflater = getLayoutInflater();
		mContext = this;

		listViewStores = (ListView) findViewById(R.id.listViewStores);

		layoutStoreDetail = (LinearLayout) findViewById(R.id.layoutStoreDetail);
		View viewStoreDetail = inflater.inflate(R.layout.layout_store_detail,
				layoutStoreDetail, false);
		layoutStoreDetail.addView(viewStoreDetail);
		layoutStoreDetail.setVisibility(View.GONE);

		imageViewQRCode = (ImageView) layoutStoreDetail
				.findViewById(R.id.imageViewQRCode);

		layoutStoreDescription = (LinearLayout) layoutStoreDetail
				.findViewById(R.id.layoutStoreDescription);
		View viewStoreDescription = inflater.inflate(
				R.layout.layout_store_description, layoutStoreDescription,
				false);
		layoutStoreDescription.addView(viewStoreDescription);

		imageViewStoreIcon = (ImageView) layoutStoreDescription
				.findViewById(R.id.imageViewStoreIcon);
		textViewStoreName = (TextView) layoutStoreDescription
				.findViewById(R.id.textViewStoreName);
		textViewStoreAddress = (TextView) layoutStoreDescription
				.findViewById(R.id.textViewStoreAddress);
		textViewStoreTelphone = (TextView) layoutStoreDescription
				.findViewById(R.id.textViewStoreTelphone);
		textViewDistance = (TextView) layoutStoreDescription
				.findViewById(R.id.textViewDistance);

		village = (TW319Village) getIntent().getSerializableExtra(
				TW319Village.class.getName());

		if(store ==null) {
			store = new TW319Store();
		}

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layoutTW319QRC);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				village.reload();
				loadListViewStores();
				swipeRefreshLayout.setRefreshing(false);
			}
		});
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_red_light,
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null) {
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		imageViewQRCode.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				checkin();		// check-in using current location.
				return v.performClick();
			}
		});
		imageViewStoreIcon.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				checkin(false);	// (cheating) check-in using the location of the store.
				return v.performClick();
			}
		});
	}

	private void loadListViewStores() {
		ListViewVillageAdapter listViewVillageAdapter = new ListViewVillageAdapter(
				this, null, village.getLocationItems());
		listViewStores.setAdapter(listViewVillageAdapter);
		listViewStores.setOnItemClickListener(listViewVillageOnClickListener);
	}

	private ListView.OnItemClickListener listViewVillageOnClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			layoutStoreDetail.setVisibility(View.VISIBLE);
			TW319StoreItem item = (TW319StoreItem) view.getTag();
			storeItem = item;
			Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
					item.getIconId());
			imageViewStoreIcon.setImageBitmap(bm);
			textViewStoreName.setText(item.getName());
			textViewStoreAddress.setText(item.getAddress());
			textViewStoreTelphone.setText(item.getTelephone());

			textViewStoreName.setSelected(true);
			textViewStoreAddress.setSelected(true);
			LatLng coordinates = store.getCoordinates(item);
			String distance = "? ";
			if (coordinates != null) {
				updateLocationIcon(view, item);
				if (location != null) {
					float results[] = new float[1];
					Location.distanceBetween(coordinates.latitude, coordinates.longitude, location.getLatitude(), location.getLongitude(), results);
					distance = String.format(Locale.getDefault(), "%.1f km ", results[0] / 1000);
					textViewDistance.setText(distance);
				}
			}

			view.setSelected(true);

			genQRCode(item.getUrl());
		}
	};

	protected void updateLocationIcon(View view, TW319StoreItem item) {
		ImageView imageViewLocation = (ImageView) view.findViewById(R.id.imageViewLocation);
		int imageViewLocationVisibility = View.GONE;
		if (store.isCoordinatesAvailable(item)) {
			LatLng coordinates = store.getCoordinates(item);
			if (coordinates != null) {
				imageViewLocationVisibility = View.VISIBLE;
				if (location != null) {
					float results[] = new float[1];
					Location.distanceBetween(coordinates.latitude, coordinates.longitude, location.getLatitude(), location.getLongitude(), results);
					boolean isNear = (results[0] < 10000.0);
					int id = isNear ? R.drawable.icon_location_near : R.drawable.icon_location_far;
					Drawable drawable = getResources().getDrawable(id);
					imageViewLocation.setImageDrawable(drawable);
				}
			}
		}
		imageViewLocation.setVisibility(imageViewLocationVisibility);			
	}
	protected void genQRCode(String url) {
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		@SuppressWarnings("deprecation")
		int height = display.getHeight();
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 7 / 8;

		Log.i("jacob_shih", "genQRCode url: " + url);
		try {
			Bitmap bitmap = encodeAsBitmap(url, smallerDimension);
			imageViewQRCode.setImageBitmap(bitmap);
		} catch (WriterException e) {
			System.out.println("Could not encode barcode");
		}
	}

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	public Bitmap encodeAsBitmap(String contents, int dimension)
			throws WriterException {
		String contentsToEncode = contents;
		if (contentsToEncode == null) {
			return null;
		}
		Map<EncodeHintType, Object> hints = null;
		String encoding = guessAppropriateEncoding(contentsToEncode);
		if (encoding != null) {
			hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		BitMatrix result;
		try {
			result = new MultiFormatWriter().encode(contentsToEncode,
					BarcodeFormat.QR_CODE, dimension, dimension, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		}
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

	private void showAbout() {
		AboutDialog about = new AboutDialog(mContext);
		about.show();
	}

	@Override
	public void onLocationChanged(Location l) {
		location = l;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("jacob_shih", "" + "status:" + status);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("jacob_shih", "onProviderEnabled");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i("jacob_shih", "onProviderDisabled");
	}

	private String md5(String s) {
		MessageDigest digest;
		String hash = "";
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for(int i = 0; i < messageDigest.length; i++) {
				hexString.append(String.format("%02x", messageDigest[i]));
			}
			hash = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}
	private String getVerifyCode(String token, String storeid, String gps_location) {
		String verifyCode = null;
		String code = String.format(Locale.getDefault(), "%s%s%s:319", token, storeid, gps_location);
		verifyCode = md5(code);
		return verifyCode;
	}

	private void checkin() {
		checkin(true);
	}

	private void checkin(boolean useCurrentLocation) {
		String token = TW319Location.getUserToken();
		if(token == null) {
			return;
		}

		double lat = 0;
		double lng = 0;
		if(useCurrentLocation) {
			lat = location.getLatitude();
			lng = location.getLongitude();
		} else {
			LatLng latlng = store.getCoordinates(storeItem);
			lat = latlng.latitude;
			lng = latlng.longitude;
		}
		String store_id = storeItem.getId();
		String gps_location = String.format(Locale.getDefault(), "%f,%f", lat, lng);
		String verifycode = getVerifyCode(token, store_id, gps_location);
		String urlCheckin = TW319Location.getUrlQRCodeCheckin();
		Builder builder = Uri.parse(urlCheckin).buildUpon();
		builder.appendQueryParameter("token", token);
		builder.appendQueryParameter("verifycode", verifycode);
		builder.appendQueryParameter("store_id", store_id);
		builder.appendQueryParameter("gps_location", gps_location);
		urlCheckin = builder.build().toString();
		try {
			String jsCheckin = new TW319HttpTask().execute(urlCheckin).get(
					K.timeoutHttpRequest, TimeUnit.MILLISECONDS);
			JSONObject joCheckin = new JSONObject(jsCheckin);
			String status = joCheckin.optString(K.jsonStatus, "");
			if(status.equals("0")) {
				String statusMsg = joCheckin.optString(K.jsonStatusMsg, "");
				Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT).show();
			} else {
				String statusMsg = String.format(Locale.getDefault(),
					"%s %s %s\n%s %s\n%s",
					joCheckin.optString(K.jsonCountyName, ""),
					joCheckin.optString(K.jsonVillageName, ""),
					joCheckin.optString(K.jsonAddress, ""),
					joCheckin.optString(K.jsonStoreId, ""),
					joCheckin.optString(K.jsonStoreName, ""),
					joCheckin.optString(K.jsonTimeCollected, ""));
				Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
}
