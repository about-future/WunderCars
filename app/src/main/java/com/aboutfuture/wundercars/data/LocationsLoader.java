package com.aboutfuture.wundercars.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.aboutfuture.wundercars.model.Location;
import com.aboutfuture.wundercars.model.Placemarks;
import com.aboutfuture.wundercars.retrofit.ApiClient;
import com.aboutfuture.wundercars.retrofit.ApiInterface;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;

public class LocationsLoader extends AsyncTaskLoader<List<Location>> {
    private List<Location> cachedLocations;

    public LocationsLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (cachedLocations == null)
            forceLoad();
    }

    @Nullable
    @Override
    public List<Location> loadInBackground() {
        ApiInterface placeMarksApiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Placemarks> call = placeMarksApiInterface.getPlacemarks();

        Placemarks result = null;
        try {
            result = call.execute().body();
        } catch (IOException e) {
            Log.v("Locations Loader", "Error: " + e.toString());
        }

        return Objects.requireNonNull(result).getLocations();
    }

    @Override
    public void deliverResult(List<Location> locations) {
        cachedLocations = locations;
        super.deliverResult(locations);
    }
}
