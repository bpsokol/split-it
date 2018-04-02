package com.scandit.extendedsample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.BarcodePickerActivity;
import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanOverlay;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.recognition.Barcode;
import com.scandit.recognition.RecognitionContext;

import java.util.Locale;

/**
 * A slightly more sophisticated activity illustrating how to embed the
 * Scandit BarcodeScanner SDK into your app.
 *
 * This activity shows 3 different ways to use the Scandit BarcodePicker:
 *
 *   - as a full-screen barcode picker in a separate activity (see
 *     SampleFullScreenBarcodeActivity).
 *   - as a cropped-view picker, only showing a small part of the video
 *     feed running
 *   - as a scaled-view picker showing a down-scaled version of the video
 *     feed.
 *
 * Copyright 2014 Scandit AG
 */

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
public class ExtendedSampleMainActivity extends Activity implements OnScanListener {

	static final int REQUEST_BARCODE_PICKER_ACTIVITY = 55;

	// The main object for scanning barcodes.
	private BarcodePicker mBarcodePicker;
	private boolean mPaused = true;
	private boolean mDeniedCameraAccess = false;

	private final static int CAMERA_PERMISSION_REQUEST = 5;


	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		if (Build.VERSION.SDK_INT >= 14) {
			super.setTheme(android.R.style.Theme_Holo_NoActionBar);
		} else if (Build.VERSION.SDK_INT >= 11) {
			super.setTheme(android.R.style.Theme_Holo);
		} else {
			super.setTheme(android.R.style.Theme_Black_NoTitleBar);
		}

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
		TextView versionLabel = (TextView) findViewById(R.id.version);
		versionLabel.setText(RecognitionContext.VERSION);
        final RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root);

        Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				if (mPaused) {
					return;
				}
                startActivity(new Intent(ExtendedSampleMainActivity.this, SettingsActivity.class));
        	}
        });

        /* Here we show how to start a new Activity that implements the Scandit
         * SDK as a full screen scanner. The Activity can be found in the
         * SampleFullScreenBarcodeActivity in this demo project. The old and
         * new GUIs can both be easily opened this way, which is also shown in 
         * the aforementioned activity. */
        Button activityButton = (Button) findViewById(R.id.fullscreen_button);
        activityButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				if (mPaused) {
					return;
				}
                startActivity(new Intent(ExtendedSampleMainActivity.this,
                        SampleFullScreenBarcodeActivity.class));
            }
        });
        
        /* Creates a button that shows how to add a view that is a scaled down 
         * version of the full screen Scandit SDK scanner. To only see part of
         * the scanner, check out the third example. */
        Button scaledButton = (Button) findViewById(R.id.scaled_button);
        scaledButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
                if (mBarcodePicker != null || mPaused){
                    return;
                }
				showScaledPicker(rootView);
            }
        });
        
        /*
         * Creates a button that shows how to add a cropped version of the full
         * screen Scandit SDK scanner. The cropping is accomplished by overlaying
         * parts of the scanner with an opaque view.
         */
        Button croppedButton = (Button) findViewById(R.id.cropped_button);
        croppedButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
                if (mBarcodePicker != null|| mPaused){
                    return;
                }
				showCroppedPicker(rootView);
            }
        });

		/*
         * Creates a button that shows how to use the BarcodePickerActivity which contains
         * a barcode picker that can be configured through an intent. The scanned barcode
         * data is returned as part of the result.
         */
		Button launchPickerButton = (Button) findViewById(R.id.launch_picker_activity);
		launchPickerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchPickerActivity();
			}
		});
        
        // Must be able to run the portrait version for this button to work.
        if (!BarcodePicker.canRunPortraitPicker()) {
            scaledButton.setEnabled(false);
            croppedButton.setEnabled(false);
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_BARCODE_PICKER_ACTIVITY) {
			return;
		}
		String message = "no code recognized";
		if (data.getBooleanExtra("barcodeRecognized", false)) {
			message = String.format("%s (%s)", data.getStringExtra("barcodeData"),
					data.getStringExtra("barcodeSymbologyName").toUpperCase(Locale.US));
		}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void launchPickerActivity() {
		ScanditLicense.setAppKey(SampleFullScreenBarcodeActivity.sScanditSdkAppKey);
		Intent launchIntent = new Intent(ExtendedSampleMainActivity.this,
				                         BarcodePickerActivity.class);
		launchIntent.putExtra("enabledSymbologies", new int[] {
			Barcode.SYMBOLOGY_EAN13,
			Barcode.SYMBOLOGY_EAN8,
			Barcode.SYMBOLOGY_UPCA,
			Barcode.SYMBOLOGY_UPCE
		});
		launchIntent.putExtra("guiStyle", ScanOverlay.GUI_STYLE_LASER);
        launchIntent.putExtra("restrictScanningArea", true);
        launchIntent.putExtra("scanningAreaHeight", 0.1f);
		startActivityForResult(launchIntent, REQUEST_BARCODE_PICKER_ACTIVITY);
	}

	@SuppressWarnings("deprecation")
	private void showCroppedPicker(final RelativeLayout rootView) {
        ScanSettings settings = SettingsActivity.getSettings(this);
		settings.setActiveScanningArea(ScanSettings.ORIENTATION_PORTRAIT, new RectF(0.0f, 0.45f, 1.0f, 0.55f));
		settings.setScanningHotSpot(0.5f, 0.5f);

		mBarcodePicker = createPicker(settings);
		RelativeLayout.LayoutParams rParams;

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		rParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, display.getHeight() / 2);
		rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		rootView.addView(mBarcodePicker, rParams);

		TextView overlay = new TextView(this);
		rParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, display.getHeight() / 2);
		rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		overlay.setBackgroundColor(0xFF000000);
		rootView.addView(overlay, rParams);
		overlay.setText("Touch to close");
		overlay.setGravity(Gravity.CENTER);
		overlay.setTextColor(0xFFFFFFFF);
		overlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rootView.removeView(v);
				rootView.removeView(mBarcodePicker);
				mBarcodePicker.stopScanning();
				mBarcodePicker = null;
			}
		});
		mBarcodePicker.startScanning();
	}

	private BarcodePicker createPicker(ScanSettings settings) {
		ScanditLicense.setAppKey(SampleFullScreenBarcodeActivity.sScanditSdkAppKey);

		BarcodePicker picker = new BarcodePicker(this, settings);

		// Set UI settings according to the settings activity. To get a
		// short overview and explanation of the most used settings please
		// check the SampleFullScreenBarcodeActivity.
		SettingsActivity.applyUISettings(this, picker.getOverlayView());

		// Register listener, in order to be notified whenever a new code is
		// scanned
		picker.setOnScanListener(this);
		return picker;

	}
	@SuppressWarnings("deprecation")
	private void showScaledPicker(final RelativeLayout rootView) {
        ScanSettings settings = SettingsActivity.getSettings(this);
		mBarcodePicker = createPicker(settings);

		RelativeLayout.LayoutParams rParams;

		RelativeLayout r = new RelativeLayout(this);
		rParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		r.setBackgroundColor(0x00000000);
		rootView.addView(r, rParams);
		r.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rootView.removeView(v);
				rootView.removeView(mBarcodePicker);
				mBarcodePicker.stopScanning();
				mBarcodePicker = null;
			}
		});

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		rParams = new RelativeLayout.LayoutParams(
				display.getWidth() * 3 / 4, display.getHeight() * 3 / 4);
		rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rParams.bottomMargin = 20;
		rootView.addView(mBarcodePicker, rParams);
		mBarcodePicker.startScanning();
	}

	@Override
	protected void onPause() {
		mPaused = true;
	    // When the activity is in the background immediately stop the 
	    // scanning to save resources and free the camera.
	    if (mBarcodePicker != null) {
	        mBarcodePicker.stopScanning();
	    }

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPaused = false;
		// note: onResume will be called repeatedly if camera access is not
		// granted.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			grantCameraPermissions();
		} else {

			// Once the activity is in the foreground again, restart scanning.
			if (mBarcodePicker != null) {
				mBarcodePicker.startScanning();
			}
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		if (requestCode == CAMERA_PERMISSION_REQUEST) {
			Button button1 = (Button) findViewById(R.id.cropped_button);
			Button button2 = (Button) findViewById(R.id.scaled_button);
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				mDeniedCameraAccess = false;
			} else {
				mDeniedCameraAccess = true;
			}
			button1.setEnabled(!mDeniedCameraAccess);
			button2.setEnabled(!mDeniedCameraAccess);
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void grantCameraPermissions() {
		if (this.checkSelfPermission(Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			if (mDeniedCameraAccess == false) {
				// it's pretty clear for why the camera is required. We don't need to give a detailed
				// reason.
				this.requestPermissions(new String[]{Manifest.permission.CAMERA},
						CAMERA_PERMISSION_REQUEST);
			}

		} else {
			// Once the activity is in the foreground again, restart scanning.
			if (mBarcodePicker != null) {
				mBarcodePicker.startScanning();
			}
		}
	}

	@Override
	public void onBackPressed() {
	    if (mBarcodePicker != null) {
	        mBarcodePicker.stopScanning();
	    }
	    finish();
	}

	@Override
	public void didScan(ScanSession session) {
		// We let the scanner continuously scan without showing results.
        Log.e("ScanditSDK", session.getNewlyRecognizedCodes().get(0).getData());
	}
}
