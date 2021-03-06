/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.liteav.demo.cap.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tencent.liteav.demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllAppActivity extends Activity {
	private static final String TAG = AllAppActivity.class.getSimpleName();

	private static ArrayList<ApplicationInfo> mApplications;

	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	private GridView mGrid;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		setContentView(R.layout.all_apps);

		registerIntentReceivers();

		loadApplications(true);

		bindApplications();
		bindButtons();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Close the menu
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			getWindow().closeAllPanels();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Remove the callback for the cached drawables or we leak
		// the previous Home screen on orientation change
		final int count = mApplications.size();
		for (int i = 0; i < count; i++) {
			mApplications.get(i).icon.setCallback(null);
		}

		unregisterReceiver(mApplicationsReceiver);
	}

	/**
	 * Registers various intent receivers. The current implementation registers
	 * only a wallpaper intent receiver to let other applications change the
	 * wallpaper.
	 */
	private void registerIntentReceivers() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, filter);
	}

	/**
	 * Creates a new appplications adapter for the grid view and registers it.
	 */
	private void bindApplications() {
		if (mGrid == null) {
			mGrid = (GridView) findViewById(R.id.all_apps);
		}
		mGrid.setAdapter(new ApplicationsAdapter(this, mApplications));
		mGrid.setSelection(0);

	}

	/**
	 * Binds actions to the various buttons.
	 */
	private void bindButtons() {
		mGrid.setOnItemClickListener(new ApplicationLauncher());
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 */
	private void loadApplications(boolean isLaunching) {
		if (isLaunching && mApplications != null) {
			return;
		}

		PackageManager manager = getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = manager.queryIntentActivities(
				mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

		if (apps != null) {
			final int count = apps.size();

			if (mApplications == null) {
				mApplications = new ArrayList<ApplicationInfo>(count);
			}
			mApplications.clear();

			for (int i = 0; i < count; i++) {
				ApplicationInfo application = new ApplicationInfo();
				ResolveInfo info = apps.get(i);

				application.title = info.loadLabel(manager);
				application.setActivity(new ComponentName(
						info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name), Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.icon = info.activityInfo.loadIcon(manager);

				mApplications.add(application);
			}
		}
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadApplications(false);
			bindApplications();
		}
	}

	/**
	 * GridView adapter to show the list of all installed applications.
	 */
	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
		private Rect mOldBounds = new Rect();

		public ApplicationsAdapter(Context context,
				ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = mApplications.get(position);

			if (convertView == null) {
				final LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.application, parent,
						false);
			}

			Drawable icon = info.icon;

			if (!info.filtered) {
				 final Resources resources = getContext().getResources();
				int width = (int)resources.getDimension(android.R.dimen.app_icon_size);
				int height = (int)resources.getDimension(android.R.dimen.app_icon_size);

				final int iconWidth = icon.getIntrinsicWidth();
				final int iconHeight = icon.getIntrinsicHeight();

				if (icon instanceof PaintDrawable) {
					PaintDrawable painter = (PaintDrawable) icon;
					painter.setIntrinsicWidth(width);
					painter.setIntrinsicHeight(height);
				}

				if (width > 0 && height > 0
						&& (width < iconWidth || height < iconHeight)) {
					final float ratio = (float) iconWidth / iconHeight;

					if (iconWidth > iconHeight) {
						height = (int) (width / ratio);
					} else if (iconHeight > iconWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565;
					final Bitmap thumb = Bitmap.createBitmap(width, height, c);
					final Canvas canvas = new Canvas(thumb);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(
							Paint.DITHER_FLAG, 0));
					// Copy the old bounds to restore them later
					// If we were to do oldBounds = icon.getBounds(),
					// the call to setBounds() that follows would
					// change the same instance and we would lose the
					// old bounds
					mOldBounds.set(icon.getBounds());
					icon.setBounds(0, 0, width, height);
					icon.draw(canvas);
					icon.setBounds(mOldBounds);
					icon = info.icon = new BitmapDrawable(thumb);
					info.filtered = true;
				}
			}

			final TextView textView = (TextView) convertView
					.findViewById(R.id.label);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null,
					null);
			textView.setText(info.title);

			return convertView;
		}
	}

	/**
	 * Starts the selected activity/application in the grid view.
	 */
	private class ApplicationLauncher implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView parent, View v, int position,
				long id) {
			ApplicationInfo app = (ApplicationInfo) parent
					.getItemAtPosition(position);
			startActivity(app.intent);
		}
	}
}
