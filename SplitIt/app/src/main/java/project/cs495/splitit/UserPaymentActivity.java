package project.cs495.splitit;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class UserPaymentActivity extends AppCompatActivity {

    public class Bill {
        private String name = "";
        private String email = "";
        private String amount = "";

        public Bill(String name, String email, String amount) {
            this.name = name;
            this.email = email;
            this.amount = amount;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getAmount() {
            return amount;
        }
    }

    public class BillAdapter extends ArrayAdapter<Bill> {

        public BillAdapter( Context context, ArrayList<Bill> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Bill bill = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_items, parent, false);
            }
            TextView name = (TextView) convertView.findViewById(R.id.manager_name);
            TextView email = (TextView) convertView.findViewById(R.id.manager_email);
            TextView amount = (TextView) convertView.findViewById(R.id.amount_owed);

            name.setText(bill.getName());
            email.setText(bill.getEmail());
            amount.setText(bill.getAmount());

            return convertView;
        }
    }

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
                billList.add(new Bill(dataSnapshot.child("managerName").getValue(String.class)
                        ,dataSnapshot.child("managerEmail").getValue(String.class)
                        ,dataSnapshot.child("cost").getValue(String.class)));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                billList.remove(dataSnapshot.getValue(String.class));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}