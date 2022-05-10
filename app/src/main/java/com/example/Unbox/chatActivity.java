 package com.example.Unbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity {

    String messageReceiverID,messageReceiverName,messageReceiverImg,messageSenderID;
    TextView username,userlastseen;
    CircleImageView userImg;
    Toolbar chatToolbar;
    FirebaseAuth auth;
    DatabaseReference RootRef;
    ImageButton send_Message_btn,send_files_btn;
    EditText messageInputText;

    private final List<messages>messagesList= new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private messageAdapter messageAdapter;
    RecyclerView userMessageList;

    String saveCurrentDate,saveCurrentTime;
    private String checker="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth=FirebaseAuth.getInstance();
        messageSenderID=auth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImg=getIntent().getExtras().get("visit_user_Img").toString();

        dis();
        username.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImg).placeholder(R.drawable.profile_image).into(userImg);

        send_Message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

       // DisplayLastSeen();

        send_files_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(chatActivity.this);
                builder.setTitle("Select The File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Images"),438);
                        }
                    }
                });
                builder.show();
            }
        });

    }


    private void dis() {

        chatToolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        username=findViewById(R.id.custom_profile_name);
        userlastseen=findViewById(R.id.custom_user_last_seen);
        userImg=findViewById(R.id.custom_profile_Img);
        send_Message_btn=findViewById(R.id.send_message_btn);
        messageInputText=findViewById(R.id.input_message);
        send_files_btn=findViewById(R.id.send_files_btn);

        messageAdapter=new messageAdapter(messagesList);
        userMessageList=findViewById(R.id.privet_message_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);


        loadingbar=new ProgressDialog(this);


        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            loadingbar.setTitle("Sending File");
            loadingbar.setMessage("please wait, we are sending that file...");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();


            fileUri=data.getData();


            if (checker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef="Message/"+messageSenderID+"/"+messageReceiverID;
                final String messageReceiverRef="Message/"+messageReceiverID+"/"+messageSenderID;

                DatabaseReference UserMessageKeyRef=RootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push();

                final String MessagePushID=UserMessageKeyRef.getKey();

                StorageReference filePath=storageReference.child(MessagePushID + "." + "jpg");
                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl=task.getResult();
                            myUrl=downloadUrl.toString();

                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageReceiverID);
                            messageTextBody.put("messageID",MessagePushID);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);


                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+MessagePushID,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+MessagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingbar.dismiss();
                                        Toast.makeText(chatActivity.this, "message send successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingbar.dismiss();
                                        Toast.makeText(chatActivity.this,  "error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else {
                loadingbar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error..", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void DisplayLastSeen(){
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("sate")){
                    String state=snapshot.child("userState").child("sate").getValue().toString();
                    String data=snapshot.child("userState").child("date").getValue().toString();
                    String time=snapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                       userlastseen.setText("online");
                    }
                    else if (state.equals("offline")){
                        userlastseen.setText("Last Seen"+data+" "+time);
                    }
                }
                else {
                    userlastseen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    @Override
    protected void onStart() {
        super.onStart();
        DisplayLastSeen();
        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        messages mmessages=snapshot.getValue(messages.class);
                        messagesList.add(mmessages);
                        messageAdapter.notifyDataSetChanged();
                        // بتاعت ال scroll
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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




    private void SendMessage() {
        String messageText=messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(chatActivity.this, "first write your message", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef="Message/"+messageSenderID+"/"+messageReceiverID;
            String messageReceiverRef="Message/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference UserMessageKeyRef=RootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();

            String MessagePushID=UserMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",MessagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);


            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+MessagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+MessagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(chatActivity.this, "message send successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(chatActivity.this,  "error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }
    }
}