// file: MapsActivity.java
package com.example.hearme.activities.home.emergency;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int REQ_LOCATION_PERMS = 1001;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedClient;
    private Marker pinnedMarker;

    private Button btnSelect;
    private TextView tvSelected;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_maps);

        setupHeader();

        btnSelect = findViewById(R.id.btnSelectLocation);
        tvSelected = findViewById(R.id.tvSelected);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mf != null) mf.getMapAsync(this);

        btnSelect.setOnClickListener(v -> {
            if (pinnedMarker != null) {
                LatLng chosen = pinnedMarker.getPosition();
                Intent out = new Intent();
                out.putExtra("lat", chosen.latitude);
                out.putExtra("lng", chosen.longitude);
                setResult(RESULT_OK, out);
                finish();
            } else {
                // ⭐️ USE STRING ⭐️
                Toast.makeText(MapsActivity.this, getString(R.string.toast_location_not_ready), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            View btnBack = header.findViewById(R.id.btn_back_header);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null && mMap.getUiSettings() != null) {
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }

        // ⭐️ Optional: Set dark mode map style if app is in night mode ⭐️
        try {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // To use this, you must create a 'res/raw/map_style_dark.json' file
                // boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
                // if (!success) {
                //     Log.e(TAG, "Style parsing failed.");
                // }
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't apply map style: ", e);
        }

        ensureLocationPermissionsAndPin();
    }

    private void ensureLocationPermissionsAndPin() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fine || !coarse) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION_PERMS);
        } else {
            pinDeviceLocation();
        }
    }

    private void pinDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try { if (mMap != null) mMap.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng device = new LatLng(location.getLatitude(), location.getLongitude());
                        placePinAndCenter(device);
                    } else {
                        requestSingleLocationUpdate();
                    }
                })
                .addOnFailureListener(e -> requestSingleLocationUpdate());
    }

    private void requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION_PERMS);
            return;
        }

        LocationRequest req = LocationRequest.create();
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        req.setNumUpdates(1);
        req.setInterval(0);

        LocationCallback cb = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult lr) {
                Location l = lr.getLastLocation();
                if (l != null) {
                    LatLng device = new LatLng(l.getLatitude(), l.getLongitude());
                    placePinAndCenter(device);
                } else {
                    // ⭐️ USE STRING ⭐️
                    Toast.makeText(MapsActivity.this, getString(R.string.toast_location_not_found), Toast.LENGTH_SHORT).show();
                }
                fusedClient.removeLocationUpdates(this);
            }
        };

        try {
            fusedClient.requestLocationUpdates(req, cb, null);
        } catch (SecurityException se) {
            // ⭐️ USE STRING ⭐️
            Toast.makeText(MapsActivity.this, getString(R.string.toast_need_location_permission), Toast.LENGTH_SHORT).show();
        }
    }

    private void placePinAndCenter(LatLng pos) {
        if (mMap == null || pos == null) return;
        if (pinnedMarker == null) {
            // ⭐️ USE STRING ⭐️
            pinnedMarker = mMap.addMarker(new MarkerOptions().position(pos).title(getString(R.string.maps_marker_title)).draggable(false));
        } else {
            pinnedMarker.setPosition(pos);
        }
        // ⭐️ USE STRING ⭐️
        tvSelected.setText(getString(R.string.maps_pinned_location_value, pos.latitude, pos.longitude));
        CameraPosition cam = new CameraPosition.Builder().target(pos).zoom(16f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam), 700, null);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_LOCATION_PERMS) {
            boolean granted = false;
            for (int r : results) if (r == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) pinDeviceLocation();
                // ⭐️ USE STRING ⭐️
            else Toast.makeText(this, getString(R.string.toast_location_permission_denied), Toast.LENGTH_LONG).show();
        }
    }
}