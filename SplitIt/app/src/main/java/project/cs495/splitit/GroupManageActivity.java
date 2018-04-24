package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.User;

public class GroupManageActivity extends Fragment implements PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "GroupManageActivity";
    public static final String EXTRA_GROUP_ID = "project.cs495.splitit.GROUP_ID";
    private FirebaseAuth auth;
    private DatabaseReference database;
    private FirebaseRecyclerAdapter adapter;
    private static int currGroupIndex = 0;
    private View view;
    RecyclerView groupList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_group_manage, container, false);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = Utils.getDatabaseReference();

        groupList = (RecyclerView) rootView.findViewById(R.id.group_list);
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
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.group_list_item, parent, false);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modifyGroup();
                    }
                });

                final ImageButton menu_options = view.findViewById(R.id.group_list_options);

                // Use temporary variable to capture value of View
                final View temp = view;
                menu_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currGroupIndex = groupList.getChildAdapterPosition(temp);
                        view.setSelected(true);
                        PopupMenu popup = new PopupMenu(getView().getContext(), view);
                        popup.setOnMenuItemClickListener(GroupManageActivity.this);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.manage_group_menu_options, popup.getMenu());
                        popup.show();
                    }
                });

                return new GroupHolder(view);
            }
        };

        groupList.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        groupList.setAdapter(adapter);
        return rootView;
    }

    private Intent buildGroupViewIntent(String groupId) {
        Intent intent = new Intent(getContext(),GroupViewActivity.class);
        intent.putExtra(EXTRA_GROUP_ID,groupId);
        return intent;
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

    private void deleteGroup() {
        final Group removedGroup = (Group) adapter.getItem(currGroupIndex);
        if (auth.getCurrentUser().getUid().equals(removedGroup.getManagerUID())) {
            Query query = database.child("users").orderByChild(getString(R.string.groups_path)+removedGroup.getGroupId()).equalTo(true);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        database.child("users").child(user.getUid()).child("groups").child(removedGroup.getGroupId()).removeValue();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            database.child("groups").child(removedGroup.getGroupId()).removeValue();
            database.child("users").child(auth.getCurrentUser().getUid()).child("groupsOwned").child(removedGroup.getGroupId()).removeValue();
            currGroupIndex = 0;
            Toast.makeText(getView().getContext(), removedGroup.getGroupName() + " deleted", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getView().getContext(), R.string.not_manager, Toast.LENGTH_LONG).show();
        }
    }

    private void modifyGroup() {
        currGroupIndex = groupList.getChildAdapterPosition(view);
        view.setSelected(true);
        Log.d(TAG,String.format("%s: %d", "Current Index", currGroupIndex));
        Group group = (Group) adapter.getItem(currGroupIndex);
        final Group modifyGroup = (Group)group;
        Intent intent = buildGroupViewIntent(modifyGroup.getGroupId());
        getContext().startActivity(intent);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_manage_modify:
                modifyGroup();
                return true;
            case R.id.group_manage_delete:
                deleteGroup();
                return true;
            default:
                return false;
        }
    }
}