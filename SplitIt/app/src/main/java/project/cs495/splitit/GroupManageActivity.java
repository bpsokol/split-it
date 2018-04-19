package project.cs495.splitit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import project.cs495.splitit.models.Group;

public class GroupManageActivity extends Fragment{
    private static final String TAG = "GroupManageActivity";
    private FirebaseAuth auth;
    private DatabaseReference database;
    private FirebaseRecyclerAdapter adapter;
    private static int currGroupIndex;
    private ImageButton fab_dismiss;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_group_manage, container, false);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        final RecyclerView groupList = (RecyclerView) rootView.findViewById(R.id.group_list);
        String userID = auth.getCurrentUser().getUid();
        Query query = database.child("groups").orderByChild("memberID/"+userID).equalTo(true);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Group, GroupHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupHolder holder, int position, @NonNull Group model) {
                holder.bindData(model);
            }

            @Override
            public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.group_list_item, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        groupList.findViewHolderForAdapterPosition(currGroupIndex).itemView.setSelected(false);
                        currGroupIndex = groupList.getChildAdapterPosition(view);
                        view.setSelected(true);
                        Log.d(TAG,String.format("%s: %d", "Current Index", currGroupIndex));
                    }
                });
                return new GroupHolder(view);
            }
        };

        fab_dismiss = (ImageButton) rootView.findViewById(R.id.dismiss_button);
        fab_dismiss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dismissGroup();
            }
        });

        groupList.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        groupList.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void dismissGroup() {
        Group group = (Group) adapter.getItem(currGroupIndex);
        if (auth.getCurrentUser().getUid().equals(group.getManagerUID()))
            database.child("groups").child(group.getGroupId()).removeValue();
        else
            Toast.makeText(getActivity(),R.string.not_manager,Toast.LENGTH_LONG).show();
    }

    private class GroupHolder extends RecyclerView.ViewHolder {
        private TextView txt;

        public GroupHolder(View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txt);
        }

        public void bindData(Group model) {
            txt.setText(model.getGroupName());
        }
    }
}