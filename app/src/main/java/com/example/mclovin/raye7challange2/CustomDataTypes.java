package com.example.mclovin.raye7challange2;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class CustomDataTypes
{
}
class Route
{
    Distance distance;
    Duration duration;
    String endAddress;
    LatLng endLocation;
    String startAddress;
    LatLng startLocation;
    List<LatLng> points;
}
class Distance
{
    public String text;
    int value;

    Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }
}
class Duration
{
    public String text;
    int value;

    Duration(String text, int value) {
        this.text = text;
        this.value = value;
    }
}