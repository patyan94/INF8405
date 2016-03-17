package com.example.yannd.tp2_inf8405;

public class ConnectivityManager {
    public boolean IsDataNetworkAvailable(){
        boolean mobileDataEnabled;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);		// Connecting to connectivity service
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);						// Getting mobile data informations
        mobileDataEnabled = networkInfo != null && networkInfo.isConnected();				// Check mobile data is connected or not and information is not null
        return mobileDataEnabled;
    }
    public boolean IsWiFiNetworkAvailable(){
        boolean wifiEnabled;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);						// Getting wifi informations
        wifiEnabled = networkInfo != null && networkInfo.isConnected();								// Check wifi is connected or not
        return wifiEnabled;
    }
    public boolean IsGPSNetworkAvailable(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);		// Connecting to GPS serivice
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);				// Checking GPS is on or not
        return isGPSEnabled;
    }

}
