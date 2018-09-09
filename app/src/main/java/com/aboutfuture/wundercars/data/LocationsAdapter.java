package com.aboutfuture.wundercars.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aboutfuture.wundercars.R;
import com.aboutfuture.wundercars.model.Location;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {
    private final Context mContext;
    private List<Location> mLocations = new ArrayList<Location>(){};

    public LocationsAdapter(Context context){
        mContext = context;
    }

    @NonNull
    @Override
    public LocationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.location_card_item, parent, false);
        view.setFocusable(false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationsAdapter.ViewHolder holder, int position) {
        holder.carNumberTextView.setText(mLocations.get(position).getName());
        holder.carAddressTextView.setText(mLocations.get(position).getAddress());
        holder.carExteriorTextView.setText(mLocations.get(position).getExterior());
        holder.carInteriorTextView.setText(mLocations.get(position).getInterior());
        holder.carEngineTextView.setText(mLocations.get(position).getEngineType());
        holder.carFuelTextView.setText(String.valueOf(mLocations.get(position).getFuel()));
    }

    @Override
    public int getItemCount() {
        return mLocations != null ? mLocations.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.car_number)
        TextView carNumberTextView;
        @BindView(R.id.car_address)
        TextView carAddressTextView;
        @BindView(R.id.car_exterior)
        TextView carExteriorTextView;
        @BindView(R.id.car_interior)
        TextView carInteriorTextView;
        @BindView(R.id.car_engine)
        TextView carEngineTextView;
        @BindView(R.id.car_fuel)
        TextView carFuelTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setLocations(List<Location> locations) {
        mLocations = locations;
        notifyDataSetChanged();
    }
}
