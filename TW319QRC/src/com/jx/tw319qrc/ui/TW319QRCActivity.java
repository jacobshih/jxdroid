package com.jx.tw319qrc.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jx.tw319qrc.K;
import com.jx.tw319qrc.R;
import com.jx.tw319qrc.data.TW319County;
import com.jx.tw319qrc.data.TW319Location;
import com.jx.tw319qrc.data.TW319LocationItem;
import com.jx.tw319qrc.data.TW319Village;
import com.jx.tw319qrc.tools.FileUtils;
import com.jx.tw319qrc.tools.ZipUtils;

public class TW319QRCActivity extends Activity {

	private final String XML_FILE = "data/tw319qrc.xml";
	private Context mContext = null;
	private ProgressBar progressBarLoading = null;
	private GridView gridViewLocation = null;
	public TW319Location twAll = null;
	public TW319County county = null;
	public TW319Village village = null;
	public int level = 0;

	// +++ data import/export
	private static final int DATA_FILE_SELECT_CODE = 0;
	private static final String DATA_FILE_PATH = "Download";
	private static final String DATA_FILE_NAME = "tw319qrc.zip";
	// --- data import/export

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tw319qrc);
		initialize();
		progressBarLoading.setVisibility(View.VISIBLE);
		initTW319Location();
		loadGridViewForTwAll();
		gridViewLocation.setVisibility(View.VISIBLE);
		progressBarLoading.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_tw319qrc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.itemAbout:
			return true;
		case R.id.itemClearData:
			if(level == 1) {
				county.deleteFile();
			} else {
				removeDirectory(getExternalFilesDir(null));
				showLocation();
			}
			break;
		case R.id.itemReload:
			if(level == 1) {
				county.reload();
				showLocation();
			}
			break;
		case R.id.itemDataImport:
			showFileChooser();
			break;
		case R.id.itemDataExport:
			exportData();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (level > 0 && level <= 2) {
				level--;
				showLocation();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		switch (level) {
		case 2:
			level--;
			showLocation();
			break;
		}
		super.onResume();
	}

// +++ data import/export
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DATA_FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String path = null;
				try {
					path = FileUtils.getPath(mContext, uri);
					importData(path);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/zip");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent,
					"Select the tw319qrc data file"), DATA_FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(this, "Please install a File Manager",
					Toast.LENGTH_LONG).show();
		}
	}

	private void importData(String path) {
		try {
			ZipUtils.unzip(path, getExternalFilesDir(null).toString());
			showLocation();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportData() {
		File pathDownlaod = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		String zipFile = pathDownlaod.toString() + "/" + "x" + DATA_FILE_NAME;
		try {
			ZipUtils.zip(getExternalFilesDir(null).toString(), zipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Toast.makeText(this,
				"The data is exported and saved in folder " + DATA_FILE_PATH,
				Toast.LENGTH_LONG).show();
	}

	// --- data import/export

	private void initialize() {
		mContext = this;
		progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
		gridViewLocation = (GridView) findViewById(R.id.gridViewLocation);

		TW319Location.setPathTW319QRC(getExternalFilesDir(null).toString());
		twAll = new TW319Location();
		county = new TW319County();
		village = new TW319Village();
	}

	private void initTW319Location() {
		AssetManager am = mContext.getAssets();
		try {
			String text = null;
			InputStream is = am.open(XML_FILE);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, null);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = xpp.getName();
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					break;
				case XmlPullParser.END_TAG:
					if (tagName.equals(K.tagUrlBase)) {
						TW319Location.setUrlBase(text);
					} else if (tagName.equals(K.tagCounties)) {
						// nothing to do ...
					} else if (tagName.equals(K.tagPathCounty)) {
						TW319Location.setUrlPrefixOfCounty(text);
					} else if (tagName.equals(K.tagPathVillage)) {
						TW319Location.setUrlPrefixOfVillage(text);
					} else if (tagName.equals(K.tagPathStoreCode)) {
						TW319Location.setUrlPrefixOfStoreCode(text);
					} else if (tagName.equals(K.tagPathStoreDetail)) {
						TW319Location.setUrlPrefixOfStoreDetail(text);
					} else if (tagName.equals(K.tagCounty)) {
						String id = xpp.getAttributeValue(null, K.tagAttrId);
						String name = xpp
								.getAttributeValue(null, K.tagAttrName);
						String url = TW319Location.getUrlPrefixOfCounty() + id;
						twAll.addLocationItem(id, name, url);
					}
					break;
				case XmlPullParser.TEXT:
					text = xpp.getText().trim();
					break;
				}
				eventType = xpp.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	private void loadGridViewForTwAll() {
		GridViewLocationAdapter gridViewLocationAdapter = new GridViewLocationAdapter(
				this, null, R.drawable.icon_grid_item_location_background,
				twAll.getLocationItems());
		gridViewLocation.setAdapter(gridViewLocationAdapter);
		gridViewLocation
				.setOnItemClickListener(gridViewLocationOnClickListener);
	}

	private void loadGridViewForCounty() {
		GridViewLocationAdapter gridViewLocationAdapter = new GridViewLocationAdapter(
				this, null, R.drawable.icon_grid_item_village_background,
				county.getLocationItems());
		gridViewLocation.setAdapter(gridViewLocationAdapter);
		gridViewLocation
				.setOnItemClickListener(gridViewLocationOnClickListener);
	}

	private void startVillageActivity() {
		Intent intent = new Intent();
		intent.setClass(this, TW319QRCVillageActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(TW319Village.class.getName(), village);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void removeDirectory(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				removeDirectory(child);
		fileOrDirectory.delete();
	}

	private GridView.OnItemClickListener gridViewLocationOnClickListener = new GridView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			TW319LocationItem locationItem = (TW319LocationItem) view.getTag();
			if (level >= 0 && level < 2) {
				switch (level) {
				case 0:
					county.getVillages(locationItem.getId());
					break;
				case 1:
					village.getStores(locationItem.getId());
					break;
				}
				level++;
				showLocation();
			}
		}
	};

	private void showLocation() {
		switch (level) {
		case 0:
			loadGridViewForTwAll();
			break;
		case 1:
			loadGridViewForCounty();
			break;
		case 2:
			startVillageActivity();
			break;
		}
	}
}
