package com.jx.tw319qrc.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.jx.tw319qrc.K;
import com.jx.tw319qrc.R;
import com.jx.tw319qrc.data.TW319County;
import com.jx.tw319qrc.data.TW319Location;
import com.jx.tw319qrc.data.TW319LocationItem;
import com.jx.tw319qrc.data.TW319Village;
import com.jx.tw319qrc.tools.FilePath;
import com.jx.tw319qrc.tools.ZipUtils;

public class TW319QRCActivity extends Activity {

	private final String XML_FILE = "data/tw319qrc.xml";
	private Context mContext = null;
	private SwipeRefreshLayout swipeRefreshLayout =null;
	private GridView gridViewLocation = null;
	public TW319Location twAll = null;
	public TW319County county = null;
	public TW319Village village = null;
	public int level = 0;

	// +++ data import/export
	private static final int DATA_FILE_SELECT_CODE = 0;
	private static final String DATA_FILE_NAME = "tw319qrc.zip";
	// --- data import/export

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tw319qrc);
		initialize();
		initTW319Location();
		loadGridViewForTwAll();
		gridViewLocation.setVisibility(View.VISIBLE);
		TW319Location.loadVisitedStoresFromFile();
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
		File fileToken = new File(TW319Location.getPathToken());
		if(!fileToken.isFile()) {
			menu.findItem(R.id.itemUser).setVisible(false);
		}
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
		case R.id.itemClearData:
			confirmClearData();
			break;
		case R.id.itemDataImport:
			showFileChooser();
			break;
		case R.id.itemDataExport:
			exportData();
			break;
		case R.id.itemUserUpdateVisitedStores:
			TW319Location.updateVisitedStores();
			TW319Location.loadVisitedStoresFromFile();
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
					path = FilePath.getPath(mContext, uri);
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
		String zipFile = pathDownlaod.toString() + "/" + DATA_FILE_NAME;
		try {
			ZipUtils.zip(getExternalFilesDir(null).toString(), zipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Toast.makeText(this,
				"The data is exported and saved in folder " + pathDownlaod,
				Toast.LENGTH_LONG).show();
	}

	// --- data import/export

	private void initialize() {
		mContext = this;
		gridViewLocation = (GridView) findViewById(R.id.gridViewLocation);

		TW319Location.setPathTW319QRC(getExternalFilesDir(null).toString());
		twAll = new TW319Location();
		county = new TW319County();
		village = new TW319Village();
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layoutTW319QRC);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (level == 1) {
					county.reload();
					showLocation();
				}
				swipeRefreshLayout.setRefreshing(false);
			}
		});
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_red_light,
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light);
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
					} else if (tagName.equals(K.tagCwApp)) {
						// nothing to do ...
					} else if (tagName.equals(K.tagPathCheckinByTime)) {
						// not used so far...
					} else if (tagName.equals(K.tagPathCheckinByCategory)) {
						TW319Location.setUrlCheckinByCategory(text);
					} else if (tagName.equals(K.tagPathCheckinByCounty)) {
						TW319Location.setUrlCheckinByCounty(text);
					} else if (tagName.equals(K.tagPathQRCodeCheckin)) {
						TW319Location.setUrlQRCodeCheckin(text);
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

	private void confirmClearData() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.text_clear_data);
		alertDialogBuilder.setMessage(R.string.text_clear_all_data);
		alertDialogBuilder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						clearData();
					}
				});
		alertDialogBuilder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
					}
				});
		final AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button n = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				n.setFocusable(true);
				n.setFocusableInTouchMode(true);
				n.requestFocus();
			}
		});
		alertDialog.show();
	}

	private void clearData() {
		if (level == 1) {
			for (TW319LocationItem item : county.getLocationItems()) {
				village.setId(item.getId());
				village.deleteFile();
			}
			county.deleteFile();
		} else {
			removeDirectory(getExternalFilesDir(null));
		}
		showLocation();
	}

	private void showAbout() {
		AboutDialog about = new AboutDialog(mContext);
		about.show();
	}
}
