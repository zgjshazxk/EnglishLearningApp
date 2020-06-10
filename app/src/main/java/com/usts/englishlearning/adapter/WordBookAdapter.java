package com.usts.englishlearning.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.ChangePlanActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.entity.ItemWordBook;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;

import java.util.List;

public class WordBookAdapter extends RecyclerView.Adapter<WordBookAdapter.ViewHolder> {

    private List<ItemWordBook> mItemWordBookList;

    private Thread thread;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imgBook;
        TextView textBookName, textBookSource, textBookWordNum;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imgBook = itemView.findViewById(R.id.item_img_book);
            textBookName = itemView.findViewById(R.id.item_text_book_name);
            textBookSource = itemView.findViewById(R.id.item_text_book_source);
            textBookWordNum = itemView.findViewById(R.id.item_text_book_word_num);
        }

    }

    public WordBookAdapter(List<ItemWordBook> mItemWordBookList) {
        this.mItemWordBookList = mItemWordBookList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final ItemWordBook itemWordBook = mItemWordBookList.get(position);
                List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
                if (userConfigs.get(0).getCurrentBookId() == itemWordBook.getBookId() &&
                        userConfigs.get(0).getWordNeedReciteNum() != 0) {
                    Toast.makeText(MyApplication.getContext(), "当前选的就是这本书哦", Toast.LENGTH_SHORT).show();
                } else {
                    // 更新数据
                    UserConfig userConfig = new UserConfig();
                    userConfig.setCurrentBookId(itemWordBook.getBookId());
                    userConfig.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
                    // 传值
                    Intent intent = new Intent(MyApplication.getContext(), ChangePlanActivity.class);
                    intent.putExtra(ConfigData.UPDATE_NAME, ConfigData.notUpdate);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(intent);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemWordBook itemWordBook = mItemWordBookList.get(position);
        Glide.with(MyApplication.getContext()).load(itemWordBook.getBookImg()).into(holder.imgBook);
        holder.textBookName.setText(itemWordBook.getBookName());
        holder.textBookSource.setText(itemWordBook.getBookSource());
        holder.textBookWordNum.setText(itemWordBook.getBookWordNum() + "");
    }

    @Override
    public int getItemCount() {
        return mItemWordBookList.size();
    }

}
