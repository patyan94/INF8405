package com.projetinfomobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import Model.DatabaseInterface;
import Model.UserData;

// Fragment to show the friends and friend resuqests received
public class FriendsFragment extends Fragment {

    FirebaseListAdapter<String> friendsListAdapter;
    FirebaseListAdapter<String> friendsRequestListAdapter;
    ArrayAdapter<String> autoCompleteAdapter;

    com.firebase.client.Query usernameQuery;

    ArrayList<String> autoCompleteSuggestions = new ArrayList<>();

    AutoCompleteTextView friendSearchAutocomplete;
    Button addFriendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_friends, container, false);

        // Sends a friend request
        addFriendButton = (Button)fragmentView.findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < friendsListAdapter.getCount(); i++){
                    if(friendsListAdapter.getItem(i).toString().equalsIgnoreCase(friendSearchAutocomplete.getText().toString())){
                        Toast.makeText(getContext(), "This user is already in your friend list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                DatabaseInterface.Instance().SendFriendRequest(friendSearchAutocomplete.getText().toString());
                friendSearchAutocomplete.setText("");
                Toast.makeText(getContext(), "Invitation sent", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the autocomplete for friend search
        friendSearchAutocomplete = (AutoCompleteTextView)fragmentView.findViewById(R.id.friend_search_entry);
        autoCompleteAdapter = new ArrayAdapter<String>(FriendsFragment.this.getActivity(),
                android.R.layout.simple_dropdown_item_1line, autoCompleteSuggestions);
        friendSearchAutocomplete.setAdapter(autoCompleteAdapter);
        friendSearchAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addFriendButton.setEnabled(s.length()>3);
            }
        });


        // Setup the view for the list of friend
        friendsListAdapter = new FirebaseListAdapter<String>(this.getActivity(), String.class, R.layout.friends_listview_item, DatabaseInterface.Instance().GetCurrentUserFriendListNode()) {
            @Override
            protected void populateView(final View view, final String username, int position) {
                Log.i("Populate", username);
                ((TextView)view.findViewById(R.id.username)).setText(username);
                ((Button)view.findViewById(R.id.delete_friend_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().DeleteFriend(username);
                    }
                });
                // Fetch the user photo
                DatabaseInterface.Instance().GetUserDataNode(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData.getUserProfileImage() != null) {
                            ((ImageView) view.findViewById(R.id.profile_picture)).setImageBitmap(userData.getUserProfileImage());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        };
        ((ListView) fragmentView.findViewById(R.id.friends_listview)).setAdapter(friendsListAdapter);

        // Setups the view for the friend requests
        friendsRequestListAdapter = new FirebaseListAdapter<String>(this.getActivity(), String.class, R.layout.friend_request_listview_item, DatabaseInterface.Instance().GetCurrentUserReceivedFriendRequestsNode()) {
            @Override
            protected void populateView(final View view, final String username, int position) {
                ((TextView)view.findViewById(R.id.username)).setText(username);
                ((Button)view.findViewById(R.id.accept)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().AcceptFriendRequest(username);
                    }
                });
                ((Button)view.findViewById(R.id.deny)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().RefuseFriendRequest(username);
                    }
                });

                // Fetch the user photo
                DatabaseInterface.Instance().GetUserDataNode(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData.getUserProfileImage() != null) {
                            ((ImageView) view.findViewById(R.id.profile_picture)).setImageBitmap(userData.getUserProfileImage());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        };
        ((ListView) fragmentView.findViewById(R.id.friends_requests_listview)).setAdapter(friendsRequestListAdapter);

        // Keeps the users list up to date for the autocomplete
        usernameQuery = DatabaseInterface.Instance().GetUsersIDNode();
        usernameQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    String val = (String) dataSnapshot.getValue();
                    if(!DatabaseInterface.Instance().GetCurrentUserData().getUsername().equals(val))
                        autoCompleteAdapter.add(val);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    autoCompleteAdapter.remove((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        friendsListAdapter.cleanup();
        friendsRequestListAdapter.cleanup();
    }
}
