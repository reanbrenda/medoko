package com.team.ambulancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HealthStatusActivity extends AppCompatActivity {

    EditText height, weight, age, hereditary, chronic, allergic, remarks;
    Spinner heightUnit, weightUnit;
    TextView save;
    ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_status);
        getSupportActionBar().hide();

        close = findViewById(R.id.close_status);
        save = findViewById(R.id.save_health_details);
        height = findViewById(R.id.i_height);
        weight = findViewById(R.id.i_weight);
        age = findViewById(R.id.i_age);
        hereditary = findViewById(R.id.i_hereditary);
        chronic = findViewById(R.id.i_chronic);
        allergic = findViewById(R.id.i_allergic);
        remarks = findViewById(R.id.i_remarks);

        heightUnit = findViewById(R.id.unit_height);
        weightUnit = findViewById(R.id.unit_weight);


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String h, w, Age, hereditry, chronc, alergy, remrks, hUnit, wUnit;

                h = height.getText().toString().trim();
                w = weight.getText().toString().trim();
                Age = age.getText().toString().trim();
                hereditry = hereditary.getText().toString().trim();
                chronc = chronic.getText().toString().trim();
                alergy = allergic.getText().toString().trim();
                remrks = remarks.getText().toString().trim();

                hUnit = heightUnit.getSelectedItem().toString().trim();
                wUnit = weightUnit.getSelectedItem().toString().trim();

                HealthStatus healthStatus = new HealthStatus(h + " " + hUnit, w + " " + wUnit, Age +  " Yrs", hereditry, chronc, alergy, remrks);

                String profile = getIntent().getStringExtra("PROFILE");
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Profile Data").child(profile).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("healthStatus");
                databaseReference.setValue(healthStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HealthStatusActivity.this, "Details Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });


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
