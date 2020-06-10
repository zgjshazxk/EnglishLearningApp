package com.usts.englishlearning.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.WordBookAdapter;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.entity.ItemWordBook;
import com.usts.englishlearning.util.ActivityCollector;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ChooseWordDBActivity extends BaseActivity {

    private RecyclerView recyclerView;

    private ImageView imgRecover;

    // 书单数据
    private List<ItemWordBook> itemWordBookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_word_db);

        init();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        initData();
        WordBookAdapter wordBookAdapter = new WordBookAdapter(itemWordBookList);
        recyclerView.setAdapter(wordBookAdapter);

        imgRecover.setVisibility(View.GONE);
        imgRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseWordDBActivity.this);
                builder.setTitle("提示")
                        .setMessage("如果您之前有备份数据，可以点此进行还原数据")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ChooseWordDBActivity.this, SynchronyActivity.class);
                                intent.putExtra(SynchronyActivity.TYPE_NAME, false);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

    }

    // 初始化控件
    private void init() {
        recyclerView = findViewById(R.id.recycler_word_book_list);
        imgRecover = findViewById(R.id.img_wb_recover);
    }

    // 初始化数据
    private void initData() {
        itemWordBookList.add(new ItemWordBook(ConstantData.CET4_WORDBOOK, ConstantData.bookNameById(ConstantData.CET4_WORDBOOK), ConstantData.wordTotalNumberById(ConstantData.CET4_WORDBOOK), "来源：有道考神", ConstantData.bookPicById(ConstantData.CET4_WORDBOOK)));
        itemWordBookList.add(new ItemWordBook(ConstantData.CET6_WORDBOOK, ConstantData.bookNameById(ConstantData.CET6_WORDBOOK), ConstantData.wordTotalNumberById(ConstantData.CET6_WORDBOOK), "来源：有道考神", ConstantData.bookPicById(ConstantData.CET6_WORDBOOK)));
        itemWordBookList.add(new ItemWordBook(ConstantData.CET6ALL, ConstantData.bookNameById(ConstantData.CET6ALL), ConstantData.wordTotalNumberById(ConstantData.CET6ALL), "来源：有道考神", ConstantData.bookPicById(ConstantData.CET6ALL)));
        itemWordBookList.add(new ItemWordBook(ConstantData.KAOYAN_WORDBOOK, ConstantData.bookNameById(ConstantData.KAOYAN_WORDBOOK), ConstantData.wordTotalNumberById(ConstantData.KAOYAN_WORDBOOK), "来源：有道考神", ConstantData.bookPicById(ConstantData.KAOYAN_WORDBOOK)));
        itemWordBookList.add(new ItemWordBook(ConstantData.KAOYANALL, ConstantData.bookNameById(ConstantData.KAOYANALL), ConstantData.wordTotalNumberById(ConstantData.KAOYANALL), "来源：有道考神", ConstantData.bookPicById(ConstantData.KAOYANALL)));
    }

    @Override
    public void onBackPressed() {
        // 已登录
        if (LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class).get(0).getCurrentBookId() != -1)
            super.onBackPressed();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChooseWordDBActivity.this);
            builder.setTitle("提示")
                    .setMessage("确定要退出吗?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCollector.finishAll();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }
}
