package ru.monochrome.test_0.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by konservator_007 on 11.05.2014.
 */
public class SettingsProvider
{
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private SharedPreferences sPref = null;

    /**
     * Initialize provider
     * @param context context
     */
    public SettingsProvider (Context context)
    {
        sPref = context.getSharedPreferences("preferences", context.MODE_MULTI_PROCESS);
    }

    /**
     * Write data to preferences
     * @param username
     * @param password
     */
    public void writeAccessData(String username, String password)
    {
        if (null != sPref)
        {
            SharedPreferences.Editor ed = sPref.edit();

            ed.putString(USERNAME, username);
            ed.putString(PASSWORD, password);
            ed.commit();
        }
    }

    /**
     * get saved username and password
     * @return [0] - username, [1] - password
     */
    public String[] getAccessData()
    {
        if (null != sPref)
        {
            String username = sPref.getString(USERNAME, "");
            String password = sPref.getString(PASSWORD, "");

            String[] result = {username,password};

            return result;
        }
        else
            return null;
    }
}
