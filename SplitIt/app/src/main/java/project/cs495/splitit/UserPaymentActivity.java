package project.cs495.splitit;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class UserPaymentActivity extends Fragment {
    private DatabaseReference database;
    private ListView listView;
    private BillAdapter adapter;

    protected View onCreate(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user_payment, container, false);
        super.onCreate(savedInstanceState);

        listView = (ListView) rootView.findViewById(R.id.bill_listview);
        final ArrayList<Bill> billList = new ArrayList<>();
        adapter = new BillAdapter(getView().getContext(),billList);
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

        return rootView;
    }
}