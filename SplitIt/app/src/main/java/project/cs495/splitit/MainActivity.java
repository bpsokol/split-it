package project.cs495.splitit;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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

import org.jetbrains.annotations.NotNull;

import project.cs495.splitit.models.Bill;
import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;
import project.cs495.splitit.models.ReceiptBuilder;

public class MainActivity extends AppCompatActivity
    implements SelectGroupDialogFragment.SelectGroupDialogListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SCAN_RECEIPT_REQUEST = 201;
    private static final String SCANDIT_KEY = "1yazq+JRXyKsna5JAQq2XRjbK2pgpikQXXSW4RPftsM";
    private static final int CAMERA_PERMISSION_REQUEST = 7;
    public static final String EXTRA_RECEIPT_ID = "project.cs495.splitit.RECEIPT_ID";
    private TextView profileName;
    private FirebaseAuth auth;
    private int fabState = 0;
    private String selectedGroupIdForReceipt;
    private ImageButton fab_plus;
    private ImageButton fab_scan_receipt;
    private ImageButton fab_add_group;

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


        TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };

        fab_plus = (ImageButton) findViewById(R.id.plus_button);
        fab_scan_receipt = (ImageButton) findViewById(R.id.scan_receipt);
        fab_add_group = (ImageButton) findViewById(R.id.add_bill_button);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                //Called when the scroll state changes.
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //This method will be invoked when a new page becomes selected.
                fabState = position;
                animateFab(position);
            }
        });

        fab_scan_receipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabState == 0) {
                    boolean isPermitted = getCameraPermissions();
                    if (isPermitted) {
                        assignGroup();
                    }
                }
            }
        });

        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabState == 1) {
                    Intent createGroupIntent = new Intent(MainActivity.this, CreateGroupActivity.class);
                    MainActivity.this.startActivity(createGroupIntent);
                }
            }
        });

        final Context context = this;

        fab_add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View billPrompt = li.inflate(R.layout.add_bill_prompt,null);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setView(billPrompt);

                final EditText nameText = (EditText) billPrompt.findViewById(R.id.input_manager_name);
                final EditText emailText = (EditText) billPrompt.findViewById(R.id.input_manager_email);
                final EditText amountText = (EditText) billPrompt.findViewById(R.id.input_cost);

                builder
                        .setCancelable(false)
                        .setPositiveButton("Save",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newName = nameText.getText().toString();
                        String newEmail = emailText.getText().toString();
                        String newAmount = amountText.getText().toString();

                        if (isEmpty(newName)) {
                            displayMessage("Enter a name");
                            alertDialog.show();
                        }
                        else if (isEmpty(newEmail)) {
                            displayMessage("Enter an email address");
                            alertDialog.show();
                        }
                        else if (isEmpty(newAmount)) {
                            displayMessage("Enter an amount owed");
                            alertDialog.show();
                        }
                        else {
                            addBillToDatabase(newName, newEmail, "$" + newAmount);
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    private void addBillToDatabase (String name, String email, String amount) {
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("bills").push();
        Bill newBill = new Bill(name, email, amount);
        ref.setValue(newBill);
    }

    private void assignGroup() {
        DialogFragment dialog = new SelectGroupDialogFragment();
        dialog.show(getFragmentManager(), SelectGroupDialogFragment.class.getSimpleName());
    }

    @Override
    public void onDialogSelectGroup(DialogFragment dialog, String groupId) {
        selectedGroupIdForReceipt = groupId;
        scanReceipt();
    }

    /*@Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }*/

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
            String receiptId = parceScanResults(brScanResults, selectedGroupIdForReceipt);
            startActivity(buildReceiptViewIntent(receiptId));
        }
    }

    private String parceScanResults(ScanResults brScanResults, String groupId) {
        DatabaseReference database = Utils.getDatabaseReference();
        String receiptId = database.child("receipts").push().getKey();
        Receipt receipt = new ReceiptBuilder()
                .setGroupId(groupId)
                .setReceiptId(receiptId)
                .setCreator(auth.getCurrentUser().getUid())
                .setVendor(brScanResults.merchantName().value() == null ? "Unknown" : brScanResults.merchantName().value())
                .setDatePurchased(brScanResults.receiptDate().value())
                .setPrice(brScanResults.total().value())
                .setSubtotal(brScanResults.subtotal() != null ? brScanResults.subtotal().value(): 0)
                .setTax(brScanResults.taxes() != null ? brScanResults.taxes().value() : 0)
                .setItems(null)
                .createReceipt();
        for (Product product : brScanResults.products()) {
            String itemId = database.child("items").push().getKey();
            Item item = new Item(itemId, product.productNumber().value(), product.description().value(), product.totalPrice(), (int) product.quantity().value(), product.unitPrice().value());
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
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                assignGroup();
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

    private boolean getCameraPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
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
                case 2:
                    UserPaymentActivity tab3 = new UserPaymentActivity();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages. Must match number of items or app will crash on hitting null
            return 3;
        }
    }

    private void animateFab(int position) {
        switch (position) {
            case 0:
                fab_scan_receipt.setVisibility(View.VISIBLE);
                fab_plus.setVisibility(View.INVISIBLE);
                fab_add_group.setVisibility(View.INVISIBLE);
                break;
            case 1:
                fab_scan_receipt.setVisibility(View.INVISIBLE);
                fab_plus.setVisibility(View.VISIBLE);
                fab_add_group.setVisibility(View.INVISIBLE);
                break;
            case 2:
                fab_scan_receipt.setVisibility(View.INVISIBLE);
                fab_plus.setVisibility(View.INVISIBLE);
                fab_add_group.setVisibility(View.VISIBLE);
            default:
                fab_scan_receipt.setVisibility(View.INVISIBLE);
                fab_plus.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0)
            return true;
        else
            return false;
    }
}
