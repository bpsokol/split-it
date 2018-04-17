package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.scandit.barcodepicker.ScanditLicense;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;


public class ManageReceiptActivity extends AppCompatActivity {
    private DatabaseReference database;
    private static ArrayList<String> receiptInfo = new ArrayList<String>();
    private static ArrayList<String> receiptIDArray = new ArrayList<String>();
    private String receiptDisplayText;
    private static int currReceiptIndex;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_manage_receipts);
        setTitle("Receipt Management");

        final ListView listv = (ListView)findViewById(R.id.receipt_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.receipt_list_item,R.id.txt,receiptInfo);
        listv.setAdapter(adapter);
        listv.setOnItemClickListener(new ItemList());
        adapter.notifyDataSetChanged();

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.child("receipts").getChildren()){
                    String vendor = childSnapshot.child("vendor").getValue(String.class);
                    String date = childSnapshot.child("datePurchased").getValue(String.class);
                    String id = childSnapshot.getKey();//childSnapshot.child("receiptId").getValue(String.class);
                    System.out.println(id);
                    receiptDisplayText = vendor + " | " + date;
                    receiptInfo.add(receiptDisplayText);
                    receiptIDArray.add(id);
                    listv.invalidateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        Button goBackButton = (Button) findViewById(R.id.go_back);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHomePage();
            }
        });
        Button deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteReceipt();
            }
        });
        Button viewButton = (Button) findViewById(R.id.view);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewReceipt();
            }
        });
    }

    public void viewReceipt(){
        SigninActivity.RECEIPT_ID = receiptIDArray.get(currReceiptIndex);
        startActivity(new Intent(ManageReceiptActivity.this, ReceiptViewActivity.class));
    }

    public void deleteReceipt(){
        String currID = receiptIDArray.get(currReceiptIndex);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("receipts").child(currID);
        dbRef.removeValue();
        receiptIDArray.remove(currReceiptIndex);
        receiptInfo.remove(currReceiptIndex);
        Toast.makeText(ManageReceiptActivity.this, "Removed receipt!", Toast.LENGTH_LONG).show();
    }
    class ItemList implements AdapterView.OnItemClickListener{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            ViewGroup vg = (ViewGroup)view;
            TextView tv = (TextView)vg.findViewById(R.id.txt);
            currReceiptIndex = position;
        }
    }
    private void openHomePage(){
        Intent homePageIntent = new Intent(ManageReceiptActivity.this, SigninActivity.class);
        startActivity(homePageIntent);
        finish();
    }

}
