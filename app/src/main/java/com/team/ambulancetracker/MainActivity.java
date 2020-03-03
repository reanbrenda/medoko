package com.team.ambulancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    ProgressBar progressBar;
    LinearLayout noUser;
    Button start;
    private final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);
        requestLocationPermission();

        noUser = findViewById(R.id.noUser);
        progressBar = findViewById(R.id.progressBar_main);
        start = findViewById(R.id.start_btn);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser == null) {
            progressBar.setVisibility(View.INVISIBLE);
            noUser.setVisibility(View.VISIBLE);
            start.setVisibility(View.VISIBLE);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    TabbedDialog dialogFragment = new TabbedDialog();
                    dialogFragment.show(ft,"dialog");
                }
            });

        } else {
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("User").child(firebaseAuth.getUid());
            final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("Driver").child(firebaseAuth.getUid());
            databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user==null) {
                        return;
                    }
                    progressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(MainActivity.this, UserMapActivity.class);
                    intent.putExtra("USERNAME", user.getName());
                    intent.putExtra("PROFILE", "User");
                    startActivity(intent);
                    finishAffinity();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user==null) {
                        return;
                    }
                    progressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(MainActivity.this, DriverMapActivity.class);
                    intent.putExtra("USERNAME", user.getName());
                    intent.putExtra("PROFILE", "Driver");
                    startActivity(intent);
                    finishAffinity();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE};
        if(EasyPermissions.hasPermissions(this, perms)) {
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
}
