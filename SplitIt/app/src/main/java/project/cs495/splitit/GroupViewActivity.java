package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.User;

public class GroupViewActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "GroupViewActivity";
    public static final String EXTRA_GROUP_ID = "project.cs495.splitit.GROUP_ID";
    private DatabaseReference mDatabase;
    private RecyclerView groupRV;
    private FirebaseRecyclerAdapter adapter;
    private String groupId;
    private Group group;
    private ImageButton fab_plus;
    private View view;
    private static int currGroupIndex = 0;
    private String currGroupId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        groupId = intent.getStringExtra(EXTRA_GROUP_ID);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    group = snapshot.getValue(Group.class);
                    if (group.getGroupId().equals(groupId))
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Query query = mDatabase.child("users").orderByChild(getString(R.string.groups_path)+groupId).equalTo(true);
        groupRV = (RecyclerView) findViewById(R.id.group_rv);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, memberHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull memberHolder holder, int position, @NonNull User model) {
                holder.bindData(model);
            }

            @Override
            public memberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item,parent,false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currGroupIndex = groupRV.getChildAdapterPosition(view);
                        User member = (User) adapter.getItem(currGroupIndex);
                    }
                });

                final ImageButton menu_options = view.findViewById(R.id.group_list_options);

                // Use temporary variable to capture value of View
                final View temp = view;
                menu_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        groupRV.findViewHolderForAdapterPosition(currGroupIndex).itemView.setSelected(false);
                        currGroupIndex = groupRV.getChildAdapterPosition(temp);
                        view.setSelected(true);
                        PopupMenu popup = new PopupMenu(view.getContext(), view);
                        popup.setOnMenuItemClickListener(GroupViewActivity.this);

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        User user = (User) adapter.getItem(currGroupIndex);
                        if(auth.getCurrentUser().getUid().equals(user.getUid())) {
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.manage_group_members_manager, popup.getMenu());
                        }
                        else {
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.manage_group_members, popup.getMenu());
                        }
                        popup.show();
                    }
                });

                return new memberHolder(view);
            }
        };
        groupRV.setAdapter(adapter);
        groupRV.setLayoutManager(new LinearLayoutManager(this));

        fab_plus = (ImageButton) findViewById(R.id.add_member);
        fab_plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openDialog(groupId);
            }
        });
    }

    public void openDialog(String groupId) {
        MemberAddDialog memberAddDialog = new MemberAddDialog(this,groupId);
        memberAddDialog.show();
    }

    public void leaveGroup(String groupId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser().getUid().equals(group.getManagerUID()))
            Toast.makeText(GroupViewActivity.this, getString(R.string.owner_leave), Toast.LENGTH_SHORT).show();
        else {
            Utils.getDatabaseReference().child("groups").child(groupId).child("memberID").child(auth.getCurrentUser().getUid()).removeValue();
            Utils.getDatabaseReference().child("groups").child(groupId).child("members").child(auth.getCurrentUser().getDisplayName()).removeValue();
            Utils.getDatabaseReference().child("users").child(auth.getCurrentUser().getUid()).child("groups").child(groupId).removeValue();
            Toast.makeText(GroupViewActivity.this, getString(R.string.left_group), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GroupViewActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private class memberHolder extends RecyclerView.ViewHolder {
        private TextView memberName;

        memberHolder(View view) {
            super(view);
            memberName = (TextView) view.findViewById(R.id.txt);
        }

        public void bindData(final User user) {
            String name = user.getName();
            memberName.setText(name);
        }
    }

    private void deleteMember() {
        User user = (User) adapter.getItem(currGroupIndex);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser().getUid().equals(group.getManagerUID()) && !auth.getCurrentUser().getUid().equals(user.getUid())) {
            final DatabaseReference removeUserFromGroup = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members").child(user.getName());
            final DatabaseReference removeUIDFromGroup = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("memberID").child(user.getUid());
            final DatabaseReference removeGroupFromUser = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("groups").child(groupId);

            removeUserFromGroup.removeValue();
            removeUIDFromGroup.removeValue();
            removeGroupFromUser.removeValue();

            Toast.makeText(GroupViewActivity.this, user.getName() + " deleted from " + group.getGroupName(), Toast.LENGTH_LONG).show();
        }
        else {
            if (!auth.getCurrentUser().getUid().equals(group.getManagerUID())) {
                Toast.makeText(GroupViewActivity.this,getString(R.string.not_group_owner),Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(GroupViewActivity.this,getString(R.string.owner_delete),Toast.LENGTH_LONG).show();
            }
        }
    }

    public void assignManager() {
        User user = (User) adapter.getItem(currGroupIndex);
        if (user.getUid().equals(group.getManagerUID())) {
            Toast.makeText(GroupViewActivity.this, user.getName() + " " + getString(R.string.manager_verify), Toast.LENGTH_SHORT).show();
        }
        else {
            Utils.getDatabaseReference().child("groups").child(groupId).child("managerUID").setValue(user.getUid());
            Utils.getDatabaseReference().child("groups").child(groupId).child("managerName").setValue(user.getName());
            Utils.getDatabaseReference().child("users").child(user.getUid()).child("groupsOwned").child(groupId).removeValue();
            Toast.makeText(GroupViewActivity.this, user.getName() + " " + getString(R.string.new_manager), Toast.LENGTH_SHORT).show();;
            Utils.getDatabaseReference().child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        group = snapshot.getValue(Group.class);
                        if (group.getGroupId().equals(groupId))
                            break;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_group_member:
                deleteMember();
                return true;
            case R.id.assign_manager:
                assignManager();
                return true;
            case R.id.leave_group:
                leaveGroup(groupId);
            return true;
            default:
                return false;
        }
    }
}
