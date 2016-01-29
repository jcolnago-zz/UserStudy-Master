package com.example.jessica.masterproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jessica.masterproject.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapFragment extends MapFragment {
    private MapFragment mMapFrag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMapFrag = (MapFragment)getFragmentManager().findFragmentById(R.id.location_map);
        mMapFrag.getMapAsync(new OnMapReadyCallback () {
            @Override
            public void onMapReady(final GoogleMap googleMap){
                setUpMap(googleMap);
            }
        });

        View v = inflater.inflate(R.layout.pending, null);
        return v;
    }

    private void setUpMap(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title(
                "Marker"));
    }
}