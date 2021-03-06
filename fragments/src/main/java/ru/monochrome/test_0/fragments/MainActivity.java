package ru.monochrome.test_0.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity
implements NavigationDrawerFragment.NavigationDrawerCallbacks,GoogleMapFragment.OnGoogleMapFragmentListener,PointsService.CoordinatesReceiver
{
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";

    /**
     * Middle point in last received coordinates list
     */
    private static final int MIDDLE_ITEM = 1;
    /**
     * General zoom to show map's points
     */
    private static final int GENERAL_ZOOM = 10;

    /**
     * Map fragment
     */
    GoogleMapFragment mapFragment;
    /**
     * Map from mapFragment
     */
    GoogleMap map;

    /**
     * Selected item from drawer
     */
    private int current_selected_position = 0;

    BroadcastReceiver br;

    /**
     * last received coordinates
     */
    private double[] last_received_latitude;
    /**
     * last received coordinates
     */
    private double[] last_received_longitude;

    /**
     * Intent to launch service
     */
    Intent intent;
    // filter for BroadcastReceiver
    IntentFilter intentFilter = new IntentFilter(PointsService.ACTION);

    SettingsProvider settings;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new SettingsProvider(getApplicationContext());

        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        intent = new Intent(this, PointsService.class);

        // создаем BroadcastReceiver
        br = new BroadcastReceiver()
        {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent)
            {
                String result = intent.getStringExtra(PointsService.ARGUMENT_FOR_ACTION);
                Log.d("LOG", "onReceive: " + result);

                onCoordinatesReceived(result);
            }

        };

        registerReceiver(br, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(br);
    }

    @Override
    /**
     * Here selecting a frame
     */
    public void onNavigationDrawerItemSelected(int position)
    {
        current_selected_position = position;

        /**
         * Менеджер фрагментов
         */
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (0 == position)
        {
            if (mapFragment == null || !mapFragment.isAdded())
            {
                mapFragment = GoogleMapFragment.newInstance(position+1);
            }

            fragmentManager.beginTransaction().replace(R.id.container, mapFragment).commit();
        }
        else
        {
            fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
        }

        // Hide or show map button
        supportInvalidateOptionsMenu();
    }

    public void onSectionAttached(int number)
    {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        if (0 == current_selected_position)
            menu.setGroupVisible(R.id.groupVsbl, true);
        else
            menu.setGroupVisible(R.id.groupVsbl, false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here.

        switch (item.getItemId())
        {
            case R.id.action_example:
            {
                startService(intent);
                return true;
            }

            case R.id.action_stop:
            {
                stopService(intent);

                return true;
            }

            case R.id.action_gps:
            {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the map here
     * @param map
     */
    @Override
    public void onMapReady(GoogleMap map)
    {
        this.map = map;
    }

    public void onClick(View v)
    {
        EditText username_field = (EditText)findViewById(R.id.editText);
        EditText password_field = (EditText)findViewById(R.id.editText2);

        settings.writeAccessData(username_field.getText().toString(),password_field.getText().toString());

        Toast.makeText(this,"Сохранено",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCoordinatesReceived(String info)
    {
        if (null != map)
        {
            map.clear();

            try // Parse coordinates from json string
            {
                JSONArray json = new JSONArray(info);

                last_received_latitude = new double[json.length()];
                last_received_longitude = new double[json.length()];

                for (int i = 0; i < json.length(); i++)
                {
                    JSONObject item = json.getJSONObject(i);

                    last_received_latitude[i] = item.getDouble("lat");
                    last_received_longitude[i] = item.getDouble("lon");

                    map.addMarker(new MarkerOptions().position(new LatLng(last_received_latitude[i], last_received_longitude[i])).title("point " + i));

                    if (i == MIDDLE_ITEM)
                    {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(last_received_latitude[i], last_received_longitude[i]), GENERAL_ZOOM);
                        map.animateCamera(cameraUpdate);
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putDoubleArray(LATITUDE,last_received_latitude);
        outState.putDoubleArray(LONGITUDE,last_received_longitude);

        Log.d("LOG", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore received points to map
        last_received_latitude = savedInstanceState.getDoubleArray(LATITUDE);
        last_received_longitude = savedInstanceState.getDoubleArray(LONGITUDE);

        if ((null != last_received_latitude) && (null != last_received_longitude))
        {
            if (null != map)
            {
                for (int i = 0;i < last_received_longitude.length;i++)
                {
                    map.addMarker(new MarkerOptions().position(new LatLng(last_received_latitude[i], last_received_longitude[i])).title("point " + i));
                }

                CameraUpdate move_to_point = CameraUpdateFactory.newLatLngZoom
                        (new LatLng(last_received_latitude[MIDDLE_ITEM], last_received_longitude[MIDDLE_ITEM]), GENERAL_ZOOM);

                map.moveCamera(move_to_point);
            }
        }

        Log.d("LOG", "onRestoreInstanceState");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
