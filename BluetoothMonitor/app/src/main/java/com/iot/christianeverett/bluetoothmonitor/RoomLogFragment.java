package com.iot.christianeverett.bluetoothmonitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.ActionTask;
import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.Actions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class RoomLogFragment extends ListFragment //implements SwipeRefreshLayout.OnRefreshListener
{
    private OnFragmentInteractionListener mListener;
    private ArrayList<HashMap<String, String>> contentList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static RoomLogFragment newInstance()
    {
        RoomLogFragment fragment = new RoomLogFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RoomLogFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String devicename, macaddress, date, time, action;

        // Keys used in Hashmap
        String[] from = { "action","devicename","macaddress","date","time"};

        // Ids of views in listview_layout
        int[] to = {R.id.action ,R.id.device_name, R.id.mac_address, R.id.date, R.id.time};

        try
        {
            ActionTask statusTask = new ActionTask(this);
            statusTask.execute(Actions.GET_LOG);
            JSONArray statusJSONArray = statusTask.get();

            for (int x = 0; x < statusJSONArray.length(); x++)
            {
                HashMap<String, String> map = new HashMap<>();

                action = ((JSONObject) statusJSONArray.get(x)).getString(Actions.ACTION);
                devicename = ((JSONObject) statusJSONArray.get(x)).getString(Actions.DEVICE_NAME);
                macaddress = ((JSONObject) statusJSONArray.get(x)).getString(Actions.MAC_ADDRESS);
                date = ((JSONObject) statusJSONArray.get(x)).getString(Actions.DATE);
                time = ((JSONObject) statusJSONArray.get(x)).getString(Actions.TIME);

                map.put(from[0], action);
                map.put(from[1], devicename);
                map.put(from[2], macaddress);
                map.put(from[3], date);
                map.put(from[4], time);

                contentList.add(map);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        setListAdapter(new SimpleAdapter(getActivity().getBaseContext(), contentList, R.layout.log_listview, from, to));
    }
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup listContainer = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        SwipeRefreshLayout refreshLayout = new SwipeRefreshLayout(inflater.getContext());
        refreshLayout.addView(listContainer);
        refreshLayout.setOnRefreshListener(this);
        return refreshLayout;
    }*/

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
        contentList.clear();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        if (null != mListener)
        {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //TODO mListener.onFragmentInteraction(contentList.get(position));
        }
    }
/*
    @Override
    public void onRefresh()
    {
        String devicename, macaddress, date, time;

        // Keys used in Hashmap
        String[] from = { "devicename","macaddress","date","time"};

        // Ids of views in listview_layout
        int[] to = { R.id.device_name, R.id.mac_address, R.id.date, R.id.time};

        try
        {
            //ActionTask statusTask = new ActionTask(this);
            //statusTask.execute(Actions.GET_LOG);
            //JSONArray statusJSONArray = statusTask.get();




                HashMap<String, String> map = new HashMap<>();



                map.put(from[0], "test");
                map.put(from[1], "test");
                map.put(from[2], "test");
                map.put(from[3], "test");

                contentList.add(map);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        setListAdapter(new SimpleAdapter(getActivity().getBaseContext(), contentList, R.layout.log_listview, from, to));
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }
}
