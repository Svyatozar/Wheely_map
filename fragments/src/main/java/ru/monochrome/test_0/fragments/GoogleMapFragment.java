package ru.monochrome.test_0.fragments;

/**
 * Created by konservator_007 on 30.04.2014.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Map - based fragment
 */
public class GoogleMapFragment extends SupportMapFragment
{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";

    private OnGoogleMapFragmentListener mCallback;

    public GoogleMapFragment()
    {
        mCallback = null;
    }

    public static interface OnGoogleMapFragmentListener
    {
        void onMapReady(GoogleMap map);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * New instance
     * @return map_fragment
     */
    public static GoogleMapFragment newInstance()
    {
        return new GoogleMapFragment();
    }

    /**
     * New map obj
     * @param key for setting an item's headr
     * @return map_fragment
     */
    public static GoogleMapFragment newInstance(int key)
    {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_SECTION_NUMBER, key);

        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * New map obj
     * @param options optns
     * @return map_fragment
     */
    public static GoogleMapFragment newInstance(GoogleMapOptions options)
    {
        Bundle arguments = new Bundle();
        arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (mCallback != null)
        {
            mCallback.onMapReady(getMap());
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        try
        {
            mCallback = (OnGoogleMapFragmentListener) getActivity();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().getClass().getName() + " must implement OnGoogleMapFragmentListener");
        }
    }
}