package com.usts.englishlearning.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.usts.englishlearning.R;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.util.TimeController;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OCRActivity extends BaseActivity {

    private EditText editText;

    private TextView textStart;

    private ImageView imgInto;

    private static final String TAG = "OCRActivity";

    private final String[] choices = {"选择已有单词夹", "自动存入新建单词夹"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        init();

        StringBuilder stringBuilder = new StringBuilder();
        if (!WordController.needLearnWords.isEmpty()) {
            for (Integer integer : WordController.needLearnWords) {
                List<Word> words = LitePal.where("wordId = ?", integer + "").select("word").find(Word.class);
                stringBuilder.append(words.get(0).getWord() + "\n");
            }
            editText.setText(stringBuilder.toString());
        }

        textStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] result = editText.getText().toString().toLowerCase().split("\n");
                if (result.length >= 1) {
                    WordController.needLearnWords.clear();
                    HashMap<Integer, Integer> map = new HashMap<>();
                    for (int i = 0; i < result.length; ++i) {
                        Log.d(TAG, i + result[i]);
                        List<Word> words = LitePal.where("word = ?", result[i]).select("wordId", "word").find(Word.class);
                        if (!words.isEmpty()) {
                            Log.d(TAG, i + "我找到了" + words.get(0).getWord());
                            if (!map.containsValue(words.get(0).getWordId())) {
                                map.put(i, words.get(0).getWordId());
                                Log.d(TAG, "我已添加" + words.get(0).getWord());
                            }
                        }
                    }
                    for (int ii : map.keySet()) {
                        WordController.needLearnWords.add(map.get(ii));
                    }
                    Log.d(TAG, "长度：" + WordController.needLearnWords.size());
                    WordController.justLearnedWords.clear();
                    WordController.needReviewWords.clear();
                    LearnWordActivity.lastWordMean = "";
                    LearnWordActivity.lastWord = "";
                    if (WordController.needLearnWords.size() != 0) {
                        Intent intent = new Intent(OCRActivity.this, LearnWordActivity.class);
                        intent.putExtra(LearnWordActivity.MODE_NAME, LearnWordActivity.MODE_ONCE);
                        startActivity(intent);
                        Toast.makeText(OCRActivity.this, "开始背单词", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OCRActivity.this, "请输入合法单词", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgInto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OCRActivity.this);
                builder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // 延迟500毫秒取消对话框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (which == 0) {
                                    final List<WordFolder> wordFolders = LitePal.findAll(WordFolder.class);
                                    if (!wordFolders.isEmpty()){
                                        String[] folderNames = new String[wordFolders.size()];
                                        for (int i = 0; i < wordFolders.size(); ++i) {
                                            folderNames[i] = wordFolders.get(i).getName();
                                        }
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(OCRActivity.this);
                                        builder2.setSingleChoiceItems(folderNames, -1, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                // 延迟500毫秒取消对话框
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog.dismiss();
                                                        String[] result = editText.getText().toString().toLowerCase().split("\n");
                                                        if (result.length >= 1) {
                                                            HashMap<Integer, Integer> map = new HashMap<>();
                                                            for (int i = 0; i < result.length; ++i) {
                                                                Log.d(TAG, i + result[i]);
                                                                List<Word> words = LitePal.where("word = ?", result[i]).select("wordId", "word").find(Word.class);
                                                                if (!words.isEmpty()) {
                                                                    Log.d(TAG, i + "我找到了" + words.get(0).getWord());
                                                                    if (!map.containsValue(words.get(0).getWordId())) {
                                                                        map.put(i, words.get(0).getWordId());
                                                                        Log.d(TAG, "我已添加" + words.get(0).getWord());
                                                                    }
                                                                }
                                                            }
                                                            for (int ii : map.keySet()) {
                                                                List<FolderLinkWord> folderLinkWords = LitePal.where("wordId = ? and folderId = ?", map.get(ii) + "", wordFolders.get(which).getId() + "").find(FolderLinkWord.class);
                                                                if (folderLinkWords.isEmpty()) {
                                                                    FolderLinkWord folderLinkWord = new FolderLinkWord();
                                                                    folderLinkWord.setFolderId(wordFolders.get(which).getId());
                                                                    folderLinkWord.setWordId(map.get(ii));
                                                                    folderLinkWord.save();
                                                                }
                                                            }
                                                            Toast.makeText(OCRActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }, 200);
                                            }
                                        }).show();
                                    } else {
                                        Toast.makeText(OCRActivity.this, "当前暂无单词夹", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    String[] result = editText.getText().toString().toLowerCase().split("\n");
                                    if (result.length >= 1) {
                                        HashMap<Integer, Integer> map = new HashMap<>();
                                        for (int i = 0; i < result.length; ++i) {
                                            Log.d(TAG, i + result[i]);
                                            List<Word> words = LitePal.where("word = ?", result[i]).select("wordId", "word").find(Word.class);
                                            if (!words.isEmpty()) {
                                                if (!map.containsValue(words.get(0).getWordId())) {
                                                    map.put(i, words.get(0).getWordId());
                                                }
                                            }
                                        }
                                        long currentTime = TimeController.getCurrentTimeStamp();
                                        WordFolder wordFolder = new WordFolder();
                                        wordFolder.setName("拍照取词");
                                        wordFolder.setRemark("自动创建于：" + TimeController.getStringDateDetail(currentTime));
                                        wordFolder.setCreateTime(currentTime + "");
                                        if (wordFolder.save()) {
                                            List<WordFolder> wordFolders = LitePal.where("createTime = ? and name = ?", currentTime + "", "拍照取词").find(WordFolder.class);
                                            if (!wordFolders.isEmpty()) {
                                                for (int ii : map.keySet()) {
                                                    List<FolderLinkWord> folderLinkWords = LitePal.where("wordId = ? and folderId = ?", map.get(ii) + "", wordFolders.get(0).getId() + "").find(FolderLinkWord.class);
                                                    if (folderLinkWords.isEmpty()) {
                                                        FolderLinkWord folderLinkWord = new FolderLinkWord();
                                                        folderLinkWord.setFolderId(wordFolders.get(0).getId());
                                                        folderLinkWord.setWordId(map.get(ii));
                                                        folderLinkWord.save();
                                                    }
                                                }
                                            }
                                            Toast.makeText(OCRActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        }, 200);
                    }
                }).show();
            }
        });

    }

    private void init() {
        editText = findViewById(R.id.edit_ocr_result);
        textStart = findViewById(R.id.text_ocr_start);
        imgInto = findViewById(R.id.img_ocr_into);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WordController.needLearnWords.clear();
    }

}
