package com.example.yannd.tp2_inf8405;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannd on 2016-03-13.
 * This class represents puts an EventPlace in a ListView row
 */
public class EventRowAdapter extends BaseAdapter{
    final int SELECT_PHOTO = 1; // Id for photo picking activity
    ArrayList<MeetingEvent> events;
    MeetingPlannerActivity parentActivity;
    Context context;
    private static LayoutInflater inflater = null;

    public EventRowAdapter(MeetingPlannerActivity parentActivity, Context context, List<MeetingEvent> events) {
        this.parentActivity = parentActivity;
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.event_listview_layout, null);

        // Initialize meeting name text
        TextView meetingName = (TextView) vi.findViewById(R.id.meetingName);
        meetingName.setText(events.get(position).getMeetingName());

        TextView meetingDate = (TextView) vi.findViewById(R.id.meetingDate);
        TextView meetingPlaceName = (TextView) vi.findViewById(R.id.meetingPlaceName);
        TextView meetingPlaceAdress = (TextView) vi.findViewById(R.id.meetingPlaceAdress);

        final MeetingEvent event = (MeetingEvent)getItem(position);
        EventPlace place;

        // Initialize meeting date view
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm aaa");
        meetingDate.setText(sdf.format(event.getDate().getTime()));

        if(event.getFinalPlace() != null){

            LinearLayout meetingOrganizerFields = (LinearLayout)vi.findViewById(R.id.organizer_fields);
            TextView descriptionView = (TextView)vi.findViewById(R.id.description_view);

            descriptionView.setText(event.getDescription() != null ? event.getDescription() : "");
            ImageView eventPhoto = (ImageView)vi.findViewById(R.id.eventPhoto);

            // Shows the event photo only if there is one
            eventPhoto.setVisibility(event.getGetDecodedImage() != null ? View.VISIBLE : View.GONE);
            if(event.getGetDecodedImage() != null)
                eventPhoto.setImageBitmap(event.getGetDecodedImage());

            // Change desription and add photo only for the organizer
            meetingOrganizerFields.setVisibility(DataManager.getInstance().getCurrentUser().isMeetingOrganizer() ? View.VISIBLE : View.GONE);

            meetingPlaceAdress.setText(event.getFinalPlace().getVicinity());
            meetingPlaceName.setText(event.getFinalPlace().getName());
            vi.findViewById(R.id.votingGroup).setVisibility(View.GONE);
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.VISIBLE);

            // Pick a picture for the event
            vi.findViewById(R.id.add_photo_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.SetEventPhoto(event);
                }
            });

            // Changes the description of the event
            vi.findViewById(R.id.change_description_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.ShowEventDescriptionChangeDialog(event);
                }
            });
        }
        else{
            // Sets the listeners for the votes
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.GONE);
            vi.findViewById(R.id.votingGroup).setVisibility(View.VISIBLE);

            final Button firstPlaceButton = (Button)vi.findViewById(R.id.firstPlaceButton);
            final Button secondPlaceButton = (Button)vi.findViewById(R.id.secondPlaceButton);
            final Button thirdPlaceButton= (Button)vi.findViewById(R.id.thirdPlaceButton);

            place = event.getPlaces().get(0);
            firstPlaceButton.setText(place.getName() + " : " + place.getVicinity());
            firstPlaceButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            firstPlaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceButton.setEnabled(false);
                    secondPlaceButton.setEnabled(true);
                    thirdPlaceButton.setEnabled(true);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(0), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            place = event.getPlaces().get(1);
            secondPlaceButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            secondPlaceButton.setText(place.getName() + " : " + place.getVicinity());
            secondPlaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceButton.setEnabled(true);
                    secondPlaceButton.setEnabled(false);
                    thirdPlaceButton.setEnabled(true);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(1), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            place = event.getPlaces().get(2);
            thirdPlaceButton.setText(place.getName() + " : " + place.getVicinity());
            thirdPlaceButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            thirdPlaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceButton.setEnabled(true);
                    secondPlaceButton.setEnabled(true);
                    thirdPlaceButton.setEnabled(false);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(2), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

        }
        return vi;
    }
}
