package com.usts.englishlearning.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.FolderDetailActivity;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.entity.ItemWordFolder;
import com.usts.englishlearning.util.MyApplication;

import org.litepal.LitePal;

import java.util.List;

public class WordFolderAdapter extends RecyclerView.Adapter<WordFolderAdapter.ViewHolder> {

    private List<ItemWordFolder> mItemWordFolderLists;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imgDelete;
        TextView textName, textRemark, textNum;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imgDelete = itemView.findViewById(R.id.img_itfl_delete);
            textName = itemView.findViewById(R.id.text_itfl_name);
            textRemark = itemView.findViewById(R.id.text_itfl_remark);
            textNum = itemView.findViewById(R.id.text_itfl_wordNum);
        }

    }

    public WordFolderAdapter(List<ItemWordFolder> mItemWordFolderLists) {
        this.mItemWordFolderLists = mItemWordFolderLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_folder, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ItemWordFolder wordFolder = mItemWordFolderLists.get(position);
                if (wordFolder.getWordNum() > 0) {
                    Intent intent = new Intent(MyApplication.getContext(), FolderDetailActivity.class);
                    FolderDetailActivity.currentFolderId = wordFolder.getFolderId();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(intent);
                } else {
                    Toast.makeText(MyApplication.getContext(), "该单词夹下没有内容哦", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                final ItemWordFolder wordFolder = mItemWordFolderLists.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle("提示")
                        .setMessage("确定删除此单词夹吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mItemWordFolderLists.remove(position);
                                notifyItemRemoved(position);
                                notifyItemChanged(0, mItemWordFolderLists.size());
                                LitePal.deleteAll(WordFolder.class, "id = ?", wordFolder.getFolderId() + "");
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemWordFolder itemWordFolder = mItemWordFolderLists.get(position);
        holder.textName.setText(itemWordFolder.getFolderName());
        holder.textRemark.setText(itemWordFolder.getFolderRemark());
        holder.textNum.setText(itemWordFolder.getWordNum() + "");
    }

    @Override
    public int getItemCount() {
        return mItemWordFolderLists.size();
    }

}
