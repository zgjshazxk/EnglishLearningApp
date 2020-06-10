package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.WordDetailActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.entity.ItemUpdateSen;
import com.usts.englishlearning.entity.ItemWordList;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.MyApplication;

import java.util.List;

public class UpdateSentenceAdapter extends RecyclerView.Adapter<UpdateSentenceAdapter.ViewHolder> {

    private List<ItemUpdateSen> mItemUpdateSentenceLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgDelete;
        EditText editChs, editEns;

        public ViewHolder(View itemView) {
            super(itemView);
            imgDelete = itemView.findViewById(R.id.img_it_us_delete);
            editChs = itemView.findViewById(R.id.edit_it_us_sec);
            editEns = itemView.findViewById(R.id.edit_it_us_see);
        }

    }

    public UpdateSentenceAdapter(List<ItemUpdateSen> mItemUpdateSentenceLists) {
        this.mItemUpdateSentenceLists = mItemUpdateSentenceLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_update_sentence, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                mItemUpdateSentenceLists.remove(position);
                notifyItemRemoved(position);
                notifyItemChanged(0, mItemUpdateSentenceLists.size());
            }
        });
        holder.editChs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int position = holder.getAdapterPosition();
                ItemUpdateSen itemUpdateSen = new ItemUpdateSen(s.toString().trim(), mItemUpdateSentenceLists.get(position).getEnSentences());
                mItemUpdateSentenceLists.set(position, itemUpdateSen);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        holder.editEns.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int position = holder.getAdapterPosition();
                ItemUpdateSen itemUpdateSen = new ItemUpdateSen(mItemUpdateSentenceLists.get(position).getChsSentences(), s.toString().trim());
                mItemUpdateSentenceLists.set(position, itemUpdateSen);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemUpdateSen itemUpdateSen = mItemUpdateSentenceLists.get(position);
        holder.editEns.setText(itemUpdateSen.getEnSentences());
        holder.editChs.setText(itemUpdateSen.getChsSentences());
    }

    @Override
    public int getItemCount() {
        return mItemUpdateSentenceLists.size();
    }

}
