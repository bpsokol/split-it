package project.cs495.splitit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import project.cs495.splitit.models.Item;

public class AddItemFragment extends DialogFragment {
    private EditText itemDescription;
    private EditText itemPrice;

    public interface AddItemFragmentListener {
        void onDialogAddItem(DialogFragment dialog, String description, float price);
    }

    AddItemFragmentListener mListener;
    private AlertDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String receiptId = getArguments().getString("receiptId");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = createItemView();

        builder
                .setView(inflater.inflate(R.layout.edit_item_dialog, null))
                .setTitle(R.string.add_item)
                .setPositiveButton(R.string.add_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(ModifyItemFragment.class.getSimpleName(), "Accepted");
                        mListener.onDialogAddItem(AddItemFragment.this,
                                itemDescription.getText().toString(),
                                Float.parseFloat(itemPrice.getText().toString()));
                    }
                });
        dialog = builder.create();
        itemDescription = dialog.findViewById(R.id.edit_item_description);
        itemPrice = dialog.findViewById(R.id.edit_item_price);

        Utils.getDatabaseReference().child("receipts").child(receiptId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                //origPrice = item.getPrice();
                if (itemDescription == null) {
                    itemDescription = dialog.findViewById(R.id.edit_item_description);
                }
                itemDescription.setText("");
                if (itemPrice == null) {
                    itemPrice = dialog.findViewById(R.id.edit_item_price);
                }
                itemPrice.setText("");
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
            mListener = (AddItemFragmentListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AddItemFragmentListener");
        }
    }
}