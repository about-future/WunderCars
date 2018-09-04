package com.aboutfuture.wundercars.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.aboutfuture.wundercars.data.AppDatabase;
import com.aboutfuture.wundercars.model.Location;

import java.util.List;

public class LocationsViewModel extends AndroidViewModel {

    private final LiveData<List<Location>> locations;

    public LocationsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        locations = appDatabase.locationsDao().loadAllLocations();
    }

    public LiveData<List<Location>> getLocations() {
        return locations;
    }
}
