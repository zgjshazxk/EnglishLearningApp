package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.WordDetailActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemShow;
import com.usts.englishlearning.util.MyApplication;

import java.util.List;

public class ShowWordAdapter extends RecyclerView.Adapter<ShowWordAdapter.ViewHolder> {

    private List<ItemShow> mItemShowLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutMain;
        LinearLayout layout;
        ImageView imgStar;
        TextView textWord, textMean;

        public ViewHolder(View itemView) {
            super(itemView);
            layoutMain = itemView.findViewById(R.id.layout_show);
            layout = itemView.findViewById(R.id.layout_is);
            imgStar = itemView.findViewById(R.id.img_is_star);
            textWord = itemView.findViewById(R.id.text_is_name);
            textMean = itemView.findViewById(R.id.text_is_mean);
        }

    }

    public ShowWordAdapter(List<ItemShow> mItemShowLists) {
        this.mItemShowLists = mItemShowLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemShow itemShow = mItemShowLists.get(position);
                if (itemShow.isStar()) {
                    itemShow.setStar(false);
                    Word word = new Word();
                    word.setToDefault("isCollected");
                    word.updateAll("wordId = ?", itemShow.getWordId() + "");
                } else {
                    itemShow.setStar(true);
                    Word word = new Word();
                    word.setIsCollected(1);
                    word.updateAll("wordId = ?", itemShow.getWordId() + "");
                }
                notifyDataSetChanged();
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemShow itemShow = mItemShowLists.get(position);
                WordDetailActivity.wordId = itemShow.getWordId();
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
        ItemShow itemShow = mItemShowLists.get(position);
        holder.textMean.setText(itemShow.getWordMean());
        holder.textWord.setText(itemShow.getWord());
        if (itemShow.isStar())
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_star_fill).into(holder.imgStar);
        else
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_star).into(holder.imgStar);
        if (ConfigData.getIsNight()) {
            holder.layoutMain.setBackgroundColor(MyApplication.getContext().getColor(R.color.colorLittleWhiteN));
        }
    }

    @Override
    public int getItemCount() {
        return mItemShowLists.size();
    }

}
