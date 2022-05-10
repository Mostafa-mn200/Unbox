package com.example.Unbox.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.Unbox.R;
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

public class contactsFragment extends Fragment {

    View view;
    RecyclerView myContactsList;
    DatabaseReference contactsRef, usersRef;
    FirebaseAuth auth;
    String currentUserID;
    public contactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList=view.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));


        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactsRef,contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts, ContactsViewHolder>adapter=new FirebaseRecyclerAdapter<contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull contacts model) {
                String usersID=getRef(position).getKey();

                usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            if (snapshot.child("userState").hasChild("sate")){
                                String state=snapshot.child("userState").child("sate").getValue().toString();
                                String data=snapshot.child("userState").child("date").getValue().toString();
                                String time=snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.onlinIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")){
                                    holder.onlinIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                holder.onlinIcon.setVisibility(View.INVISIBLE);
                            }

                            if(snapshot.hasChild("image")){
                                String profileImguser=snapshot.child("image").getValue().toString();
                                String name=snapshot.child("name").getValue().toString();
                                String status=snapshot.child("states").getValue().toString();

                                Picasso.get().load(profileImguser).placeholder(R.drawable.profile_image).into(holder.profileImg);
                                holder.userName.setText(name);
                                holder.userStatus.setText(status);
                            }
                            else {
                                String name=snapshot.child("name").getValue().toString();
                                String status=snapshot.child("states").getValue().toString();
                                holder.userName.setText(name);
                                holder.userStatus.setText(status);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactsViewHolder contactsViewHolder=new ContactsViewHolder(view);
                return contactsViewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName,userStatus;
        CircleImageView profileImg;
        ImageView onlinIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg=itemView.findViewById(R.id.users_profile_img);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            onlinIcon=itemView.findViewById(R.id.user_online_status);
        }
    }
}