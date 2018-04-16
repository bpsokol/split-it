package project.cs495.splitit;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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

        //Fills fields with users current data
        setTextFields();

        //Change User ID
        ImageButton changeUserButton = (ImageButton)findViewById(R.id.change_user);
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangeUserActivity.class));
            }
        });

        //Change Email Address
        ImageButton changeEmailButton = (ImageButton)findViewById(R.id.change_email);
        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class));
            }
        });

        //Change Password
        ImageButton changePasswordButton = (ImageButton)findViewById(R.id.change_password);
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
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to permanently delete your account?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void signOut(){
        Intent signOutIntent = new Intent(this, SignInActivity.class);
        startActivity(signOutIntent);
        finish();
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setTextFields() {
        //Load user data into fields
        TextView user_name = (TextView) findViewById(R.id.user_name);
        user_name.setText(auth.getCurrentUser().getDisplayName());

        TextView user_email = (TextView) findViewById(R.id.user_email);
        user_email.setText(auth.getCurrentUser().getEmail());

        TextView user_password = (TextView) findViewById(R.id.user_password);
        user_password.setText("password");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextFields();
    }
}
