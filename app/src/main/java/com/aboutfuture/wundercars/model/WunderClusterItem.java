package com.aboutfuture.wundercars.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class WunderClusterItem implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;

    public WunderClusterItem(LatLng latLng, String title) {
        this.position = latLng;
        this.title = title;
    }

    @Override
    public LatLng getPosition() { return position; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getSnippet() { return snippet; }
}
