package com.iot.christianeverett.bluetoothmonitor;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.ActionTask;
import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.Actions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class RoomStatusFragment extends ListFragment implements ActionTask.UpdateListener
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<String> contentList = new ArrayList<>();

    public static RoomStatusFragment newInstance(String param1, String param2)
    {
        RoomStatusFragment fragment = new RoomStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RoomStatusFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //call back must be set for GET_STATUS action
        ActionTask statusTask = new ActionTask(this);
        statusTask.execute(Actions.GET_STATUS);
        statusTask.registerUpdateListener(this);

        try
        {
            String devicename, macaddress;
            JSONArray statusJSONArray = statusTask.get();

            for (int x = 0; x < statusJSONArray.length(); x++)
            {
                devicename = ((JSONObject) statusJSONArray.get(x)).getString(Actions.DEVICE_NAME);
                macaddress = ((JSONObject) statusJSONArray.get(x)).getString(Actions.MAC_ADDRESS);

                contentList.add(devicename + "\n" + macaddress);
            }

            setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, contentList));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


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
            mListener.onFragmentInteraction(contentList.get(position));
        }
    }

    @Override
    public void onDeviceUpdate(String update)
    {
        String[] updateData = update.split(Pattern.quote("^"));
        String devicename = updateData[0];
        String macaddress = updateData[1];
        boolean isPresent = Boolean.parseBoolean(updateData[2]);

        //remove from UI
        if (!isPresent)
        {
            for (int x = 0; x < contentList.size(); x++)
            {
                if (contentList.get(x).contains(macaddress))
                {
                    contentList.remove(x);
                    break;
                }
            }
        }
        else
        {
            //add to UI
            contentList.add(devicename + "\n" + macaddress);
        }
        Activity activity = getActivity();
        //update UI
        if(activity != null)
            setListAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, contentList));
    }

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
