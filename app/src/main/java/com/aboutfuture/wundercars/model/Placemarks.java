package com.aboutfuture.wundercars.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Placemarks {
    @SerializedName("placemarks")
    private List<Location> locations;

    public Placemarks(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() { return locations; }
}
