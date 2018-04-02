package com.scandit.batchmodescansample;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.scandit.recognition.RecognitionContext;

/**
 * A slightly more sophisticated activity illustrating how to embed the
 * Scandit BarcodeScanner SDK into your app.
 *
 * This activity shows 3 different ways to use the Scandit Barcode Picker:
 *
 *   - as a full-screen barcode picker in a separate activity (see
 *     SampleFullScreenBarcodeActivity).
 *   - as a cropped-view picker, only showing a small part of the video
 *     feed running
 *   - as a scaled-view picker showing a down-scaled version of the video
 *     feed.
 */
public class BatchModeScanSampleMainActivity extends Activity  {
	// The main object for scanning barcodes.
	private boolean mPaused = true;
	private final static int CAMERA_PERMISSION_REQUEST = 5;
	private boolean mDeniedCameraAccess = false;
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if (Build.VERSION.SDK_INT >= 14) {
			super.setTheme(android.R.style.Theme_DeviceDefault);
		} else if (Build.VERSION.SDK_INT >= 11) {
			super.setTheme(android.R.style.Theme_Holo);
		} else {
			super.setTheme(android.R.style.Theme_Black_NoTitleBar);
		}

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
		TextView versionLabel = (TextView) findViewById(R.id.version);
		versionLabel.setText(RecognitionContext.VERSION);

        Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				if (mPaused) {
					return;
				}
                startActivity(new Intent(BatchModeScanSampleMainActivity.this, SettingsActivity.class));
        	}
        });

        /* Here we show how to start a new Activity that implements the Scandit
         * SDK as a full screen scanner. The Activity can be found in the
         * SampleFullScreenBarcodeActivity in this demo project. The old and
         * new GUIs can both be easily opened this way, which is also shown in 
         * the aforementioned activity. */

		Button activityAimButton = (Button) findViewById(R.id.aim_scan_button);
		activityAimButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPaused) {
					return;
				}
				startActivity(new Intent(BatchModeScanSampleMainActivity.this,
						SampleAimAndScanBarcodeActivity.class));
			}
		});

		Button activityConfirmButton = (Button) findViewById(R.id.scan_confirm_button);
		activityConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPaused) {
					return;
				}
				startActivity(new Intent(BatchModeScanSampleMainActivity.this,
						SampleScanAndConfirmBarcodeActivity.class));
			}
		});
	}

	@Override
	protected void onPause() {
		mPaused = true;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		mPaused = false;
	    super.onResume();
		// note: onResume will be called repeatedly if camera access is not
		// granted.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			grantCameraPermissions();
		}

	}

	@TargetApi(Build.VERSION_CODES.M)
	private void grantCameraPermissions() {
		if (this.checkSelfPermission(Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			if (mDeniedCameraAccess == false) {
				// it's pretty clear for why the camera is required. We don't need to give a
				// detailed reason.
				this.requestPermissions(new String[]{Manifest.permission.CAMERA},
						CAMERA_PERMISSION_REQUEST);
			}

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		if (requestCode == CAMERA_PERMISSION_REQUEST) {
			Button button1 = (Button) findViewById(R.id.aim_scan_button);
			Button button2 = (Button) findViewById(R.id.scan_confirm_button);
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				button1.setEnabled(true);
				button2.setEnabled(true);
			} else {
				button1.setEnabled(false);
				button2.setEnabled(false);
				mDeniedCameraAccess = true;
			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onBackPressed() {
	    finish();
	}

}
