package com.team.ambulancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotActivity extends AppCompatActivity {

    EditText email;
    Button sendbtn;
    ImageView imageView;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        getSupportActionBar().hide();

        email = (EditText) findViewById(R.id.iemail_forgot);
        sendbtn = (Button) findViewById(R.id.send_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar_forgot);
        imageView = findViewById(R.id.close);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPassword();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendPassword() {
        String entrdEmail = email.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);

        if(entrdEmail.equals("")) {
            Toast.makeText(ForgotActivity.this, "Please enter the E-mail.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmail(entrdEmail)) {
            Toast.makeText(ForgotActivity.this,"Please enter valid Email.",Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(entrdEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(ForgotActivity.this, "Password Reset Link sent!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    finish();
                } else {
                    Toast.makeText(ForgotActivity.this, "Error, please check the details.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean isEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        overridePendingTransition(0, R.anim.slide_out);
        super.finish();
    }
}
