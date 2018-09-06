package com.aboutfuture.wundercars.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aboutfuture.wundercars.MainActivity;
import com.aboutfuture.wundercars.R;
import com.aboutfuture.wundercars.model.Location;
import com.aboutfuture.wundercars.viewmodel.LocationsViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.gms.maps.GoogleMap.*;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, OnMarkerClickListener, OnMapClickListener {

    private final static int FINE_PERMISSION_REQUEST_CODE = 9031;
    private final static int COARSE_PERMISSION_REQUEST_CODE = 9032;
    private final static int PERMISSION_REQUEST_CODE = 903;
    private final static String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final static String PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private MapView mMapView;

    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private boolean isClicked = false;
    private boolean mPermissionsGranted = false;
    private LatLng mLocation;

    // TODO: group markers
    // TODO: Finalize permissions
    // TODO: Get device location
    // TODO: Create card view for RecyclerView

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //ButterKnife.bind(this, view);

        if (isServiceAvailable()) {
            // Request runtime permissions if device SDK is Marshmallow or above
            if (Build.VERSION.SDK_INT >= 23) {
                requestLocationPermissions();
                if (mPermissionsGranted) {
                    mMapView = view.findViewById(R.id.map);
                    mMapView.onCreate(savedInstanceState);
                    mMapView.getMapAsync(this);
                }
            } else {
                mMapView = view.findViewById(R.id.map);
                mMapView.onCreate(savedInstanceState);
                mMapView.getMapAsync(this);
            }

//            // If permissions were granted or device is Pre-Marshmallow, load the map
//            if ((mPermissionsGranted) || (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23)) {
//                mMapView = view.findViewById(R.id.map);
//                mMapView.onCreate(savedInstanceState);
//
////                try {
////                    MapsInitializer.initialize(getActivityCast());
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//
//                mMapView.getMapAsync(this);
            //}
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Setup ViewModel and generate all the markers and map bounds
        setupViewModel();

        // Set a listener for marker click
        mMap.setOnMarkerClickListener(this);

        // Set a listener for map click
        mMap.setOnMapClickListener(this);

        // Get current location
        getDeviceLocation();

        if (ActivityCompat.checkSelfPermission(getActivityCast(), PERMISSION_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivityCast(), PERMISSION_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    private void setupViewModel() {
        LocationsViewModel locationsViewModel = ViewModelProviders.of(this).get(LocationsViewModel.class);
        locationsViewModel.getLocations().observe(this, new Observer<List<Location>>() {
            @Override
            public void onChanged(@Nullable List<Location> locations) {
                if (locations != null) {
                    createMarkersAndBounds(locations);
                }
            }
        });
    }

    private void createMarkersAndBounds(List<Location> locations) {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        // For each location in our list:
        // create a marker and add the mMarkers list.
        // include a map bound, so when the camera is moved, will include all the markers.
        for (Location location : locations) {
            LatLng position = new LatLng(
                    location.getCoordinates()[1],
                    location.getCoordinates()[0]);
            mMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(location.getName())));
            bounds.include(position);
        }

        if (mPermissionsGranted || Build.VERSION.SDK_INT < 23) {
            if (mLocation != null) {
                //TODO: fix my location bounds
                bounds.include(getDeviceLocation());
                bounds.include(mLocation);
            }
        }

        try {
            LatLngBounds mapBounds = bounds.build();
            // TODO: set correct width and height
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 500, 500, 0));
        } catch (IllegalStateException e) {
            Log.e("Exception", e.toString());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String id = marker.getId();

        if (isClicked) {
            marker.hideInfoWindow();
            //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 500,500,0));
        } else {
            marker.showInfoWindow();
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }

        isClicked = !isClicked;

        for (Marker mark : mMarkers) {
            if (!id.equals(mark.getId())) {
                mark.setVisible(!isClicked);
            }
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        for (Marker mark : mMarkers) {
            mark.setVisible(true);
        }
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }

    // Check if GooglePlayServices is available
    public boolean isServiceAvailable() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivityCast());
        if (available == ConnectionResult.SUCCESS) {
            // Google Play services is working fine and we don't need to do anything.
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // An error occurred, but it's resolvable. Show a Google specific dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(getActivityCast(), available, 344).show();
        } else {
            // Unfortunately, the users device doesn't have GooglePlayServices available
            // and can't install it either.
            Toast.makeText(getContext(), getString(R.string.no_map_request), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Request localizations permissions
    private void requestLocationPermissions() {
        String[] permissions = {PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(getActivityCast(),
                PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivityCast(),
                PERMISSION_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mPermissionsGranted = true;
                getDeviceLocation();
            } else {
                ActivityCompat.requestPermissions(getActivityCast(), permissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivityCast(), permissions, PERMISSION_REQUEST_CODE);
        }

//        if (ContextCompat.checkSelfPermission(getActivityCast(),
//                PERMISSION_ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            permissionFineGranted = true;
//        } else {
//            requestLocationPermission(PERMISSION_ACCESS_FINE_LOCATION, FINE_PERMISSION_REQUEST_CODE);
//        }
//
//        if (ContextCompat.checkSelfPermission(getActivityCast(),
//                PERMISSION_ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            permissionCoarseGranted = true;
//        } else {
//            requestLocationPermission(PERMISSION_ACCESS_COARSE_LOCATION, COARSE_PERMISSION_REQUEST_CODE);
//        }
    }

    private void requestLocationPermission(final String permission, final int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivityCast(), permission)) {
            new AlertDialog.Builder(getActivityCast())
                    .setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_message))
                    .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivityCast(), new String[]{permission}, code);
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(getActivityCast(), new String[]{permission}, code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                        return;
                    }
                }
                mPermissionsGranted = true;
            }
        }

//        if (requestCode == FINE_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                permissionFineGranted = true;
//            } else {
//                permissionFineGranted = false;
//                Toast.makeText(getActivityCast(), "Localization will not be available!", Toast.LENGTH_SHORT).show();
//            }
//        } else if (requestCode == COARSE_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                permissionCoarseGranted = true;
//            } else {
//                permissionCoarseGranted = false;
//                Toast.makeText(getActivityCast(), "Localization will not be available!", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    private LatLng getDeviceLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivityCast());

        try {
            if (mPermissionsGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            android.location.Location currentLocation = (android.location.Location) task.getResult();
                            mLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        } else {
                            Toast.makeText(getActivityCast(), getString(R.string.location_unknown), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            // Don't do anything.
        }

        return mLocation;
    }
}
