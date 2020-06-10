package com.usts.englishlearning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.entity.ItemWordMeanChoice;
import com.usts.englishlearning.listener.OnItemClickListener;
import com.usts.englishlearning.util.MyApplication;

import java.util.List;

public class MeanChoiceAdapter extends RecyclerView.Adapter<MeanChoiceAdapter.ViewHolder> implements View.OnClickListener {

    // 判断是否是第一次点击
    public static boolean isFirstClick = true;

    private RecyclerView recyclerView;

    private List<ItemWordMeanChoice> mItemWordMeanChoiceList;

    private static final String TAG = "MeanChoiceAdapter";

    // 声明单击接口
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // 将RecyclerView附加到Adapter上


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @Override
    public void onClick(View v) {
        int position = recyclerView.getChildAdapterPosition(v);
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(recyclerView, v, position, mItemWordMeanChoiceList.get(position));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View meanView;
        CardView cardMean;
        ImageView imgChoice;
        TextView textWordMean;

        public ViewHolder(View itemView) {
            super(itemView);
            meanView = itemView;
            cardMean = itemView.findViewById(R.id.item_card_word_choice);
            imgChoice = itemView.findViewById(R.id.item_img_word_choice_status);
            textWordMean = itemView.findViewById(R.id.item_text_word_means);
        }

    }

    public MeanChoiceAdapter(List<ItemWordMeanChoice> mItemWordMeanChoiceList) {
        this.mItemWordMeanChoiceList = mItemWordMeanChoiceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_mean_choice, parent, false);
        view.setOnClickListener(this);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemWordMeanChoice itemWordMeanChoice = mItemWordMeanChoiceList.get(position);
        holder.textWordMean.setText(itemWordMeanChoice.getWordMean());
        holder.imgChoice.setVisibility(View.GONE);
        if (ConfigData.getIsNight())
            holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlack));
        else
            holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlack));
        if (itemWordMeanChoice.isRight() == ItemWordMeanChoice.WRONG) {
            // 说明答错了
            if (ConfigData.getIsNight())
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLittleRedN));
            else
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLittleRed));
            holder.imgChoice.setVisibility(View.VISIBLE);
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_wrong).into(holder.imgChoice);
            if (ConfigData.getIsNight())
                holder.textWordMean.setTextColor(MyApplication.getContext().getColor(R.color.colorLightRedN));
            else
                holder.textWordMean.setTextColor(MyApplication.getContext().getColor(R.color.colorLightRed));
        } else if (itemWordMeanChoice.isRight() == ItemWordMeanChoice.RIGHT) {
            // 说明答对了
            if (ConfigData.getIsNight())
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.colorLittleBlueN));
            else
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.colorLittleBlue));
            holder.imgChoice.setVisibility(View.VISIBLE);
            Glide.with(MyApplication.getContext()).load(R.drawable.icon_select_blue).into(holder.imgChoice);
            if (ConfigData.getIsNight())
                holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlueN));
            else
                holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlue));
        } else if (itemWordMeanChoice.isRight() == ItemWordMeanChoice.NOTSTART) {
            if (ConfigData.getIsNight())
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorBgWhiteN));
            else
                holder.cardMean.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorBgWhite));
            holder.imgChoice.setVisibility(View.GONE);
            if (ConfigData.getIsNight())
                holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlackN));
            else
                holder.textWordMean.setTextColor(MyApplication.getContext().getResources().getColor(R.color.colorLightBlack));
        }
    }

    @Override
    public int getItemCount() {
        return mItemWordMeanChoiceList.size();
    }

}
