package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView profileName;
    private EditText groupName;
    private EditText lastName;
    private EditText firstName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (!isUserLogin()) {
            signOut();
        }
        setContentView(R.layout.activity_creategroup);
        setTitle(getString(R.string.profile_title));
        profileName = (TextView) findViewById(R.id.user_name);
        groupName = (EditText) findViewById(R.id.group_name_input);
        lastName = (EditText) findViewById(R.id.last_name_input);
        firstName = (EditText) findViewById(R.id.first_name_input);
        Button createButton = (Button)findViewById(R.id.create);
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                create(groupName,firstName,lastName);
            }
        });
        Button cancelButton = (Button)findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    private boolean isUserLogin(){
        if(auth.getCurrentUser() != null){
            return true;
        }
        return false;
    }

    private void signOut(){
        Intent signOutIntent = new Intent(this, MainActivity.class);
        startActivity(signOutIntent);
        finish();
    }

    private void create(EditText groupName, EditText firstName, EditText lastName) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String gName = groupName.getText().toString();
        String fName = firstName.getText().toString();
        String lName = lastName.getText().toString();
        String groupId = mDatabase.child("groups").push().getKey();
        GroupOwner manager = new GroupOwner(auth.getInstance().getCurrentUser().getUid(),fName+lName);
        Group group = new Group(groupId,gName,manager.getManagerUID(),manager.getManagerName(),null);
        group.addMember(manager.getManagerName());
        group.commitToDB(mDatabase);
        Intent createIntent =  new Intent(this, GroupManageActivity.class);
        startActivity(createIntent);
        finish();
    }

    private void cancel() {
        Intent cancelIntent = new Intent(this,GroupManageActivity.class);
        startActivity(cancelIntent);
        finish();
    }

}
