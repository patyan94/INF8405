package com.example.yannd.tp2_inf8405;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannd on 2016-03-13.
 * This class represents puts an EventPlace in a ListView row
 */
public class EventRowAdapter extends BaseAdapter{
    ArrayList<MeetingEvent> events;
    Context context;
    private static LayoutInflater inflater = null;

    public EventRowAdapter(Context context, List<MeetingEvent> events) {
        this.context = context;
        this.events = (ArrayList)events;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.event_listview_layout, null);


        TextView meetingName = (TextView) vi.findViewById(R.id.meetingName);
        meetingName.setText(events.get(position).getMeetingName());

        TextView meetingDate = (TextView) vi.findViewById(R.id.meetingDate);


        TextView meetingPlaceName = (TextView) vi.findViewById(R.id.meetingPlaceName);
        TextView meetingPlaceAdress = (TextView) vi.findViewById(R.id.meetingPlaceAdress);

        MeetingEvent event = (MeetingEvent)getItem(position);
        EventPlace place;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, hh:mm");
        meetingDate.setText(sdf.format(event.getDate().getTime()));
        if(event.getFinalPlace() != null){

            meetingPlaceAdress.setText(event.getFinalPlace().getVicinity());
            meetingPlaceName.setText(event.getFinalPlace().getName());
            vi.findViewById(R.id.votingRadioGroup).setVisibility(View.GONE);
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.VISIBLE);
        }
        else{
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.GONE);
            vi.findViewById(R.id.votingRadioGroup).setVisibility(View.VISIBLE);

            RadioButton firstPlaceRadioButton = (RadioButton)vi.findViewById(R.id.firstPlaceRadioButton);
            place = event.getPlaces().get(0);
            firstPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            firstPlaceRadioButton.setChecked(place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            firstPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(0), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            RadioButton secondPlaceRadioButton = (RadioButton)vi.findViewById(R.id.secondPlaceRadioButton);
            place = event.getPlaces().get(1);
            secondPlaceRadioButton.setChecked(place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            secondPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            secondPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(1), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            RadioButton thirdPlaceRadioButton= (RadioButton)vi.findViewById(R.id.thirdPlaceRadioButton);
            place = event.getPlaces().get(2);
            thirdPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            thirdPlaceRadioButton.setChecked(place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            thirdPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(2), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

        }
        return vi;
    }
}
