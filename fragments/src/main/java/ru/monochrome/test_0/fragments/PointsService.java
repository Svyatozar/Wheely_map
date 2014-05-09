package ru.monochrome.test_0.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PointsService extends Service
{
    NotificationManager nm;
    ConnectionBinder binder = new ConnectionBinder();

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
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0;i < 150; i++)
                {
                    Log.i("LOG","Signal^"+i);

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {
                        Log.i("LOG","Exception sleep");
                    }
                }
            }
        });

        t.start();

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
        return null;
    }

    /**
     * describes actions for service-management activity
     */
    class ConnectionBinder extends Binder
    {
        /**
         * Initialise network activity
         * @return success or not
         */
        boolean start()
        {
            return true;
        }

        /**
         * Stop network activity
         * @return success or not
         */
        boolean stop()
        {
            return true;
        }
    }
}
