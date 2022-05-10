package com.example.Unbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settingActivity extends AppCompatActivity implements View.OnClickListener {
Button mupdate;
EditText musername,muserstates;
CircleImageView muserprofileimg;
Toolbar setting_toolbar;
String currentUserID;
FirebaseAuth auth;
DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
    DatabaseReference first=Rootref;
static final int GalleryPick=1;
StorageReference userProfileImgRef=FirebaseStorage.getInstance().getReference();
ProgressDialog loadingbar;
DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();
        userProfileImgRef=FirebaseStorage.getInstance().getReference().child("profileImage");
        RootRef= FirebaseDatabase.getInstance().getReference();
        dis();
        musername.setVisibility(View.INVISIBLE);
        RetriveUserInfo();
    }

    private void dis() {
        mupdate=findViewById(R.id.update_setting_btn);
        musername=findViewById(R.id.set_user_name);
        muserstates=findViewById(R.id.set_profile_states);
        muserprofileimg=findViewById(R.id.profile_image);
        setting_toolbar=findViewById(R.id.setting_toolbar);
        mupdate.setOnClickListener(this);
        muserprofileimg.setOnClickListener(this);
        loadingbar=new ProgressDialog(this);
        setSupportActionBar(setting_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.update_setting_btn:
                updateSetting();
                break;
            case R.id.profile_image:
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK &&data!=null && data.getData()!=null){
            Uri imageuri=data.getData();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                loadingbar.setTitle("Set Profile Image");
                loadingbar.setMessage("please wait your image is updating.....");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resultUri=result.getUri();

                StorageReference filepath=userProfileImgRef.child(currentUserID+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(settingActivity.this, "profile image uploaded successfully..", Toast.LENGTH_SHORT).show();

                            final String downloadUrl=task.getResult().getStorage().getDownloadUrl().toString();
                            Rootref.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(settingActivity.this, "image save in database successfully....", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }else {
                                                String message=task.getException().toString();
                                                Toast.makeText(settingActivity.this, message, Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                        }
                                    });

                        }else {
                            String message=task.getException().toString();
                            Toast.makeText(settingActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });
            }
        }
    }





    private void updateSetting() {
        String setUserName=musername.getText().toString();
        String setUserStates=muserstates.getText().toString();
        if(TextUtils.isEmpty(setUserName)||TextUtils.isEmpty(setUserStates)){
            Toast.makeText(this, "please Enter All Fields...", Toast.LENGTH_SHORT).show();
        }else {
            HashMap<String,Object>profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("states",setUserStates);
            Rootref.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendusertomainactivity();
                        Toast.makeText(settingActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        String message=task.getException().toString();
                        Toast.makeText(settingActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void RetriveUserInfo() {
        first.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if user create account and has ID
                if ((snapshot.exists())&& (snapshot.hasChild("name")&& (snapshot.hasChild("image")) )){
                    String retriveusername=snapshot.child("name").getValue().toString();
                    String retriveStatus=snapshot.child("states").getValue().toString();
                    String retriveImage=snapshot.child("image").getValue(String.class);

                    musername.setText(retriveusername);
                    muserstates.setText(retriveStatus);
                    Picasso.get().load(retriveImage).placeholder(R.drawable.profile_image).into(muserprofileimg);
                }
                else if ((snapshot.exists())&& (snapshot.hasChild("name"))){
                    String retriveusername=snapshot.child("name").getValue().toString();
                    String retriveStatus=snapshot.child("states").getValue().toString();
                    musername.setText(retriveusername);
                    muserstates.setText(retriveStatus);
                }
                else {
                    musername.setVisibility(View.VISIBLE);
                    Toast.makeText(settingActivity.this, "Set & update your profile info...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void sendusertomainactivity() {
        Intent mainintent =new Intent(settingActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}