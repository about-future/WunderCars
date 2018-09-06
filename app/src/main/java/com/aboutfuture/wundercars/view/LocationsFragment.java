package com.aboutfuture.wundercars.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aboutfuture.wundercars.MainActivity;
import com.aboutfuture.wundercars.R;
import com.aboutfuture.wundercars.data.AppDatabase;
import com.aboutfuture.wundercars.data.AppExecutors;
import com.aboutfuture.wundercars.data.LocationsAdapter;
import com.aboutfuture.wundercars.data.LocationsLoader;
import com.aboutfuture.wundercars.model.Location;
import com.aboutfuture.wundercars.utils.LocationsPreferences;
import com.aboutfuture.wundercars.utils.NetworkUtils;
import com.aboutfuture.wundercars.viewmodel.LocationsViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationsFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Location>> {

    private static final int LOCATIONS_LOADER_ID = 903;
    private final String LOCATIONS_RECYCLER_POSITION_KEY = "locations_recycler_position";
    private int mLocationsPosition = RecyclerView.NO_POSITION;

    private LinearLayoutManager mLinearLayoutManager;
    private LocationsAdapter mLocationsAdapter;
    //private List<Location> mLocations;
    private AppDatabase mDb;

    @BindView(R.id.locations_rv)
    RecyclerView mLocationsRecyclerView;
    @BindView(R.id.locations_progress_bar)
    ProgressBar mLocationProgressBar;
    @BindView(R.id.locations_no_connection_cloud)
    ImageView mNoConnectionImageView;
    @BindView(R.id.locations_no_connection_message)
    TextView mNoConnectionMessage;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(LOCATIONS_RECYCLER_POSITION_KEY)) {
            mLocationsPosition = savedInstanceState.getInt(LOCATIONS_RECYCLER_POSITION_KEY);
        }

        View view = inflater.inflate(R.layout.fragment_locations_list, container, false);
        ButterKnife.bind(this, view);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLocationsRecyclerView.setLayoutManager(mLinearLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                mLocationsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mLocationsRecyclerView.addItemDecoration(mDividerItemDecoration);
        mLocationsRecyclerView.setHasFixedSize(false);
        mLocationsAdapter = new LocationsAdapter(getContext());
        mLocationsRecyclerView.setAdapter(mLocationsAdapter);

        mDb = AppDatabase.getInstance(getContext());

        // If locations were already loaded once, just query the DB and display them,
        // otherwise init the loader and get data from server
        if (LocationsPreferences.getLocationsStatus(getActivityCast())) {
            // Load data from DB
            setupViewModel();
        } else {
            // Get data from internet
            getData();
        }

        return view;
    }

    private void setupViewModel() {
        LocationsViewModel locationsViewModel = ViewModelProviders.of(this).get(LocationsViewModel.class);
        locationsViewModel.getLocations().observe(this, new Observer<List<Location>>() {
            @Override
            public void onChanged(@Nullable List<Location> locations) {
                if (locations != null) {
                    mLocationsAdapter.setLocations(locations);
                    //mLocations = locations;
                    restorePosition();
                }
            }
        });
    }

    private void getData() {
        // Get of refresh data, if there is a network connection
        if (NetworkUtils.isConnected(getActivityCast())) {
            mLocationsRecyclerView.setVisibility(View.VISIBLE);
            mLocationProgressBar.setVisibility(View.VISIBLE);
            mNoConnectionImageView.setVisibility(View.INVISIBLE);
            mNoConnectionMessage.setVisibility(View.INVISIBLE);

            //Init or restart locations loader
            getLoaderManager().restartLoader(LOCATIONS_LOADER_ID, null, this);
        } else {
            // Otherwise, if missions were loaded before, just display a toast
            if (LocationsPreferences.getLocationsStatus(getActivityCast())) {
                Toast.makeText(getActivityCast(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, display a connection error message and a no connection icon
                mLocationsRecyclerView.setVisibility(View.INVISIBLE);
                mLocationProgressBar.setVisibility(View.GONE);
                mNoConnectionImageView.setVisibility(View.VISIBLE);
                mNoConnectionMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }

    private void restorePosition() {
        if (mLocationsPosition == RecyclerView.NO_POSITION) mLocationsPosition = 0;
        // Scroll the RecyclerView to mPosition
        mLocationsRecyclerView.scrollToPosition(mLocationsPosition);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Save RecyclerView state
        mLocationsPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

        outState.putInt(LOCATIONS_RECYCLER_POSITION_KEY, mLocationsPosition);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<List<Location>> onCreateLoader(int loaderId, @Nullable Bundle args) {
        switch (loaderId) {
            case LOCATIONS_LOADER_ID:
                // If the loaded id matches locations loader, return a new location loader
                return new LocationsLoader(getActivityCast());
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Location>> loader, final List<Location> data) {
        mLocationProgressBar.setVisibility(View.GONE);

        switch (loader.getId()) {
            case LOCATIONS_LOADER_ID:
                if (data != null && data.size() > 0) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            // If data indeed was retrieved, insert locations into the DB
                            mDb.locationsDao().insertLocations(data);
                            // This loader is activate the first time the activity is open.
                            // Set the locations status preference to TRUE, so the next time
                            // data needs to be loaded, the app will opt for loading it from DB
                            LocationsPreferences.setLocationsStatus(getContext(), true);
                        }
                    });
                }

                // Setup the view model, especially if this is the first time the data is loaded
                setupViewModel();

                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Location>> loader) {
        mLocationsAdapter.setLocations(null);
    }
}
