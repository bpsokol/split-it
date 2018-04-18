package project.cs495.splitit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
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
    public static String RECEIPT_ID;
    private TextView profileName;
    private FirebaseAuth auth;
    private int fabState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanditLicense.setAppKey(SCANDIT_KEY);
        auth = FirebaseAuth.getInstance();
        if (!isUserLogin()) {
            signOut();
        }
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.home));

        //Tabbed activity implementation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        ImageButton btn = (ImageButton) findViewById(R.id.scan_receipt);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraPermissions();
            }
        });


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                //Called when the scroll state changes.
            }

            @Override
            public void onPageScrolled(int position,
                                       float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //This method will be invoked when a new page becomes selected.
                fabState = position;
            }
        });

        ImageButton plus = (ImageButton) findViewById(R.id.plus_button);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabState == 0) {
                    //write actions related to page one
                }
                else if (fabState == 1) {
                    Intent createGroupIntent = new Intent(MainActivity.this, CreateGroupActivity.class);
                    MainActivity.this.startActivity(createGroupIntent);
                }
                else {

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            RECEIPT_ID = receiptId;
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
        Intent signOutIntent = new Intent(this, SigninActivity.class);
        startActivity(signOutIntent);
        finish();
    }
    private void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void accountSettings() {
        Intent accountSetingsIntent = new Intent(MainActivity.this,ReauthenticateActivity.class);
        startActivity(accountSetingsIntent);
    }


    //Tabbed activity implementation
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the account_settings_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            accountSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //return current tab
            switch (position) {
                case 0:
                    ManageReceiptActivity tab1 = new ManageReceiptActivity();
                    return tab1;
                case 1:
                    GroupManageActivity tab2 = new GroupManageActivity();
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages. Must match number of items or app will crash on hitting null
            return 2;
        }
    }
}
