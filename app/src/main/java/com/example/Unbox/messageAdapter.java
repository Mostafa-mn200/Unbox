package com.example.Unbox;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.messageViewHolder>{

    private List<messages>userMessageList;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    public messageAdapter(List<messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    public class messageViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessageText,receiverMessageText;
        CircleImageView receiverProfileImg;
        ImageView messageSenderPicture,messageReceiverPicture;
        public messageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText=itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImg=itemView.findViewById(R.id.message_profile_img);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);

        }
    }



    @NonNull
    @Override
    public messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        auth=FirebaseAuth.getInstance();
        return new messageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull messageViewHolder holder, int position) {
        String messageSenderID=auth.getCurrentUser().getUid();
        messages mmessages=userMessageList.get(position);

        String fromUserID=mmessages.getFrom();
        String fromMessageType=mmessages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")){
                    String receiverImg=snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImg).placeholder(R.drawable.profile_image).into(holder.receiverProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImg.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text")){

            if (fromUserID.equals(messageSenderID)){
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(mmessages.getMessage()+"\n \n"+mmessages.getTime()+"-"+mmessages.getDate());
            }
            else {

                holder.receiverProfileImg.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(mmessages.getMessage()+"\n \n"+mmessages.getTime()+"-"+mmessages.getDate());
            }
        }
        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderID)){
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(mmessages.getMessage()).into(holder.messageSenderPicture);
            }
            else {
                holder.receiverProfileImg.setVisibility(View.INVISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(mmessages.getMessage()).into(holder.messageReceiverPicture);
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
