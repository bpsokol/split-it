package project.cs495.splitit;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import project.cs495.splitit.models.Item;

public class ModifyItemFragment extends DialogFragment {
    private EditText itemDescription;
    private EditText itemPrice;

    public interface ModifyItemFragmentListener {
        void onDialogEditItem(DialogFragment dialog, String description, float price);
    }

    ModifyItemFragmentListener mListener;
    private String receiptId;
    private AlertDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String itemId = getArguments().getString("itemId");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = createItemView();
        System.out.println(itemId);
        // adapter = creatorAdapter(itemId);
        builder
                .setView(inflater.inflate(R.layout.edit_item_dialog, null))
                .setTitle(R.string.edit_item)
//                .setAdapter(adapter, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Item selectedItem = (Item) adapter.getItem(i);
//                        mListener.onDialogEditItem(ModifyItemFragment.this, selectedItem.getDescription(), selectedItem.getPrice());
//                    }
//                });
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(ModifyItemFragment.class.getSimpleName(), "Accepted");
                    mListener.onDialogEditItem(ModifyItemFragment.this, itemDescription.getText().toString(), Float.parseFloat(itemPrice.getText().toString()));
                }
            });
        dialog = builder.create();
        itemDescription = dialog.findViewById(R.id.edit_item_description);
        itemPrice = dialog.findViewById(R.id.edit_item_price);
        Utils.getDatabaseReference().child("items").child(itemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                if (itemDescription == null) {
                    itemDescription = dialog.findViewById(R.id.edit_item_description);
                }
                itemDescription.setText(item.getDescription());
                if (itemPrice == null) {
                    itemPrice = dialog.findViewById(R.id.edit_item_price);
                }
                itemPrice.setText(String.valueOf(item.getPrice()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return dialog;
    }

    private View createItemView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
        return inflater.inflate(R.layout.edit_item_dialog, null);
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
}