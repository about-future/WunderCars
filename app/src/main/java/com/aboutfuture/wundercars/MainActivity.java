package com.aboutfuture.wundercars;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.aboutfuture.wundercars.view.LocationsFragment;
import com.aboutfuture.wundercars.view.MapFragment;
import com.aboutfuture.wundercars.view.SectionsPagerAdapter;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LocationsFragment.OnCarClickListener {

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_tabs)
    TabLayout tabLayout;
    @BindView(R.id.main_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        setTitle("");

        // Create the adapter that will return a fragment for each of each section of the activity
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    // When user clicked on a car in the list, create a new map fragment, replace the old one
    // and set map's center to be car's location.
    @Override
    public void onCarSelected(LatLng coordinates) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setMapLocation(coordinates);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        // Switch page to show map
        mViewPager.setCurrentItem(1);
    }
}
