package com.iot.christianeverett.bluetoothmonitor;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.ActionTask;
import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.Actions;
import com.iot.christianeverett.bluetoothmonitor.NetworkHandler.HTTPHandler;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RoomStatusFragment.OnFragmentInteractionListener, RoomLogFragment.OnFragmentInteractionListener
{
    private RoomStatusFragment roomStatusFragment = RoomStatusFragment.newInstance("", "");
    private RoomLogFragment roomLogFragment = RoomLogFragment.newInstance();
    private ProgressBar scanProgress;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.room_status);
        setSupportActionBar(toolbar);

        scanProgress = (ProgressBar) findViewById(R.id.scan_progress);

        FragmentTransaction transaction = getFragmentManager().beginTransaction().add(R.id.fragment_frame, roomStatusFragment);
        transaction.commit();
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(3).setTitle(
             HTTPHandler.getHTTPHandler().isPingRunning()?R.string.stop_scan:  R.string.start_scan
        );
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        switch (id)
        {
            case R.id.nav_room_status:
                transaction.replace(R.id.fragment_frame, roomStatusFragment);
                transaction.commit();
                break;
            case R.id.nav_room_log:
                transaction.replace(R.id.fragment_frame, roomLogFragment);
                transaction.commit();
                toolbar.setTitle(R.string.room_log);
                break;
            case R.id.nav_discover:
                ActionTask discoverTask = new ActionTask(this);
                discoverTask.execute(Actions.DISCOVER);
                scanProgress.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_polling:
                ActionTask scanTask = new ActionTask(this);

                if(item.getTitle().toString().equals(getString(R.string.start_scan)))
                {
                    //Scanning is not currently running; start scanning
                    scanTask.execute(Actions.START_SCAN);
                    item.setTitle(R.string.stop_scan);
                }
                else
                {
                    //Scanning is currently running; stop scanning
                    scanTask.execute(Actions.STOP_SCAN);
                    item.setTitle(R.string.start_scan);
                }

                break;
            case R.id.nav_settings:
                Toast.makeText(getApplicationContext(), "Settings not implemented yet", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_logout:

                logoutCurrentUser();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
                break;
            default:
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutCurrentUser()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    HTTPHandler.getHTTPHandler().logoutFromServer();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop()
    {
        logoutCurrentUser();

        super.onStop();
    }

    @Override
    public void onFragmentInteraction(String id)
    {

    }
}
