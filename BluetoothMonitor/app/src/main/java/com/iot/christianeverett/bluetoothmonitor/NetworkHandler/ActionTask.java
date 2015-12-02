package com.iot.christianeverett.bluetoothmonitor.NetworkHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.iot.christianeverett.bluetoothmonitor.R;
import com.iot.christianeverett.bluetoothmonitor.RoomStatusFragment;

import org.json.JSONArray;

/**
 * Created by Christian Everett on 11/1/2015.
 */
// <Params, Progress, Result>
public class ActionTask extends AsyncTask<Integer, Void, JSONArray>
{
    private HTTPHandler httpHandler = HTTPHandler.getHTTPHandler();

    private Object classObject = null;

    private String monitorUpdate = null;

    private Integer action;

    private static UpdateListener updateListener = null;

    public ActionTask(Object classObject)
    {
        this.classObject = classObject;
    }

    @Override
    protected JSONArray doInBackground(Integer... integers)
    {
        action = integers[0];
        JSONArray statusJSONArray = null;

        if (action != Actions.UPDATE_UI)
        {
            try
            {
                httpHandler.executeAction(action);
                statusJSONArray = httpHandler.getJSONResults();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return statusJSONArray;
    }

    @Override
    protected void onPostExecute(JSONArray statusJSONArray)
    {
        //update UI action
        if (action == Actions.UPDATE_UI && updateListener != null)
        {
            updateListener.onDeviceUpdate(monitorUpdate);
        }
        else if(action == Actions.DISCOVER)
        {
            try
            {
                Activity activity = (Activity) classObject;

                activity.findViewById(R.id.scan_progress).setVisibility(View.GONE);

                if(statusJSONArray != null)
                    for (int x = 0; x < statusJSONArray.length(); x++)
                    {
                        Toast.makeText(activity.getApplicationContext(), statusJSONArray.get(x).toString(), Toast.LENGTH_SHORT).show();
                    }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public void registerUpdateListener(UpdateListener updateListener) {this.updateListener = updateListener;}
    public void setMonitorUpdate(String monitorUpdate)
    {
        this.monitorUpdate = monitorUpdate;
    }

    public interface UpdateListener
    {
        public void onDeviceUpdate(String update);
    }
}
