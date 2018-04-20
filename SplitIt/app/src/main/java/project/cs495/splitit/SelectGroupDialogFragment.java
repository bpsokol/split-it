package project.cs495.splitit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import project.cs495.splitit.models.Group;

public class SelectGroupDialogFragment extends DialogFragment {
    private FirebaseListAdapter<Group> adapter;

    public interface SelectGroupDialogListener {
        void onDialogSelectGroup(DialogFragment dialog, String groupId);
        //void onDialogPositiveClick(DialogFragment dialog);
        //void onDialogNegativeClick(DialogFragment dialog);
    }
    SelectGroupDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Query query = Utils.getDatabaseReference().child("groups")
                .orderByChild("memberID/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("test", "check here for snapshot");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseListOptions<Group> options = new FirebaseListOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .setLayout(android.R.layout.select_dialog_item)
                .build();
        adapter = new FirebaseListAdapter<Group>(options) {
            @Override
            protected void populateView(View v, Group model, int position) {
                ((TextView) v.findViewById(android.R.id.text1)).setText(model.getGroupName());
            }
        };
        builder.setTitle("Select Group")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String groupId = adapter.getItem(i).getGroupId();
                        mListener.onDialogSelectGroup(SelectGroupDialogFragment.this, groupId);
                    }
                });

                //No need for these buttons currently (saved for possible future use)
                /*.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDialogNegativeClick(SelectGroupDialogFragment.this);
                    }
                });
                .setPositiveButton("Add Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDialogPositiveClick(SelectGroupDialogFragment.this);
                    }
                })*/
                return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectGroupDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectGroupDialogListener");
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
