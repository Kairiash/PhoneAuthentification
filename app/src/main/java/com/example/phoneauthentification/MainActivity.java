package com.example.phoneauthentification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.phoneauthentification.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private static  final String TAG = "MAIN_TAG";
    private FirebaseAuth firebaseAuth;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneLl.setVisibility(View.VISIBLE);
        binding.codeLl.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait......");
        dialog.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                Log.d(TAG, "onCodeSent: "+verificationId);
                mVerificationId = verificationId;
                forceResendingToken = token;
                dialog.dismiss();

                binding.phoneLl.setVisibility(View.GONE);
                binding.codeLl.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Verification code sent...", Toast.LENGTH_SHORT).show();
                binding.codeSentDesc.setText("Please Enter the verification code that was sent \\nto "+binding.phoneEt.getText().toString().trim());

            }
        };

        binding.phoneContinueBtn.setOnClickListener(view -> {

            String phone = binding.phoneEt.getText().toString().trim();
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(MainActivity.this, "Please enter phone number!", Toast.LENGTH_SHORT).show();
            } else {
                startVerification(phone);
            }

        });

        binding.codeSubmitBtn.setOnClickListener(view -> {

            String code = binding.codeEt.getText().toString().trim();
            if (TextUtils.isEmpty(code)){
                Toast.makeText(MainActivity.this, "Please enter verification code....", Toast.LENGTH_SHORT).show();
            } else {
                verifyPhoneNumber(mVerificationId, code);
            }

        });

        binding.resendCodeTv.setOnClickListener(view -> {

            String phone = binding.phoneEt.getText().toString().trim();
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(MainActivity.this, "Please enter phone number!", Toast.LENGTH_SHORT).show();
            } else {
                resendVerification(phone,forceResendingToken);
            }

        });

    }

    private void verifyPhoneNumber(String verificationId, String code) {

        dialog.setMessage("Verifying Phone Number");
        dialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        dialog.setMessage("Signing In");
        dialog.show();

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        dialog.dismiss();
                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(MainActivity.this, "Logged in as "+phone, Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void resendVerification(String phone, PhoneAuthProvider.ForceResendingToken token) {

        dialog.setMessage("Resending code.....");
        dialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone).setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this).setCallbacks(mCallbacks)
                .setForceResendingToken(token).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void startVerification(String phone) {

        dialog.setMessage("Verifying Phone Number");
        dialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone).setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this).setCallbacks(mCallbacks).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }
}