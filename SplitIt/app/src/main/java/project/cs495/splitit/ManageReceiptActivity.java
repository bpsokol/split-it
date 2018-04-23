package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import project.cs495.splitit.models.Receipt;

public class ManageReceiptActivity extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "ManageReceiptActivity";
    private DatabaseReference database;
    private static int currReceiptIndex = 0;
    private FirebaseRecyclerAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_manage_receipts, container, false);
        super.onCreate(savedInstanceState);

        final RecyclerView receiptRV = rootView.findViewById(R.id.receipt_list);
        database = Utils.getDatabaseReference();
        Query query = database.child("receipts").orderByChild("creator").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions<Receipt> options = new FirebaseRecyclerOptions.Builder<Receipt>()
                .setQuery(query, Receipt.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Receipt, ReceiptHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReceiptHolder holder, int position, @NonNull Receipt model) {
                holder.bindData(model);
            }

            @Override
            public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.receipt_list_item, parent, false);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        receiptRV.findViewHolderForAdapterPosition(currReceiptIndex).itemView.setSelected(false);
                        currReceiptIndex = receiptRV.getChildAdapterPosition(view);
                        view.setSelected(true);
                        viewReceipt();
                    }
                });

                final ImageButton menu_options = view.findViewById(R.id.receipt_list_options);

                // Use temporary variable to capture value of View
                final View temp = view;
                menu_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        receiptRV.findViewHolderForAdapterPosition(currReceiptIndex).itemView.setSelected(false);
                        currReceiptIndex = receiptRV.getChildAdapterPosition(temp);
                        view.setSelected(true);
                        PopupMenu popup = new PopupMenu(getView().getContext(), view);
                        popup.setOnMenuItemClickListener(ManageReceiptActivity.this);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.manage_receipt_menu, popup.getMenu());
                        popup.show();
                    }
                });

                return new ReceiptHolder(view);
            }
        };
        receiptRV.setAdapter(adapter);
        receiptRV.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void viewReceipt(){
        Receipt receipt = (Receipt) adapter.getItem(currReceiptIndex);
        Intent intent = new Intent(getView().getContext(), ReceiptViewActivity.class);
        intent.putExtra(MainActivity.EXTRA_RECEIPT_ID, receipt.getReceiptId());
        startActivity(intent);
    }

    public void archiveReceipt(){
        Receipt receipt = (Receipt) adapter.getItem(currReceiptIndex);
        moveToArchive(receipt);
    }

    private void moveToArchive(final Receipt receipt) {
        final DatabaseReference archivedRef = FirebaseDatabase.getInstance().getReference("archivedReceipts").child(receipt.getReceiptId());
        final DatabaseReference oldRef = FirebaseDatabase.getInstance().getReference("receipts").child(receipt.getReceiptId());
        archivedRef.setValue(receipt, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.w(TAG, "Move to Archive Error", firebaseError.toException());
                } else {
                    oldRef.removeValue();
                    Toast.makeText(getView().getContext(), "Archived receipt!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class ReceiptHolder extends RecyclerView.ViewHolder {
        private TextView vendorName;
        private TextView date;
        private TextView totalPrice;

        ReceiptHolder(View view) {
            super(view);
            vendorName = (TextView) view.findViewById(R.id.vendor);
            date = (TextView) view.findViewById(R.id.date);
            totalPrice = (TextView) view.findViewById(R.id.total_price);
        }

        public void bindData(final Receipt receipt) {
            vendorName.setText(String.format("%s", receipt.getVendor()));
            date.setText(String.format("%s", receipt.getDatePurchased()));
            Currency currency = Currency.getInstance(Locale.getDefault());
            totalPrice.setText(String.format("%s%s", currency.getSymbol(), String.format(Locale.getDefault(), "%.2f", receipt.getPrice())));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_menu_archive_receipt:
                archiveReceipt();
                return true;
            default:
                return false;
        }
    }
}
