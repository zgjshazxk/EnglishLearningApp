package com.usts.englishlearning.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.WordListAdapter;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemWordList;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends BaseActivity {

    private List<ItemWordList> wordLists = new ArrayList<>();

    private List<Word> words = new ArrayList<>();

    private RecyclerView recyclerView;

    private WordListAdapter wordListAdapter;

    private final int modeAll = 0;
    private final int modeStar = 1;
    private final int modeEasy = 2;

    private int currentMode;

    private ProgressDialog progressDialog;

    public static boolean isUpdate = false;

    private final int FINISH = 1;

    private TextView textInfor, textSort;

    private LinearLayout layoutSort, layoutTip;

    private final String[] sorts = {"全部单词", "收藏单词", "简单词"};

    private static final String TAG = "ListActivity";

    private int currentItemNum = 0;

    private int addItemNum = 100;

    private int currentAllNumber;

    private ProgressBar progressBar;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    wordListAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    if (words.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        layoutTip.setVisibility(View.VISIBLE);
                    } else {
                        layoutTip.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    switch (currentMode) {
                        case modeAll:
                            textSort.setText(sorts[modeAll]);
                            textInfor.setText("单词数：" + currentAllNumber);
                            break;
                        case modeStar:
                            textSort.setText(sorts[modeStar]);
                            textInfor.setText("单词数：" + words.size());
                            break;
                        case modeEasy:
                            textSort.setText(sorts[modeEasy]);
                            textInfor.setText("单词数：" + words.size());
                            break;

                    }
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        init();

        currentAllNumber = ConstantData.wordTotalNumberById(LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class).get(0).getCurrentBookId());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        wordListAdapter = new WordListAdapter(wordLists);
        recyclerView.setAdapter(wordListAdapter);

        showProgressDialog();

        currentMode = modeAll;

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateData(currentMode);
            }
        }).start();

        layoutSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setSingleChoiceItems(sorts, currentMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // 延迟500毫秒取消对话框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                // 更换模式
                                if (currentMode != which) {
                                    switch (which) {
                                        case 0:
                                            showProgressDialog();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    wordLists.clear();
                                                    currentItemNum = 0;
                                                    addItemNum = 100;
                                                    updateData(modeAll);
                                                    currentMode = modeAll;
                                                }
                                            }).start();
                                            break;
                                        case 1:
                                            showProgressDialog();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateData(modeStar);
                                                    currentMode = modeStar;
                                                }
                                            }).start();
                                            break;
                                        case 2:
                                            showProgressDialog();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateData(modeEasy);
                                                    currentMode = modeEasy;
                                                }
                                            }).start();
                                            break;
                                    }
                                }
                            }
                        }, 200);
                    }
                }).show();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (currentMode == modeAll) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstCompletelyVisibleItemPosition == 0)
                        Log.d(TAG, "top");
                    int lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastCompletelyVisibleItemPosition == layoutManager.getItemCount() - 1) {
                        progressBar.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                updateData(currentMode);
                            }
                        }).start();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isUpdate && currentMode != modeAll) {
            showProgressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateData(currentMode);
                }
            }).start();
            isUpdate = false;
        }
    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_list_word);
        textInfor = findViewById(R.id.text_list_infor);
        layoutSort = findViewById(R.id.layout_list_sort);
        textSort = findViewById(R.id.text_list_sort);
        layoutTip = findViewById(R.id.layout_list_tip);
        progressBar = findViewById(R.id.progress_list_load);
    }

    private void updateData(int mode) {
        words.clear();
        switch (mode) {
            case modeAll:
                words = LitePal.select("wordId", "word").limit(addItemNum).offset(currentItemNum + addItemNum).find(Word.class);
                currentItemNum = addItemNum + currentItemNum;
                break;
            case modeStar:
                wordLists.clear();
                words = LitePal.where("isCollected = ?", 1 + "").select("wordId", "word").find(Word.class);
                break;
            case modeEasy:
                wordLists.clear();
                words = LitePal.where("isEasy = ?", 1 + "").select("wordId", "word").find(Word.class);
                break;
        }
        for (Word word : words) {
            wordLists.add(new ItemWordList(word.getWordId(), word.getWord(), getMeans(word.getWordId()), false, true));
        }
        Message message = new Message();
        message.what = FINISH;
        handler.sendMessage(message);
    }

    private String getMeans(int id) {
        List<Interpretation> interpretationList = LitePal.where("wordId = ?", id + "").find(Interpretation.class);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(interpretationList.get(0).getWordType() + ". " + interpretationList.get(0).getCHSMeaning());
        return stringBuilder.toString();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(ListActivity.this);
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage("数据正在加载中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currentItemNum = 0;
        addItemNum = 100;
        finish();
    }

}
