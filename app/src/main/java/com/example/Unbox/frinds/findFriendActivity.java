package com.example.Unbox.frinds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.Unbox.ProfileActivity;
import com.example.Unbox.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class findFriendActivity extends AppCompatActivity {
Toolbar mtoolbar;
RecyclerView FindFriendRecylerList;
DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        dis();
    }

    private void dis() {
        FindFriendRecylerList=findViewById(R.id.find_friend_recyl_list);
        FindFriendRecylerList.setLayoutManager(new LinearLayoutManager(this));
        mtoolbar=findViewById(R.id.find_friend_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<contacts>option=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(usersRef,contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts,FindFriendViewHolder> adapter=
                new FirebaseRecyclerAdapter<contacts, FindFriendViewHolder>(option) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull contacts model) {
                        holder.username.setText(model.getName());
                        holder.userstatus.setText(model.getStates());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImg);

                        //to get id
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id=getRef(position).getKey();
                                Intent profileIntent=new Intent(findFriendActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                        return  viewHolder;
                    }
                };

        FindFriendRecylerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView username,userstatus;
        CircleImageView profileImg;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImg=itemView.findViewById(R.id.users_profile_img);
        }
    }
}