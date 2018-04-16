package project.cs495.splitit;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ReauthenticateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reauthenticate);
        setTitle("Account Settings");

        final Button buttonDone = (Button)findViewById(R.id.button_done_auth);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        TextView password = (TextView) findViewById(R.id.enter_password);
        password.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    authenticate();
                    return true;
                }
                return false;
            }
        });
    }

    private void authenticate() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final TextView password = (TextView)findViewById(R.id.enter_password);
        final String userEmail = user.getEmail();
        final CharSequence userPassword = password.getText();
        if(userPassword.length() != 0) {
            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword.toString());
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(ReauthenticateActivity.this, AccountSettingsActivity.class));
                                finish();
                            } else {
                                password.setText("");
                                Toast.makeText(getApplicationContext(),"Authentication failed!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}