package com.projetinfomobile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.HashMap;

import Model.DatabaseInterface;
import Model.Serie;

/**
 * Created by yannd on 2016-04-05.
 */
public class SerieSearchResultAdapter extends ArrayAdapter<Serie> {

    RequestQueue imageRequests;
    private LayoutInflater inflater = null;
    public SerieSearchResultAdapter(Context context){
        super(context, R.layout.serie_search_result_listview_item);
        imageRequests = Volley.newRequestQueue(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View row = inflater.inflate(R.layout.serie_search_result_listview_item, parent, false);

        final Serie serie = getItem(position);
        Button add = (Button) row.findViewById(R.id.add_serie);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseInterface.Instance().AddSerie(serie.getID());
            }
        });
        TextView serieName = (TextView)row.findViewById((R.id.serie_name));
        serieName.setText(serie.getName());

        if(!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
            ImageRequest request = new ImageRequest(serie.getPhotoURL(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            ImageView imageView = (ImageView) row.findViewById(R.id.serie_poster);
                            imageView.setImageBitmap(bitmap);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            imageRequests.add(request);
        }

        return row;
    }

    @Override
    public void clear(){
        super.clear();
    }
}
