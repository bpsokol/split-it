package project.cs495.splitit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import project.cs495.splitit.models.Group;

public class GroupDialog extends Dialog {
    private Button dismiss;
    private Button modify;
    private FirebaseAuth auth;
    private DatabaseReference database;
    public static final String EXTRA_GROUP_ID = "project.cs495.splitit.GROUP_ID";

    public GroupDialog(Context context, Group group) {
        super(context);
        setContentView(R.layout.group_dialog);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        dismiss = (Button)findViewById(R.id.dismiss);
        final Group removedGroup = (Group) group;
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dismiss:
                        if (auth.getCurrentUser().getUid().equals(removedGroup.getManagerUID())) {
                            database.child("groups").child(removedGroup.getGroupId()).removeValue();
                            database.child("users").child(auth.getCurrentUser().getUid()).child("groups").child(removedGroup.getGroupId()).removeValue();
                            database.child("users").child(auth.getCurrentUser().getUid()).child("groupsOwned").child(removedGroup.getGroupId()).removeValue();
                            dismiss();
                        }
                        else
                            Toast.makeText(getContext(),R.string.not_manager,Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        });
        final Group modifyGroup = (Group)group;
        modify = (Button)findViewById(R.id.modify);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupViewIntent = new Intent(getContext(),GroupViewActivity.class);
                getContext().startActivity(groupViewIntent);
                Intent intent = buildGroupViewIntent(modifyGroup.getGroupId());
                getContext().startActivity(intent);
                dismiss();
            }
        });

    }
    private Intent buildGroupViewIntent(String groupId) {
        Intent intent = new Intent(getContext(),GroupViewActivity.class);
        intent.putExtra(EXTRA_GROUP_ID,groupId);
        return intent;
    }

}
