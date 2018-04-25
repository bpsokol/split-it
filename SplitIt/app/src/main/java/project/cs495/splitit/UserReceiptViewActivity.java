package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;
import project.cs495.splitit.models.User;

public class UserReceiptViewActivity extends AppCompatActivity{
    private UserReceiptAdapter adapter;
    private DatabaseReference receiptReference;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String receiptId = intent.getStringExtra(MainActivity.EXTRA_RECEIPT_ID);
        setContentView(R.layout.activity_user_receipt_view);
        setupToolbar();
        initializeRecyclerView();
        populateRecyclerView(receiptId);

        final TextView receiptPriceView = findViewById(R.id.receipt_price);
        final TextView receiptCreator = findViewById(R.id.receipt_creator);
        receiptReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Receipt receipt = dataSnapshot.getValue(Receipt.class);

                if (receipt == null)
                    return;

                Currency currency = Currency.getInstance(Locale.getDefault());
                receiptPriceView.setText(String.format("%s: %s%s", getString(R.string.price), currency.getSymbol(), String.format(Locale.getDefault(), "%.2f", receipt.getPrice())));
                Utils.getDatabaseReference().child("users").child(receipt.getCreator()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        receiptCreator.setText(String.format("%s: %s", "Receipt Creator", user.getName()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initializeRecyclerView() {
        adapter = new UserReceiptAdapter();
        recyclerView = (RecyclerView) findViewById(R.id.item_rv);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateRecyclerView(String receiptId) {

        receiptReference = Utils.getDatabaseReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("receipts").child(receiptId);
        receiptReference.child("items").orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String itemKey = dataSnapshot.getKey();
                addItemToAdapter(itemKey);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addItemToAdapter(String itemKey) {
        Utils.getDatabaseReference().child("items").child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.add(dataSnapshot.getValue(Item.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Receipt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private class UserReceiptAdapter extends RecyclerView.Adapter<ReceiptViewActivity.ItemHolder> {
        private List<Item> items;
        UserReceiptAdapter() {
            items = new ArrayList<>();
        }
        @Override
        public ReceiptViewActivity.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            return new ReceiptViewActivity.ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ReceiptViewActivity.ItemHolder holder, int position) {
            Item item = items.get(position);
            holder.bindData(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void add(Item item) {
            items.add(item);
            notifyItemInserted(getItemCount()-1);
        }
    }
}
