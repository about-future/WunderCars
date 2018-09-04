package com.aboutfuture.wundercars.retrofit;

import com.aboutfuture.wundercars.model.Location;
import com.aboutfuture.wundercars.model.Placemarks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("locations.json")
    Call<Placemarks> getPlacemarks();
    //Call<List<Location>> getLocations();
}
