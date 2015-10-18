package com.tugba.gps_googlemaps;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
public class MainActivity extends Activity implements LocationListener {
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocation();
        sendLatLng_in_Server(getLatitude(), getLongitude());
        showGoogleMap(getLatitude(), getLongitude(),"konumum");
    }
    public Location getLocation() {
        try {
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            // GPS durumunu(true/false) elde ettik
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Network durumunu(true/false) elde ettik
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //Network olmadığında bu koşula girer
            } else {
                this.canGetLocation = true;
                //Network Provider'dan ilk lokasyonu aldık
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                //Eğer GPS etkin ise GPS Services kullanarak  latitude/longitude değerlerini alıyoruz
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    /**
     * GPS listener kullanılmasının durdurulması
     * Cihazda Gps kullanımı durdurulduğunda, uygulamada bu metod çağırılır
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(MainActivity.this);
        }
    }
    /**
     * latitude(enlem) değerini donduren metod
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }
    /**
     * longitude(boylam) değerini donduren metod
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }
    @Override
    public void onLocationChanged(Location location) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    /**
     * Google Map üzerinde latitude ve longitude değerlerine göre konumları işaretleyerek gösteren metod
     * @param lat
     * @param lng
     * @param locatioName
     */
    public void showGoogleMap(double lat,double lng,String locatioName){
        LatLng TutorialsPoint = new LatLng(lat, lng);
        GoogleMap googleMap = null;

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            }
            //Haritanın üzerinde bulunan, haritayı büyütüp küçültmek için kullanılan zooming button aktif ettim
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            //Harita üzerinde işaretlenmiş konumlara haritayı büyüterek yani zoomlama yaparak fokuslanmasını yapan kod
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //--
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //Google Map üzerinde konum işaretlemeyi sağlayan imleci olusturan kod
            //title metodu; imlec konulan yere isim vermenizi sağlar
            Marker TP = googleMap.addMarker(new MarkerOptions().position(TutorialsPoint).title(locatioName));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Konumuma en yakın olan yerlerin enlem ve boylam değerlerini sunucudan alarak haritada
    //bu yerlerin işaretlenmesini sağlayan metod
    private void sendLatLng_in_Server(double latitude,double longitude){

        //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String wcfUrl="http://tugbaustundag.com/restaruantMaps.php";
        JSONObject obj=new JSONObject();
        String jsonString="";
        try {
            //Konum değerlerimi sunucuya gönderiyorum...
            obj.put("latitude",latitude);
            obj.put("longitude",longitude);
            HttpClientMy HttpClientMy=new HttpClientMy();
            jsonString=HttpClientMy.callWebService(wcfUrl, obj);

            //Json objesi olusturuyoruz..
            JSONObject jsonResponse = new JSONObject(jsonString);
            //Olusturdugumuz obje üzerinden  json string deki dataları kullanıyoruz..
            JSONArray jArray=jsonResponse.getJSONArray("Android");
            //Konumuma en yakın olan yerlerin enlem ve boylam değerlerini sunucudan aldım.
            for(int i=0;i<jArray.length();i++) {
                JSONObject json_data=jArray.getJSONObject(i);
                String restoran = json_data.getString("restoran");
                Log.w("restoran",restoran);
                Double lat= json_data.getDouble("latitude");
                Double lng= json_data.getDouble("longitude");
                //ve bana en yakın yerleri haritada işaretleyerek göstermek için showGoogleMap metodunu kullandım
                showGoogleMap(lat, lng,restoran);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}