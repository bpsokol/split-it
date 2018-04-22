package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import project.cs495.splitit.models.Group;

public class GroupManageActivity extends Fragment implements PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "GroupManageActivity";
    private FirebaseAuth auth;
    private DatabaseReference database;
    private FirebaseRecyclerAdapter adapter;
    private static int currGroupIndex;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_group_manage, container, false);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = Utils.getDatabaseReference();

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
                        //viewGroup();
                    }
                });

                final ImageButton menu_options = view.findViewById(R.id.group_list_options);

                // Use temporary variable to capture value of View
                final View temp = view;
                menu_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        groupList.findViewHolderForAdapterPosition(currGroupIndex).itemView.setSelected(false);
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
        Group group = (Group) adapter.getItem(currGroupIndex);
        final DatabaseReference removeGroup = FirebaseDatabase.getInstance().getReference("groups").child(group.getGroupId());
        if (auth.getCurrentUser().getUid().equals(group.getManagerUID())) {
            removeGroup.removeValue();
            currGroupIndex = 0;
            Toast.makeText(getView().getContext(), group.getGroupName() + " deleted", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getView().getContext(), R.string.not_manager, Toast.LENGTH_LONG).show();
        }
    }

    private void viewGroup() {
        //TODO: Create a dialog box with each users name, user selection is not necessary
    }

    private void modifyGroup() {
        Group group = (Group) adapter.getItem(currGroupIndex);
        Intent intent = new Intent(getView().getContext(), GroupModifyActivity.class);
        startActivity(intent);
        //TODO: The modify activity still needs to be created
    }

    //TODO: Create easy way to add new group members to group
    //TODO: Figure out how to allow users to find other users and add them to their group

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