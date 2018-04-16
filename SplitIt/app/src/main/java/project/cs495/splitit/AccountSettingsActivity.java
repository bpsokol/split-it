package project.cs495.splitit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountSettingsActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        setTitle("Account Settings");
        auth = FirebaseAuth.getInstance();

        //Change User ID
        Button changeUserButton = (Button)findViewById(R.id.change_user);
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangeUserActivity.class));
            }
        });
        //Change Email Address
        Button changeEmailButton = (Button)findViewById(R.id.change_email);
        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class));
            }
        });
        //Change Password
        Button changePasswordButton = (Button)findViewById(R.id.change_password);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailAddress = auth.getCurrentUser().getEmail();
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    android.content.Context context = getApplicationContext();
                                    CharSequence text = "Email sent";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        });
            }
        });
        //Logout
        Button logoutButton = (Button)findViewById(R.id.sign_out);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(AccountSettingsActivity.this)
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
        //DeleteUser
        Button deleteUserButton = (Button)findViewById(R.id.delete_user);
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }
    private void signOut(){
        Intent signOutIntent = new Intent(this, SignInActivity.class);
        startActivity(signOutIntent);
        finish();
    }
    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
