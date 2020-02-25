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



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The launcher activity which collects the users data
 * and pushes it to the database if successful using
 * a Firebase authenticator and database.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FirebaseAuth authenticator;
    private ProgressBar progressBar;


    /**
     * Constructor initiates necessary variables.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        authenticator = FirebaseAuth.getInstance();


    }

    /**
     * Method is bound to the login button and
     * gets the data from the input fields and starts
     * a Firebase login session though the Firebase authenticator.
     * Starts the MainActivity if login is successful.
     * @param view
     */
    public void login(View view) {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password is required!");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        /**
         * authenticator onCompleteListener that starts the mainactivity
         * if successful or shows an error if not successful.
         */
        authenticator.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    progressBar.setVisibility(View.INVISIBLE);
                    finish();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Method is bound to a textview which starts
     * the register activity and stops itself.
     * @param view
     */
    public void register(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }
}
