package support.esri.com.silentoffliner;

import android.app.IntentService;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTableEditErrors;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.core.tasks.geodatabase.SyncGeodatabaseParameters;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by kwas7493 on 3/2/2017.
 */

public class GDBBackgroundService extends IntentService {

    public static final String SERVICE_NAME = "Geodatabase Service";
    private ProgressDialog progressDialog;
    private Notification notification;


    /**
     * implement the no-argument construct
     */
    public GDBBackgroundService() {
        super(SERVICE_NAME);
    }


    public void onCreate() {
        Log.e("ServicePre", "PreExecute");
    }

    /**
     * This method handles the task of handling the intent that starts the service
     * No other methods needs to be overridden. Check the android documentation at
     * https://developer.android.com/reference/android/app/IntentService.html
     *
     * @param intent - this is the intent that starts this service using the startService
     *               method on the Context.
     */

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.e("TestingService", "Started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MyBackGroundGeodatabaseAsyncTask().execute(intent.getStringExtra("whatYouWant"));
            }
        }).start();
    }


    private class MyBackGroundGeodatabaseAsyncTask extends AsyncTask<String, Void, Intent> {

        private static final String GEODATABASE_SERVICE_URL = "http://supt004514.esri.com:6080/arcgis/rest/services/CaliSync/FeatureServer/5";
        private static final String FEATURESERVICE_URL = "http://supt004514.esri.com:6080/arcgis/rest/services/CaliSync/FeatureServer";
        private static final String PATH_TO_DOWNLOADEDGDB = "pathToDownloaded_geodatabaseFile";
        private FeatureServiceInfo featureServiceInfo;
        private GenerateGeodatabaseParameters geodatabaseParams;
        GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(GEODATABASE_SERVICE_URL, null);
        public Intent syncIntent;
        public Intent generateIntent;
        private Future<String> geodatabase;

        /**
         * Inform the user of the beginning of a background process
         */
        protected void onPreExecute() {

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Starting offline download")
                    .setContentText("background process ongoing this may take a while. Go eat some bbq")
                    .build();
        }

        /**
         * This is where you implement the logic to do the work. Ideal because this
         * takes care of all network calls on a worker thread leaving the UI responsive to
         * user. Fashion out your workflow to ensure the right approach is taken
         *
         * @param whatYouWant
         * @return
         */

        protected Intent doInBackground(String... whatYouWant) {
            Intent intentToBroadcast= null;
            if (whatYouWant[0].equalsIgnoreCase("GenerateOfflineGDB")) {
              intentToBroadcast = performNetworkGDBGeneration();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToBroadcast);
            } else if (whatYouWant[0].equalsIgnoreCase("SynchronizeOfflineGDB")) {
                intentToBroadcast = performOfflineSynchronization();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToBroadcast);
            }

            return null;
        }

        private Intent performOfflineSynchronization() {

            try {

                Geodatabase geodatabase = new Geodatabase(PATH_TO_DOWNLOADEDGDB);
                SyncGeodatabaseParameters syncGeodatabaseParameters = geodatabase.getSyncParameters();
                //TODO: modify the syncGeodatabaseParameters reference as needed
                geodatabaseSyncTask.syncGeodatabase(syncGeodatabaseParameters, geodatabase, new GeodatabaseStatusCallback() {
                    @Override
                    public void statusUpdated(GeodatabaseStatusInfo geodatabaseStatusInfo) {
                        //TODO: update the user
                        if(geodatabaseStatusInfo.getStatus() == GeodatabaseStatusInfo.Status.COMPLETED){
                            syncIntent = new Intent(Constants.BROADCAST_ACTION)
                                    .putExtra(Constants.EXTRAS, geodatabaseStatusInfo.getStatus().toString());
                        }

                    }
                }, new CallbackListener<Map<Integer, GeodatabaseFeatureTableEditErrors>>() {
                    @Override
                    public void onCallback(Map<Integer, GeodatabaseFeatureTableEditErrors> integerGeodatabaseFeatureTableEditErrorsMap) {
                        //TODO: update the user
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        //TODO: report any errors
                    }
                });
            } catch (Exception fNf) {

            }
            return syncIntent;
        }

        private Intent performNetworkGDBGeneration() {

            try {
                featureServiceInfo = geodatabaseSyncTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {
                    @Override
                    public void onCallback(FeatureServiceInfo featureServiceInfo) {
                        //TODO: do some fancy stuff here to let the user know the offline data generation of on-going

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("Error", throwable.getMessage());
                    }

                }).get();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            geodatabaseParams = new GenerateGeodatabaseParameters(featureServiceInfo);
            geodatabase = geodatabaseSyncTask.generateGeodatabase(geodatabaseParams, "offline.geodatabase", true, new GeodatabaseStatusCallback() {
                @Override
                public void statusUpdated(GeodatabaseStatusInfo geodatabaseStatusInfo) {
                    if (geodatabaseStatusInfo.getStatus() == GeodatabaseStatusInfo.Status.COMPLETED) {
                        //TODO: notify user here. You can create a notification or an alarm or whatever
                        generateIntent = new Intent(Constants.BROADCAST_ACTION)
                                .putExtra(Constants.EXTRAS, geodatabaseStatusInfo.getStatus().toString());
                    }
                }
            }, new CallbackListener<String>() {

                @Override
                public void onCallback(String s) {
                    //TODO: notify the user with the callback s
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
            return generateIntent;
        }


        protected void onPostExecute(Intent results) {
            //TODO: clean up code to take care of any resources that might be exposed
        }
    }

    public final class Constants{

        public static final String BROADCAST_ACTION = "";
        public static final String EXTRAS = "";

    }
}
