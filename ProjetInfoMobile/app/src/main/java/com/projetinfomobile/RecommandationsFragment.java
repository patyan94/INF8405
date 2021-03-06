package com.projetinfomobile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.json.JSONObject;

import Model.DatabaseInterface;
import Model.OMDBInterface;
import Model.Recommendation;
import Model.Serie;


public class RecommandationsFragment extends Fragment {
    FirebaseRecyclerAdapter<Recommendation, RecommendedSerieViewHolder> recommendedSeriesAdapter;
    RecyclerView recommendedSeriesView;
    OMDBInterface omdbInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        omdbInterface = OMDBInterface.Start(getContext());
        View view = inflater.inflate(R.layout.fragment_recommandations, container, false);

        // Setups the view for the list of recommendations
        recommendedSeriesView = (RecyclerView)view.findViewById(R.id.recommendation_recyclerView);
        recommendedSeriesAdapter = new FirebaseRecyclerAdapter<Recommendation, RecommendedSerieViewHolder>(Recommendation.class, R.layout.series_suggsetion_listview_item, RecommendedSerieViewHolder.class, DatabaseInterface.Instance().GetCurrentUserSeriesSuggestionNode()) {
            @Override
            protected void populateViewHolder(final RecommendedSerieViewHolder recommendedSerieViewHolder, final Recommendation r, int i) {
                recommendedSerieViewHolder.addRecommendationSerieButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().AddSerie(r.getSerieID());
                    }
                });
                recommendedSerieViewHolder.removeRecommendationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseInterface.Instance().RefuseSerieSuggestion(r.getSerieID());
                    }
                });
                omdbInterface.GetSerieInfo(r.getSerieID(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Serie serie = Serie.FromJSONObject(response);
                            recommendedSerieViewHolder.title.setText(serie.getName());
                            recommendedSerieViewHolder.description.setText(serie.getDescription());
                            if (!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                                omdbInterface.GetPoster(serie.getPhotoURL(), recommendedSerieViewHolder.posterView);
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
        recommendedSeriesView.setHasFixedSize(true);
        recommendedSeriesView.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendedSeriesView.setAdapter(recommendedSeriesAdapter);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recommendedSeriesAdapter.cleanup();
    }

    // Holder for the recommandation item
    public static class RecommendedSerieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;
        Button removeRecommendationButton;
        Button addRecommendationSerieButton;
        public RecommendedSerieViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.serie_name);
            description = (TextView)itemView.findViewById(R.id.serie_description);
            posterView = (ImageView)itemView.findViewById(R.id.serie_poster);
            removeRecommendationButton =(Button)itemView.findViewById(R.id.remove_recommendation);
            addRecommendationSerieButton =(Button)itemView.findViewById(R.id.add_recommendation);
        }
    }
}
