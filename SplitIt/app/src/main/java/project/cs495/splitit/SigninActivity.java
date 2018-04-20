package project.cs495.splitit;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import project.cs495.splitit.models.User;

public class SigninActivity extends AppCompatActivity {
    private static final String TAG = SigninActivity.class.getSimpleName();
    private static final String PATH_TOS = "";
    private Button loginButton;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private static final int RC_SIGN_IN = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        if(isUserLogin()){loginUser();}
        setContentView(R.layout.activity_signin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    User user = new User(auth.getCurrentUser().getUid(),auth.getCurrentUser().getDisplayName());
                    database.child("users").child(user.getUid()).setValue(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.child("users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(eventListener);
        loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setTosUrl(PATH_TOS)
                        .build(), RC_SIGN_IN);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                loginUser();
            }
            if(resultCode == RESULT_CANCELED){
                displayMessage(getString(R.string.signin_failed));
            }
            return;
        }
        displayMessage(getString(R.string.unknown_response));
    }
    private boolean isUserLogin(){
        if(auth.getCurrentUser() != null){
            return true;
        }
        return false;
    }
    private void loginUser(){
        Intent loginIntent = new Intent(SigninActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}