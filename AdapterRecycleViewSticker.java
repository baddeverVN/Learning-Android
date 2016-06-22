package com.beautiful.photosticker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.beautiful.photosticker.R;
import com.beautiful.photosticker.models.Sticker;

import java.util.ArrayList;


/**
 * Created by beautiful on 4/7/2016.
 */
public class AdapterRecycleViewSticker extends RecyclerView.Adapter<AdapterRecycleViewSticker.StickerViewHolde>{

    private Context context;
    private ArrayList<Sticker> stickers;
    private OnAdapterRecycleCallback mOnAdapterRecycleCallback;
    public AdapterRecycleViewSticker(Context context, ArrayList<Sticker> stickers) {
        this.context = context;
        this.stickers = stickers;
    }

    @Override
    public StickerViewHolde onCreateViewHolder(ViewGroup parent, int viewType) {
        StickerViewHolde viewHolder;
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycleview, parent, false);
        viewHolder = new StickerViewHolde(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StickerViewHolde holder, int position) {
        Sticker sticker = stickers.get(position);
        try {
            Glide.with(context)
                    .load(sticker.getmId())
                    .fitCenter()
                    .into(holder.imageView);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    public class StickerViewHolde extends RecyclerView.ViewHolder{
        ImageView imageView;
        public StickerViewHolde(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_view_avt);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAdapterRecycleCallback.onItemClicked(stickers.get(getPosition()).getmId());
                }
            });
        }
    }
    public interface OnAdapterRecycleCallback{
        public void onItemClicked(int id);
    }

    public void setOnItemClickListener(OnAdapterRecycleCallback onAdapterRecycleCallback){
        mOnAdapterRecycleCallback=onAdapterRecycleCallback;
    }
}
