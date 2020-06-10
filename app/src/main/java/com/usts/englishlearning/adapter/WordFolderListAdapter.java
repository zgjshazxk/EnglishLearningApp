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
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.entity.ItemWordList;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;

import java.util.List;

public class WordFolderListAdapter extends RecyclerView.Adapter<WordFolderListAdapter.ViewHolder> {

    private List<ItemWordList> mItemWordLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imgReduce;
        TextView textWord, textMean;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imgReduce = itemView.findViewById(R.id.img_itwfd_reduce);
            textWord = itemView.findViewById(R.id.text_itwfd_word);
            textMean = itemView.findViewById(R.id.text_itwfd_mean);
        }

    }

    public WordFolderListAdapter(List<ItemWordList> mItemWordLists) {
        this.mItemWordLists = mItemWordLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder_word_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordList itemWordList = mItemWordLists.get(position);
                if (!itemWordList.isSearch()) {
                    mItemWordLists.remove(position);
                    notifyItemRemoved(position);
                    notifyItemChanged(0, mItemWordLists.size());
                    LitePal.deleteAll(FolderLinkWord.class, "folderId = ? and wordId = ?", FolderDetailActivity.currentFolderId + "", itemWordList.getWordId() + "");
                }
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
        holder.textMean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordList itemWordList = mItemWordLists.get(position);
                WordDetailActivity.wordId = itemWordList.getWordId();
                Intent intent = new Intent();
                intent.setClass(MyApplication.getContext(), WordDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(WordDetailActivity.TYPE_NAME, WordDetailActivity.TYPE_GENERAL);
                MyApplication.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemWordList itemWordList = mItemWordLists.get(position);
        holder.textMean.setText(itemWordList.getWordMean());
        holder.textWord.setText(itemWordList.getWordName());
        Glide.with(MyApplication.getContext()).load(R.drawable.icon_reduce).into(holder.imgReduce);
    }

    @Override
    public int getItemCount() {
        return mItemWordLists.size();
    }

}
