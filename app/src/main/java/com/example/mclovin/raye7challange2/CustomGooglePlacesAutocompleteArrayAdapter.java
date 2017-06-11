package com.example.mclovin.raye7challange2;

import android.content.Context;
import android.text.style.CharacterStyle;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CustomGooglePlacesAutocompleteArrayAdapter extends ArrayAdapter implements Filterable
{
    private ArrayList<AutocompletePrediction> resultList;
    private ArrayList<String> strValues;
    private GoogleApiClient googleApiClient;
    private LatLng coordinatesNE, coordinatesSW;


    public CustomGooglePlacesAutocompleteArrayAdapter(Context context, int textViewResourceId, GoogleApiClient googleApiClient, LatLng coordinatesNE, LatLng coordinatesSW)
    {
        super(context, textViewResourceId);
        this.googleApiClient = googleApiClient;
        this.coordinatesNE = coordinatesNE;
        this.coordinatesSW = coordinatesSW;
    }

    @Override
    public int getCount()
    {
        return resultList.size();
    }

    @Override
    public String getItem(int index)
    {
        return resultList.get(index).getPrimaryText(null) + "\n" + resultList.get(index).getSecondaryText(null);
    }

    public AutocompletePrediction getItem(int index, Boolean if_you_want_the_other_getItem_function_pass_true)
    {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query)
            {
                FilterResults filterResults = new FilterResults();
                if (query != null)
                {
                    // Retrieve the autocomplete results.
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    Future<ArrayList<AutocompletePrediction>> futureResultList = executorService.submit(new GooglePlacesAutocomplete(query.toString(), googleApiClient, coordinatesNE, coordinatesSW));
                    try
                    {
                        resultList = futureResultList.get();
                    }
                    catch (Exception e) { e.printStackTrace(); }

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                if (results != null && results.count > 0)
                {
                    notifyDataSetChanged();
                } else
                    {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
