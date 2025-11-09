// file: MapsActivity.java
package com.example.hearme.activities.home.emergency;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.hearme.R;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

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

        // --- (FIX 1) Add the setupHeader call ---
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
                Toast.makeText(MapsActivity.this, "Lokasi belum tersedia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- (FIX 2) This method now uses the correct ID 'btn_back_header' ---
    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            // Find the back button *within* the header
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
            // --- (FIX 3) This is your original code to lock the map ---
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }
        ensureLocationPermissionsAndPin();
    }

    private void ensureLocationPermissionsAndPin() {
        // (This is your original code)
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
        // (This is your original code)
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
        // (This is your original code)
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
                    Toast.makeText(MapsActivity.this, "Tidak mendapatkan lokasi", Toast.LENGTH_SHORT).show();
                }
                fusedClient.removeLocationUpdates(this);
            }
        };

        try {
            fusedClient.requestLocationUpdates(req, cb, null);
        } catch (SecurityException se) {
            Toast.makeText(MapsActivity.this, "Perlukan kebenaran lokasi", Toast.LENGTH_SHORT).show();
        }
    }

    private void placePinAndCenter(LatLng pos) {
        // (This is your original code)
        if (mMap == null || pos == null) return;
        if (pinnedMarker == null) {
            pinnedMarker = mMap.addMarker(new MarkerOptions().position(pos).title("Lokasi Anda").draggable(false));
        } else {
            pinnedMarker.setPosition(pos);
        }
        tvSelected.setText(String.format("Pinned: %.6f, %.6f", pos.latitude, pos.longitude));
        CameraPosition cam = new CameraPosition.Builder().target(pos).zoom(16f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam), 700, null);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        // (This is your original code)
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_LOCATION_PERMS) {
            boolean granted = false;
            for (int r : results) if (r == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) pinDeviceLocation();
            else Toast.makeText(this, "Kebenaran lokasi ditolak", Toast.LENGTH_LONG).show();
        }
    }
}