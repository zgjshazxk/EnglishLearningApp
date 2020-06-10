package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.ShowActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.entity.IdAnalyse;
import com.usts.englishlearning.entity.ItemMatch;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    private List<IdAnalyse> idAnalyseList = new ArrayList();

    private static final String TAG = "MatchAdapter";

    private List<ItemMatch> mItemMatchList;

    private ShowActivity showActivity = new ShowActivity();

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        CardView cardView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            cardView = itemView.findViewById(R.id.card_mt);
            textView = itemView.findViewById(R.id.text_mt_word);
        }

    }

    public MatchAdapter(List<ItemMatch> mItemMatchList) {
        this.mItemMatchList = mItemMatchList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemMatch itemMatch = mItemMatchList.get(position);
                // 一开始没有
                if (idAnalyseList.isEmpty()) {
                    idAnalyseList.add(new IdAnalyse(itemMatch.getId(), position));
                    itemMatch.setChosen(true);
                    notifyDataSetChanged();
                } else if (idAnalyseList.size() == 1) {
                    if (idAnalyseList.get(0).getWordId() == itemMatch.getId() && idAnalyseList.get(0).getPosition() != position) {
                        idAnalyseList.add(new IdAnalyse(itemMatch.getId(), position));
                        List<ItemMatch> itemMatches = new ArrayList<>();
                        itemMatches.add(mItemMatchList.get(idAnalyseList.get(0).getPosition()));
                        itemMatches.add(mItemMatchList.get(position));
                        mItemMatchList.removeAll(itemMatches);
                        notifyItemRemoved(position);
                        notifyItemChanged(position, mItemMatchList.size());
                        if (idAnalyseList.get(0).getPosition() == 0) {
                            notifyItemRemoved(0);
                            notifyItemChanged(0, mItemMatchList.size());
                        } else {
                            notifyItemRemoved(idAnalyseList.get(0).getPosition() - 1);
                            notifyItemChanged(idAnalyseList.get(0).getPosition() - 1, mItemMatchList.size());
                        }
                        notifyItemChanged(0, mItemMatchList.size());
                        idAnalyseList.clear();
                        MediaHelper.playLocalFile(ConstantData.RIGHT_SIGN);
                        if (mItemMatchList.isEmpty()) {
                            Toast.makeText(MyApplication.getContext(), "匹配完成", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.setClass(MyApplication.getContext(), ShowActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(showActivity.SHOW_TYPE, showActivity.TYPE_MATCH);
                            MyApplication.getContext().startActivity(intent);
                        }
                    } else {
                        ItemMatch itemMatch1 = mItemMatchList.get(idAnalyseList.get(0).getPosition());
                        itemMatch1.setChosen(false);
                        ItemMatch itemMatch2 = mItemMatchList.get(position);
                        itemMatch2.setChosen(false);
                        notifyDataSetChanged();
                        idAnalyseList.clear();
                        MediaHelper.playLocalFile(ConstantData.WRONG_SIGN);
                        Toast.makeText(MyApplication.getContext(), "点错了哦", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemMatch itemMatch = mItemMatchList.get(position);
        holder.textView.setText(itemMatch.getWordString());
        if (itemMatch.isChosen()) {
            if (ConfigData.getIsNight()) {
                holder.cardView.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLightBlueN));
                holder.textView.setTextColor(MyApplication.getContext().getColor(R.color.colorFontWhite));
                Log.d(TAG, "sss");
            } else {
                holder.cardView.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLightBlue));
                holder.textView.setTextColor(MyApplication.getContext().getColor(R.color.colorBgWhite));
            }
        } else {
            if (ConfigData.getIsNight()) {
                holder.cardView.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLightWhiteN));
                holder.textView.setTextColor(MyApplication.getContext().getColor(R.color.colorLightBlackN));
            } else {
                holder.cardView.setCardBackgroundColor(MyApplication.getContext().getColor(R.color.colorLightWhite));
                holder.textView.setTextColor(MyApplication.getContext().getColor(R.color.colorLightBlack));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItemMatchList.size();
    }

}
