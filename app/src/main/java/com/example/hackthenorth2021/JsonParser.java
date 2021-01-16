package com.example.hackthenorth2021;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String, String> parseJsonObject(JSONObject object) throws JSONException {
        //Initialize hash map
        HashMap<String, String> placeList = new HashMap<>();
        Log.d("JSONParser", "JSON object= " + placeList.toString());
        try {
            String name = object.getString("name");
            String latitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");

            String longitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");

            placeList.put("name", name);
            placeList.put("lat", latitude);
            placeList.put("lng", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeList;
    }

    private List<HashMap<String, String>> parseJsonArray(JSONArray jsonArray) {
        List<HashMap<String, String>> placeList = new ArrayList<>();
        for (int i=0; i < jsonArray.length(); i++) {
            try {
                // initialize hashmap
                HashMap<String, String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                // add data in hash map list
                placeList.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placeList;
    }

    List<HashMap<String, String>> parseResult(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
        Log.d("JSON data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parseJsonArray(jsonArray);
    }
}
