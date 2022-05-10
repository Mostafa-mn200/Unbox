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
import android.widget.Toast;

import com.example.Unbox.MainActivity;
import com.example.Unbox.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
Button mSendVerificationCode_btn, mverify_btn;
EditText mInputPhoneNumber, mInputVerificationCode;
PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
String mVerificationId;
PhoneAuthProvider.ForceResendingToken mResendToken;
FirebaseAuth auth;
ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        auth=FirebaseAuth.getInstance();
        dis();
        loadingbar=new ProgressDialog(this);
    }

    private void dis() {
        mSendVerificationCode_btn=findViewById(R.id.send_ver_code_btn);
        mverify_btn=findViewById(R.id.verify_btn);
        //mverify_btn.setOnClickListener(this);
        mInputPhoneNumber=findViewById(R.id.phone_number_input);
        mInputVerificationCode=findViewById(R.id.verification_code_input);


////////////////////////////////////////////////////////////////////////////////////////////////////
        mSendVerificationCode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber=mInputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "phone number is required", Toast.LENGTH_SHORT).show();
                }else {
                    loadingbar.setTitle("Phone Verification");
                    loadingbar.setMessage("please wait, while we are authenticating your phone...");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,
                            PhoneLoginActivity.this, callbacks);
                }
            }
        });

        mverify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendVerificationCode_btn.setVisibility(View.INVISIBLE);
                mInputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode=mInputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingbar.setTitle("Verification code");
                    loadingbar.setMessage("please wait, while we are verify Verification code...");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);;
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });





        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingbar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, please enter correct phone number with correct country code... ", Toast.LENGTH_SHORT).show();
                mSendVerificationCode_btn.setVisibility(View.VISIBLE);
                mInputPhoneNumber.setVisibility(View.VISIBLE);
                mverify_btn.setVisibility(View.INVISIBLE);
                mInputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "code has been sent", Toast.LENGTH_SHORT).show();

                mSendVerificationCode_btn.setVisibility(View.INVISIBLE);
                mInputPhoneNumber.setVisibility(View.INVISIBLE);
                mverify_btn.setVisibility(View.VISIBLE);
                mInputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingbar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "you are login successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
                            finish();
                        }
                        else {
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}