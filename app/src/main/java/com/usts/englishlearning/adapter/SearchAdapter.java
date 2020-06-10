package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.WordDetailActivity;
import com.usts.englishlearning.entity.ItemSearch;
import com.usts.englishlearning.util.MyApplication;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<ItemSearch> mItemSearchLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView textWord, textMean, textSound;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            textWord = itemView.findViewById(R.id.text_search_name);
            textMean = itemView.findViewById(R.id.text_search_means);
            textSound = itemView.findViewById(R.id.text_search_sound);
        }

    }

    public SearchAdapter(List<ItemSearch> mItemSearchLists) {
        this.mItemSearchLists = mItemSearchLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemSearch itemSearch = mItemSearchLists.get(position);
                WordDetailActivity.wordId = itemSearch.getWordId();
                Intent intent = new Intent(MyApplication.getContext(), WordDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_GENERAL);
                MyApplication.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemSearch itemSearch = mItemSearchLists.get(position);
        holder.textSound.setText(itemSearch.getWordSound());
        holder.textMean.setText(itemSearch.getWordMeans());
        holder.textWord.setText(itemSearch.getWordName());
    }

    @Override
    public int getItemCount() {
        return mItemSearchLists.size();
    }

}
