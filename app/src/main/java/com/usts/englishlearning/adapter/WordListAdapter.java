package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.FolderDetailActivity;
import com.usts.englishlearning.activity.WordDetailActivity;
import com.usts.englishlearning.activity.WordFolderActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.entity.ItemWordList;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {

    private List<ItemWordList> mItemWordLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imgSearch;
        TextView textWord, textMean;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imgSearch = itemView.findViewById(R.id.img_itls_search);
            textWord = itemView.findViewById(R.id.text_itls_word);
            textMean = itemView.findViewById(R.id.text_itls_mean);
        }

    }

    public WordListAdapter(List<ItemWordList> mItemWordLists) {
        this.mItemWordLists = mItemWordLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordList itemWordList = mItemWordLists.get(position);
                if (itemWordList.isSearch()) {
                    WordDetailActivity.wordId = itemWordList.getWordId();
                    Intent intent = new Intent();
                    intent.setClass(MyApplication.getContext(), WordDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_GENERAL);
                    MyApplication.getContext().startActivity(intent);
                } else {
                    mItemWordLists.remove(position);
                    notifyItemRemoved(position);
                    notifyItemChanged(0, mItemWordLists.size());
                    LitePal.deleteAll(FolderLinkWord.class, "folderId = ? and wordId = ?", FolderDetailActivity.currentFolderId + "", itemWordList.getWordId() + "");
                }
            }
        });
        holder.textMean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordList itemWordList = mItemWordLists.get(position);
                if (itemWordList.isOnClick())
                    itemWordList.setOnClick(false);
                else
                    itemWordList.setOnClick(true);
                notifyDataSetChanged();
            }
        });
        holder.textWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordList itemWordList = mItemWordLists.get(position);
                MediaHelper.play(itemWordList.getWordName());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemWordList itemWordList = mItemWordLists.get(position);
        holder.textMean.setText(itemWordList.getWordMean());
        holder.textWord.setText(itemWordList.getWordName());
        if (itemWordList.isOnClick())
            if (ConfigData.getIsNight())
                holder.textMean.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.colorBgWhiteN));
            else
                holder.textMean.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.colorBgWhite));
        else
            holder.textMean.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.colorGrey));
        if (itemWordList.isSearch()) {
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_search).into(holder.imgSearch);
        } else {
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_reduce).into(holder.imgSearch);
        }
    }

    @Override
    public int getItemCount() {
        return mItemWordLists.size();
    }

}
