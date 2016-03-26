package com.projetinfomobile;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import Model.DatabaseInterface;
import Model.UserData;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FriendsFragment.OnFriendsFragmentInteractionListener,
        MapFragment.OnMapFragmentInteractionListener,
        SeriesFragment.OnSeriesFragmentInteractionListener,
        RecommandationsFragment.OnRecommandationsFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener{

    ImageView profilePictureView;
    TextView usernameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_serie);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View drawerHeader = navigationView.getHeaderView(0);
        final UserData userData = DatabaseInterface.Instance().getUserData();
        profilePictureView = (ImageView)drawerHeader.findViewById(R.id.profile_picture_view);
        profilePictureView.setImageBitmap(userData.getUserProfileImage());

        usernameView = (TextView)drawerHeader.findViewById(R.id.user_display_name);
        usernameView.setText(userData.getUsername());
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                fragment = new MapFragment();
                break;
            case  R.id.nav_your_series:
                fragment = new SeriesFragment();
                break;
        }

        if(fragment != null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getTag());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFriendsFragmentInteraction(Uri uri) {
        //TODO
    }

    @Override
    public void onMapFragmentInteraction(Uri uri) {
        //TODO
    }

    @Override
    public void onSeriesFragmentInteraction(Uri uri) {
        //TODO
    }

    @Override
    public void onSettingsFragmentInteraction(Uri uri) {
        //TODO
    }
    @Override
    public void onRecommandationsFragmentInteraction(Uri uri) {
        //TODO
    }
}