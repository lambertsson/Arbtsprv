package com.example.lambertsson.arbtsprv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make http request
        makeRequest();
    }

    private void makeRequest() {
        final TextView myTextView = (TextView) findViewById(R.id.myTextViewID);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.google.com";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 200 characters of the response string.
                        myTextView.setText("Response is: " + response.substring(0, 200));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        myTextView.setText("That didn't work!");
                    }
                }
        );
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
