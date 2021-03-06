package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Sacha on 2016-03-10.
 */
public class CalendarManager {
    private static CalendarManager instance = null;
    private Context appCtx = null;

    private CalendarManager() {
    }

    public static CalendarManager getInstance() {
        if (instance == null) {
            instance = new CalendarManager();
        }
        return instance;
    }

    //Call this on the object returned by getInstance as early as possible in the application code
    public void setApplicationContext(Context applicationContext){
        appCtx = applicationContext;
    }

    /*
    Exemple :
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        calStart.set(2016, 0, 10); //Note that months start from 0 (January)
        calEnd.set(2016, 0, 17); //Note that months start from 0 (January)

        CalendarManager.getInstance().getAvailabilities(calStart, calEnd);

    This exemple will return a list of Calendar objects, one per day that is available to book an event
    This is basic, since if there is already something booked on a day (at any time), the ENTIRE day won't be available (excluded from the returned list)
    This is enough for this application
     */
    public List<Calendar> getAvailabilities(Calendar dateStart, Calendar dateEnd) {
        List<Calendar> availabilitesList = new ArrayList<>();

        Calendar currentDayCalendar = dateStart;
        while(currentDayCalendar.compareTo(dateEnd) < 0){
            currentDayCalendar.add(Calendar.DAY_OF_YEAR, 1);
            availabilitesList.add((Calendar)currentDayCalendar.clone());
        }

        //Ici on va chercher les évènements du calendrier de la personne durant l'interval donné en paramètre
        Cursor cursor = null;
        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        //String[] projection = new String[] { "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" };

        //On ne veut que le debut et fin de chacun des évènements, puisque nous c'est les disponibilités qui nous intéressent
        String[] projection = new String[] {"dtstart", "dtend"};

        String selection = "((dtstart >= " + dateStart.getTime().getTime() + ") AND (dtend <= " + dateEnd.getTime().getTime() + "))";

        cursor = appCtx.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), projection, selection, null, null);

        //Pour chaque event, on modifie la liste de disponibilités en conséquence, de manière à exclure toutes les journées qui ont déje un evenement
        while(cursor.moveToNext()){
            long startTime = cursor.getLong(0);
            long endTime = cursor.getLong(1);

            Calendar startEvent = getCalendar(startTime);
            Calendar endEvent = getCalendar(endTime);

            int[] toRemove = new int[availabilitesList.size()];
            int idx = 0;
            //Ici on détermine l'ensemble des jours qui devront être retirés de la liste de dispo
            for(Calendar c : availabilitesList){
                if(startEvent.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)){
                    toRemove[idx++] = availabilitesList.indexOf(c);
                }
            }

            //Ici on effectue le retrait des dispos à partir du tableau rempli (toRemove) un peu plus haut
            for(int i = 0; i < idx; i++){
                availabilitesList.remove(toRemove[i]);
            }
        }

        return availabilitesList;

    }
    //Used to convert timestamp from calendar query to an actual Calendar object
    private Calendar getCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    /*
    Example :

        Calendar start = Calendar.getInstance();
        start.set(2016, 2, 28, 7, 45);
        Calendar end = Calendar.getInstance();
        end.set(2016, 2, 28, 15, 30);

        CalendarManager.getInstance().addEventToCalendar(start, end, "Meeting 1", "Description du meeting #1 .. .. ..");

    This method adds the event to the main calendar of the app user.
    Note : It may take a minute or two for the event to sync with the desktop version of google calendar
     */
    public void addEventToCalendar(Calendar start, Calendar end, String title, String description, String location){
        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();

        int calId = 1; //We're using the default/main calendar of the user

        ContentResolver cr = appCtx.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.EVENT_LOCATION, location);
        values.put(CalendarContract.Events.CALENDAR_ID, calId);
        TimeZone tz = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());

        //Required check, even though the manifest file does have the correct permission
        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        cr.insert(CalendarContract.Events.CONTENT_URI, values);

    }
}
