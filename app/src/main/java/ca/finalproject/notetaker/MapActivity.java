package ca.finalproject.notetaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by gustavo on 2017-04-23.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final float ZOOM = 15;
    private GoogleMap map;
    private Intent myIntent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        myIntent = getIntent();

        initMap();
    }

    private void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(MapActivity.this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        gotoLocation(myIntent.getDoubleExtra("currentLatitude",0),myIntent.getDoubleExtra("currentLongitude",0));
    }

    private void gotoLocation(double latitude, double longitude){
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), ZOOM);
        map.moveCamera(camUpdate);
        map.addMarker(
                new MarkerOptions()
                        .title("Picture location")
                        .position(new LatLng(latitude,longitude))
        );
    }
}
