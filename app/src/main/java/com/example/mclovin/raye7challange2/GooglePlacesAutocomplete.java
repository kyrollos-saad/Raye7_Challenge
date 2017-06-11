package com.example.mclovin.raye7challange2;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

public class GooglePlacesAutocomplete implements Callable
{
    String query;
    GoogleApiClient googleApiClient;
    LatLng coordinatesNE, coordinatesSW;

    public GooglePlacesAutocomplete(String query, GoogleApiClient googleApiClient, LatLng coordinatesNE, LatLng coordinatesSW)
    {
        this.query = query;
        this.googleApiClient= googleApiClient;
        this.coordinatesNE = coordinatesNE;
        this.coordinatesSW = coordinatesSW;
    }

    @Override
    public ArrayList<AutocompletePrediction> call() throws Exception
    {
        return autocomplete(query, googleApiClient, coordinatesNE, coordinatesSW);
    }

    public static ArrayList<AutocompletePrediction> autocomplete(CharSequence query, GoogleApiClient googleApiClient, LatLng coordinatesNE, LatLng coordinatesSW)
    {
        if (googleApiClient.isConnected())
        {
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query.toString(), new LatLngBounds(coordinatesNE, coordinatesSW), null);

            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS); //block this thread for 60 seconds at most waiting for a result form the API

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess())
            {
                Log.e("Error!!", "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i("Alert!!", "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            Iterator itr = autocompletePredictions.iterator();
            while (itr.hasNext())
            {

                itr.next();
            }

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        }
        Log.e("Error!!", "Google API client is not connected for autocomplete query.");
        return null;
    }
}
