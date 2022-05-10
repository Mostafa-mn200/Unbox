package com.example.Unbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Unbox.frinds.contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends BottomSheetDialogFragment {

    View view;
    RecyclerView myRequestList;
    DatabaseReference chatRequestRef, usersRef, contactsRef;
    FirebaseAuth auth;
    String currentUser;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_request, container, false);

        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestList=view.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<contacts> options=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(chatRequestRef.child(currentUser),contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull contacts model) {

                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String user_list_id=getRef(position).getKey();

                DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type=snapshot.getValue().toString();

                            if (type.equals("received")){

                                usersRef.child(user_list_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            final String userImage=snapshot.child("image").getValue().toString();

                                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImg);

                                        }
                                        String requestuserName=snapshot.child("name").getValue().toString();
                                        String requestuserStatus=snapshot.child("states").getValue().toString();
                                        holder.username.setText(requestuserName);
                                        holder.userstatus.setText("Want To Connect With You");


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[]=new CharSequence[]{
                                                        "Accept",
                                                        "Cancel"
                                                };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestuserName+"  Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if(i==0){
                                                            contactsRef.child(currentUser).child(user_list_id).child("Contact")
                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){

                                                                        contactsRef.child(user_list_id).child(currentUser).child("Contact")
                                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    chatRequestRef.child(currentUser).child(user_list_id).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){


                                                                                                chatRequestRef.child(user_list_id).child(currentUser).removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()){
                                                                                                                    Toast.makeText(getContext(), "New Contacts Added", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });

                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });

                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (i==1){

                                                            chatRequestRef.child(currentUser).child(user_list_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){


                                                                                chatRequestRef.child(user_list_id).child(currentUser).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Contacts Deleted", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                            else if (type.equals("sent")){
                                Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");

                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                usersRef.child(user_list_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            final String userImage=snapshot.child("image").getValue().toString();

                                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImg);

                                        }
                                        String requestuserName=snapshot.child("name").getValue().toString();
                                        String requestuserStatus=snapshot.child("states").getValue().toString();
                                        holder.username.setText(requestuserName);
                                        holder.userstatus.setText("you have sent request to "+requestuserName);


                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[]=new CharSequence[]{
                                                        "Cancel Chat Request"
                                                };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i==0){

                                                            chatRequestRef.child(currentUser).child(user_list_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){


                                                                                chatRequestRef.child(user_list_id).child(currentUser).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "You have canceled chat request", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
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
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestViewHolder requestViewHolder=new RequestViewHolder(view);
                return requestViewHolder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView username,userstatus;
        CircleImageView profileImg;
        Button acceptbtn,cancelbtn;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImg=itemView.findViewById(R.id.users_profile_img);
            acceptbtn=itemView.findViewById(R.id.request_accept_btn);
            cancelbtn=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}