package support.esri.com.silentoffliner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kwas7493 on 3/2/2017.
 */

public class GDBBackgroundBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: handle intents here
        /*
         * Your activity should have intent filters that can filter the
         * specific broadcast actions from the background service
         *
         * Also remember to register the broadcast receiver and intent filters to the system as follow
         *
         * IntentFilter intentFilter = new IntentFilter(GDBBackgroundService.Constants.BROADCAST_ACTION);
         * GDBBackgroundBroadcastReceiver gdbBackgroundBroadcastReceiver = new GDBBackgroundBroadcastReceiver();
         * LocalBroadcastManager.getInstance(context).registerReceiver(gdbBackgroundBroadcastReceiver, intentFilter);
         *
         * this should be done your activity of interest
         */


    }
}
