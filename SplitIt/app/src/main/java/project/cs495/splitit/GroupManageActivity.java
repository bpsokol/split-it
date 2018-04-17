package project.cs495.splitit;

import java.util.UUID;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupManageActivity extends AppCompatActivity{
    private FirebaseAuth auth;
    private TextView profileName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance().getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (!isUserLogin()) {signOut();}
        setContentView(R.layout.activity_groupmanage);
        setTitle(getString(R.string.profile_title));
        profileName = (TextView)findViewById(R.id.user_name);
        displayLoginUserProfileName();
        Button logoutButton = (Button) findViewById(R.id.sign_out2);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(GroupManageActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    signOut();
                                }else {
                                    displayMessage(getString(R.string.sign_out_error));
                                }
                            }
                        });
            }
        });
        Button deleteUserButton = (Button)findViewById(R.id.delete_user2);
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            signOut();
                        }else{
                            displayMessage(getString(R.string.user_deletion_error));
                        }
                    }
                });
            }
        });
        Button createGroupButton = (Button)findViewById(R.id.create_group);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createGroup();
            }
        });
    }

    private void createGroup() {
        Intent createGroupIntent = new Intent(GroupManageActivity.this,CreateGroupActivity.class);
        startActivity(createGroupIntent);
        finish();
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

    private void displayLoginUserProfileName(){
        FirebaseUser mUser = auth.getCurrentUser();
        if(mUser != null){
            profileName.setText(TextUtils.isEmpty(mUser.getDisplayName())? "No name found" : "Welcome " + mUser.getDisplayName());
        }
    }

    private void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
