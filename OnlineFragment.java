package com.minhtien.iwallpaper.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.minhtien.iwallpaper.R;
import com.minhtien.iwallpaper.adapters.CategoryAdapter;
import com.minhtien.iwallpaper.adapters.RecyclerItemClickListener;
import com.minhtien.iwallpaper.helper.AppController;
import com.minhtien.iwallpaper.helper.VarHolder;
import com.minhtien.iwallpaper.models.object.online.Category;
import com.minhtien.iwallpaper.screens.ViewOnline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class OnlineFragment extends Fragment {
    private static final String TAG_FEED = "feed", TAG_ENTRY = "entry",
            TAG_GPHOTO_ID = "gphoto$id", TAG_T = "$t",
            TAG_ALBUM_TITLE = "title";
    private RecyclerView rcvLayoutOnlineAlbums;
    private CategoryAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.online_fragment, container, false);
        if (VarHolder.arrAlbumOnline.size() == 0 )
        {
            initData();
            Log.d("test20","get data from server");
            for (Category category : VarHolder.arrAlbumOnline){
                Log.d("test21","url luu duoc : " + category.getUrl());
            }
        }
        mAdapter = new CategoryAdapter(getActivity(),VarHolder.arrAlbumOnline);
        rcvLayoutOnlineAlbums = (RecyclerView) view.findViewById(R.id.rcv_activity_main__list_catagories_online);
        rcvLayoutOnlineAlbums.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rcvLayoutOnlineAlbums.setHasFixedSize(true);
        Log.d("test20", "size albums :" + VarHolder.arrAlbumOnline.size());

        rcvLayoutOnlineAlbums.setAdapter(mAdapter);
        rcvLayoutOnlineAlbums.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), ViewOnline.class);
                        intent.putExtra(VarHolder.KEY_ID_ALBUM, VarHolder.arrAlbumOnline.get(position).getId());
                        startActivity(intent);
                        getActivity().finish();
                    }
                }));
        new Handler(Looper.getMainLooper()).post(new Runnable() {
           @Override
           public void run() {
               mAdapter.notifyDataSetChanged();
           }
       });
        return view;
    }
    private void initData() {
        final String url = VarHolder.URL_PICASA_ALBUMS
                .replace("_PICASA_USER_", AppController.getInstance()
                        .getPrefManger().getGoogleUserName());
        Log.d("test10", "Albums request url: " + url);
        // Preparing volley's json object request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("test", "Albums Response: " + response.toString());
                        List<Category> albums = new ArrayList<Category>();
                        try {
                            JSONArray entry = response.getJSONObject(TAG_FEED)
                                    .getJSONArray(TAG_ENTRY);
                            // loop through albums nodes and add them to album
                            // list
                            Log.d("test10","entry.length : " + entry.length());
                            for (int i = 0; i < entry.length(); i++) {
                                JSONObject albumObj = (JSONObject) entry.get(i);
                                // album id
                                String albumId = albumObj.getJSONObject(
                                        TAG_GPHOTO_ID).getString(TAG_T);
                                // album title
                                String albumTitle = albumObj.getJSONObject(
                                        TAG_ALBUM_TITLE).getString(TAG_T);
                                Category album = new Category();
                                album.setId(albumId);
                                album.setTitle(albumTitle);
                                // get thumb
                                JSONObject media = albumObj.getJSONObject("media$group");
                                JSONArray thumbail = media.getJSONArray("media$thumbnail");
//                                if (thumbail!= null) {
//                                    Log.d("test20", "thum" + thumbail);

                                    JSONObject object = thumbail.getJSONObject(0);
                                    String urlThum = object.getString("url");
                                    Log.d("test20","urlThum :" + urlThum);
                                    album.setUrl(urlThum);
//                                }


                                // add album to list
                                albums.add(album);
                                Log.d("test10", "Album Id: " + albumId
                                        + ", Album Title: " + albumTitle
                                  + "url : " + urlThum
                                );
                            }
                            // Store albums in shared pref
                            AppController.getInstance().getPrefManger()
                                    .storeCategories(albums);
                            VarHolder.arrAlbumOnline.clear();
                            VarHolder.arrAlbumOnline.addAll(albums);
                            mAdapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),
                                    getString(R.string.msg_unknown_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("test10", "Volley Error: " + error.getMessage());
                        // show error toast
                        Toast.makeText(getContext(),
                                getString(R.string.splash_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
        // disable the cache for this request, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);
        // Making the request
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
    @UiThread
    protected void dataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }
}
