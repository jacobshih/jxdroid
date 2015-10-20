package com.jx.tw319qrc.ui;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.jx.tw319qrc.R;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	private Context mContext;
	public AboutDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(R.string.app_name);
		setAppVersion();
		setButtonClickListener();
	}

	private void setAppVersion() {
		TextView viewAppVersion = (TextView) findViewById(R.id.textViewAppVersion);
		String appVersion = mContext.getResources().getString(R.string.text_about_app_version);
		viewAppVersion.setText(appVersion);

		TextView viewBuildDate = (TextView) findViewById(R.id.textViewBuildDate);
		String appBuildDate = mContext.getResources().getString(R.string.text_about_app_build_date);
		String packageName = mContext.getPackageName();
		try {
			ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);
			ZipFile zf = new ZipFile(appInfo.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long builtTime = ze.getTime();
			zf.close();
			String format = new String("yyyyMMdd HHmmss");
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
			String fileTime = sdf.format(new java.util.Date(builtTime));
			appBuildDate += " " + fileTime;
			viewBuildDate.setText(appBuildDate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setButtonClickListener() {
		Button btnOK = (Button) findViewById(R.id.buttonAboutOK);
		btnOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
