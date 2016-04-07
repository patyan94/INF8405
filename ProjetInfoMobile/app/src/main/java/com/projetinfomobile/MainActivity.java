package com.projetinfomobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.json.JSONObject;

import Model.DatabaseInterface;
import Model.OMDBInterface;
import Model.Serie;
import Model.UserData;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener{


    //Variables for shake detection
    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 5000;


    private SensorManager mSensorMgr;

    ImageView profilePictureView;
    TextView usernameView;

    private long mLastShakeTime;
    private long mLastShakeDetectTime;
    private int previousFragmentId;
    private int shakeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Loading the "Your series" fragment at application start
        LoadFragment(new SeriesFragment());
        previousFragmentId = R.id.nav_your_series;

        // Setup the drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Setup the navigation view in the drawer
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(3).setChecked(true);//Set the item menu "Your series" checked by default


        // Setup the floating button to open the search fragment
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_serie);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationView.getMenu().getItem(3).setChecked(true);
                LoadFragment(new SeriesFragment());
                previousFragmentId = R.id.nav_your_series;

            }
        });

        View drawerHeader = navigationView.getHeaderView(0);
        UserData userData = DatabaseInterface.Instance().getUserData();

        // Sets the user photo
        profilePictureView = (ImageView)drawerHeader.findViewById(R.id.profile_picture_view);
        profilePictureView.setImageBitmap(userData.getUserProfileImage());

        // Sets the username
        usernameView = (TextView)drawerHeader.findViewById(R.id.user_display_name);
        usernameView.setText(userData.getUsername());

        // Get a sensor manager to listen for shakes
        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Listen for shakes
        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Fragment fragment = null;
        switch(item.getItemId())
        {
            case R.id.action_settings :
                LoadFragment(new SettingsFragment());
                return true;

            //Add any new button's action in a new case if needed

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Change views when the orientation changes
        Fragment fragment = null;
        boolean mapMode = false;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fragment = new CloseUsersMapFragment();
            mapMode = true;
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            navigationView.getMenu().getItem(1).setChecked(false);
        }

        switch (previousFragmentId){
            case R.id.nav_friends_recommendations:
                if(!mapMode) {
                    fragment = new RecommandationsFragment();
                }
                navigationView.getMenu().getItem(2).setChecked(!mapMode);
                break;
            case R.id.nav_friends:
                if(!mapMode){
                    fragment = new FriendsFragment();
                }
                navigationView.getMenu().getItem(0).setChecked(!mapMode);
                break;
            case R.id.nav_settings:
                if(!mapMode){
                    fragment = new SettingsFragment();
                }
                navigationView.getMenu().getItem(4).setChecked(!mapMode);
                break;
            case R.id.nav_map:
                if(!mapMode){
                    fragment = new CloseUsersMapFragment();
                }
                navigationView.getMenu().getItem(1).setChecked(!mapMode);
                break;
            case  R.id.nav_your_series:
                if(!mapMode){
                    fragment = new SeriesFragment();
                }
                navigationView.getMenu().getItem(3).setChecked(!mapMode);
                break;
        }

        if(fragment != null){
            LoadFragment(fragment);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        switch (id){

            case R.id.nav_friends_recommendations:
                fragment = new RecommandationsFragment();
                break;
            case R.id.nav_friends:
                fragment = new FriendsFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                    break;
            case R.id.nav_map:
                fragment = new CloseUsersMapFragment();
                break;
            case  R.id.nav_your_series:
                fragment = new SeriesFragment();
                break;
        }

        if(fragment != null){
            LoadFragment(fragment);

            previousFragmentId = id;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();

            if (shakeCount >= 4 && (curTime - mLastShakeDetectTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                Toast.makeText(getApplicationContext(), "Shake activated!", Toast.LENGTH_SHORT).show();
                shakeCount = 0;
                mLastShakeDetectTime = curTime;

                //TODO : Action on shake
            }else{
                if(shakeCount == 0 || (curTime - mLastShakeTime) < 700) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                    if (acceleration > SHAKE_THRESHOLD) {
                        mLastShakeTime = curTime;
                        shakeCount = ++shakeCount % 5;
                    }
                } else {
                    shakeCount = 0;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore
    }

    void LoadFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getTag());
        fragmentTransaction.commit();

    }

    //Static method used to pop up an altert dialog with the specified user series, it's used in the map and friend activity
    public static void PromptUserSeries(final String username, Context ctx){
        final OMDBInterface omdbInterface;
        omdbInterface = OMDBInterface.Start(ctx);

        //We build the window
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(username);
        View seriesView = View.inflate(ctx, R.layout.alert_dialog_series, null);
        builder.setView(seriesView);

        RecyclerView seriesListview = (RecyclerView)seriesView.findViewById(R.id.series_listview_alert);

        seriesListview.setHasFixedSize(true);
        seriesListview.setLayoutManager(new LinearLayoutManager(ctx));

        FirebaseRecyclerAdapter<String, SeriesViewHolder> seriesAdapter = new FirebaseRecyclerAdapter<String, SeriesViewHolder>(String.class, R.layout.alert_series_listview_item, SeriesViewHolder.class,DatabaseInterface.Instance().GetUsersNode().child(username).child("series")) {
            @Override
            protected void populateViewHolder(final SeriesViewHolder view, final String serieID, int position) {
                omdbInterface.GetSerieInfo(serieID, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Serie serie = Serie.FromJSONObject(response);
                            view.title.setText(serie.getName());
                            view.description.setText(serie.getDescription());
                            if (!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                                omdbInterface.GetPoster(serie.getPhotoURL(), view.posterView);
                            }
                            if(DatabaseInterface.Instance().GetCurrentUserData().getSeriesList().containsKey(serie.getID())) {
                                view.itemView.setBackgroundColor(Color.LTGRAY);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
            }
        };
        seriesListview.setAdapter(seriesAdapter);


        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.show();
    }

    public static class SeriesViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;
        public SeriesViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.serie_name);
            description = (TextView)itemView.findViewById(R.id.serie_description);
            posterView = (ImageView)itemView.findViewById(R.id.serie_poster);
        }
    }
}
