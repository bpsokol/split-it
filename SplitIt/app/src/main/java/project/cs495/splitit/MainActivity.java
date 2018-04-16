package project.cs495.splitit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.microblink.EdgeDetectionConfiguration;
import com.microblink.FrameCharacteristics;
import com.microblink.IntentUtils;
import com.microblink.Media;
import com.microblink.Product;
import com.microblink.Retailer;
import com.microblink.ScanOptions;
import com.microblink.ScanResults;
import com.scandit.barcodepicker.ScanditLicense;

import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SCAN_RECEIPT_REQUEST = 201;
    private static final String SCANDIT_KEY = "1yazq+JRXyKsna5JAQq2XRjbK2pgpikQXXSW4RPftsM";
    private static final int CAMERA_PERMISSION_REQUEST = 7;
    public static final String EXTRA_RECEIPT_ID = "project.cs495.splitit.RECEIPT_ID";
    private TextView profileName;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanditLicense.setAppKey(SCANDIT_KEY);
        auth = FirebaseAuth.getInstance();
        if (!isUserLogin()) {
            signOut();
        }
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.profile_title));
        profileName = (TextView) findViewById(R.id.user_name);
        displayLoginUserProfileName();

        //Scan Receipt
        Button scanReceiptButton = (Button) findViewById(R.id.scan_receipt);
        scanReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraPermissions();
            }
        });

        //Account Settings
        Button accountSettingsButton = (Button) findViewById(R.id.account_settings);
        accountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ReauthenticateActivity.class));
            }
        });

        //Logout
        Button logoutButton = (Button) findViewById(R.id.sign_out);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    signOut();
                                } else {
                                    displayMessage(getString(R.string.sign_out_error));
                                }
                            }
                        });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLoginUserProfileName();
        if(!isUserLogin()) {
            signOut();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == SCAN_RECEIPT_REQUEST && resultCode == Activity.RESULT_OK ) {
            ScanResults brScanResults = data.getParcelableExtra( IntentUtils.DATA_EXTRA );
            Media media = data.getParcelableExtra( IntentUtils.MEDIA_EXTRA );
            String receiptId = parceScanResults(brScanResults);
            startActivity(buildReceiptViewIntent(receiptId));
        }
    }

    private String parceScanResults(ScanResults brScanResults) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String receiptId = database.child("receipts").push().getKey();
        Receipt receipt = new Receipt(receiptId, brScanResults.merchantName().value(), brScanResults.receiptDate().value(), null);
        for (Product product : brScanResults.products()) {
            String itemId = database.child("items").push().getKey();
            Item item = new Item(itemId, product.productNumber().value(), product.description().value(), product.totalPrice(),(int) product.quantity().value(), product.unitPrice().value());
            item.addReceiptId(receiptId);
            item.commitToDB(database);
            receipt.addItem(item.getItemId());
        }
        receipt.commitToDB(database);
        return receiptId;
    }

    private Intent buildReceiptViewIntent(String receiptId) {
        Intent intent = new Intent(this, ReceiptViewActivity.class);
        intent.putExtra(EXTRA_RECEIPT_ID, receiptId);
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanReceipt();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void scanReceipt() {
        ScanOptions scanOptions = ScanOptions.newBuilder()
                .retailer( Retailer.UNKNOWN )
                .frameCharacteristics( FrameCharacteristics.newBuilder()
                        .storeFrames( true )
                        .compressionQuality( 100 )
                        .externalStorage( false )
                        .build() )
                .edgeDetectionConfiguration( EdgeDetectionConfiguration.defaults() )
                .scanBarcode( true )
                .logoDetection( true )
                .build();

        Intent intent = IntentUtils.cameraScan( this, scanOptions );

        startActivityForResult( intent, SCAN_RECEIPT_REQUEST );
    }

    private boolean isUserLogin(){
         return auth.getCurrentUser() != null;
    }
    private void signOut(){
        Intent signOutIntent = new Intent(this, SignInActivity.class);
        startActivity(signOutIntent);
        finish();
    }
    private void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void displayLoginUserProfileName(){
        FirebaseUser mUser = auth.getCurrentUser();
        if(mUser != null){
            profileName.setText(TextUtils.isEmpty(mUser.getDisplayName())? "No name found" : "Welcome " + mUser.getDisplayName());
        }
    }

    private void getCameraPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[] {android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            } else {
                scanReceipt();
            }
        }
        else {
            scanReceipt();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
