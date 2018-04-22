package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        implements AssignUserDialogFragment.AssignUserDialogListener, ModifyItemFragment.ModifyItemFragmentListener, PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "ReceiptViewActivity";
    private DatabaseReference mDatabaseReference;
    private RecyclerView itemRV;
    private FirebaseRecyclerAdapter adapter;
    private String receiptId;
    private Receipt receipt;
    private String currItemId;
    private TextView receiptPriceView;
    private TextView receiptCreatorView;
    private static int currItemIndex;

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
            protected void onBindViewHolder(@NonNull ItemHolder holder, int currItemIndex, @NonNull Item model) {
                holder.bindData(model);
            }

            @Override
            public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_view, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currItemIndex = itemRV.getChildAdapterPosition(view);
                        Item item = (Item) adapter.getItem(currItemIndex);
                        currItemId = item.getItemId();
                        Log.d(TAG, "Accessing item with description " + item.getDescription());
                        if (receipt.getCreator().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            showDialogAssignUser();
                        } else {
                            assignToSelf(item);
                        }
                    }
                });


                final ImageButton menu_options = view.findViewById(R.id.item_view_options);

                // Use temporary variable to capture value of View
                final View temp = view;
                menu_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemRV.findViewHolderForAdapterPosition(currItemIndex).itemView.setSelected(false);
                        currItemIndex = itemRV.getChildAdapterPosition(temp);
                        Item item = (Item) adapter.getItem(currItemIndex);
                        currItemId = item.getItemId();
                        view.setSelected(true);
                        PopupMenu popup = new PopupMenu(view.getContext(), view);
                        popup.setOnMenuItemClickListener(ReceiptViewActivity.this);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.receipt_menu_options, popup.getMenu());
                        popup.show();
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

                if (receipt == null)
                    return;

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

    @Override
    public void onDialogEditItem(DialogFragment dialog, String description, float price) {
        Log.d(TAG, "edited " + description + price);
        mDatabaseReference.child("items").child(currItemId).child("description").setValue(description, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(ReceiptViewActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDatabaseReference.child("items").child(currItemId).child("price").setValue(price, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(ReceiptViewActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDialogAssignUser() {
        DialogFragment dialog = new AssignUserDialogFragment();
        Bundle args = new Bundle();
        args.putString("receiptId", receiptId);
        args.putString("groupId", receipt.getGroupId());
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "AssignUserFragment");
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

            //Displays assigned user
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

    private void modifyItem() {
        //TODO: create a dialog with description and price and allow user to input new value or convert textview to edittext and allow user to edit on the same screen inside the item
        DialogFragment dialog = new ModifyItemFragment();
        Bundle args = new Bundle();
        args.putString("itemId", currItemId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "ModifyItemFragment");
    }

    private void deleteItem() {
        final Item item = (Item) adapter.getItem(currItemIndex);
        final DatabaseReference removeItemFromItemList = FirebaseDatabase.getInstance().getReference("items").child(item.getItemId());
        final DatabaseReference removeItemFromReceipt = FirebaseDatabase.getInstance().getReference("receipts").child(receipt.getReceiptId()).child("items").child(item.getItemId());

        removeItemFromItemList.removeValue();
        removeItemFromReceipt.removeValue();

        float newPrice = receipt.getPrice() - item.getPrice();
        //receipt.setPrice(newPrice);

        mDatabaseReference.child("receipts").child(receiptId).child("price").setValue(newPrice, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(ReceiptViewActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Toast.makeText(ReceiptViewActivity.this, item.getDescription() + " deleted", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_modify_item:
                modifyItem();
                return true;
            case R.id.menu_delete_item:
                deleteItem();
                return true;
            default:
                return false;
        }
    }
}
