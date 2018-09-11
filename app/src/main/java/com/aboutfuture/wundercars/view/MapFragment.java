package com.aboutfuture.wundercars.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aboutfuture.wundercars.MainActivity;
import com.aboutfuture.wundercars.R;
import com.aboutfuture.wundercars.model.Location;
import com.aboutfuture.wundercars.model.WunderClusterItem;
import com.aboutfuture.wundercars.model.WunderClusterRenderer;
import com.aboutfuture.wundercars.utils.WunderUtils;
import com.aboutfuture.wundercars.viewmodel.LocationsViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.maps.GoogleMap.*;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, OnMapClickListener,
        ClusterManager.OnClusterClickListener<WunderClusterItem>,
        ClusterManager.OnClusterItemClickListener<WunderClusterItem> {

    private final static int PERMISSION_REQUEST_CODE = 903;
    private final static String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final static String PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final static String MAP_ZOOM_KEY = "map_zoom";
    private final static String MAP_LOCATION_TARGET_KEY = "map_location_target";

    private MapView mMapView;
    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private boolean mPermissionsGranted = false;
    private boolean isClicked = false;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private ClusterManager<WunderClusterItem> mClusterManager;
    private int[] mScreenSize;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private float mMapZoom;
    private LatLng mMapLocationTarget;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MAP_ZOOM_KEY)) {
                mMapZoom = savedInstanceState.getFloat(MAP_ZOOM_KEY);
            }
            if (savedInstanceState.containsKey(MAP_LOCATION_TARGET_KEY)) {
                mMapLocationTarget = savedInstanceState.getParcelable(MAP_LOCATION_TARGET_KEY);
            }
        }

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (isServiceAvailable()) {
            mMapView = view.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);

            // Request runtime permissions, if device SDK is Marshmallow or above
            if (Build.VERSION.SDK_INT >= 23) {
                requestLocationPermissions();
            }

            // Create an instance of the fused location provider client
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivityCast());

            // Create a callback for location updates
            createLocationCallback();

            // Save the screen size, for the map bounds dimensions
            mScreenSize = WunderUtils.getScreenSize(getActivityCast());
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Create an instance of the map and cluster manager
        mMap = googleMap;
        mClusterManager = new ClusterManager<>(getActivityCast(), mMap);

        // Set a custom cluster renderer, that creates clusters for groups with more than 15 markers
        mClusterManager.setRenderer(new WunderClusterRenderer(getContext(), mMap, mClusterManager));

        // Set listeners for map and cluster manager
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnCameraIdleListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        // Setup ViewModel
        setupViewModel();

        // Create location request
        createLocationRequest();

        // Enable user's location on map
        enableLocationOnMap();

        // Get current location
        getDeviceCurrentLocation();
    }

    // Create a new instance of ViewModel and attach an observer that notices any chance in the database.
    private void setupViewModel() {
        LocationsViewModel locationsViewModel = ViewModelProviders.of(this).get(LocationsViewModel.class);
        locationsViewModel.getLocations().observe(this, new Observer<List<Location>>() {
            @Override
            public void onChanged(@Nullable List<Location> locations) {
                if (locations != null) {
                    // If locations are available, create all the clusters, markers and map bounds.
                    createClustersAndBounds(locations);
                }
            }
        });
    }

    // Enable user's location on map, if permissions were granted
    private void enableLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(getActivityCast(), PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivityCast(), PERMISSION_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable user's location on map
            mMap.setMyLocationEnabled(true);
        }
    }

    // Create a location request instance and set the updating intervals and priority
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Create a callback for receiving location events
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // Every time the location is updated, make sure location is enabled
                if (!mMap.isMyLocationEnabled()) {
                    enableLocationOnMap();
                }
            }
        };
    }

    // Add items to cluster manager and generate map bounds based on each item.
    private void createClustersAndBounds(List<Location> locations) {
        // For each item in our locations list:
        // create a marker as a cluster item and add it to the cluster manager.
        // include a map bound, so when the camera is moved, will include all the markers.
        for (Location location : locations) {
            LatLng position = new LatLng(location.getCoordinates()[1], location.getCoordinates()[0]);
            mClusterManager.addItem(new WunderClusterItem(position, location.getName()));
            mBounds.include(position);
        }
        // Cluster our markers
        mClusterManager.cluster();

        // If there is a previous map location, restore it
        if (mMapLocationTarget != null) {
            restoreMap();
        } else {
            // Otherwise, try to set the new map bounds using the mapBounds.
            try {
                LatLngBounds mapBounds = mBounds.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        mapBounds, mScreenSize[0], mScreenSize[1], 0));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    // Restore previous map location
    private void restoreMap() {
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapLocationTarget, mMapZoom));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show all hidden markers
    @Override
    public void onMapClick(LatLng latLng) {
        for (Marker mark : mMarkers) {
            mark.setVisible(true);
        }

        // Set isClicked value to false, so the next time user clicks a marker,
        // the process of hiding the rest of the markers is restarted.
        isClicked = false;
    }

    // Zoom in on the clicked cluster and show all it's markers
    @Override
    public boolean onClusterClick(Cluster<WunderClusterItem> cluster) {
        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the new bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set isClicked value to false, so the next time user clicks a marker,
        // the process of hiding the rest of the markers is restarted.
        isClicked = false;

        return true;
    }

    // When user clicks on a marker, hide all other markers and show the car's number as a title.
    // If user clicks again on the same marker, show all hidden markers and hide the title.
    @Override
    public boolean onClusterItemClick(WunderClusterItem wunderClusterItem) {
        isClicked = !isClicked;

        mMarkers = new ArrayList<>(mClusterManager.getMarkerCollection().getMarkers());
        for (Marker marker : mMarkers) {
            // If it's the clicked marker
            if (wunderClusterItem.getTitle().equals(marker.getTitle())) {
                // If isClicked is true, show the title (car's number)
                if (isClicked) {
                    marker.showInfoWindow();
                } else {
                    // Otherwise, hide the car number
                    marker.hideInfoWindow();
                }
            } else {
                // Otherwise, if it's not the clicked marker, hide it or show it,
                // depending on isClicked value.
                marker.setVisible(!isClicked);
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
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        stopLocationUpdates();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mMap != null) {
            // Save map's position
            outState.putParcelable(MAP_LOCATION_TARGET_KEY, mMap.getCameraPosition().target);
            // Save map's zoom
            outState.putFloat(MAP_ZOOM_KEY, mMap.getCameraPosition().zoom);
        }

        mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    null);
        } catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    // Check if GooglePlayServices is available
    private boolean isServiceAvailable() {
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

    // Request location permissions
    private void requestLocationPermissions() {
        String[] permissions = {PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(getActivityCast(),
                PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivityCast(),
                    PERMISSION_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivityCast(), permissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivityCast(), permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    // If one of the permissions was denied, set mPermissionsGranted to false and return
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                        return;
                    }
                }
                // If the app reached this point, all permissions were granted
                mPermissionsGranted = true;
            }
        }
    }

    // Get current location
    private void getDeviceCurrentLocation() {
        try {
            if (mPermissionsGranted || Build.VERSION.SDK_INT < 23) {
                mFusedLocationProviderClient.getLastLocation();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Set the coordinates upon selecting a car from the list
    public void setMapLocation(LatLng coordinates) {
        mMapLocationTarget = coordinates;
        mMapZoom = 19;
    }
}
