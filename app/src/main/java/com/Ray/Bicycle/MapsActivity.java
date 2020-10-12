package com.Ray.Bicycle;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private MarkerOptions mMarkerOptions;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    private DrawerLayout drawer;
    private RxOkHttp3 rxOkHttp3 = new RxOkHttp3();
    private RxTimerUtil rxTimerUtil = new RxTimerUtil();
    private CompositeDisposable MapCompositeDisposable = new CompositeDisposable();
    private LatLng RxLocation;
   // private LatLng TestLocation = new LatLng(24.922582, 121.422590);
    private boolean SubFlag = false;
    private RxMapTimer rxTimer = new RxMapTimer();
    private SharedPreferences userSetting;
    private boolean MapReady = false;
    //private double a = 25.079597;
    //private double d = 121.557757;
    //private LatLng c = new LatLng (a,d);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        drawer = findViewById(R.id.drawer_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        InitNavi();
        userSetting = getSharedPreferences("UserSetting" , MODE_PRIVATE);
        mapFragment.getMapAsync(this);
        ButtonListen();

    }
    private void InitNavi(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("地圖");
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void ButtonListen(){
        FloatingActionButton NowGet = findViewById(R.id.getBicycleFAB);
        NowGet.setOnClickListener(v -> NowGetBicycle());
    }
    private void NowGetBicycle(){
        String id = userSetting.getString("id",null);
        if(id == null){
            Toast.makeText(this,"請先設定使用者名稱",Toast.LENGTH_SHORT).show();
            return;
        }
        RxLocation = rxOkHttp3.getLocation(id);
        if(RxLocation == null){
            Toast.makeText(this,"取得定位失敗",Toast.LENGTH_SHORT).show();
            return;
        }
        SetMark(RxLocation,true);
    }
    @Override
    protected void onDestroy() {
        if (disposable != null)
            cancel();
        rxTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (disposable != null)
            cancel();
        rxTimer.cancel();
        super.onStop();
    }

    @Override
    protected void onStart() {
        MapReady = true;
        GetBicycle();
        super.onStart();
    }

    /***********Navigation*************/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_map:
                Intent intent2 = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:
                /*if (!BTConnFlag.Flag) {
                    Toast.makeText(this, "藍芽連線失敗，請先連線藍芽", Toast.LENGTH_SHORT).show();
                    break;
                }*/
                Intent intent3 = new Intent(MapsActivity.this, SettingPage.class);
                startActivity(intent3);
                onStop();
                break;

        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapReady = true;
        getMyLocation();
        //SetMark(c);
        //GetBicycle();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == 100) {
            if (!verifyAllPermissions(grantResults)) {
                Toast.makeText(getApplicationContext(), "No sufficient permissions", Toast.LENGTH_LONG).show();
            } else {
                getMyLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean verifyAllPermissions(int[] grantResults) {

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    protected void SetMark(LatLng RxLocation,boolean Move) {

        System.out.print("Set Mark:");
        System.out.println(RxLocation);
        mMarkerOptions = new MarkerOptions()
                .position(RxLocation)
                //.position(c)
                .title("Destination")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.bicycle48p));
        mMap.addMarker(mMarkerOptions);
        if(MapReady || Move){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RxLocation, 14));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(c, 12));
            MapReady = false;
        }

    }

    private void getMyLocation() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mOrigin = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOrigin,12));
                if (mOrigin != null && mDestination != null)
                    drawRoute();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mMap.setMyLocationEnabled(true);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);

                mMap.setOnMapLongClickListener(latLng -> {
                    mDestination = latLng;
                    //mMap.clear();
                    //mMarkerOptions = new MarkerOptions().position(mDestination).title("Destination");
                    mMarkerOptions = new MarkerOptions().position(RxLocation).title("Destination");
                    mMap.addMarker(mMarkerOptions);
                    if (mOrigin != null && mDestination != null)
                        drawRoute();
                });

            } else {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 100);
            }
        }
    }


    private void drawRoute() {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private void GetBicycle() {

        rxTimer.interval(5000,
                number -> {
                    Log.e("home_show_three", "======MainActivity======" + number);
                    sub();
                    System.out.println(number);
                });

    }

    ObservableOnSubscribe<LatLng> observableOnSubscribe = new ObservableOnSubscribe<LatLng>() {
        @Override
        public void subscribe(ObservableEmitter<LatLng> emitter) {
            System.out.println("Map已經訂閱：subscribe，获取发射器");
            String id = userSetting.getString("id",null);
            if(id == null){
                System.out.println("id null");
                return;
            }
            System.out.println("id:"+id);
            RxLocation = rxOkHttp3.getLocation(id);
            if (RxLocation != null)
                emitter.onNext(RxLocation);
            System.out.println("Map信號發射：onComplete");
        }
    };
    /**
     * 创建被观察者，并带上被观察者的订阅
     */
    Observable<LatLng> observable = Observable.create(observableOnSubscribe);

    Disposable[] disposable = new Disposable[1];

    Observer<LatLng> observer = new Observer<LatLng>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            System.out.println("Map已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(LatLng integer) {
            System.out.println("Map信号接收：onNext " + integer);
            SetMark(integer,false);

        }

        @Override
        public void onError(Throwable e) {
            System.out.println("Map信号接收：onError " + e.getMessage());
            cancel();
        }

        @Override
        public void onComplete() {
            System.out.println("Map信号接收：onComplete");
        }
    };

    public void sub() {
        System.out.println("Map開始訂閱：subscribe");
        observable.subscribe(observer);
    }

    public void cancel() {
        System.out.println("Map取消訂閱：unsubscribe");
        if (disposable[0] != null)
            disposable[0].dispose();
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }

    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {

                DirectionsJSONParser parser = new DirectionsJSONParser();
                jObject = new JSONObject(jsonData[0]);
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }

}


