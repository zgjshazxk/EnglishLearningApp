package com.usts.englishlearning.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.WordFolderListAdapter;
import com.usts.englishlearning.adapter.WordListAdapter;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.entity.ItemWordList;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class FolderDetailActivity extends BaseActivity {

    public static int currentFolderId;

    private RecyclerView recyclerView;

    private TextView textName, textRemark;

    private ImageView imgStart;

    private CardView cardRemark;

    private List<ItemWordList> wordLists = new ArrayList<>();

    private WordFolderListAdapter wordListAdapter;

    private String[] editType = {"更改名称", "更改备注"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        init();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        wordListAdapter = new WordFolderListAdapter(wordLists);
        recyclerView.setAdapter(wordListAdapter);

        List<WordFolder> wordFolders = LitePal.where("id = ?", currentFolderId + "").find(WordFolder.class);
        textName.setText(wordFolders.get(0).getName());
        if (wordFolders.get(0).getRemark() != null) {
            if (TextUtils.isEmpty(wordFolders.get(0).getRemark().trim())) {
                textRemark.setVisibility(View.GONE);
                cardRemark.setVisibility(View.GONE);
            } else {
                cardRemark.setVisibility(View.VISIBLE);
                textRemark.setVisibility(View.VISIBLE);
                textRemark.setText(wordFolders.get(0).getRemark());
            }
        } else {
            textRemark.setVisibility(View.GONE);
            cardRemark.setVisibility(View.GONE);
        }

        List<FolderLinkWord> folderLinkWords = LitePal.where("folderId = ?", currentFolderId + "").find(FolderLinkWord.class);
        wordLists.clear();
        if (!folderLinkWords.isEmpty()) {
            for (FolderLinkWord folderLinkWord : folderLinkWords) {
                List<Word> words = LitePal.where("wordId = ?", folderLinkWord.getWordId() + "").select("wordId", "word").find(Word.class);
                Word word = words.get(0);
                wordLists.add(new ItemWordList(word.getWordId(), word.getWord(), getMeans(word.getWordId()), false, false));
            }
            wordListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "暂无单词", Toast.LENGTH_SHORT).show();
        }

        imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FolderLinkWord> folderLinkWords = LitePal.where("folderId = ?", currentFolderId + "").find(FolderLinkWord.class);
                if (!folderLinkWords.isEmpty()) {
                    WordController.needLearnWords.clear();
                    for (ItemWordList itemWordList : wordLists) {
                        WordController.needLearnWords.add(itemWordList.getWordId());
                    }
                    WordController.justLearnedWords.clear();
                    WordController.needReviewWords.clear();
                    LearnWordActivity.lastWordMean = "";
                    LearnWordActivity.lastWord = "";
                    if (WordController.needLearnWords.size() != 0) {
                        Intent intent = new Intent(FolderDetailActivity.this, LearnWordActivity.class);
                        intent.putExtra(LearnWordActivity.MODE_NAME, LearnWordActivity.MODE_ONCE);
                        startActivity(intent);
                        Toast.makeText(FolderDetailActivity.this, "开始背单词", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FolderDetailActivity.this, "请输入合法单词", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder2 = new AlertDialog.Builder(FolderDetailActivity.this);
                builder2.setSingleChoiceItems(editType, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final int type = which;
                        // 延迟500毫秒取消对话框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                final View dialogView = LayoutInflater.from(FolderDetailActivity.this)
                                        .inflate(R.layout.item_edit, null);
                                final EditText editText = dialogView.findViewById(R.id.edit_text);
                                if (type == 0)
                                    editText.setText(textName.getText().toString());
                                else {
                                    List<WordFolder> wordFolders = LitePal.where("id = ?", currentFolderId + "").find(WordFolder.class);
                                    textName.setText(wordFolders.get(0).getName());
                                    if (wordFolders.get(0).getRemark() != null) {
                                        if (TextUtils.isEmpty(wordFolders.get(0).getRemark().trim())) {
                                            editText.setText("");
                                        } else {
                                            editText.setText(wordFolders.get(0).getRemark());
                                        }
                                    } else {
                                        editText.setText("");
                                    }
                                }
                                AlertDialog.Builder inputDialog = new AlertDialog.Builder(FolderDetailActivity.this);
                                if (type == 0)
                                    inputDialog.setTitle("编辑单词夹名称");
                                else
                                    inputDialog.setTitle("编辑备注");
                                inputDialog.setView(dialogView)
                                        .setPositiveButton("确定",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                                                            if (type == 0) {
                                                                WordFolder wordFolder = new WordFolder();
                                                                wordFolder.setName(editText.getText().toString().trim());
                                                                wordFolder.updateAll("id = ?", currentFolderId + "");
                                                                dialog.dismiss();
                                                                textName.setText(editText.getText().toString().trim());
                                                                Toast.makeText(FolderDetailActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                WordFolder wordFolder = new WordFolder();
                                                                wordFolder.setRemark(editText.getText().toString().trim());
                                                                wordFolder.updateAll("id = ?", currentFolderId + "");
                                                                cardRemark.setVisibility(View.VISIBLE);
                                                                textRemark.setVisibility(View.VISIBLE);
                                                                textRemark.setText(editText.getText().toString().trim());
                                                            }
                                                        } else {
                                                            if (type == 0)
                                                                Toast.makeText(FolderDetailActivity.this, "不得为空", Toast.LENGTH_SHORT).show();
                                                            else {
                                                                WordFolder wordFolder = new WordFolder();
                                                                wordFolder.setToDefault("remark");
                                                                wordFolder.updateAll("id = ?", currentFolderId + "");
                                                                textRemark.setVisibility(View.GONE);
                                                                cardRemark.setVisibility(View.GONE);
                                                                Toast.makeText(FolderDetailActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                })
                                        .setNegativeButton("取消", null).show();
                            }
                        }, 200);
                    }
                }).show();
            }
        });

    }

    private void init() {
        textName = findViewById(R.id.text_fd_name);
        textRemark = findViewById(R.id.text_fd_remark);
        imgStart = findViewById(R.id.img_fd_start);
        cardRemark = findViewById(R.id.card_fd_remark);
        recyclerView = findViewById(R.id.recycler_fd);
    }

    private String getMeans(int id) {
        List<Interpretation> interpretationList = LitePal.where("wordId = ?", id + "").find(Interpretation.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < interpretationList.size(); ++i) {
            stringBuilder.append(interpretationList.get(i).getWordType() + ". " + interpretationList.get(i).getCHSMeaning());
            if (i != interpretationList.size() - 1)
                stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

}
