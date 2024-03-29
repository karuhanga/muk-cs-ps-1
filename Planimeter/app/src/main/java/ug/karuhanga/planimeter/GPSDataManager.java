package ug.karuhanga.planimeter;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 * @author Karuhanga Lincoln
 * on 7/30/17.
 * GPS Data collection and Analysis Class
 */

final class GPSDataManager implements LocationListener, Constants {
    private Context context;
    private LocationManager locationManager;
    private List<Location> locations;
    private List<LatLng> latLngs;
    private int len_locations;
    private List<Location> turns;
    private int len_turns;

    GPSDataManager(Context context){
        this.context= context;
        this.locationManager= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locations= new ArrayList<>();
        latLngs= new ArrayList<>();
        len_locations= 0;
        turns= new ArrayList<>();
        len_turns= 0;
    }

    //start recording location changes (possibly raise and throw security exception if GPS Access not permitted)
    void startRecording() throws SecurityException{
        this.locations= new ArrayList<>();
        this.latLngs= new ArrayList<>();
        len_locations= 0;
        this.turns= new ArrayList<>();
        len_turns= 0;
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time_sensitivity, distance_sensitivity, this);
    }

    //add last recorded location to turn list on turn
    void recordTurn(){
        if (len_locations>0) {
            Location turn= locations.get(len_locations - 1);
            if (len_turns>0){
                if (turns.get(len_turns-1).equals(turn)){
                    return;
                }
            }
            //TODO Remove Unnecessary Toast
            Toast.makeText(this.context, "Adding Turn: "+turn.toString(), Toast.LENGTH_SHORT).show();
            turns.add(turn);
            len_turns++;
        }
    }

    void stopRecording(){
        //de-register location change listener on collection complete
        this.locationManager.removeUpdates(this);
        this.generateLatLngs();
    }
    
    void generateLatLngs(){
        if (len_locations<3){
            return;
        }
        for (Location location : this.locations) {
            this.latLngs.add(new LatLng(location.getLatitude(),location.getLongitude()));
        }
        latLngs.add(latLngs.get(0));
    }

    boolean evaluateArea(){
        if (len_locations<3){
            return false;
        }
        double result= SphericalUtil.computeArea(this.latLngs);
        renderResult(result);
        return true;
    }

    private void renderResult(double result) {
        PolylineOptions polylineOptions= new PolylineOptions();
        for (int i = 0; i < len_locations; i++) {
            polylineOptions.add(latLngs.get(i), latLngs.get(i+1)).width(5).color(Color.RED);
        }

        ((GPSResultListener) context).displayResult(polylineOptions, Double.valueOf(result), latLngs.get(0));
    }

    //Location Listener Methods
    @Override
    public void onLocationChanged(Location location) {
        //TODO Remove Unnecessary Toast
        ((GPSResultListener) context).dataReceived();
        Toast.makeText(this.context, ".", Toast.LENGTH_SHORT).show();
        locations.add(location);
        len_locations++;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
