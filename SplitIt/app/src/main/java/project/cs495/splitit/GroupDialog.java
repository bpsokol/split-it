package project.cs495.splitit;

import android.app.Dialog;
import android.content.Context;
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
                        if (auth.getCurrentUser().getUid().equals(removedGroup.getManagerUID()))
                            database.child("groups").child(removedGroup.getGroupId()).removeValue();
                        else
                            Toast.makeText(getContext(),R.string.not_manager,Toast.LENGTH_LONG).show();
                        dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }



}
