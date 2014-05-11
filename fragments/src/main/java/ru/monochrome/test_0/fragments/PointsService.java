package ru.monochrome.test_0.fragments;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class PointsService extends Service
{
    public static final String ACTION= "mono_service.gps.receive_action";
    public static final String ARGUMENT_FOR_ACTION= "info_arg";

    private final String WS_URI = "ws://mini-mdt.wheely.com/";

    NotificationManager nm;

    private static WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    public void onCreate()
    {
        Log.i("LOG","onCreateService");

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mConnection.disconnect();
        Log.d("LOG", "MyService onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("LOG", "MyService onStartCommand");

        if (!mConnection.isConnected())
        {
            startConnection(WS_URI + "?username=atr&password=atr");
            Log.d("LOG", "MyService onStartCommand START CONNECTION");
        }

        return START_STICKY;
    }

    /**
     * Connect to server and sell received data to binder
     * @param uri
     */
    private void startConnection(String uri)
    {
        try
        {
            mConnection.connect(uri, new WebSocketHandler()
            {

                @Override
                public void onOpen()
                {
                    Log.d("LOG", "Status: Connected to " + WS_URI);
                    mConnection.sendTextMessage("{\n" +
                            "    \"lat\": 55.749792,\n" +
                            "    \"lon\": 37.632495\n" +
                            "}");
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
                    Log.d("LOG", "Connection lost.");
                }
            });
        }
        catch (WebSocketException e)
        {
            Log.d("LOG", e.toString());
        }
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
