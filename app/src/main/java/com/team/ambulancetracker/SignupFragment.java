package com.team.ambulancetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFragment extends Fragment {

    EditText name, phone, email, password;
    CheckBox checkBox;
    Button signup;
    String editPass,editName,editEmail,editPhone, editProfile;
    RadioGroup radioGroup;
    RadioButton profileRadioBtn;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        name = view.findViewById(R.id.iname);
        phone = view.findViewById(R.id.iphone);
        email = view.findViewById(R.id.iemail);
        radioGroup = view.findViewById(R.id.radioGroup);
        password = view.findViewById(R.id.ipassword);
        checkBox = view.findViewById(R.id.checkbox_password_signup);
        signup = view.findViewById(R.id.signup);
        progressBar = view.findViewById(R.id.progressBar_signup);

        firebaseAuth = FirebaseAuth.getInstance();

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

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                createUser();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void createUser() {
        /* here, entered password will be retrieved
                   checked if equal, create user on firebase
                   then intent will take screen to TandC Activity
         */

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_signup, null);

        editPass = password.getText().toString();
        editName = name.getText().toString();
        editEmail = email.getText().toString();
        editPhone = phone.getText().toString();

        int selectedId = radioGroup.getCheckedRadioButtonId();
        profileRadioBtn = (RadioButton) view.findViewById(selectedId);

        editProfile = profileRadioBtn.getText().toString();

        if(TextUtils.isEmpty(editPass)) {
            Toast.makeText(getContext(),"Please enter password.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(editName)) {
            Toast.makeText(getContext(),"Please enter your name.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(editPhone)) {
            Toast.makeText(getContext(),"Please enter your phone number.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(editEmail)) {
            Toast.makeText(getContext(),"Please enter your email.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(editPass.length()<6) {
            Toast.makeText(getContext(),"Minimum 6 characters required for Password!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isPhone(editPhone)) {
            Toast.makeText(getContext(),"Please enter valid Phone Number.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmail(editEmail)) {
            Toast.makeText(getContext(),"Please enter valid Email.",Toast.LENGTH_SHORT).show();
            return;
        }


        //create user and put name,email,password and phone in database
        firebaseAuth.createUserWithEmailAndPassword(editEmail,editPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    Toast.makeText(getContext(), "Account Created", Toast.LENGTH_SHORT).show();
                    sendEmailVerification();
                    setDatabase(editProfile);
                    Intent intent = new Intent(getContext(),MainActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                    }
                }
            });
        }
    }

    private void setDatabase(String profileFactor) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Profile Data").child(editProfile);
        User userData = new User(editName, editPhone, editEmail);
        databaseReference.child(firebaseAuth.getUid()).setValue(userData);
    }

    private static boolean isPhone(String str) {
        boolean isit=true;
        if(str.matches("-?\\d+(\\.\\d+)?")&&str.length()==10)
            isit=true;
        else
            isit=false;

        return isit;
    }

    private static boolean isEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

}
