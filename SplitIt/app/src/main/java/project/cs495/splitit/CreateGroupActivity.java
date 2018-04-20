package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.GroupOwner;

public class CreateGroupActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView profileName;
    private EditText groupName;
    private String userName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (!isUserLogin()) {
            signOut();
        }
        setContentView(R.layout.activity_create_group);
        setTitle(getString(R.string.create_group));
        groupName = (EditText) findViewById(R.id.create_group);
        userName = auth.getCurrentUser().getDisplayName();

        Button createButton = (Button)findViewById(R.id.button_done_group_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                create(groupName, userName);
            }
        });

        final TextView textUserID = (TextView)findViewById(R.id.create_group);
        textUserID.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    create(groupName, userName);
                    return true;
                }
                return false;
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

    private void create(EditText groupName, String userName) {
        DatabaseReference mDatabase = Utils.getDatabaseReference();
        String gName = groupName.getText().toString();

        if (isEmpty(gName)) {
            displayMessage(getString(R.string.empty_warning));
        }
        else {
            String groupId = mDatabase.child("groups").push().getKey();
            GroupOwner manager = new GroupOwner(auth.getCurrentUser().getUid(), userName);
            Group group = new Group(groupId, gName, manager.getManagerUID(), manager.getManagerName(), null, null);
            group.addMember(manager.getManagerName(),manager.getManagerUID());
            group.commitToDB(mDatabase);

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