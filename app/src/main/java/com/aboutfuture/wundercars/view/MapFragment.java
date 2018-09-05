package com.aboutfuture.wundercars.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aboutfuture.wundercars.MainActivity;
import com.aboutfuture.wundercars.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.gms.maps.GoogleMap.*;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, OnMarkerClickListener {
    private GoogleMap mMap;
    private List<Marker> mMarkers = new ArrayList<>();
    private boolean isClicked = false;
    //@BindView(R.id.map)
    private MapView mMapView;
    private LatLngBounds mBounds;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //ButterKnife.bind(this, view);
        mMapView = view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        return view;
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng hamburg = new LatLng(53.59301, 10.07526);
        LatLng hamburg2 = new LatLng(53.54847, 9.99622);

        mMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(hamburg)
                .title("Marker in hamburg")));
        mMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(hamburg2)
                .title("Marker in hamburg23")));
        mMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(new LatLng(53.56388,10.07838))
                .title("Marker in hamburg3")));

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for(int i = 0; i < mMarkers.size(); i++) {
            bounds.include(mMarkers.get(i).getPosition());
        }
        mBounds = bounds.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 500,500,0));

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
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

        for(int i = 0; i < mMarkers.size(); i++) {
            if (!id.equals(mMarkers.get(i).getId())) {
                mMarkers.get(i).setVisible(!isClicked);
            }
        }

        return true;
    }


}
