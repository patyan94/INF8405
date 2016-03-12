package com.example.yannd.tp2_inf8405;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Sacha on 2016-03-10.
 */
public class CalendarManager {
    private static CalendarManager instance = null;
    private Context applicationContext = null;

    private CalendarManager(Context ctx) {
        applicationContext = ctx;
    }

    public static CalendarManager getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new CalendarManager(applicationContext);
        }
        return instance;
    }

    /*
    Exemple :
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        calStart.set(2016, 0, 10); //Note that months start from 0 (January)
        calEnd.set(2016, 0, 17); //Note that months start from 0 (January)

        CalendarManager.getInstance(getApplicationContext()).getAvailabilities(calStart, calEnd);

    This exemple will return a list of Calendar objects, one per day that is available to book an event
    This is basic, since if there is already something booked on a day (at any time), the ENTIRE day won't be available (excluded from the returned list)
    This is enough for this application
     */
    public List<Calendar> getAvailabilities(Calendar dateStart, Calendar dateEnd) {
        List<Calendar> availabilitesList = new ArrayList<>();

        int currentDay = dateStart.get(Calendar.DAY_OF_MONTH);
        int lastDay = dateEnd.get(Calendar.DAY_OF_MONTH);

        //Ici je construit la liste initiale des disponibilités qui consiste en la liste des jours qui
        //sont contenus dans l'interval fourni dans les paramètres de la fonction
        while(currentDay != (lastDay + 1)) {
            Calendar start = Calendar.getInstance();
            start.set(dateStart.get(Calendar.YEAR), dateStart.get(Calendar.MONTH), currentDay);
            availabilitesList.add(start);

            currentDay++;
        }

        //Ici on va chercher les évènements du calendrier de la personne durant l'interval ddonné en paramètre
        Cursor cursor = null;
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        //String[] projection = new String[] { "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" };

        //On ne veut que le debut et fin de chacun des évènements, puisque nous c'est les disponibilités qui nous intéressent
        String[] projection = new String[] {"dtstart", "dtend"};

        String selection = "((dtstart >= " + dateStart.getTime().getTime() + ") AND (dtend <= " + dateEnd.getTime().getTime() + "))";

        cursor = applicationContext.getContentResolver().query(Uri.parse("content://com.android.calendar/events"), projection, selection, null, null);

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
    public Calendar getCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }
}
