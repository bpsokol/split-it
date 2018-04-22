package project.cs495.splitit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.User;

public class ModifyItemFragment extends DialogFragment {
    public interface ModifyItemFragmentListener {
        void onDialogEditItem(DialogFragment dialog, String description, float price);
    }

    ModifyItemFragmentListener mListener;
    private String receiptId;
    private FirebaseListAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String itemId = getArguments().getString("itemId");
        System.out.println(itemId);
        adapter = creatorAdapter(itemId);
        builder.setTitle(R.string.edit_item)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Item selectedItem = (Item) adapter.getItem(i);
                        mListener.onDialogEditItem(ModifyItemFragment.this, selectedItem.getDescription(), selectedItem.getPrice());
                    }
                });

        return builder.create();
    }

    private FirebaseListAdapter creatorAdapter(String itemId) {
        Query query = FirebaseDatabase.getInstance().getReference().child("items").child(itemId).orderByChild(itemId).equalTo(true);
        FirebaseListOptions<Item> options = new FirebaseListOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .setLayout(R.layout.edit_item_dialog)
                .build();
        return new FirebaseListAdapter<Item>(options) {
            @Override
            protected void populateView(final View v, final Item model, int position) {
                ((EditText) v.findViewById(R.id.edit_item_description)).setText(model.getDescription());
                ((EditText) v.findViewById(R.id.edit_item_price)).setText(String.valueOf(model.getPrice()));
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener =(ModifyItemFragmentListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ModifyItemFragmentListener");
        }
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
}