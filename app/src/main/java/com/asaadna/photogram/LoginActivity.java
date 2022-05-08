package com.asaadna.photogram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailTextField,passwordTextField;
    private Button loginButton;
    private FirebaseAuth auth;

    private void startMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish(); //exit the app and do not go back to the login activity..
    }

    public void onLogin(View view) {

        loginButton.setEnabled(false);

        String email = emailTextField.getText().toString();
        String password = passwordTextField.getText().toString();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Fields cannot be empty" , Toast.LENGTH_SHORT).show();
            if(!loginButton.isEnabled()) loginButton.setEnabled(true);
        } else {
            //Firebase Auth Check
            auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                }
            });
        }
    }

    public void onRegister(View view) {
        Toast.makeText(this,"Register",Toast.LENGTH_LONG).show();
    }

    public void onForgotPassword(View view) {
        Toast.makeText(this,"Forgot Password",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check if user already in session
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            startMainActivity();
        } else {
            emailTextField = findViewById(R.id.emailTextField);
            passwordTextField = findViewById(R.id.passwordTextField);
            loginButton = findViewById(R.id.loginButton);
        }
    }
}