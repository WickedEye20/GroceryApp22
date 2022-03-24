package com.example.groceryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private EditText currentPass, newPass;
    private Button saveChangesBtn;
    private ImageButton backBtn;
    private String pass, accountType;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentPass = findViewById(R.id.currentPass);
        newPass = findViewById(R.id.newPass);
        saveChangesBtn = findViewById(R.id.changePasswordBtn);
        backBtn = findViewById(R.id.backBtn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                ref1.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pass = "" + snapshot.child("password").getValue();
                        accountType = "" + snapshot.child("accountType").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                String currentPassword = currentPass.getText().toString();
                String newPassword = newPass.getText().toString();

                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter all the details..", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!currentPassword.equals(pass)){
                    Toast.makeText(ChangePasswordActivity.this, "Please enter correct password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentPassword.equals(newPassword)){
                    Toast.makeText(ChangePasswordActivity.this, "Please enter different password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(newPassword)){
                    Toast.makeText(ChangePasswordActivity.this, "Please enter all the details..", Toast.LENGTH_SHORT).show();
                    return;
                }


                mProgressDialog.setMessage("Updating...");
                mProgressDialog.show();




                String currentEmail = firebaseUser.getEmail();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentEmail, currentPassword);

// Prompt the user to re-provide their sign-in credentials
                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                                ref.child(mAuth.getUid()).child("password").setValue(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        mProgressDialog.dismiss();

                                                        if (accountType.equals("Buyer")){
                                                            startActivity(new Intent(ChangePasswordActivity.this, ProfileEditBuyerActivity.class));
                                                        }else {
                                                            startActivity(new Intent(ChangePasswordActivity.this, ProfileEditSellerActivity.class));
                                                        }

                                                        Toast.makeText(ChangePasswordActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();

                                                        finish();
                                                    }
                                                });

                                            } else {
                                                Toast.makeText(ChangePasswordActivity.this, "Not Updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });


    }
}