package project.cs495.splitit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import project.cs495.splitit.models.User;

public class AssignUserDialogFragment extends DialogFragment {
    public interface AssignUserDialogListener {
        void onDialogSelectUser(DialogFragment dialog, String i);
    }
    AssignUserDialogListener mListener;
    private String receiptId;
    private FirebaseListAdapter adapter;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        receiptId = getArguments().getString("receiptId");
        String groupId = getArguments().getString("groupId");
        adapter = creatorAdapter(groupId);
        builder.setTitle(R.string.assign_to_user)
            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    User selectedUser = (User) adapter.getItem(i);
                    mListener.onDialogSelectUser(AssignUserDialogFragment.this, selectedUser.getUid());
                }
            });

        return builder.create();
    }

    private FirebaseListAdapter creatorAdapter(String groupId) {
        Query query = FirebaseDatabase.getInstance().getReference().child("users")
                .orderByChild("groups/" + groupId).equalTo(true);
        FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLayout(android.R.layout.select_dialog_item)
                .build();
        return new FirebaseListAdapter<User>(options) {
            @Override
            protected void populateView(final View v, final User model, int position) {
                ((TextView) v.findViewById(android.R.id.text1)).setText(model.getName());
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener =(AssignUserDialogListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AssignUserDialogListener");
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
