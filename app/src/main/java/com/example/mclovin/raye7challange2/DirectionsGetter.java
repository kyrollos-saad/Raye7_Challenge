package com.example.mclovin.raye7challange2;

import com.example.mclovin.raye7challange2.CustomDataTypes;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;



public class DirectionsGetter
{
    String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    String GOOGLE_API_KEY = "AIzaSyAoBNUd_6EUEBkeaBOFv6VNO_lGCbVabZA";
    LatLng fromLatLng, toLatLng;
    CustomDataTypes customDataTypes;

    public DirectionsGetter(LatLng fromLatLng, LatLng toLatLng)
    {
        this.fromLatLng = fromLatLng;
        this.toLatLng = toLatLng;

        customDataTypes = new CustomDataTypes();
    }

    String createURL()//add the multiple routes parameter later
    {
        String stringFromLatLng = fromLatLng.toString();
        stringFromLatLng = stringFromLatLng.substring(10, stringFromLatLng.length()-2);
        String stringToLatLng = toLatLng.toString();
        stringToLatLng = stringToLatLng.substring(10, stringToLatLng.length()-2);

        String astringFromLatLng = "Ramsis+st.+extension+-+beside+NBE+branch+-+Cairo,+Al+Fagalah,+Al+Azbakeyah,+Cairo+Governorate+11432";//test value
        String astringToLatLng = "35+Cleopatra,+Almazah,+Heliopolis,+Cairo+Governorate";//test value

        String bstringFromLatLng = "place_id:ChIJZZy6l4VAWBQRrTrT2P3iXAI";//test value
        String bstringToLatLng = "place_id:ChIJW22wmIoVWBQRV8Q8iyJ2CZ0";//test value

        return DIRECTION_URL_API + "origin=" + stringFromLatLng + "&destination=" + stringToLatLng + "&alternatives=true" + "&key=" + GOOGLE_API_KEY;
    }

    class GetJsonDirections implements Callable<String>
    {
        String stringURL;
        public GetJsonDirections(String stringURL)
        {
            this.stringURL = stringURL;
        }

        @Override
        public String call() throws Exception
        {
            try
            {
                URL url = new URL(stringURL);
                InputStream inputStream = url.openConnection().getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String temp;
                while ((temp = bufferedReader.readLine()) != null)
                    stringBuffer.append(temp);
                return stringBuffer.toString();
            }
            catch (Exception e)
            {
                Log.i("FuckingError!!", "in DirectionsGetter->GetJsonDirections->call(): " + e.toString());
            }
            return null;
        }
    }

    ArrayList parseJSON(String stringJSON) throws Exception
    {
        if (stringJSON == null)
            return null;

        ArrayList<Route> routesArr = new ArrayList<>();
        JSONObject theWholeJSON = new JSONObject(stringJSON);
        JSONArray jsonRoutesArray = theWholeJSON.getJSONArray("routes");

        JSONArray jsonLegs;
        JSONObject jsonRoute;
        Route tempRoute;
        for (int i=0; i<jsonRoutesArray.length(); i++)
        {
            jsonRoute = jsonRoutesArray.getJSONObject(i);
            tempRoute = new Route();

            JSONObject jsonOverviewPolyline = jsonRoute.getJSONObject("overview_polyline");
            jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");

            tempRoute.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            tempRoute.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            tempRoute.startAddress = jsonLeg.getString("start_address");
            tempRoute.endAddress = jsonLeg.getString("end_address");
            tempRoute.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            tempRoute.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            tempRoute.points = decodePolyLine(jsonOverviewPolyline.getString("points"));
            routesArr.add(tempRoute);
        }
        return routesArr;
    }

    List<LatLng> decodePolyLine(String polyline)
    {
        int len = polyline.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<>();
        int lat = 0;
        int lng = 0;

        while (index < len)
        {
            int b;
            int shift = 0;
            int result = 0;
            do
            {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(lat / 100000d, lng / 100000d));
        }
        return decoded;
    }
}
