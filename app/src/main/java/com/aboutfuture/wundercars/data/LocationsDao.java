package com.aboutfuture.wundercars.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.aboutfuture.wundercars.model.Location;

import java.util.List;

@Dao
public interface LocationsDao {
    @Query("SELECT * FROM locations")
    LiveData<List<Location>> loadAllLocations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocations(List<Location> locations);
}
