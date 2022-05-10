package com.example.Unbox.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Unbox.MainActivity;
import com.example.Unbox.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button mloginbtn,mphonebtn;
    EditText museremail,muserpass;
    TextView mneedacount,mforgitpass;
    ProgressDialog mprogressDialog;  //it loading dialog
    FirebaseAuth auth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth=FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        dis();
    }

    private void dis() {
        mloginbtn=findViewById(R.id.login_btn);
        mphonebtn=findViewById(R.id.phone_login_btn);
        museremail=findViewById(R.id.login_email);
        muserpass=findViewById(R.id.login_pass);
        mneedacount=findViewById(R.id.need_new_account_link);
        mprogressDialog=new ProgressDialog(this);
        mloginbtn.setOnClickListener(this);
        mphonebtn.setOnClickListener(this);
        mneedacount.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_btn:
                AllowUserToLogin();
                break;
            case R.id.phone_login_btn:
                startActivity(new Intent(LoginActivity.this,PhoneLoginActivity.class));
                break;
            case R.id.need_new_account_link:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
        }
    }

    private void AllowUserToLogin() {
        String email=museremail.getText().toString();
        String pass=muserpass.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
            Toast.makeText(this, "please enter all fields", Toast.LENGTH_SHORT).show();
        }else {
            mprogressDialog.setTitle("Sign In");
            mprogressDialog.setMessage("Please Wait .......");
            mprogressDialog.setCanceledOnTouchOutside(true);
            mprogressDialog.show();
            auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserID=auth.getCurrentUser().getUid();
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        usersRef.child(currentUserID).child("device_token")
                                .setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sendusertomainactivity();
                                            Toast.makeText(LoginActivity.this, "Logged in successful", Toast.LENGTH_SHORT).show();
                                            mprogressDialog.dismiss();
                                        }
                                    }
                                });


                    }else {
                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        mprogressDialog.dismiss();
                    }
                }
            });
        }
    }
    private void sendusertomainactivity() {
        Intent mainintent =new Intent(LoginActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}