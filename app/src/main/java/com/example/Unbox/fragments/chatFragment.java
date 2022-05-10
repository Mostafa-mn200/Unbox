package com.example.Unbox.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.Unbox.R;
import com.example.Unbox.chatActivity;
import com.example.Unbox.frinds.contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatFragment extends Fragment {

    View views;
    RecyclerView chatList;
    DatabaseReference chatRef,userRef;
    FirebaseAuth auth;
    String currentuser;


    public chatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        views= inflater.inflate(R.layout.fragment_chat, container, false);

        auth=FirebaseAuth.getInstance();
//        currentuser=auth.getCurrentUser().getUid();
        chatRef= FirebaseDatabase.getInstance().getReference().child("Contacts");//.child(currentuser);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

        chatList=views.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return views;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts>options=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(chatRef,contacts.class)
                .build();


        FirebaseRecyclerAdapter<contacts,chatsViewHolder>adapter=new FirebaseRecyclerAdapter<contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull chatsViewHolder holder, int position, @NonNull contacts model) {
               final String usersID=getRef(position).getKey();
                final String[] retImg = {"default_img"};
                userRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("image")){
                                retImg[0] =snapshot.child("image").getValue().toString();
                                Picasso.get().load(retImg[0]).placeholder(R.drawable.profile_image).into(holder.profileImg);
                            }

                            final String retName=snapshot.child("name").getValue().toString();
                            final String retStatus=snapshot.child("states").getValue().toString();
                            holder.username.setText(retName);

                            if (snapshot.child("userState").hasChild("sate")){
                                String state=snapshot.child("userState").child("sate").getValue().toString();
                                String data=snapshot.child("userState").child("date").getValue().toString();
                                String time=snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.userstatus.setText("online");
                                }
                                else if (state.equals("offline")){
                                    holder.userstatus.setText("Last Seen"+data+" "+time);
                                }
                            }
                            else {
                                holder.userstatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent=new Intent(getContext(), chatActivity.class);
                                    chatIntent.putExtra("visit_user_id",usersID);
                                    chatIntent.putExtra("visit_user_name",retName);
                                    chatIntent.putExtra("visit_user_Img", retImg[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new chatsViewHolder(view);
            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImg;
        TextView username,userstatus;
        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg=itemView.findViewById(R.id.users_profile_img);
            userstatus=itemView.findViewById(R.id.user_status);
            username=itemView.findViewById(R.id.user_profile_name);

        }
    }
}