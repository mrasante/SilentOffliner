package support.esri.com.silentoffliner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.map.CallbackListener;

public class MainActivity extends AppCompatActivity {

    public static final String FEATURE_SERVICE_URL = "http://supt004514.esri.com:6080/arcgis/rest/services/CaliSync/FeatureServer";
    private MapView mapView;
    private FeatureLayer featureLayer;
    private GeodatabaseFeatureServiceTable geodatabaseFeatureServiceTable;
    private Button btnGenerator;
    private Button btnSynchronizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map_view);
        new FeatureLayerLoader().execute(FEATURE_SERVICE_URL);

        btnGenerator = (Button)findViewById(R.id.offline_generator);

        btnGenerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GDBBackgroundService.class);
                intent.putExtra("WhatYouWant", "GenerateOfflineGDB");
                startService(intent);
            }


        });
        btnSynchronizer = (Button)findViewById(R.id.synchronize);
    }



    class FeatureLayerLoader extends AsyncTask<String, Void, GeodatabaseFeatureServiceTable> {

        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "executing", Toast.LENGTH_LONG).show();
                }
            });
        }

        protected GeodatabaseFeatureServiceTable doInBackground(String... urls) {
            geodatabaseFeatureServiceTable = new GeodatabaseFeatureServiceTable(urls[0], 5);
            geodatabaseFeatureServiceTable.initialize(new CallbackListener<GeodatabaseFeatureServiceTable.Status>() {
                @Override
                public void onCallback(GeodatabaseFeatureServiceTable.Status status) {
                    if (status == GeodatabaseFeatureServiceTable.Status.INITIALIZED) {
                        featureLayer = new FeatureLayer(geodatabaseFeatureServiceTable);

                        if (featureLayer != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "adding layer to map", Toast.LENGTH_LONG).show();
                                    GeometryEngine.project(featureLayer.getFullExtent(), featureLayer.getSpatialReference(), mapView.getSpatialReference());
                                    mapView.addLayer(featureLayer);
                                    mapView.setExtent(featureLayer.getFullExtent());
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("InitializationError: ", throwable.getMessage());
                }
            });


            return geodatabaseFeatureServiceTable;
        }

    }
}
