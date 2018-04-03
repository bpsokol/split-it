package com.scandit.casesample;
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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scandit.barcodepicker.ScanCase;
import com.scandit.barcodepicker.ScanCaseListener;
import com.scandit.barcodepicker.ScanCaseSession;
import com.scandit.barcodepicker.ScanCaseSettings;
import com.scandit.barcodepicker.ScanOverlay;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.recognition.Barcode;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple demo application illustrating the use of the Scandit BarcodeScanner SDK for
 * tracking codes with the Case.
 */
public class CaseSampleActivity extends CameraPermissionActivity implements ScanCaseListener {

    // Enter your Scandit SDK License key here.
    // Your Scandit SDK License key is available via your Scandit SDK web account.
    public static final String sScanditSdkAppKey = "-- ENTER YOUR SCANDIT LICENSE KEY HERE --";

    // The main object for recognizing a displaying barcodes with the Case.
    private ScanCase scanCase;

    private FrameLayout previewContainer;
    private TextView stateLabel;
    private TextView barcodeLabel;
    private Button scanButton;
    private Button matrixScanButton;
    private View initText;

    private List<Barcode> uniqueBarcodes = new ArrayList<>();
    private State state = State.STANDBY;


    private enum State {
        STANDBY, FORWARD, DOWNWARD
    }

    private final int[] forwardSymbologiesToEnable = new int[]{
            Barcode.SYMBOLOGY_EAN13,
            Barcode.SYMBOLOGY_EAN8,
            Barcode.SYMBOLOGY_UPCA,
            Barcode.SYMBOLOGY_CODE128,
            Barcode.SYMBOLOGY_UPCE
    };
    private final int[] downwardSymbologiesToEnable = new int[]{
            Barcode.SYMBOLOGY_EAN13,
            Barcode.SYMBOLOGY_EAN8,
            Barcode.SYMBOLOGY_UPCA,
            Barcode.SYMBOLOGY_CODE128,
            Barcode.SYMBOLOGY_UPCE,
            Barcode.SYMBOLOGY_QR
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanditLicense.setAppKey(sScanditSdkAppKey);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.case_sample_activity);

        stateLabel = (TextView) findViewById(R.id.state_label);
        barcodeLabel = (TextView) findViewById(R.id.barcode_label);

        scanButton = (Button) findViewById(R.id.scan_button);
        matrixScanButton = (Button) findViewById(R.id.matrix_button);
        initText = findViewById(R.id.init_text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestCameraPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanButton.setVisibility(View.GONE);
        matrixScanButton.setVisibility(View.GONE);

        if (scanCase != null) {
            previewContainer.removeAllViews();
            scanCase.setListener(null);
            scanCase = null;
            scanButton.setOnClickListener(null);
            matrixScanButton.setOnClickListener(null);
        }
    }

    @Override
    public void onCameraPermissionGranted() {
        // This call might come at odd points and on any thread, we need to make sure to go back
        // on the main thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initializeScanCase();
            }
        });
    }

    /**
     * Forwards key down events to the Case.
     * Required only when volume or hardware scanButton support is turned on.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return scanCase.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Forwards key up events to the Case.
     * Required only when volume or hardware scanButton support is turned on.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return scanCase.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }

    /**
     * Initializes and starts the bar code scanning with the Case.
     */
    public void initializeScanCase() {
        if (scanCase != null) {
            return;
        }

        initText.setVisibility(View.VISIBLE);

        // The scanning behavior of the barcode picker is configured through scan
        // settings. We start with empty scan settings and enable a very generous
        // set of symbologies. In your own apps, only enable the symbologies you
        // actually need.
        final ScanCaseSettings settings = ScanCaseSettings.create();
        for (int sym : forwardSymbologiesToEnable) {
            settings.setSymbologyEnabled(sym, true, ScanCase.MODE_FACE_FORWARD);
        }
        for (int sym : downwardSymbologiesToEnable) {
            settings.setSymbologyEnabled(sym, true, ScanCase.MODE_FACE_DOWNWARD);
        }

        settings.setMatrixScanEnabled(true);
        settings.setMaxNumberOfCodesPerFrame(12);
        settings.setHighDensityModeEnabled(true);

        // Initialize the scan case
        scanCase = ScanCase.acquire(CaseSampleActivity.this, settings, CaseSampleActivity.this);

        previewContainer = (FrameLayout) findViewById(R.id.preview_container);
        previewContainer.setAlpha(0f);

        previewContainer.addView(scanCase.getCameraPreview());

        // Set the overlay's UI style to matrix scan because its used in downward scanning. As there
        // is no visible preview for the forward mode it does not matter what UI is enabled for it.
        scanCase.getCameraPreviewOverlay().setGuiStyle(ScanOverlay.GUI_STYLE_MATRIX_SCAN);

        // Turn the vibration on successful scans off (a beep is still enabled).
        scanCase.getCameraPreviewOverlay().setVibrateEnabled(false);

        // Enable the volume scanButton to activate the scanner - overriding onKeyDown and onKeyUp
        // methods is also necessary, so that the key events could be forwarded to the ScanCase
        // and handled accordingly
        scanCase.setVolumeButtonToScanEnabled(true);

        // Enable the hardware scanButton to activate the scanner - overriding onKeyDown and onKeyUp
        // methods is also necessary, so that the key events could be forwarded to the ScanCase
        // and handled accordingly
        scanCase.setHardwareButtonToScanEnabled(true);
    }

    @Override
    public void didInitializeScanCase(final ScanCase scanCase) {
        // The scan case is ready to be used.
        // Let's setup a timeout. If the scan case is in idle for more than 30 secs,
        // probably the user is not using the phone, so better save some power and
        // switch the scanner off completely.
        // In order to do that, let's just set a timeout here, then didChangeState() will be called
        // and the reason will be ScanCaseListener.STATE_CHANGE_REASON_TIMEOUT
        scanCase.setTimeout(ScanCase.STATE_STANDBY, ScanCase.STATE_OFF, 30000);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initText.setVisibility(View.GONE);
                initializeButtons();
            }
        });

        if (scanCase.getState() == ScanCase.STATE_STANDBY) {
            onStandby();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeButtons() {
        scanButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        state = State.FORWARD;
                        scanCase.setScanCaseMode(ScanCase.MODE_FACE_FORWARD, new Runnable() {
                            @Override
                            public void run() {
                                scanCase.setState(ScanCase.STATE_ACTIVE);
                            }
                        });

                        return true;
                    case MotionEvent.ACTION_UP:
                        // Pause downward scanning when the button is released
                        scanCase.setState(ScanCase.STATE_STANDBY);
                        return true;
                }
                return false;
            }
        });
        scanButton.setVisibility(View.VISIBLE);

        matrixScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (state) {
                    case STANDBY:
                        state = State.DOWNWARD;
                        scanCase.removeTimeoutFromState(ScanCase.STATE_ACTIVE);
                        scanCase.setScanCaseMode(ScanCase.MODE_FACE_DOWNWARD, new Runnable() {
                            @Override
                            public void run() {
                                scanCase.setState(ScanCase.STATE_ACTIVE);
                            }
                        });
                        matrixScanButton.setText("Done");
                        scanButton.setVisibility(View.GONE);
                        break;
                    default:
                        state = State.STANDBY;
                        matrixScanButton.setText("MatrixScan");
                        scanButton.setVisibility(View.VISIBLE);
                        previewContainer.setAlpha(0f);
                        scanCase.setState(ScanCase.STATE_STANDBY);
                        break;
                }
            }
        });
        matrixScanButton.setVisibility(View.VISIBLE);
    }

    @Override
    public int didScan(ScanCase scanCase, final ScanCaseSession session) {
        final List<Barcode> newBarcodes = session.getNewlyRecognizedCodes();

        for (Barcode newBarcode : newBarcodes) {
            boolean isNew = true;
            for (Barcode barcode : uniqueBarcodes) {
                if (newBarcode.getData().equals(barcode.getData())
                        && newBarcode.getSymbology() == barcode.getSymbology()) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                uniqueBarcodes.add(newBarcode);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "";
                for (int i = 0; i < uniqueBarcodes.size(); i++) {
                    Barcode barcode = uniqueBarcodes.get(i);
                    String formattedBarcode = String.format(
                            "%s - %s", barcode.getSymbologyName(), barcode.getData());
                    if (i == 0) {
                        text = formattedBarcode;
                    } else {
                        text = String.format("%s\n%s", text, formattedBarcode);
                    }
                }
                barcodeLabel.setText(text);
            }
        });

        if (state == State.DOWNWARD) {
            return ScanCase.STATE_ACTIVE;
        } else {
            return ScanCase.STATE_STANDBY;
        }
    }

    @Override
    public int didProcess(byte[] imageBuffer, int width, int height, ScanCaseSession session) {
        // This is where codes could be rejected in the downward facing matrix scan mode. Since we
        // are interested in all codes we do not reject anything.
        // Have a look at the matrix scan sample for more details about matrix scan.

        // Always return active, the state returned by didScan will take precedence in this case.
        return ScanCase.STATE_ACTIVE;
    }

    @Override
    public void didChangeState(ScanCase scanCase, int state, int reason) {
        switch (state) {
            case ScanCase.STATE_STANDBY:
                onStandby();
                break;
            case ScanCase.STATE_ACTIVE:
                onActive();
                break;
            case ScanCase.STATE_OFF:
                onOff();
                break;
        }
    }

    private void onStandby() {
        this.state = State.STANDBY;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateLabel.setText(R.string.stand_by_state);
                previewContainer.setAlpha(0f);
                scanButton.setText("Scan");
                scanButton.setVisibility(View.VISIBLE);
                matrixScanButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onActive() {
        barcodeLabel.setText("");
        uniqueBarcodes.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateLabel.setText(R.string.active_state);
                if (state == State.DOWNWARD) {
                    previewContainer.setAlpha(1f);
                }
            }
        });
    }

    private void onOff() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateLabel.setText(R.string.off_state);
            }
        });
    }
}
