package project.cs495.splitit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import project.cs495.splitit.models.Group;

public class GroupModifyActivity extends AppCompatActivity{
    private FirebaseAuth auth;
    private DatabaseReference database;
    private RecyclerView groupRV;
    private FirebaseRecyclerAdapter adapter;
    private String groupId;
    private Group group;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);
        auth = FirebaseAuth.getInstance();
        database = Utils.getDatabaseReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.modify_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        database = Utils.getDatabaseReference();
        String userID = auth.getCurrentUser().getUid();
        Query query = database.child("groups").orderByChild("memberID/"+userID).equalTo(true);

        groupRV = (RecyclerView) findViewById(R.id.group_rv);
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, String.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<String, memberHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull memberHolder holder, int position, @NonNull String model) {
                holder.bindData(model);
            }

            @Override
            public memberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = groupRV.getChildAdapterPosition(view);
                        String member = (String) adapter.getItem(position);
                    }
                });
                return new memberHolder(view);
            }
        };
        groupRV.setAdapter(adapter);
        groupRV.setLayoutManager(new LinearLayoutManager(this));

    }

    private class memberHolder extends RecyclerView.ViewHolder {
        private TextView memberName;

        memberHolder(View view) {
            super(view);
            memberName = (TextView) view.findViewById(R.id.member_name);
        }

        public void bindData(String model) {
            memberName.setText(model);
        }
    }
}
