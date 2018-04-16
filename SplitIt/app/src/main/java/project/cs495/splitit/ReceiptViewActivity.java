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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Currency;
import java.util.Locale;

import project.cs495.splitit.models.Item;

public class ReceiptViewActivity extends AppCompatActivity
        implements AssignUserDialogFragment.AssignUserDialogListener{
    private static final String TAG = "ReceiptVeiwActivity";
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
        receiptId = intent.getStringExtra(MainActivity.EXTRA_RECEIPT_ID);
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
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = itemRV.getChildAdapterPosition(view);
                        Item item = (Item) adapter.getItem(position);
                        Log.d(TAG, "Accessing item with description " + item.getDescription());
                        showDialog();
                    }
                });
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.d(TAG, "user assigned");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.d(TAG, "dialog canceled");
    }

    @Override
    public void onDialogSelectUser(DialogFragment dialog, int i) {
        Log.d(TAG, "selected " + getResources().getStringArray(R.array.users)[i]);
    }

    public void showDialog() {
        DialogFragment dialog = new AssignUserDialogFragment();
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
            itemPrice.setText(new StringBuilder().append(currency.getSymbol()).append(String.format(Locale.getDefault(), "%.2f", item.getPrice())).toString());

            //temporary until group members are added to the db
            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            itemAssignee.setText(new StringBuilder().append("Assigned to: ").append(mUser.getDisplayName()).toString());
        }
    }
}
