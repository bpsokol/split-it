package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import project.cs495.splitit.models.Group;

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
        if (isEmpty(gName) || isEmpty(fName) || isEmpty(lName)) {
            displayMessage(getString(R.string.empty_warning));
        }
        else {
            String groupId = mDatabase.child("groups").push().getKey();
            GroupOwner manager = new GroupOwner(auth.getCurrentUser().getUid(), fName + " " + lName);
            Group group = new Group(groupId, gName, manager.getManagerUID(), manager.getManagerName(), null);
            group.addMember(manager.getManagerName());
            group.commitToDB(mDatabase);

            //Intent createIntent = new Intent(CreateGroupActivity.this,GroupManageActivity.class);
            // CreateGroupActivity.this.startActivity(createIntent);

            Intent createIntent = new Intent(CreateGroupActivity.this, MainActivity.class);
            startActivity(createIntent);
            finish();
            displayMessage(getString(R.string.create_successful));
        }
    }

    private void cancel() {
        Intent cancelIntent = new Intent(this,GroupManageActivity.class);
        startActivity(cancelIntent);
        finish();
    }

    private boolean isEmpty(String str) {
        if (str == null)
            return true;
        else if (str.trim().length() == 0)
            return true;
        return false;
    }

    private void displayMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
