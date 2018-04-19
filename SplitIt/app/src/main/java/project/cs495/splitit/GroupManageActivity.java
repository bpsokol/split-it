package project.cs495.splitit;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.List;

public class GroupManageActivity extends Fragment{
    private FirebaseAuth auth;
    private DatabaseReference database;
    private static ArrayList<String> groupInfo = new ArrayList<String>();
    private static ArrayList<String> groupIDArray = new ArrayList<String>();
    private static int currGroupIndex;
    private List<String> memberID = new ArrayList<String>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_group_manage, container, false);
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
                    String managerId = childSnapshot.child("managerUID").getValue(String.class);
                    memberID = (ArrayList) childSnapshot.child("memberID").getValue();
                    if (managerId.equals(auth.getCurrentUser().getUid()) || memberID.contains(auth.getCurrentUser().getUid())) {
                        String groupName = childSnapshot.child("groupName").getValue(String.class);
                        String groupID = childSnapshot.getKey();
                        groupInfo.add(groupName);
                        groupIDArray.add(groupID);
                    }
                    groupList.invalidateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), getString(R.string.database_error), Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    class GroupList implements AdapterView.OnItemClickListener{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            ViewGroup vg = (ViewGroup)view;
            TextView tv = (TextView)vg.findViewById(R.id.txt);
            currGroupIndex = position;
        }
    }
}