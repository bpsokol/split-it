package project.cs495.splitit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class MemberAddActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener{
    private static final String TAG = "GroupViewActivity";
    public static final String EXTRA_GROUP_ID = "project.cs495.splitit.GROUP_ID";
    private Button add;
    private EditText email;
    private RecyclerView groupRV;
    private MemberAdapter adapter;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String groupId;
    private View view;
    private List<User> options;
    private static int currGroupIndex = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        database = Utils.getDatabaseReference();
        groupRV = (RecyclerView) findViewById(R.id.search_rv);
        Intent intent = getIntent();
        groupId = intent.getStringExtra(EXTRA_GROUP_ID);
        options = new ArrayList<>();
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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
        Query query = database.child("users").orderByChild(getString(R.string.groups_path)+groupId).equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (User model: options) {
                        if (model.getUid().equals(user.getUid())) {
                            options.remove(model);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new MemberAdapter(this,alphabeticalComparator);
        groupRV.setLayoutManager(new LinearLayoutManager(this));
        groupRV.setAdapter(adapter);

        adapter.edit()
                .add(options)
                .commit();
    }

    public void addMember(final String groupId) {
        final DatabaseReference mDatabase = Utils.getDatabaseReference();
        User user = (User) adapter.getItem(currGroupIndex);
        String email = user.getEmail();
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getEmail().equals(email)) {
                        Map<String,Object> member = new HashMap<>();
                        member.put(user.getName(),true);
                        Map<String,Object> memberId = new HashMap<>();
                        memberId.put(user.getUid(),true);
                        Map<String,Object> addGroup = new HashMap<>();
                        addGroup.put(groupId,true);
                        mDatabase.child("users").child(user.getUid()).child("groups").updateChildren(addGroup);
                        mDatabase.child("groups").child(groupId).child("members").updateChildren(member);
                        mDatabase.child("groups").child(groupId).child("memberID").updateChildren(memberId);
                        Intent intent = buildGroupViewIntent(groupId);
                        startActivity(intent);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private Intent buildGroupViewIntent(String groupId) {
        Intent intent = new Intent(MemberAddActivity.this,GroupViewActivity.class);
        intent.putExtra(EXTRA_GROUP_ID,groupId);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<User> filteredUserList = filter(options,query);
        adapter.edit().replaceAll(filteredUserList).commit();
        groupRV.scrollToPosition(0);
        groupRV.setAdapter(adapter);
        groupRV.setLayoutManager(new LinearLayoutManager(this));
        return true;
    }


    private static List<User> filter(List<User> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            final String text = model.getEmail().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private static Comparator<User> alphabeticalComparator = (a, b) -> a.getEmail().compareTo(b.getEmail());

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
            menu_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currGroupIndex = groupRV.getChildAdapterPosition(temp);
                    view.setSelected(true);
                    PopupMenu popup = new PopupMenu(view.getContext(), view);
                    popup.setOnMenuItemClickListener(MemberAddActivity.this);

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    User user = (User) adapter.getItem(currGroupIndex);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.add_member, popup.getMenu());
                    popup.show();
                }
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addMember(groupId);
                return true;
            default:
                return false;
        }
    }
}

