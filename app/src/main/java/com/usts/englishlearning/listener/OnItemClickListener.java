package com.usts.englishlearning.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.entity.ItemWordMeanChoice;

public interface OnItemClickListener {

    void onItemClick(RecyclerView parent, View view, int position, ItemWordMeanChoice itemWordMeanChoice);

}
