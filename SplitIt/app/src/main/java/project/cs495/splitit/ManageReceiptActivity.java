package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.Receipt;

public class ManageReceiptActivity extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "ManageReceiptActivity";
    private DatabaseReference database;
    private static int currReceiptIndex;
    private ReceiptAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_manage_receipts, container, false);
        super.onCreate(savedInstanceState);

        final RecyclerView receiptRV = rootView.findViewById(R.id.receipt_list);
        database = Utils.getDatabaseReference();
        createAdapter(receiptRV);
        receiptRV.setAdapter(adapter);
        receiptRV.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        final FloatingActionButton add_receipt_fab = (FloatingActionButton) ((ViewGroup) container.getParent()).findViewById(R.id.scan_receipt);
        receiptRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && add_receipt_fab.getVisibility() == View.VISIBLE) {
                    add_receipt_fab.hide();
                } else if (dy < 0 && add_receipt_fab.getVisibility() != View.VISIBLE) {
                    add_receipt_fab.show();
                }

            }
        });
        return rootView;
    }

    private void createAdapter(final RecyclerView receiptRV) {
        final Map<String, Query> receiptQueries = new HashMap<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new ReceiptAdapter(receiptRV);
        Query groupQuery = database.child("groups").orderByChild("memberID/"+currentUserId).equalTo(true);
        final ChildEventListener receiptListener  = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Receipt receipt = dataSnapshot.getValue(Receipt.class);
                adapter.add(receipt);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Receipt receipt = dataSnapshot.getValue(Receipt.class);
                adapter.change(receipt);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Receipt receipt = dataSnapshot.getValue(Receipt.class);
                adapter.remove(receipt);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Receipt child listener failure", databaseError.toException());
            }
        };
        ChildEventListener groupChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Group group = dataSnapshot.getValue(Group.class);
                Query receiptQuery = database.child("receipts").orderByChild("groupId").equalTo(group.getGroupId());
                receiptQuery.addChildEventListener(receiptListener);
                receiptQueries.put(group.getGroupId(), receiptQuery);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                Query receiptQuery = receiptQueries.get(group.getGroupId());
                receiptQuery.removeEventListener(receiptListener);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        groupQuery.addChildEventListener(groupChildListener);
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

    private class ReceiptAdapter extends RecyclerView.Adapter<ReceiptHolder>{
        private final RecyclerView receiptRV;
        private List<Receipt> dataSet;

        public ReceiptAdapter(RecyclerView receiptRV) {
            this.receiptRV = receiptRV;
            dataSet = new ArrayList<>();
        }

        @Override
        public void onBindViewHolder(@NonNull ReceiptHolder holder, int position) {
            Receipt receipt = dataSet.get(position);
            holder.bindData(receipt);
        }

        @Override
        public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receipt_list_item, parent, false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public void add(Receipt receipt) {
            dataSet.add(receipt);
            notifyItemInserted(getItemCount()-1);
        }
        public void remove(Receipt receipt) {
            for (int i = 0; i < getItemCount(); i++) {
                if (dataSet.get(i).getReceiptId().equals(receipt.getReceiptId())) {
                    dataSet.remove(i);
                    notifyItemRemoved(i);
                    return;
                }
            }
        }
        public void change(Receipt changedReceipt) {
            for (Receipt receipt : dataSet) {
                if (receipt.getReceiptId().equals(changedReceipt.getReceiptId())) {
                    int position = dataSet.indexOf(receipt);
                    dataSet.set(position, changedReceipt);
                    notifyItemChanged(position);
                    return;
                }
            }
        }

        public Object getItem(int position) {
            return dataSet.get(position);
        }
    }
}
