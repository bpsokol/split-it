package project.cs495.splitit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import project.cs495.splitit.models.Bill;

public class UserPaymentActivity extends Fragment {
    private DatabaseReference database;
    private RecyclerView recyclerView;
    private BillAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user_payment, container, false);
        super.onCreate(savedInstanceState);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.bill_list);
        final List billList = new ArrayList<>();
        adapter = new BillAdapter(billList,rootView.getContext());

        database = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("bills");

        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.insert(new Bill (
                        dataSnapshot.child("name").getValue(String.class)
                        ,dataSnapshot.child("email").getValue(String.class)
                        ,dataSnapshot.child("amount").getValue(String.class)));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove(new Bill (
                        dataSnapshot.child("name").getValue(String.class)
                        ,dataSnapshot.child("email").getValue(String.class)
                        ,dataSnapshot.child("amount").getValue(String.class)));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
        final FloatingActionButton addBillFab = ((ViewGroup)container.getParent()).findViewById(R.id.add_bill_button);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && addBillFab.getVisibility() == View.VISIBLE) {
                    addBillFab.hide();
                } else if (dy < 0 && addBillFab.getVisibility() != View.VISIBLE) {
                    addBillFab.show();
                }
            }
        });

        return rootView;
    }

}