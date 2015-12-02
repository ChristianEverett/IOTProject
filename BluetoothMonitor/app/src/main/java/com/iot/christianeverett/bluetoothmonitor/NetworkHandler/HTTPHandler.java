package com.iot.christianeverett.bluetoothmonitor.NetworkHandler;

import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Christian Everett on 10/21/2015.
 */
public class HTTPHandler implements Runnable
{
    private final String TAG = "HTTPHandler";
    //IP or domain of Bluetooth server
    private String HOST = "10.0.0.5";
    //Process Port
    private String PORT = "8081";
    //HTTP connection
    private HttpURLConnection connection;
    //Current session variable
    private String session_id;
    //JSON result from last action
    private JSONArray jsonArray = null;
    //Polling thread
    private Thread pollerThread = null;
    //Polling socket
    private Socket pollerSocket = null;
    //Singleton object
    private static HTTPHandler handler = null;
    //Status of bluetooth ping
    private boolean pingStatus  = true;
    
    //TODO add session validator
    private HTTPHandler()
    {}

    //Singleton class creation
    public static HTTPHandler getHTTPHandler()
    {
        if(handler == null)
            handler = new HTTPHandler();

        return handler;
    }

    public boolean loginToServer(String username, String password) throws Exception
    {
        if(username == null || password == null)
            return false;

        URL url = new URL("http://" + HOST + ":" + PORT + "/loginScript.php");
        connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        //Send login credentials
        outputStream.writeBytes("username=" + username + "&password=" + password);
        outputStream.flush();
        outputStream.close();

        //get session id
        session_id = connection.getHeaderField("Set-Cookie");
        session_id = session_id.substring(0, session_id.indexOf(';'));

        int code = connection.getResponseCode();

        if (code == HttpURLConnection.HTTP_MOVED_TEMP)
        {
            String redirectAddress = connection.getHeaderField("Location");

            connectServerSidePush();

            executeAction(Actions.IS_SCAN_ENABLE);

            connection.disconnect();
            return true;
        }
        else
        {
            connection.disconnect();
            return false;
        }
    }

    public boolean logoutFromServer() throws Exception
    {
        URL url = new URL("http://" + HOST + ":" + PORT + "/logout.php");
        connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", session_id);

        int code = connection.getResponseCode();
        session_id = null;

        if (pollerSocket != null )
            pollerSocket.close();
        pollerThread = null;
        pollerSocket = null;

        return true;
    }

    public void executeAction(int action) throws Exception
    {
        if (action < 0 || action > 6)
            throw new Exception("Action not Supported");
        else if(session_id == null)
            throw  new Exception("User not logged in");

        URL url = new URL("http://" + HOST + ":" + PORT + "/page1.php");
        connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", session_id);
        connection.setDoOutput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        //Send action
        outputStream.writeBytes("action=" + Integer.toString(action));
        outputStream.flush();
        outputStream.close();

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            BufferedReader inputStreamBufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            switch (action)
            {
                case Actions.GET_STATUS:
                case Actions.GET_LOG:
                case Actions.DISCOVER:

                    String line, jsonString = "";
                    while ((line = inputStreamBufferedReader.readLine()) != null)
                    {
                        if(line.contains("[") == true)
                        {
                            jsonString = line;
                        }
                    }

                    Log.d(TAG, "Finished reading json");

                    if (!jsonString.equals(""))
                        jsonArray = new JSONArray(jsonString);
                    else
                        jsonArray = null;
                    break;
                case Actions.START_SCAN:

                    break;
                case Actions.STOP_SCAN:

                    break;
                case Actions.IS_SCAN_ENABLE:
                    while ((line = inputStreamBufferedReader.readLine()) != null)
                    {
                        if(line.contains("status") == true)
                        {
                            String[] status = line.split(Pattern.quote("status="));

                            if(status[1].equals("1"))
                                pingStatus = true;
                            else
                                pingStatus = false;
                            break;
                        }
                    }
                    break;
                default:
            }
        }
        else
        {
            jsonArray = null;
        }

        connectServerSidePush();

        connection.disconnect();
    }

    private void connectServerSidePush()
    {
        if (pollerThread == null)
        {
            Log.d(TAG, "Creating new polling thread");
            pollerThread = new Thread(this);
            pollerThread.start();
        }
    }

    public boolean isPingRunning() {return pingStatus;}

    public JSONArray getJSONResults()
    {
        return jsonArray;
    }

    @Override
    public void run()
    {
        ActionTask updateUITask;

        try
        {
            pollerSocket = new Socket();
            pollerSocket.connect(new InetSocketAddress(HOST, 8082), 0);
            Scanner inputStream = new Scanner(new BufferedReader(new InputStreamReader(pollerSocket.getInputStream())));

            while(inputStream.hasNext())
            {
                String newData = inputStream.nextLine();
                updateUITask = new ActionTask(this);
                updateUITask.setMonitorUpdate(newData);
                updateUITask.execute(Actions.UPDATE_UI);
            }

            Log.d(TAG, "Poll Stopped");
        }
        catch (Exception e)
        {
            Log.d(TAG, "socket connection failed");
        }
        finally
        {
            pollerThread = null;
        }
    }
}
