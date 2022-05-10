package com.example.Unbox.Group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Unbox.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {
Toolbar mtoolbar;
ImageButton sendmessagebtn;
EditText usermessageinput;
ScrollView mscrollView;
TextView displaytextmessage;
String currentGroupName,currentUserID,currentUserName,currentdata,currenttime;
FirebaseAuth auth;
DatabaseReference userRef, GroupNameRef,GroupMessgaeKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("GroupName").toString();

        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        dis();
        GetUserInfo();
    }

    private void dis() {
        mtoolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendmessagebtn=findViewById(R.id.send_message_btn);
        sendmessagebtn.setOnClickListener(this);
        usermessageinput=findViewById(R.id.input_group_message);
        mscrollView=findViewById(R.id.my_scroll_view);
        displaytextmessage=findViewById(R.id.group_chat_text_display);
    }


    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void GetUserInfo() {
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName=snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send_message_btn:
                saveMessageInfoToDB();
                usermessageinput.setText("");
                mscrollView.fullScroll(ScrollView.FOCUS_DOWN);
                break;
        }
    }

    private void saveMessageInfoToDB() {
        String message=usermessageinput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "please write message...", Toast.LENGTH_SHORT).show();
        }else {
            //to get data that message send in do it
            Calendar colfordata=Calendar.getInstance();
            SimpleDateFormat currentdataformat=new SimpleDateFormat("MMM dd, yyyy");
            currentdata=currentdataformat.format(colfordata.getTime());
            //to get time that message send in do it
            Calendar colfortime=Calendar.getInstance();
            SimpleDateFormat currenttimeformat=new SimpleDateFormat("hh:mm a");
            currenttime=currenttimeformat.format(colfortime.getTime());

            HashMap<String,Object>groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessgaeKeyRef=GroupNameRef.child(messageKey);

            HashMap<String,Object>messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("data",currentdata);
            messageInfoMap.put("time",currenttime);
            GroupMessgaeKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator iterator=snapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            displaytextmessage.append(chatName+":\n"+chatMessage+"\n"+chatTime+"\n\n\n");
            mscrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}