package com.aboutfuture.wundercars.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "locations")
public class Location {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @SerializedName("address")
    private String address;
    @SerializedName("coordinates")
    @TypeConverters(CoordinatesTypeConverter.class)
    private double[] coordinates;
    @ColumnInfo(name = "engine_type")
    @SerializedName("engineType")
    private String engineType;
    @SerializedName("exterior")
    private String exterior;
    @SerializedName("fuel")
    private int fuel;
    @SerializedName("interior")
    private String interior;
    @SerializedName("name")
    private String name;
    @SerializedName("vin")
    private String vin;

    public Location(int id, String address, double[] coordinates, String engineType, String exterior,
                    int fuel, String interior, String name, String vin) {
        this.id = id;
        this.address = address;
        this.coordinates = coordinates;
        this.engineType = engineType;
        this.exterior = exterior;
        this.fuel = fuel;
        this.interior = interior;
        this.name = name;
        this.vin = vin;
    }

    @Ignore
    public Location(String address, double[] coordinates, String engineType, String exterior,
                    int fuel, String interior, String name, String vin) {
        this.address = address;
        this.coordinates = coordinates;
        this.engineType = engineType;
        this.exterior = exterior;
        this.fuel = fuel;
        this.interior = interior;
        this.name = name;
        this.vin = vin;
    }

    public int getId() { return id; }
    public String getAddress() { return address; }
    public double[] getCoordinates() { return coordinates; }
    public String getEngineType() { return engineType; }
    public String getExterior() { return exterior; }
    public int getFuel() { return fuel; }
    public String getInterior() { return interior; }
    public String getName() { return name; }
    public String getVin() { return vin; }
}
