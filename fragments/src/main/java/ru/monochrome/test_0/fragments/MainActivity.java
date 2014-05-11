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
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity
implements NavigationDrawerFragment.NavigationDrawerCallbacks,GoogleMapFragment.OnGoogleMapFragmentListener,PointsService.CoordinatesReceiver
{
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
     * Intent to launch service
     */
    Intent intent;
    // filter for BroadcastReceiver
    IntentFilter intentFilter = new IntentFilter(PointsService.ACTION);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void restoreActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
                //service_binder.start();
                startService(intent);
                return true;
            }

            case R.id.action_stop:
            {
                stopService(intent);

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
        Toast.makeText(this,"CLICK",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCoordinatesReceived(String info)
    {
        /*
        int point = Integer.parseInt(info);

        if (null != map)
        {
            map.addMarker(new MarkerOptions().position(new LatLng(0, point)).title("Test point"));
        }*/

        Toast.makeText(this,info,Toast.LENGTH_SHORT).show();
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
