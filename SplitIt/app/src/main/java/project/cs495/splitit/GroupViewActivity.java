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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

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
    private static int currGroupIndex;
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
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.manage_group_members, popup.getMenu());
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

    private void deleteItem() {
        Group group = (Group) adapter.getItem(currGroupIndex);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final DatabaseReference removeUserFromGroup = FirebaseDatabase.getInstance().getReference("groups").child(group.getGroupId()).child("members").child(auth.getCurrentUser().getDisplayName());
        final DatabaseReference removeGroupFromUser = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid()).child("groups").child(group.getGroupId());

        removeUserFromGroup.removeValue();
        removeGroupFromUser.removeValue();

        Toast.makeText(GroupViewActivity.this, auth.getCurrentUser().getDisplayName() + " deleted from " + group.getGroupName(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_group_member:
                deleteItem();
                return true;
            default:
                return false;
        }
    }
}

