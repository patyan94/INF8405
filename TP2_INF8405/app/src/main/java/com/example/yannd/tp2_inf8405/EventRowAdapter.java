package com.example.yannd.tp2_inf8405;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yannd on 2016-03-13.
 * This class represents puts an EventPlace in a ListView row
 */
public class EventRowAdapter extends BaseAdapter{
    final int SELECT_PHOTO = 1;
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

        boolean isMeetinOrganizer = DataManager.getInstance().getCurrentUser().isMeetingOrganizer();
        if(event.getFinalPlace() != null){

            LinearLayout meetingOrganizerFields = (LinearLayout)vi.findViewById(R.id.organizer_fields);
            TextView descriptionView = (TextView)vi.findViewById(R.id.description_view);

            descriptionView.setText(event.getDescription() != null ? event.getDescription() : "");
            ImageView eventPhoto = (ImageView)vi.findViewById(R.id.eventPhoto);
            eventPhoto.setVisibility(event.getGetDecodedImage() != null ? View.VISIBLE : View.GONE);
            if(event.getGetDecodedImage() != null)
                eventPhoto.setImageBitmap(event.getGetDecodedImage());
            // Change desription and add photo only for the organizer
            meetingOrganizerFields.setVisibility(isMeetinOrganizer ? View.VISIBLE : View.GONE);

            meetingPlaceAdress.setText(event.getFinalPlace().getVicinity());
            meetingPlaceName.setText(event.getFinalPlace().getName());
            vi.findViewById(R.id.votingGroup).setVisibility(View.GONE);
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.VISIBLE);

            vi.findViewById(R.id.add_photo_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.eventBeingModified = event;
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    parentActivity.startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            });
            vi.findViewById(R.id.change_description_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.ShowEventDescriptionChangeDialog(event);
                }
            });
        }
        else{
            vi.findViewById(R.id.confirmedEventLocation).setVisibility(View.GONE);
            vi.findViewById(R.id.votingGroup).setVisibility(View.VISIBLE);

            final Button firstPlaceRadioButton = (Button)vi.findViewById(R.id.firstPlaceRadioButton);
            final Button secondPlaceRadioButton = (Button)vi.findViewById(R.id.secondPlaceRadioButton);
            final Button thirdPlaceRadioButton= (Button)vi.findViewById(R.id.thirdPlaceRadioButton);

            place = event.getPlaces().get(0);
            firstPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            firstPlaceRadioButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            firstPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceRadioButton.setEnabled(false);
                    secondPlaceRadioButton.setEnabled(true);
                    thirdPlaceRadioButton.setEnabled(true);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(0), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            place = event.getPlaces().get(1);
            secondPlaceRadioButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            secondPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            secondPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceRadioButton.setEnabled(true);
                    secondPlaceRadioButton.setEnabled(false);
                    thirdPlaceRadioButton.setEnabled(true);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(1), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

            place = event.getPlaces().get(2);
            thirdPlaceRadioButton.setText(place.getName() + " : " + place.getVicinity());
            thirdPlaceRadioButton.setEnabled(!place.hasVoted(DataManager.getInstance().getCurrentUser().getUsername()));
            thirdPlaceRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstPlaceRadioButton.setEnabled(true);
                    secondPlaceRadioButton.setEnabled(true);
                    thirdPlaceRadioButton.setEnabled(false);
                    MeetingEvent event = events.get(position);
                    events.get(position).Vote(event.getPlaces().get(2), DataManager.getInstance().getCurrentUser().getUsername());
                    DataManager.getInstance().addOrUpdateEvent(event);
                }
            });

        }
        return vi;
    }
}
