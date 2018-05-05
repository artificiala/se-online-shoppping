package com.example.arta.online_shop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private ProgressDialog mDialog;
    private EditText inputName, inputEmail, inputPassword;
    private Button btnSignUp;
    private Button mGoToLoginButton;

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference mUsersDBref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //assign the views
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputName = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        //Firebase assign
        auth = FirebaseAuth.getInstance();

        //dialog
        mDialog = new ProgressDialog(this);

        /**listen to sign up button click**/
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();


                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please complete all information requested on this form!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isEmailValid(email)) {
                    Toast.makeText(getApplicationContext(), "Wrong type of email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 8 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                signUpUserWithFirebase(name, email, password);
            }
        });
    }

    // Sign Up user in the database
    private void signUpUserWithFirebase(final String name, String email, String password){
        mDialog.setMessage("Please wait...");
        mDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    //there was an error
                    Toast.makeText(SignupActivity.this, "Error " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    final FirebaseUser newUser = task.getResult().getUser();
                    //success creating user, now set display name as name
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    newUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Log.d(SignupActivity.class.getName(), "User profile updated.");
                                        /***CREATE USER IN FIREBASE DB AND REDIRECT ON SUCCESS**/
                                        createUserInDb(newUser.getUid(), newUser.getDisplayName(), newUser.getEmail());

                                    }else{
                                        //error
                                        Toast.makeText(SignupActivity.this, "Error " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });
    }

    // Creating user in the database
    private void createUserInDb(String userId, String displayName, String email){
        mUsersDBref = FirebaseDatabase.getInstance().getReference().child("Users");
        User user = new User(userId, displayName, email);
        mUsersDBref.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    //error
                    Toast.makeText(SignupActivity.this, "Error " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    //success adding user to db as well
                    //go to users chat list
                    goToMainActivity();
                }
            }
        });
    }

    // goToMainActivity
    private void goToMainActivity(){
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    // Checking Email is valid or not
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

}
