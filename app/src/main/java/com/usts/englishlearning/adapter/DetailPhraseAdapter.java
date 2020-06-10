package com.usts.englishlearning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.entity.ItemPhrase;

import java.util.List;

public class DetailPhraseAdapter extends RecyclerView.Adapter<DetailPhraseAdapter.ViewHolder> {

    private List<ItemPhrase> mItemPhraseList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textEn, textCn;

        public ViewHolder(View itemView) {
            super(itemView);
            textEn = itemView.findViewById(R.id.text_wd_item_phrase_en);
            textCn = itemView.findViewById(R.id.text_wd_item_phrase_cn);
        }

    }

    public DetailPhraseAdapter(List<ItemPhrase> mItemPhraseList) {
        this.mItemPhraseList = mItemPhraseList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wd_phrase, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemPhrase itemPhrase = mItemPhraseList.get(position);
        holder.textCn.setText(itemPhrase.getCn());
        holder.textEn.setText(itemPhrase.getEn());
    }

    @Override
    public int getItemCount() {
        return mItemPhraseList.size();
    }

}
