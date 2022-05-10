package com.example.Unbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Unbox.auth.LoginActivity;
import com.example.Unbox.frinds.findFriendActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
Toolbar mtoolbar;
ViewPager myviewpager;
TabLayout mytabLayout;
TabsAccesstorAdapter mytabsAccesstorAdapter;
FloatingActionButton mrequest_btn;

FirebaseAuth auth;
DatabaseReference Rootref;
String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth=FirebaseAuth.getInstance();

        Rootref= FirebaseDatabase.getInstance().getReference();
        dis();

        mrequest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestFragment requestFragment=new RequestFragment();
                requestFragment.show(getSupportFragmentManager(),"Requests");
            }
        });
    }

    private void dis() {
        mtoolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("UnBox");
        myviewpager=findViewById(R.id.main_tabs_pager);
        mytabsAccesstorAdapter=new TabsAccesstorAdapter(getSupportFragmentManager());
        myviewpager.setAdapter(mytabsAccesstorAdapter);
        mytabLayout=findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewpager);
        mrequest_btn=findViewById(R.id.request_fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();

       FirebaseUser currentuser=auth.getCurrentUser();
        if(currentuser==null)
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        else {
            updateUserStates("online");
            verifyuserexistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser=auth.getCurrentUser();
        if (currentuser!=null){
            updateUserStates("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentuser=auth.getCurrentUser();
        if (currentuser!=null){
            updateUserStates("offline");
        }
    }

    private void verifyuserexistance() {
        String currentuserID=auth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name").exists())){
                }else {
                    sendusertoSettingctivity();                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_option){
             updateUserStates("offline");
             auth.signOut();
             startActivity(new Intent(MainActivity.this,LoginActivity.class));
         }
        if(item.getItemId()==R.id.main_setting_option){
            startActivity(new Intent(MainActivity.this,settingActivity.class));
        }
        if(item.getItemId()==R.id.main_find_friends_option){
            startActivity(new Intent(MainActivity.this, findFriendActivity.class));
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");
        final EditText GroupNameField=new EditText(MainActivity.this);
        GroupNameField.setHint("  Name");
        builder.setView(GroupNameField);
        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupname=GroupNameField.getText().toString();
                if(TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "please enter group name", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewGroup(groupname);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void createNewGroup(String groupname) {
        Rootref.child("Groups").child(groupname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupname+"is created successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendusertoSettingctivity() {
        Intent Settingintent =new Intent(MainActivity.this, settingActivity.class);
        startActivity(Settingintent);
    }

    private void updateUserStates(String state){
        String saveCurrentDate,saveCurrentTime;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object>onlineStatusMap=new HashMap<>();
        onlineStatusMap.put("time",saveCurrentTime);
        onlineStatusMap.put("date",saveCurrentDate);
        onlineStatusMap.put("sate",state);

        currentUserID=auth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentUserID).child("userState").updateChildren(onlineStatusMap);
    }
}