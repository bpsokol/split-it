package com.scandit.extendedsample;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.scandit.barcodepicker.*;
import com.scandit.recognition.Barcode;

/**
 * Example for a full-screen barcode scanning activity using the Scandit
 * Barcode picker.
 *
 * The activity does the following:
 *
 *  - starting the picker full-screen mode
 *  - configuring the barcode picker with the settings defined in the
 *    SettingsActivity.
 *  - registering a listener to get notified whenever a barcode gets
 *    scanned. Upon a successful scan, the barcode scanner is paused and
 *    the recognized barcode is displayed in an overlay. When the user
 *    taps the screen, barcode recognition is resumed.
 *
 * For non-fullscreen barcode scanning take a look at the ExtendedSampleMainActivity
 * class.
 *
 * Copyright 2014 Scandit AG
 * 
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
public class SampleFullScreenBarcodeActivity extends Activity implements OnScanListener {

    // The main object for recognizing a displaying barcodes.
    private BarcodePicker mBarcodePicker;
	private Button mBarcodeSplash = null;
    private UIHandler mHandler = null;
    private boolean mDeniedCameraAccess = false;

    private final static int CAMERA_PERMISSION_REQUEST = 5;

    // Enter your Scandit SDK License key here.
    // Your Scandit SDK License key is available via your Scandit SDK web account.
    public static final String sScanditSdkAppKey = "-- ENTER YOUR SCANDIT LICENSE KEY HERE --";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mHandler = new UIHandler(this);
        // We keep the screen on while the scanner is running.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize and start the bar code recognition.
        initializeAndStartBarcodeScanning();
    }
    
    @Override
    protected void onPause() {
		super.onPause();
        // When the activity is in the background immediately stop the 
        // scanning to save resources and free the camera.
        mBarcodePicker.stopScanning();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // note: onResume will be called repeatedly if camera access is not
        // granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantCameraPermissions();
        } else {
            mBarcodePicker.startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mDeniedCameraAccess = false;
            } else {
                mDeniedCameraAccess = true;
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void grantCameraPermissions() {
        if (this.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (!mDeniedCameraAccess) {
                // it's pretty clear for why the camera is required. We don't need to give a detailed
                // reason.
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST);
            }
        } else {
            mBarcodePicker.startScanning();
        }
    }

    /**
     * Initializes and starts the bar code scanning.
     */
    public void initializeAndStartBarcodeScanning() {
        ScanditLicense.setAppKey(sScanditSdkAppKey);
        // Switch to full screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        SettingsActivity.setActivityOrientation(this);

		// Set all scan settings according to the settings activity. Typically
		// you will hard-code the settings for your app and do not need a settings
		// activity.
		ScanSettings settings = SettingsActivity.getSettings(this);
        
        //settings.setScanningHotSpot(0.25f, 0.25f);

		// the following code caching and duplicate filter values are the
		// defaults, they are nevertheless listed here to introduce them.

		// keep codes forever
		settings.setCodeCachingDuration(-1);
		// classify codes as duplicates if the same data/symbology is scanned within 500ms.
		settings.setCodeDuplicateFilter(500);

        // Some Android 2.3+ devices do not support rotated camera feeds. On these devices, the
        // barcode picker emulates portrait mode by rotating the scan UI.
        boolean emulatePortraitMode = !BarcodePicker.canRunPortraitPicker();
        if (emulatePortraitMode) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mBarcodePicker = new BarcodePicker(this, settings);
		// Apply UI settings to barcode picker
		SettingsActivity.applyUISettings(this, mBarcodePicker.getOverlayView());
        
		// Add listeners for scan events and search bar events (only needed if the bar is visible).
        mBarcodePicker.setOnScanListener(this);

        // Add both views to activity, with the scan GUI on top.
        setContentView(mBarcodePicker);

        // If you want to process the individual frames yourself, add a capture
        // listener.
        // mBarcodePicker.setCaptureListener(this);
    }

	@Override
	public void didScan(ScanSession session) {
		List<Barcode> newlyDecoded = session.getNewlyRecognizedCodes();
        // because the callback is invoked inside the thread running the barcode
        // recognition, any UI update must be posted to the UI thread.
        // In this example, we want to show the first decoded barcode in a
        // splash screen covering the full display.
        Message msg = mHandler.obtainMessage(UIHandler.SHOW_BARCODES, newlyDecoded);
        mHandler.sendMessage(msg);
        // pause scanning and clear the session. The scanning itself is resumed
        // when the user taps the screen.
        session.pauseScanning();
        session.clear();
	}

    @Override
    public void onBackPressed() {
        mBarcodePicker.stopScanning();
        mBarcodeSplash = null;
        finish();
    }

    static private class UIHandler extends Handler {

        public static final int SHOW_BARCODES = 0;

        private WeakReference<SampleFullScreenBarcodeActivity> mActivity;

        UIHandler(SampleFullScreenBarcodeActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_BARCODES:
                    showSplash(createMessage((List<Barcode>)msg.obj));
                    break;
            }
        }

        private String createMessage(List<Barcode> codes) {
            String message = "";
            for (Barcode code : codes) {
                String data = code.getData();
                // truncate code to certain length
                String cleanData;
                if (data.length() > 30) {
                    cleanData = data.substring(0, 25)+"[...]";
                } else {
                    cleanData = data;
                }
                if (message.length() > 0) {
                    message += "\n\n\n";
                }
                message += cleanData;
                message += "\n\n(" + code.getSymbologyName().toUpperCase(Locale.US)+")";
            }
            return message;
        }

        private void showSplash(String msg) {
            SampleFullScreenBarcodeActivity activity = mActivity.get();
            activity.mBarcodeSplash = new Button(activity);
            activity.mBarcodeSplash.setTextColor(Color.WHITE);
            activity.mBarcodeSplash.setTextSize(30);
            activity.mBarcodeSplash.setGravity(Gravity.CENTER);
            activity.mBarcodeSplash.setBackgroundColor(0x0039C1CC);
            activity.mBarcodeSplash.setText(msg);
            RelativeLayout layout = activity.mBarcodePicker;
            layout.addView(activity.mBarcodeSplash, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            activity.mBarcodePicker.pauseScanning();

            activity.mBarcodeSplash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SampleFullScreenBarcodeActivity activity = mActivity.get();
                    activity.mBarcodePicker.resumeScanning();
                    activity.mBarcodePicker.removeView(activity.mBarcodeSplash);
                    activity.mBarcodeSplash = null;
                }
            });
            activity.mBarcodeSplash.requestFocus();
        }
    }
}
