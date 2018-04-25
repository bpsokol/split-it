package project.cs495.splitit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.support.v7.widget.SearchView;
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
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.cs495.splitit.models.Group;
import project.cs495.splitit.models.User;

public class GroupViewActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = "GroupViewActivity";
    public static final String EXTRA_GROUP_ID = "project.cs495.splitit.GROUP_ID";
    private DatabaseReference mDatabase;
    private RecyclerView groupRV;
    private RecyclerView memberRV;
    private MemberAdapter adapter;
    private FirebaseRecyclerAdapter mAdapter;
    private String groupId;
    private Group group;
    private ImageButton fab_plus;
    private View view;
    private List<User> options;
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
        mDatabase.child("groups").orderByChild("groupId").equalTo(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    group = snapshot.getValue(Group.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query query = mDatabase.child("users").orderByChild(getString(R.string.groups_path)+groupId).equalTo(true);
        groupRV = (RecyclerView) findViewById(R.id.group_rv);
        memberRV = (RecyclerView) findViewById(R.id.group_rv);
        FirebaseRecyclerOptions<User> cards = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        memberRV.setAdapter(mAdapter);
        mAdapter = new FirebaseRecyclerAdapter<User, regularHolder>(cards) {
            @Override
            protected void onBindViewHolder(@NonNull regularHolder holder, int position, @NonNull User model) {
                holder.bindData(model);
            }

            @Override
            public regularHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item,parent,false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currGroupIndex = groupRV.getChildAdapterPosition(view);
                        User member = (User) mAdapter.getItem(currGroupIndex);
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

                return new regularHolder(view);
            }
        };
        options = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    options.add(snapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        memberRV.setAdapter(mAdapter);
        memberRV.setLayoutManager(new LinearLayoutManager(this));

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

    /*@Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        groupRV.setAdapter(adapter);
        adapter = new MemberAdapter(this,alphabeticalComparator);
        adapter.edit()
                .add(options)
                .commit();
        groupRV.setAdapter(adapter);
        groupRV.setLayoutManager(new LinearLayoutManager(this));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<User> filteredUserList = filter(options,query);
        adapter.edit().replaceAll(filteredUserList).commit();
        groupRV.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private static List<User> filter(List<User> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private static Comparator<User> alphabeticalComparator = (a, b) -> a.getName().compareTo(b.getName());

    public class MemberAdapter extends SortedListAdapter<User> {

        public MemberAdapter(Context context, Comparator<User> comparator) {
            super(context, User.class, comparator);
        }

        @Override
        protected memberHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item, parent, false);
            view.setOnClickListener(view -> {
                currGroupIndex = groupRV.getChildAdapterPosition(view);
                User member = (User) adapter.getItem(currGroupIndex);
            });
            final ImageButton menu_options = view.findViewById(R.id.group_list_options);

            // Use temporary variable to capture value of View
            final View temp = view;
            menu_options.setOnClickListener(view -> {
                groupRV.findViewHolderForAdapterPosition(currGroupIndex).itemView.setSelected(false);
                currGroupIndex = groupRV.getChildAdapterPosition(temp);
                view.setSelected(true);
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.setOnMenuItemClickListener(GroupViewActivity.this);

                FirebaseAuth auth = FirebaseAuth.getInstance();
                User user = (User) adapter.getItem(currGroupIndex);
                if(auth.getCurrentUser().getUid().equals(user.getUid())) {
                    MenuInflater inflater1 = popup.getMenuInflater();
                    inflater1.inflate(R.menu.manage_group_members_manager, popup.getMenu());
                }
                else {
                    MenuInflater inflater1 = popup.getMenuInflater();
                    inflater1.inflate(R.menu.manage_group_members, popup.getMenu());
                }
                popup.show();
            });
            return new memberHolder(view);
        }
    }

    private class memberHolder extends SortedListAdapter.ViewHolder<User> {
        private TextView memberName;

        memberHolder(View view) {
            super(view);
            memberName = (TextView) view.findViewById(R.id.txt);
        }

        @Override
        protected void performBind(User user) {
            memberName.setText(user.getName());
        }
    }

    private class regularHolder extends RecyclerView.ViewHolder {
        private TextView memberName;

        regularHolder (View view) {
            super(view);
            memberName = (TextView) view.findViewById(R.id.txt);
        }

        public void bindData(final User user) {
            memberName.setText(user.getName());
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (user.getUid().equals(group.getManagerUID())) {
            Toast.makeText(GroupViewActivity.this, user.getName() + " " + getString(R.string.manager_verify), Toast.LENGTH_SHORT).show();
        }
        else {
            Utils.getDatabaseReference().child("groups").child(groupId).child("managerUID").setValue(user.getUid());
            Utils.getDatabaseReference().child("groups").child(groupId).child("managerName").setValue(user.getName());
            Utils.getDatabaseReference().child("users").child(auth.getCurrentUser().getUid()).child("groupsOwned").child(groupId).removeValue();
            Map<String,Boolean> newGroup = new HashMap<>();
            newGroup.put(groupId,true);
            Utils.getDatabaseReference().child("users").child(user.getUid()).child("groupsOwned").setValue(newGroup);
            Toast.makeText(GroupViewActivity.this, user.getName() + " " + getString(R.string.new_manager), Toast.LENGTH_SHORT).show();;
            Utils.getDatabaseReference().child("groups").orderByChild("groupId").equalTo(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                        group = snapshot.getValue(Group.class);
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
