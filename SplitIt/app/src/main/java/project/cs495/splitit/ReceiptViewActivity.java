package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Locale;

import project.cs495.splitit.models.Item;

public class ReceiptViewActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private RecyclerView itemRV;
    private FirebaseRecyclerAdapter adapter;
    private String receiptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.receipt);

        Intent intent = getIntent();
        receiptId = intent.getStringExtra(SigninActivity.EXTRA_RECEIPT_ID);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //Query query = mDatabaseReference.child("items").limitToFirst(5);
        Query query = mDatabaseReference.child(getString(R.string.items)).orderByChild(getString(R.string.receipt_ids_path)+receiptId).equalTo(true);

        itemRV = (RecyclerView) findViewById(R.id.item_rv);
        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Item, ItemHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemHolder holder, int position, @NonNull Item model) {
                holder.bindData(model);
            }

            @Override
            public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_view, parent, false);

                return new ItemHolder(view);
            }
        };
        itemRV.setAdapter(adapter);
        itemRV.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        private TextView itemCode;
        private TextView itemDescription;
        private TextView itemPrice;

        ItemHolder(View view) {
            super(view);
            itemCode = (TextView) view.findViewById(R.id.item_code);
            itemDescription = (TextView) view.findViewById(R.id.item_description);
            itemPrice = (TextView) view.findViewById(R.id.item_price);
        }

        public void bindData(final Item item) {
            itemCode.setText(item.getCode());
            itemDescription.setText(item.getDescription());
            itemPrice.setText(String.format(Locale.US, "%.2f", item.getPrice()));
        }
    }
}
