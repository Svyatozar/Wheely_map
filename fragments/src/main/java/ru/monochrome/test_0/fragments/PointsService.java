package ru.monochrome.test_0.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

public class PointsService extends Service
{
    NotificationManager nm;
    ConnectionBinder binder = new ConnectionBinder();

    private final WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    public void onCreate()
    {
        Log.i("LOG","onCreateService");

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sendNotif();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        final String wsuri = "ws://mini-mdt.wheely.com/?username=atr&password=atr";

        try
        {
            mConnection.connect(wsuri, new WebSocketHandler()
            {

                @Override
                public void onOpen()
                {
                    Log.d("LOG", "Status: Connected to " + wsuri);
                    mConnection.sendTextMessage("{\n" +
                            "    \"lat\": 55.749792,\n" +
                            "    \"lon\": 37.632495\n" +
                            "}");
                }

                @Override
                public void onTextMessage(String payload)
                {
                    Log.d("LOG", "Got echo: " + payload);
                    binder.sendCoordinatesInfo("Hi, WebSocket!~" + payload);
                }

                @Override
                public void onClose(int code, String reason)
                {
                    Log.d("LOG", "Connection lost.");
                }
            });
        } catch (WebSocketException e) {

            Log.d("LOG", e.toString());
        }

        return START_STICKY;
    }

    void sendNotif()
    {
        // 1-я часть
        Notification notif = new Notification(R.drawable.ic_launcher, "Test Service",System.currentTimeMillis());

        // 3-я часть
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 2-я часть
        notif.setLatestEventInfo(this, "Test", "Service is work", pIntent);

        // отправляем
        //nm.notify(1, notif);
        startForeground(7,notif);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i("LOG","onBind");
        return binder;
    }

    /**
     * Interface to receive coordinates from server
     */
    public interface CoordinatesReceiver
    {
        void onCoordinatesReceived(String info);
    }

    /**
     * describes actions for service-management activity
     */
    class ConnectionBinder extends Binder
    {
        private CoordinatesReceiver listener = null;

        /**
         * Handler for send message from other thread
         */
        private Handler handler = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                String info = (String)msg.obj;
                listener.onCoordinatesReceived(info);
            }
        };

        /**
         * Message code for handleMessage
         */
        private final int MESSAGE_CODE = 0;

        /**
         * Initialise network activity
         * @return success or not
         */
        boolean start()
        {
            return true;
        }

        /**
         * Send coordinates info to listener
         * @param info
         */
        void sendCoordinatesInfo(String info)
        {
            if (null != listener)
            {
                Message msg = handler.obtainMessage(MESSAGE_CODE,info);
                handler.sendMessage(msg);
            }
        }

        /**
         * Register to receive coordinates info
         * @param listener
         */
        void registerObserver(CoordinatesReceiver listener)
        {
            this.listener = listener;
        }

        /**
         * Stop network activity
         * @return success or not
         */
        boolean stop()
        {
            if (null != mConnection)
            {
                mConnection.disconnect();

                return mConnection.isConnected();
            }

            return false;
        }
    }
}
