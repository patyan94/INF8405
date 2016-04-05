package com.projetinfomobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.firebase.ui.FirebaseListAdapter;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.DatabaseInterface;
import Model.OMDBInterface;
import Model.Serie;

public class SeriesFragment extends Fragment {

    public class WatchedSerieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;
        Button removeSerieButton;
        public WatchedSerieViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.serie_name);
            description = (TextView)itemView.findViewById(R.id.serie_description);
            posterView = (ImageView)itemView.findViewById(R.id.serie_poster);
            removeSerieButton =(Button)itemView.findViewById(R.id.remove_serie_button);
        }
    }
    public class SearchSerieResultViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;
        Button removeSerieButton;
        public SearchSerieResultViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.serie_name);
            description = (TextView)itemView.findViewById(R.id.serie_description);
            posterView = (ImageView)itemView.findViewById(R.id.serie_poster);
            removeSerieButton =(Button)itemView.findViewById(R.id.remove_serie_button);
        }
    }

    Button searchSeriesButton;
    EditText searchSerieTitle;
    ListView searchSerieResults;
    RecyclerView watchedSeriesListview;
    OMDBInterface omdbInterface;
    FirebaseRecyclerAdapter<String, WatchedSerieViewHolder> watchedSeriesAdapter;
    SerieSearchResultAdapter serieSearchResultAdapter;
    int currentSearchPage = 0;

    public SeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        serieSearchResultAdapter = new SerieSearchResultAdapter(getContext());
        omdbInterface = OMDBInterface.Start(getContext());

        View view = inflater.inflate(R.layout.fragment_series, container, false);
        searchSeriesButton = (Button)view.findViewById(R.id.search_serie_button);
        searchSeriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSeriesButton.setEnabled(false);
                serieSearchResultAdapter.clear();
                currentSearchPage = 1;
                omdbInterface.SearchSerie(searchSerieTitle.getText().toString(), currentSearchPage, onSerieSearchResponse, onSerieSearchError);
            }
        });
        searchSerieTitle = (EditText)view.findViewById(R.id.search_serie_title);
        watchedSeriesListview = (RecyclerView)view.findViewById(R.id.series_listview);
        watchedSeriesListview.setHasFixedSize(true);
        watchedSeriesListview.setLayoutManager(new LinearLayoutManager(getContext()));
        searchSerieResults = (ListView)view.findViewById(R.id.search_results_listview);
        searchSerieResults.setAdapter(serieSearchResultAdapter);


        watchedSeriesAdapter = new FirebaseRecyclerAdapter<String, WatchedSerieViewHolder>(String.class, R.layout.series_listview_item, WatchedSerieViewHolder.class,DatabaseInterface.Instance().GetSeriesListNode()) {
            @Override
            protected void populateViewHolder(final WatchedSerieViewHolder view, final String serieID, int position) {
                Log.i("Populate", serieID);
                view.removeSerieButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().DeleteSerie(serieID);
                    }
                });
                omdbInterface.GetSerieInfo(serieID, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("Response", serieID);

                            Serie serie = Serie.FromJSONObject(response);
                            view.title.setText(serie.getName());
                            view.description.setText(serie.getDescription());
                            if(!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                                omdbInterface.GetPoster(serie.getPhotoURL(), view.posterView);
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
        watchedSeriesListview.setAdapter(watchedSeriesAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        watchedSeriesAdapter.cleanup();
    }

    Response.Listener<JSONObject> onSerieSearchResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONArray results = response.getJSONArray("Search");
                for(int i = 0; i<results.length(); ++i){
                    serieSearchResultAdapter.add(Serie.FromJSONObject(results.getJSONObject(i)));
                }
                int nbResults = response.getInt("totalResults");
                if(serieSearchResultAdapter.getCount() < nbResults){
                    omdbInterface.SearchSerie(searchSerieTitle.getText().toString(), ++currentSearchPage, onSerieSearchResponse, onSerieSearchError);
                }
                else{
                    searchSeriesButton.setEnabled(true);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener onSerieSearchError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };
}
