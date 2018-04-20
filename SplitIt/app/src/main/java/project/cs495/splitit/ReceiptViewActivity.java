package project.cs495.splitit;


import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Currency;
import java.util.Locale;

import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;
import project.cs495.splitit.models.User;

public class ReceiptViewActivity extends AppCompatActivity
        implements AssignUserDialogFragment.AssignUserDialogListener{
    private static final String TAG = "ReceiptVeiwActivity";
    private DatabaseReference mDatabaseReference;
    private RecyclerView itemRV;
    private FirebaseRecyclerAdapter adapter;
    private String receiptId;
    private Receipt receipt;
    private String currItemId;
    private TextView receiptPriceView;
    private TextView receiptCreatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_receipt_view);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.receipt);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Intent intent = getIntent();
            receiptId = intent.getStringExtra(MainActivity.EXTRA_RECEIPT_ID);

        mDatabaseReference = Utils.getDatabaseReference();
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
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = itemRV.getChildAdapterPosition(view);
                        Item item = (Item) adapter.getItem(position);
                        currItemId = item.getItemId();
                        Log.d(TAG, "Accessing item with description " + item.getDescription());
                        if (receipt.getCreator().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            showDialog();
                        } else {
                            assignToSelf(item);
                        }
                    }
                });
                return new ItemHolder(view);
            }
        };
        itemRV.setAdapter(adapter);
        itemRV.setLayoutManager(new LinearLayoutManager(this));

        receiptPriceView = findViewById(R.id.receipt_price);
        receiptCreatorView = findViewById(R.id.receipt_creator);
        Utils.getDatabaseReference().child(getString(R.string.receipts_path)+receiptId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                receipt = dataSnapshot.getValue(Receipt.class);
                Currency currency = Currency.getInstance(Locale.getDefault());
                receiptPriceView.setText(String.format("%s: %s%s", getString(R.string.price), currency.getSymbol(), String.format(Locale.getDefault(), "%.2f", receipt.getPrice())));
                setCreatorNameDisplay();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadReceipt:onCancelled", databaseError.toException());
            }
        });
    }

    private ValueEventListener setCreatorNameDisplay() {
        return mDatabaseReference.child("users").child(receipt.getCreator()).addValueEventListener(new CreatorValueEventListener());
    }

    private void assignToSelf(Item item) {
        item.setAssignedUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        item.commitToDB(mDatabaseReference);
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

    @Override
    public void onDialogSelectUser(DialogFragment dialog, String userId) {
        Log.d(TAG, "selected " + userId);
        mDatabaseReference.child("items").child(currItemId).child("assignedUser").setValue(userId, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(ReceiptViewActivity.this, "User assigned", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDialog() {
        DialogFragment dialog = new AssignUserDialogFragment();
        Bundle args = new Bundle();
        args.putString("receiptId", receiptId);
        args.putString("groupId", receipt.getGroupId());
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "AssignUserFragment");
    }

    private static String TitleCaseString(String s) {
        StringBuilder res = new StringBuilder();
        String[] words = s.split(" ");
        for(String word: words) {
            res.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
        }
        return res.toString();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        //private TextView itemCode;
        private TextView itemDescription;
        private TextView itemPrice;
        private TextView itemAssignee;

        ItemHolder(View view) {
            super(view);
            itemDescription = (TextView) view.findViewById(R.id.item_description);
            itemPrice = (TextView) view.findViewById(R.id.item_price);
            itemAssignee = (TextView) view.findViewById(R.id.item_assignee);
        }

        public void bindData(final Item item) {
            //Convert item descriptions to title case (first letter of each word is capitalized)
            String description = item.getDescription();
            String formattedDescription = TitleCaseString(description);
            itemDescription.setText(formattedDescription);

            //uses the default locale of the user
            Currency currency = Currency.getInstance(Locale.getDefault());
            itemPrice.setText(String.format("%s%s", currency.getSymbol(), String.format(Locale.getDefault(), "%.2f", item.getPrice())));

            //Displyas assigned user
            if (item.getAssignedUser() != null) {
                mDatabaseReference.child("users").child(item.getAssignedUser()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        itemAssignee.setText(String.format("Assigned to: %s", user.getName()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                itemAssignee.setText("No User Assigned");
            }
        }
    }

    private class CreatorValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            receiptCreatorView.setText(String.format("%s: %s", getString(R.string.receipt_creator), user.getName()));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
