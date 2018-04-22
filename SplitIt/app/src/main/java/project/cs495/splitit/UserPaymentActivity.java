package project.cs495.splitit;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class UserPaymentActivity extends AppCompatActivity {

    private DatabaseReference database;
    private ListView listView;
    private BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_payment);
        setTitle("Bills");

        listView = (ListView) findViewById(R.id.bill_listview);
        final ArrayList<Bill> billList = new ArrayList<>();
        adapter = new BillAdapter(this,billList);
        listView.setAdapter(adapter);

        database = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("bills");

        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                billList.add(new Bill(dataSnapshot.child("name").getValue(String.class)
                        ,dataSnapshot.child("email").getValue(String.class)
                        ,dataSnapshot.child("amount").getValue(String.class)));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                billList.remove(dataSnapshot.getValue(Bill.class));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button addBillButton = (Button)findViewById(R.id.add_bill);
        final Context context = this;

        addBillButton.setOnClickListener(new View.OnClickListener() {
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

    private boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0)
            return true;
        else
            return false;
    }

    private void displayMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}