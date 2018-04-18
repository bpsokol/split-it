package project.cs495.splitit;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class ManageReceiptActivity extends Fragment {
    private DatabaseReference database;
    private static ArrayList<String> receiptInfo = new ArrayList<String>();
    private static ArrayList<String> receiptIDArray = new ArrayList<String>();
    private String receiptDisplayText;
    private static int currReceiptIndex;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_manage_receipts, container, false);
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();

        final ListView listv = (ListView)rootView.findViewById(R.id.receipt_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.receipt_list_item,R.id.txt,receiptInfo);
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

        Button deleteButton = (Button) rootView.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteReceipt();
            }
        });
        Button viewButton = (Button) rootView.findViewById(R.id.view);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewReceipt();
            }
        });
        return rootView;
    }

    public void viewReceipt(){
        MainActivity.RECEIPT_ID = receiptIDArray.get(currReceiptIndex);
        startActivity(new Intent(getView().getContext(), ReceiptViewActivity.class));
    }

    public void deleteReceipt(){
        String currID = receiptIDArray.get(currReceiptIndex);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("receipts").child(currID);
        dbRef.removeValue();
        receiptIDArray.remove(currReceiptIndex);
        receiptInfo.remove(currReceiptIndex);
        Toast.makeText(getView().getContext(), "Removed receipt!", Toast.LENGTH_LONG).show();
    }
    class ItemList implements AdapterView.OnItemClickListener{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            ViewGroup vg = (ViewGroup)view;
            TextView tv = (TextView)vg.findViewById(R.id.txt);
            currReceiptIndex = position;
        }
    }
    private void openHomePage(){
        Intent homePageIntent = new Intent(getView().getContext(), MainActivity.class);
        startActivity(homePageIntent);
        getActivity().finish();
    }

}