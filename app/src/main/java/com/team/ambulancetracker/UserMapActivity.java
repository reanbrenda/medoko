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
import android.media.Image;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class UserMapActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    NavigationView navigationView;
    DrawerLayout drawer;
    Button menubtn, swipe_up, cancelRide;
    LinearLayout drivrLayout;
    TextView dName, dNumber, name;
    ImageView callDriver;
    LocationRequest locationRequest;
    Button request;
    Marker pickupMarker;
    Boolean isRequestPresent = false;
    private LatLng pickupLocation;
    private int count = 0;
    String username = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        swipe_up = findViewById(R.id.swipeup_driverdetails);
        drivrLayout = findViewById(R.id.driver_details_layout);
        dName = findViewById(R.id.driver_name);
        dNumber = findViewById(R.id.driver_number);
        callDriver = findViewById(R.id.call_driver_btn);
        cancelRide = findViewById(R.id.cancel_ride);

        username = getIntent().getStringExtra("USERNAME");

        request = findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRequestPresent = true;

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CustomerRequest");
                GeoFire geoFire = new GeoFire(databaseReference);
                geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

                pickupLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup)));

                request.setText("Finding an Ambulance...");
                request.setClickable(false);
                getClosestAmbulance();
            }
        });

        cancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoQuery.removeAllListeners();
                driverLocationRef.removeEventListener(valueEventListener);
                isRequestPresent = false;

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if(driverFoundId != null) {
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("Driver").child(driverFoundId).child("CUSTOMER_ID");
                    driverRef.removeValue();
                    driverFoundId = null;
                }
                driverFound = false;
                radius = 1;
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CustomerRequest");
                GeoFire geoFire = new GeoFire(databaseReference);
                geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

                if(pickupMarker != null)
                    pickupMarker.remove();
                if(driverMarker != null)
                    driverMarker.remove();
                request.setText("Call an Ambulance");
                if(valueEventListener2 != null)
                    driverRef.removeEventListener(valueEventListener2);

                drivrLayout.setVisibility(View.GONE);
                swipe_up.setVisibility(View.GONE);

                request.setClickable(true);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Navigation drawer

        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        menubtn = findViewById(R.id.menu2);

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



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }




    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
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

    }

    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundId;
    private GeoQuery geoQuery;


    private void getClosestAmbulance() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && isRequestPresent) {
                    driverFound = true;
                    driverFoundId = key;

                    DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("Driver").child(driverFoundId);
                    String customId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("CUSTOMER_ID", customId);
                    driverReference.updateChildren(map);

                    getDriverLocation();
                    request.setText("Looking for Driver's location!");
                    request.setClickable(false);

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound) {
                    radius++;
                    getClosestAmbulance();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker driverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener valueEventListener;
    private void getDriverLocation() {

        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversAvailable").child(driverFoundId).child("l");
        valueEventListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    request.setText("Driver Found !");

                    if(map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(0) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if(driverMarker != null) {
                        driverMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    request.setText("Driver is " + distance + "meters away");
                    request.setClickable(false);

                    if(distance <= 100) {
                        request.setText("Ambulance Reached.");
                        request.setClickable(false);
                    }

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Ambulance").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance)));
                    fillDriverDetails(driverFoundId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private DatabaseReference driverRef;
    private ValueEventListener valueEventListener2;
    private void fillDriverDetails(final String driverId) {

        driverRef = FirebaseDatabase.getInstance().getReference().child("Profile Data").child("Driver").child(driverId);
        valueEventListener2 = driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals(true)){
                    return;
                }
                User driver = dataSnapshot.getValue(User.class);
                String name = driver.getName();
                final String no = driver.getPhone();

                dName.setText(name);
                dNumber.setText(no);
                drivrLayout.setVisibility(View.VISIBLE);
                callDriver.setOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(UserMapActivity.this,"Press BACK again to exit", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UserMapActivity.this, HealthStatusActivity.class);
            intent.putExtra("PROFILE", getIntent().getStringExtra("PROFILE"));
            overridePendingTransition(R.anim.fade_out, R.anim.slide_in);
            startActivity(intent);
        }
        else if (id == R.id.my_profile) {
            Intent intent = new Intent(UserMapActivity.this, myprofileinfo.class);
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

            String shareSub = "MedOkoa";
            intent4.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            intent4.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(intent4,"Share Via."));
        }
        else if(id == R.id.nav_logout) {
            new AlertDialog.Builder(UserMapActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onStop();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(UserMapActivity.this, MainActivity.class));
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
