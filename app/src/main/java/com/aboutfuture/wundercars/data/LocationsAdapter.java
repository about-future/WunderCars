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
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {
    private final Context mContext;
    private List<Location> mLocations = new ArrayList<Location>(){};
    private final ListItemClickListener mOnClickListener;

    public LocationsAdapter(Context context, ListItemClickListener listener){
        mContext = context;
        mOnClickListener = listener;
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
        holder.carVinTextView.setText(mLocations.get(position).getVin());
    }

    @Override
    public int getItemCount() {
        return mLocations != null ? mLocations.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        @BindView(R.id.car_vin)
        TextView carVinTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            double[] coordinates = mLocations.get(getAdapterPosition()).getCoordinates();
            mOnClickListener.onItemClickListener(new LatLng(coordinates[1], coordinates[0]));
        }
    }

    // Interface needed to pass the location from the adapter to location fragment
    public interface ListItemClickListener {
        void onItemClickListener(LatLng location);
    }

    public void setLocations(List<Location> locations) {
        mLocations = locations;
        notifyDataSetChanged();
    }
}
