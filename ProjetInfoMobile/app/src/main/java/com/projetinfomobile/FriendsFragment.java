package com.projetinfomobile;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;

import Model.DatabaseInterface;
import Model.UserData;

public class FriendsFragment extends Fragment {
    FirebaseListAdapter<String> friendsListAdapter;
    FirebaseListAdapter<String> friendsRequestListAdapter;
    ArrayList<String> autoCompleteSuggestions = new ArrayList<>();
    AutoCompleteTextView friendSearchAutocomplete;
    com.firebase.client.Query usernameQuery;
    ArrayAdapter<String> autoCompleteAdapter;
    Button addFriendButton;
    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_friends, container, false);

        addFriendButton = (Button)fragmentView.findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseInterface.Instance().SendFriendRequest(friendSearchAutocomplete.getText().toString());
            }
        });
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


        friendsListAdapter = new FirebaseListAdapter<String>(this.getActivity(), String.class, R.layout.friends_listview_item, DatabaseInterface.Instance().GetFriendListNode()) {
            @Override
            protected void populateView(View view, final String username, int position) {
                ((TextView)view.findViewById(R.id.username)).setText(username);
                ((Button)view.findViewById(R.id.delete_friend_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().DeleteFriend(username);
                    }
                });
                //((ImageView)view.findViewById(R.id.profile_picture)).setText(username);
            }
        };
        ((ListView) fragmentView.findViewById(R.id.friends_listview)).setAdapter(friendsListAdapter);
        friendsRequestListAdapter = new FirebaseListAdapter<String>(this.getActivity(), String.class, R.layout.friend_request_layout, DatabaseInterface.Instance().GetReceivedFriendRequestsNode()) {
            @Override
            protected void populateView(View view, final String username, int position) {
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
            }
        };
        ((ListView) fragmentView.findViewById(R.id.friends_requests_listview)).setAdapter(friendsRequestListAdapter);


        usernameQuery = DatabaseInterface.Instance().GetUserIDNode();
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        friendsListAdapter.cleanup();
        friendsRequestListAdapter.cleanup();
    }
}
