package com.example.pocketbook.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pocketbook.R;
import com.example.pocketbook.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUp extends AppCompatActivity {

    EditText Name, Email, Username, Password;
    String name, email, username, password, profileImageUrl;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private static final String USER = "users";
    private static final String TAG = "SignUpActivity";
    private User user;
    private Button registerConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        Name = findViewById(R.id.Name);
        Email = findViewById(R.id.EmailReg);
        Username = findViewById(R.id.userName);
        Password = findViewById(R.id.PasswordReg);

//        findViewById(R.id.AddProfilePicture).setOnClickListener(this);
//        findViewById(R.id.userImage).setOnClickListener(this);
//        img = (ImageView) findViewById(R.id.userImage);

        registerConfirm = findViewById(R.id.RegisterConfirm);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();
        registerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = Name.getText().toString().trim();
                email = Email.getText().toString().trim();
                username = Username.getText().toString().trim();
                password = Password.getText().toString().trim();

                if(name.isEmpty()){
                    Name.setError("Name Required!");
                    Name.requestFocus();
                    return;
                }

                if(username.isEmpty()){
                    Username.setError("User Name Required!");
                    Username.requestFocus();
                    return;
                }

                if(email.isEmpty()){
                    Email.setError("Need an email!");
                    Email.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Email.setError("Enter a valid email!.");
                    Email.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    Password.setError("Password Required");
                    Password.requestFocus();
                    return;
                }

                if(password.length() < 6){
                    Password.setError("Password needs to be of length greater than 6.");
                    Password.requestFocus();
                    return;
                }

                user = new User(name,email,username,password);
                Register(email,password,name,username);
            }
        });

    }

    public void Register(String Email, String Password, String name, String username){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUp.this, "Account Created Successfully!",
                                    Toast.LENGTH_SHORT).show();
                            mFirestore = FirebaseFirestore.getInstance();
                            mFirestore.collection("users").document(username).set(user);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "User Already Exists with the email",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });


    }

    public void updateUI(FirebaseUser currentUser){
        String keyID = mDatabase.push().getKey();
        mDatabase.child(keyID).setValue(user);
        Intent loginIntent = new Intent(this,MainActivity.class);
        startActivity(loginIntent);

    }

}