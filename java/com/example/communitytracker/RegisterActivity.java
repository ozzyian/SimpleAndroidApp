package com.example.communitytracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


/**
 * An Activity which enables a user to register
 * an account to the firebase authenticator and pushes
 * the data to the firebase realtime database.
 */
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth authenticator;
    private EditText registerName, registerEmail, registerPassword, registerPhone;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerPhone = findViewById(R.id.registerPhone);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        authenticator = FirebaseAuth.getInstance();

    }

    /**
     * The method is bound to the register button and
     * initiates an account creating session and if successful
     * creates the account and uses the data to the database.
     * @param view
     */
    public void register(View view) {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            registerEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registerPassword.setError("Password is required!");
            return;
        }

        if (password.length() < 6) {
            registerPassword.setError("Password must be at least 6 characters!");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        authenticator.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                    User user = new User(authenticator.getCurrentUser().getUid(), registerEmail.getText().toString(), null, registerName.getText().toString(), registerPhone.getText().toString());
                    usersRef.child(authenticator.getCurrentUser().getUid()).setValue(user);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    progressBar.setVisibility(View.INVISIBLE);
                    finish();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterActivity.this, "failed... " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    /**
     * Method is bound to a textview which starts
     * the login activity and stops itself.
     * @param view
     */
    public void login(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}
