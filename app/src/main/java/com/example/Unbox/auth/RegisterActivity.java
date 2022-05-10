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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
EditText museremail,muserpass;
Button mcreateAcount_btn;
TextView malreadyhaveaccount;
ProgressDialog mprogressDialog;  //it loading dialog
FirebaseAuth auth;
DatabaseReference Rootref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth=FirebaseAuth.getInstance();
        Rootref= FirebaseDatabase.getInstance().getReference();
        dis();
    }

    private void dis() {
        mcreateAcount_btn=findViewById(R.id.register_btn);
        museremail=findViewById(R.id.register_email);
        muserpass=findViewById(R.id.register_pass);
        malreadyhaveaccount=findViewById(R.id.already_have_account_link);
        mprogressDialog=new ProgressDialog(this);
        mcreateAcount_btn.setOnClickListener(this);
        malreadyhaveaccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register_btn:
                CreateNewAccount();
                break;
            case R.id.already_have_account_link:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
        }

    }

    private void CreateNewAccount() {
        String email=museremail.getText().toString();
        String pass=muserpass.getText().toString();

        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
            Toast.makeText(this, "please enter all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            mprogressDialog.setTitle("Creating New Account");
            mprogressDialog.setMessage("Please Wait , while Creating New Account For You");
            mprogressDialog.setCanceledOnTouchOutside(true);
            mprogressDialog.show();

            auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        String currentUserID=auth.getCurrentUser().getUid();
                        Rootref.child("Users").child(currentUserID).setValue("");

                        Rootref.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                        sendusertomainactivity();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        mprogressDialog.dismiss();
                    }else {
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        mprogressDialog.dismiss();
                    }
                }
            });
        }

    }

    private void sendusertomainactivity() {
        Intent mainintent =new Intent(RegisterActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}