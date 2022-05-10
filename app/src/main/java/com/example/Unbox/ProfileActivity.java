package com.example.Unbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity{
    String receiverUserId, currentState, senderUserID;
    CircleImageView userProfileImg;
    TextView userProfileName,UserProfilesStatus;
    Button sendMessagebtn, cancel_btn;
    DatabaseReference UserRef, chatRequestRef, contactsRef,NotificationRef;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");


        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        senderUserID=auth.getCurrentUser().getUid();

        dis();
        currentState="new";
    }

    private void dis() {
        userProfileImg=findViewById(R.id.visit_profile_img);
        userProfileName=findViewById(R.id.visit_user_name);
        UserProfilesStatus=findViewById(R.id.visit_profile_status);
        sendMessagebtn=findViewById(R.id.send_message_request_btn);
        cancel_btn=findViewById(R.id.decline_message_request_btn);

        RetriveUserInfo();
    }


    private void RetriveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && snapshot.hasChild("image")){
                    String userImage=snapshot.child("image").getValue().toString();
                    String userName=snapshot.child("name").getValue().toString();
                    String userStatus=snapshot.child("states").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImg);
                    userProfileName.setText(userName);
                    UserProfilesStatus.setText(userStatus);
                    
                    ManagechatRequest();
                }else {
                    String userName=snapshot.child("name").getValue().toString();
                    String userStatus=snapshot.child("states").getValue().toString();
                    userProfileName.setText(userName);
                    UserProfilesStatus.setText(userStatus);

                    ManagechatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManagechatRequest() {
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String request_typ=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_typ.equals("sent")){
                        currentState="request_sent";
                        sendMessagebtn.setText("Cancel Chat Request");
                    }
                    else if (request_typ.equals("received")){
                        currentState="request_received";
                        sendMessagebtn.setText("Accept chat Request");
                        cancel_btn.setVisibility(View.VISIBLE);
                        cancel_btn.setEnabled(true);
                        cancel_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                currentState="friends";
                                sendMessagebtn.setText("Remove This Contacts");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!senderUserID.equals(receiverUserId)){
            sendMessagebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessagebtn.setEnabled(false);
                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                   else if(currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    else if(currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                   else if(currentState.equals("friends")){
                        RemoveSpecificContant();
                    }
                }
            });
        }
        else {
            sendMessagebtn.setVisibility(View.INVISIBLE);
        }
    }


    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(senderUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                HashMap<String,String>chatnotificationMap=new HashMap<>();
                                                chatnotificationMap.put("from",senderUserID);
                                                chatnotificationMap.put("type","request");

                                                NotificationRef.child(receiverUserId).push()
                                                        .setValue(chatnotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    sendMessagebtn.setEnabled(true);
                                                                    currentState="request_sent";
                                                                    sendMessagebtn.setText("cancel chat request");
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

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequestRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendMessagebtn.setEnabled(true);
                                currentState="new";
                                sendMessagebtn.setText("Send Message");
                                cancel_btn.setVisibility(View.INVISIBLE);
                                cancel_btn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        contactsRef.child(senderUserID).child(receiverUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    contactsRef.child(receiverUserId).child(senderUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                chatRequestRef.child(senderUserID).child(receiverUserId)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){


                                            chatRequestRef.child(receiverUserId).child(senderUserID)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    sendMessagebtn.setEnabled(true);
                                                    currentState="friends";
                                                    sendMessagebtn.setText("Remove This Contact");
                                                    cancel_btn.setVisibility(View.INVISIBLE);
                                                    cancel_btn.setEnabled(false);
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


    private void RemoveSpecificContant() {

        contactsRef.child(senderUserID).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequestRef.child(receiverUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendMessagebtn.setEnabled(true);
                                currentState="new";
                                sendMessagebtn.setText("Send Message");
                                cancel_btn.setVisibility(View.INVISIBLE);
                                cancel_btn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

}