package com.usts.englishlearning.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.entity.ItemSentence;
import com.usts.englishlearning.util.MediaHelper;

import java.util.List;

public class DetailSentenceAdapter extends RecyclerView.Adapter<DetailSentenceAdapter.ViewHolder> {

    private List<ItemSentence> mItemSentenceList;

    private static final String TAG = "DetailSentenceAdapter";
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgVoice;
        TextView textEn, textCn;

        public ViewHolder(View itemView) {
            super(itemView);
            imgVoice = itemView.findViewById(R.id.img_wd_item_sentence_voice);
            textEn = itemView.findViewById(R.id.text_wd_item_sen_en);
            textCn = itemView.findViewById(R.id.text_wd_item_sen_cn);
        }

    }

    public DetailSentenceAdapter(List<ItemSentence> mItemSentenceList) {
        this.mItemSentenceList = mItemSentenceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wd_sentence, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final ItemSentence itemSentence = mItemSentenceList.get(position);
                // 播放
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaHelper.play(itemSentence.getEn());
                    }
                }).start();
                Log.d(TAG, itemSentence.getEn());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemSentence itemSentence = mItemSentenceList.get(position);
        holder.textCn.setText(itemSentence.getCn());
        holder.textEn.setText(itemSentence.getEn());
    }

    @Override
    public int getItemCount() {
        return mItemSentenceList.size();
    }

}
