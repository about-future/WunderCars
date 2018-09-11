package com.aboutfuture.wundercars.model;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class WunderClusterRenderer extends DefaultClusterRenderer<WunderClusterItem> {

    public WunderClusterRenderer(Context context, GoogleMap map, ClusterManager<WunderClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<WunderClusterItem> cluster) {
        return cluster.getSize() > 15;
    }
}
