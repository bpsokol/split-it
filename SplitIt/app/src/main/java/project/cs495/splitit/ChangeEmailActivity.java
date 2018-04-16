package project.cs495.splitit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        setTitle("Change Email");

        Button buttonDone = (Button)findViewById(R.id.button_done_email);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
            }
        });

        TextView textEmail = (TextView)findViewById(R.id.email_address);
        textEmail.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    updateEmail();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateEmail() {
        TextView textEmail = (TextView)findViewById(R.id.email_address);
        if (textEmail.getText().length() != 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String newEmail = textEmail.getText().toString();
            user.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to change Email!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}