package ru.monochrome.test_0.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class PointsService extends Service
{
    public static final String ACTION= "mono_service.gps.receive_action";
    public static final String ARGUMENT_FOR_ACTION= "info_arg";
    public static final int NOTIFICATION_ID = 7;

    /**
     * for detect when not need to restore connection
     */
    private static volatile boolean isServiceStopped = true;

    private final String WS_URI = "ws://mini-mdt.wheely.com/";

    private LocationManager locationManager;
    SettingsProvider settings;

    private static volatile WebSocketConnection mConnection = new WebSocketConnection();
    private WebSocketHandler socketHandler = new WebSocketHandler()
    {
        @Override
        public void onOpen()
        {
            Log.d("LOG", "Status: Connected to " + WS_URI);

            double[] coordinates = getLastKnownCoordinates();

            sendLocationInfo(coordinates[0],coordinates[1]);
        }

        @Override
        public void onTextMessage(String payload)
        {
            Log.d("LOG", "Got echo: " + payload);

            Intent broadcast = new Intent(ACTION);
            broadcast.putExtra(ARGUMENT_FOR_ACTION, payload);
            sendBroadcast(broadcast);

            broadcast = null;
        }

        @Override
        public void onClose(int code, String reason)
        {
            Log.d("LOG", "Connection lost." + reason);

            if (!isServiceStopped)
                restoreConnection(WS_URI);
        }
    };

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            sendLocationInfo(location.getLatitude(),location.getLongitude());
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            double[] coordinates = getLastKnownCoordinates();
            sendLocationInfo(coordinates[0],coordinates[1]);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.i("LOG", "Status: " +  String.valueOf(status));
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.i("LOG", "Provider disabled: " + provider);
        }
    };

    @Override
    public void onCreate()
    {
        Log.i("LOG","onCreateService");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        settings = new SettingsProvider(getApplicationContext());

        // Listen location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        isServiceStopped = true;

        godModeOff();

        mConnection.disconnect();
        Log.d("LOG", "MyService onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("LOG", "MyService onStartCommand");

        if (isServiceStopped)
        {
            isServiceStopped = false;

            godModeOn();

            startConnection(WS_URI); //entry point
            Log.d("LOG", "MyService onStartCommand START CONNECTION");
        }

        return START_STICKY;
    }

    /**
     * Get username and password in the required form
     * @return
     */
    private String getAuthorizationString()
    {
        String[] access_info = settings.getAccessData();

        Log.d("LOG", "ACCESS INFO: " + access_info[0] + " | " + access_info[1]);

        return "?username=" + access_info[0]+"&password="+access_info[1];
    }

    /**
     * Start foreground service
     */
    void godModeOn()
    {
        Notification notification = new Notification(R.drawable.ic_launcher, "Test Service",System.currentTimeMillis());

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notification.setLatestEventInfo(this, "Test", "Service is work", pIntent);

        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Stop foreground magic
     */
    void godModeOff()
    {
        stopForeground(true);
    }

    /**
     * Sent location info to server
     * @param latitude
     * @param longitude
     */
    private void sendLocationInfo(Double latitude, Double longitude)
    {
        if (mConnection.isConnected())
            mConnection.sendTextMessage("{\n" +
                    "    \"lat\": "+latitude+",\n" +
                    "    \"lon\": "+longitude+"\n" +
                    "}");

        Log.i("LOG", "Location sent to server: " + latitude + " || " + longitude);
    }

    /**
     * get some info about location
     * @return [0] - latitude, [1] = longitude
     */
    private double[] getLastKnownCoordinates()
    {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // last known coordinates
        double last_known_latitude;
        double last_known_longitude;

        if (null == location)
        {
            last_known_latitude = 55.749792;
            last_known_longitude = 37.632495;
        }
        else
        {
            last_known_latitude = location.getLatitude();
            last_known_longitude = location.getLongitude();
        }

        Log.d("LOG", "Last known coordinates: " + last_known_latitude + " || " + last_known_longitude);

        double[] result = {last_known_latitude,last_known_longitude};

        return result;
    }

    /**
     * Connect to server and sell received data to binder
     * @param uri
     */
    private void startConnection(String uri)
    {
        try
        {
            mConnection.connect(uri + getAuthorizationString(), socketHandler);
        }
        catch (WebSocketException e)
        {
            Log.d("LOG", e.toString());
        }
    }

    /**
     * restore lost connection in other thread
     */
    private void restoreConnection(final String uri)
    {
        Thread restoreWorker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // wait for 2 seconds and try to restore connection
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                startConnection(uri);
                Log.d("LOG", "START CONNECTION FROM RESTORE THREAD");
            }
        });

        restoreWorker.start();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i("LOG","onBind");

        return null;
    }

    /**
     * Interface to receive coordinates from server
     */
    public interface CoordinatesReceiver
    {
        void onCoordinatesReceived(String info);
    }
}
