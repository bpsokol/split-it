package project.cs495.splitit;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class GroupManageActivity extends Fragment{
    private FirebaseAuth auth;
    private TextView profileName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance().getInstance();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_group_manage, container, false);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        return rootView;
    }

    private void createGroup() {
        Intent createGroupIntent = new Intent(GroupManageActivity.this.getActivity(),CreateGroupActivity.class);
        GroupManageActivity.this.getActivity().startActivity(createGroupIntent);
        //getActivity().finish();
    }

    private void displayMessage(String message){
        Toast.makeText(getView().getContext(), message, Toast.LENGTH_LONG).show();
    }
}
