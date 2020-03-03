package com.team.ambulancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SigninFragment extends Fragment {
    EditText email, password;
    CheckBox checkBox;
    Button signin;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    TextView forgot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        email = view.findViewById(R.id.iemail_login);
        password = view.findViewById(R.id.iPassword_login);
        checkBox = view.findViewById(R.id.checkbox_password_signin);
        signin = view.findViewById(R.id.signin);
        progressBar = view.findViewById(R.id.progressBar_login);
        forgot = view.findViewById(R.id.forgotPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ForgotActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    // show password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }   else {
                    // hide password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loginUser();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void loginUser() {
        /* here, entered details will be retrieved
                   checked if correct,
                   then intent will take screen to User Activity
                 */
        String login_email,login_password;
        login_email = email.getText().toString().trim();
        login_password = password.getText().toString();

        if(TextUtils.isEmpty(login_email)) {
            Toast.makeText(getContext(),"Please enter your email.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmail(login_email)) {
            Toast.makeText(getContext(),"Please enter valid Email.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(login_password)) {
            Toast.makeText(getContext(),"Please enter password.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(login_password.length()<6) {
            Toast.makeText(getContext(),"Minimum 6 characters Required!",Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(login_email,login_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getContext(), "Logging in...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(),MainActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                } else {
                    Toast.makeText(getContext(), task.getException().getMessage() , Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static boolean isEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

}
