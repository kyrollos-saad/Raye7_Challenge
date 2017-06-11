package com.example.mclovin.raye7challange2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/*




DISCLAIMER: sorry for the mess :)
I only have 30 minutes left for the dead line, so sorry for the messy code




 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleMap mMap;

    NavigationView navigationView; //manages the items in the navigation drawer
    DrawerLayout drawerLayout; //manages the root of the activity
    ActionBarDrawerToggle actionBarDrawerToggle; //manages the the navigation drawer
    GoogleApiClient googleApiClient;
    AutoCompleteTextView txtViewFrom, txtViewTo;
    LatLng coordinatesNECurrent, coordinatesSWCurrent;
    FromToCoordinates fromToCoordinates;
    ImageButton clearFromButt, clearToButt;
    ImageView locationBut;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initNavigationDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the three-bars icon on the left-most of the action bar

        locationBut = (ImageView) findViewById(R.id.locationBut);
        locationBut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeFromLoc();
            }
        });
    }

    /************************************************** Overrides ************************************************************/

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permissionsToRequest = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
            ActivityCompat.requestPermissions(this, permissionsToRequest, 0);
        }

        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(30.0645428, 31.2531528), 15.0f)));
        mMap.setTrafficEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng) { fromToCoordinates.setToLatLng(latLng); }
        });

        fromToCoordinates = new FromToCoordinates(mMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater(); //make a menu object to manage a XML menu
        menuInflater.inflate(R.menu.action_bar_items, menu); //assigns the action_bar_items menu to menuInflater object
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int itemID = menuItem.getItemId();
        if (itemID == R.id.bell)
            Toast.makeText(this, R.string.makeActionAcc, Toast.LENGTH_LONG).show();
        else if (itemID == R.id.temp)
        {
            popAnAlert("is connected? " + googleApiClient.isConnected() + '\n' + "is connecting? " + googleApiClient.isConnecting());
        }

        if (actionBarDrawerToggle.onOptionsItemSelected(menuItem))
            return true;
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        coordinatesNECurrent = new LatLng(30.061630, 31.257429); //test values
        coordinatesSWCurrent = new LatLng(30.066644, 31.259832); //test values

        final CustomGooglePlacesAutocompleteArrayAdapter customArrayAdapterFrom = new CustomGooglePlacesAutocompleteArrayAdapter(this, android.R.layout.simple_list_item_1, googleApiClient, coordinatesNECurrent, coordinatesSWCurrent);
        txtViewFrom = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewFrom);
        txtViewFrom = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewFrom);
        txtViewFrom.setAdapter(customArrayAdapterFrom);
        txtViewFrom.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Places.GeoDataApi.getPlaceById(googleApiClient, customArrayAdapterFrom.getItem(position, true).getPlaceId()).setResultCallback(new ResultCallback<PlaceBuffer>()
                {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places)
                    {
                        if (places.getStatus().isSuccess())
                            fromToCoordinates.setFromLatLng(places.get(0).getLatLng());
                        places.release();
                    }
                });
            }
        });

        final CustomGooglePlacesAutocompleteArrayAdapter customArrayAdapterTo = new CustomGooglePlacesAutocompleteArrayAdapter(this, android.R.layout.simple_list_item_1, googleApiClient, coordinatesNECurrent, coordinatesSWCurrent);
        txtViewTo = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewTo);
        txtViewTo.setAdapter(customArrayAdapterTo);
        txtViewTo.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Places.GeoDataApi.getPlaceById(googleApiClient, customArrayAdapterTo.getItem(position, true).getPlaceId()).setResultCallback(new ResultCallback<PlaceBuffer>()
                {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places)
                    {
                        if (places.getStatus().isSuccess())
                            fromToCoordinates.setToLatLng(places.get(0).getLatLng());
                        places.release();
                    }
                });
            }
        });

        clearFromButt = (ImageButton)findViewById(R.id.x_from);
        clearFromButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                txtViewFrom.setText("");
                fromToCoordinates.setFromLatLng(null);
            }
        });

        clearToButt = (ImageButton)findViewById(R.id.x_to);
        clearToButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                txtViewTo.setText("");
                fromToCoordinates.setToLatLng(null);
            }
        });
    }

    /************************************************** My Tools ***************************************************************/

    void popAnAlert(Object msg)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(String.format("%s", msg));
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                return;
            }
        });
        alertDialog.show();
    }
    void changeFromLoc()
    {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        List<String> deleteMe = mLocationManager.getProviders(false);
        Location location = mLocationManager.getLastKnownLocation("gps");
        fromToCoordinates.setFromLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /************************************** Code segments for better readability ***********************************************/

    void initNavigationDrawer()
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null); //fixes the grey icons bug (maybe it prevents android from automatically add any tint by itself=

        drawerLayout = (DrawerLayout) findViewById(R.id.root_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState(); //Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        popAnAlert("onConnectionFailed(ConnectionResult result)");
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        popAnAlert("onConnectionSuspended");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}

class FromToCoordinates
{
    LatLng fromLatLng, toLatLng;
    Marker fromMarker, toMarker;
    GoogleMap mMap;

    public FromToCoordinates(GoogleMap mMap)
    {
        this.mMap = mMap;
    }

    public void setFromLatLng(LatLng latLng)
    {
        fromLatLng = latLng;
        if (fromLatLng == null)//if fromLatLng was set to null explicitly
        {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(30.0939351,31.2237256), 10.0f)));
            fromMarker.setVisible(false);
            return;
        }

        fromMarker = mMap.addMarker(new MarkerOptions().position(fromLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_from_marker)));
        if (toLatLng == null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(fromLatLng, 15.0f)));
        else
            drawRoute();
    }
    public void setToLatLng(LatLng latLng)
    {
        toLatLng = latLng;
        if (toLatLng == null)//if toLatLng was set to null explicitly
        {
            zoomOutToCairoNewCairoGiza6thOfOctober();
            toMarker.setVisible(false);
            return;
        }

        toMarker = mMap.addMarker(new MarkerOptions().position(toLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_to_marker)));
        if (fromLatLng == null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(toLatLng, 15.0f)));
        else
            drawRoute();
    }
    public LatLng getFromLatLng()
    {
        return fromLatLng;
    }
    public LatLng setFromLatLng()
    {
        return toLatLng;
    }
    public void drawRoute()
    {
        DirectionsGetter directionsGetter = new DirectionsGetter(fromLatLng, toLatLng);

        String stringURL = directionsGetter.createURL();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(directionsGetter.new GetJsonDirections(stringURL));
        String stringJSON = null;
        try
        {
            stringJSON = (String)future.get();
            Log.i("FuckingJSON", stringJSON);
        }
        catch (Exception e) { Log.i("FuckingError!!", "in MapsActivity->FromToCoordinates->drawRoute(): " + e.toString()); }

        List<com.example.mclovin.raye7challange2.Route> decodedRoutesArr = null;

        try
        {
            decodedRoutesArr = directionsGetter.parseJSON(stringJSON);
        }
        catch (Exception e) { Log.i("FuckingError!!", e.toString()); }

        List<Polyline> polylinePaths = new ArrayList<>();

        for (com.example.mclovin.raye7challange2.Route route : decodedRoutesArr)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            PolylineOptions polylineOptions = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        zoomOutToCairoNewCairoGiza6thOfOctober();
    }
    void zoomOutToCairoNewCairoGiza6thOfOctober()
    {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(30.0939351,31.2237256), 10.0f)));
    }
}
