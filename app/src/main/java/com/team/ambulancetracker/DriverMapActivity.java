package com.team.ambulancetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// this activity is for driver's location on the map

public class    DriverMapActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    NavigationView navigationView;
    DrawerLayout drawer;
    Button menubtn, swipe_up;
    LinearLayout customerDetails;
    TextView cName, cNumber, name, disease;
    Marker pickup;
    String username;
    ImageView callCustomer;
    int count = 0;


    String customid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        swipe_up = findViewById(R.id.swipeup_customerdetails);
        customerDetails = findViewById(R.id.customer_details_layout);
        cName = findViewById(R.id.customer_name);
        disease = findViewById(R.id.customer_disease);
        cNumber = findViewById(R.id.customer_number);
        callCustomer = findViewById(R.id.call_customer);
        username = getIntent().getStringExtra("USERNAME");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getAssignedCustomer();

        //Navigation drawer

        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        menubtn = findViewById(R.id.menu1);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });

        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        name.setText(username);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getAssignedCustomer() {
        final String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("Driver").child(driverId).child("CUSTOMER_ID");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    customid = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                } else {
                    customid = "";
                    if(pickup != null) {
                        pickup.remove();
                    }
                    if(valueEventListener != null) {
                        assignedCustomerRef.removeEventListener(valueEventListener);
                    }
                    if(valueEventListener2 != null) {
                        driverRef.removeEventListener(valueEventListener2);
                    }
                    swipe_up.setVisibility(View.GONE);
                    customerDetails.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener valueEventListener;
    private void getAssignedCustomerPickupLocation() {

        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customid).child("l");

        valueEventListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customid.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if(map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(0) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng pickupLatLng = new LatLng(locationLat, locationLng);
                    pickup = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Your Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup)));
                    fillCustomerDetails(customid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private DatabaseReference driverRef;
    private ValueEventListener valueEventListener2;

    private void fillCustomerDetails(String customid) {
        driverRef = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("User").child(customid);
        valueEventListener2 = driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals(true)){
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                String name = user.getName();
                final String no = user.getPhone();
                HealthStatus healthStatus = user.getHealthStatus();
                String chronic = healthStatus.getChronic();

                cName.setText(name);
                cNumber.setText(no);
                if(chronic.equals("") && chronic.equals("No") && chronic.equals("No.")) {
                    disease.setVisibility(View.GONE);
                } else{
                    disease.setText("Respond to " + chronic);
                }
                customerDetails.setVisibility(View.VISIBLE);
                callCustomer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + no));
                        startActivity(intent);
                    }
                });
                swipe_up.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.getUiSettings().setRotateGesturesEnabled(false);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if( user == null) {
            return;
        }
        String userId = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire = new GeoFire(databaseReference);
        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if( user== null) {
            return;
        }
        String userId = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire = new GeoFire(databaseReference);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        count = 0;
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            if(count==1){
                finishAffinity();
            } else
                Toast.makeText(DriverMapActivity.this,"Press BACK again to exit", Toast.LENGTH_SHORT).show();
            count++;
        }
    }


    public void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_about_you) {
            Intent intent = new Intent(DriverMapActivity.this, HealthStatusActivity.class);
            intent.putExtra("PROFILE", getIntent().getStringExtra("PROFILE"));
            overridePendingTransition(R.anim.fade_out, R.anim.slide_in);
            startActivity(intent);

        }
        else if (id == R.id.my_profile) {
            Intent intent = new Intent(DriverMapActivity.this, myprofileinfo.class);
            intent.putExtra("PROFILE", getIntent().getStringExtra("PROFILE"));
            overridePendingTransition(R.anim.fade_out, R.anim.slide_in);
            startActivity(intent);

        }
        else if (id == R.id.first_aid_info) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://nhcps.com/lesson/cpr-first-aid-first-aid-basics/")));
        }
        else if(id == R.id.nav_share) {
            Intent intent4 = new Intent(Intent.ACTION_SEND);
            final String appPackagename = getApplicationContext().getPackageName();
            String strAppLink = "";

            try {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackagename;
            } catch (android.content.ActivityNotFoundException anfe) {
                strAppLink = "https://play.google.com/store/apps/details?id=" + appPackagename;
            }
            intent4.setType("text/link");
            String shareBody = "Hey! Now call an ambulance without any problems using this free app.\n\nLink: " + strAppLink;

            String shareSub = "HealY";
            intent4.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            intent4.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(intent4,"Share Via."));
        }
        else if(id == R.id.nav_logout) {
            new AlertDialog.Builder(DriverMapActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onStop();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(DriverMapActivity.this, MainActivity.class));
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
