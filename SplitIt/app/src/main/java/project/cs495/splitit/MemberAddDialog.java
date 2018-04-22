package project.cs495.splitit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.User;

public class MemberAddDialog extends Dialog{
    private static final String TAG = "GroupViewActivity";
    private Button add;
    private EditText email;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String memberName;
    private String memberID;

    public MemberAddDialog(Context context, String groupId) {
        super(context);
        setContentView(R.layout.member_add_dialog);
        auth = FirebaseAuth.getInstance();
        database = Utils.getDatabaseReference();
        add = (Button) findViewById(R.id.add_button);
        final String id = groupId;
        email = (EditText) findViewById(R.id.enter_email);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMember(email,id);
            }
        });

        final TextView textEmail = (TextView)findViewById(R.id.enter_email);
        textEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    addMember(email, id);
                    return true;
                }
                return false;
            }
        });
    }

    public void addMember(EditText memberEmail, final String groupId) {
        final DatabaseReference mDatabase = Utils.getDatabaseReference();
        final String mEmail = memberEmail.getText().toString();
        if (isEmpty(mEmail))
            Toast.makeText(getContext(), R.string.adding_empty, Toast.LENGTH_SHORT).show();
        else {
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getEmail().equals(mEmail)) {
                            Map<String,Object> member = new HashMap<>();
                            member.put(user.getName(),true);
                            Map<String,Object> memberId = new HashMap<>();
                            memberId.put(user.getUid(),true);
                            Map<String,Object> addGroup = new HashMap<>();
                            addGroup.put(groupId,true);
                            mDatabase.child("users").child(user.getUid()).child("groups").updateChildren(addGroup);
                            mDatabase.child("groups").child(groupId).child("members").updateChildren(member);
                            mDatabase.child("groups").child(groupId).child("memberID").updateChildren(memberId);
                            dismiss();
                            break;
                        }
                    }
                    //Toast.makeText(getContext(), R.string.add_member_error, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private boolean isEmpty(String str) {
        if (str == null)
            return true;
        else if (str.trim().length() == 0)
            return true;
        return false;
    }
}
