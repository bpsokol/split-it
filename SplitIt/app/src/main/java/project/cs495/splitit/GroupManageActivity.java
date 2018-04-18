package project.cs495.splitit;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class GroupManageActivity extends Fragment{
    private FirebaseAuth auth;
    private DatabaseReference database;
    private static ArrayList<String> groupInfo = new ArrayList<String>();
    private static ArrayList<String> groupIDArray = new ArrayList<String>();
    private static int currGroupIndex;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_groupmanage, container, false);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        final ListView groupList = (ListView)rootView.findViewById(R.id.group_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.group_list_item,R.id.txt,groupInfo);
        groupList.setAdapter(adapter);
        groupList.setOnItemClickListener(new GroupList());
        adapter.clear();
        adapter.notifyDataSetChanged();

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.child("groups").getChildren()) {
                    String groupName = childSnapshot.child("groupName").getValue(String.class);
                    String groupID = childSnapshot.getKey();
                    groupInfo.add(groupName);
                    groupIDArray.add(groupID);
                    groupList.invalidateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), getString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        });

        Button createGroupButton = (Button)rootView.findViewById(R.id.create_group);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createGroup();
            }
        });
        return rootView;
    }

    private void createGroup() {
        Intent createGroupIntent = new Intent(getView().getContext(),CreateGroupActivity.class);
        startActivity(createGroupIntent);
        getActivity().finish();
    }

    class GroupList implements AdapterView.OnItemClickListener{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            ViewGroup vg = (ViewGroup)view;
            TextView tv = (TextView)vg.findViewById(R.id.txt);
            currGroupIndex = position;
        }
    }

}