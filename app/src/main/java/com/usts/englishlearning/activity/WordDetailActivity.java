package com.usts.englishlearning.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.DetailPhraseAdapter;
import com.usts.englishlearning.adapter.DetailSentenceAdapter;
import com.usts.englishlearning.database.DailyData;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Phrase;
import com.usts.englishlearning.database.Sentence;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.entity.ItemPhrase;
import com.usts.englishlearning.entity.ItemSentence;
import com.usts.englishlearning.util.ActivityCollector;
import com.usts.englishlearning.util.FileUtil;
import com.usts.englishlearning.util.MediaHelper;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class WordDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "WordDetailActivity";

    // 操作栏
    private RelativeLayout layoutContinue, layoutVoice, layoutDelete;
    private RelativeLayout layoutStar, layoutMore, layoutPicCustom, layoutFolder;
    private ImageView imgStar, imgDelete, imgPicCustom;
    private CardView cardPicCustom;
    private TextView textContinue;

    // 单词
    private AutofitTextView textWordName;

    // 单词发音
    private LinearLayout layoutPhoneUk, layoutPhoneUs;
    private TextView textPhoneUk, textPhoneUs;

    // 单词释义
    private TextView textInterpretation;

    // 巧记
    private CardView cardRemMind;
    private TextView textRemMind;

    // 例句
    private CardView cardSentence;
    private RecyclerView recyclerSentence;
    private DetailSentenceAdapter detailSentenceAdapter;
    private List<ItemSentence> itemSentenceList = new ArrayList<>();

    // 英文释义
    private CardView cardEnglish;
    private TextView textEnglish;

    // 词组
    private CardView cardPhrase;
    private RecyclerView recyclerPhrase;
    private DetailPhraseAdapter detailPhraseAdapter;
    private List<ItemPhrase> itemPhraseList = new ArrayList<>();

    // 图片记忆
    private CardView cardPic;
    private ImageView imgPic;

    // 备注
    private CardView cardRemark;
    private TextView textRemark;

    // 单词
    List<Word> words;

    // 传入的单词ID
    public static int wordId;
    private Word currentWord;

    public static final String TYPE_NAME = "typeName";
    public static final int TYPE_LEARN = 1;
    public static final int TYPE_GENERAL = 2;

    private int currentType;

    private String[] mores = {"更改释义", "更改例句", "修改备注", "增加/修改自定义图片"};

    private final int IMAGE_REQUEST_CODE = 1;

    private ProgressDialog progressDialog;

    private byte[] imgByte;
    private Bitmap bitmap;

    private final int FINISH = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    Word word = new Word();
                    word.setPicCustom(imgByte);
                    word.updateAll("wordId = ?", currentWord.getWordId() + "");
                    progressDialog.dismiss();
                    cardPicCustom.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_detail);

        init();

        windowSlide(Gravity.TOP);

        currentType = getIntent().getIntExtra(TYPE_NAME, 0);

        if (currentType == TYPE_GENERAL) {
            textContinue.setText("返回");
        } else {
            textContinue.setText("继续");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setData();
    }

    private void init() {
        layoutContinue = findViewById(R.id.layout_wd_continue);
        layoutContinue.setOnClickListener(this);
        layoutDelete = findViewById(R.id.layout_wd_delete);
        layoutDelete.setOnClickListener(this);
        layoutVoice = findViewById(R.id.layout_wd_voice);
        layoutVoice.setOnClickListener(this);
        textWordName = findViewById(R.id.text_wd_name);
        layoutPhoneUk = findViewById(R.id.layout_wd_phone_uk);
        layoutPhoneUk.setOnClickListener(this);
        layoutPhoneUs = findViewById(R.id.layout_wd_phone_usa);
        layoutPhoneUs.setOnClickListener(this);
        layoutStar = findViewById(R.id.layout_wd_star);
        layoutStar.setOnClickListener(this);
        layoutMore = findViewById(R.id.layout_wd_more);
        layoutMore.setOnClickListener(this);
        textPhoneUk = findViewById(R.id.text_wd_phone_uk);
        textPhoneUs = findViewById(R.id.text_wd_phone_usa);
        textInterpretation = findViewById(R.id.text_wd_interpretation);
        cardRemMind = findViewById(R.id.card_wd_remMethod);
        textRemMind = findViewById(R.id.text_wd_remMethod);
        recyclerSentence = findViewById(R.id.recycler_wd_sentence);
        recyclerPhrase = findViewById(R.id.recycler_wd_phrase);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerSentence.setLayoutManager(linearLayoutManager);
        recyclerSentence.setHasFixedSize(false);
        recyclerSentence.setNestedScrollingEnabled(false);
        recyclerSentence.setFocusable(false);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setSmoothScrollbarEnabled(true);
        linearLayoutManager2.setAutoMeasureEnabled(true);
        recyclerPhrase.setLayoutManager(linearLayoutManager2);
        recyclerPhrase.setHasFixedSize(false);
        recyclerPhrase.setNestedScrollingEnabled(false);
        recyclerPhrase.setFocusable(false);
        detailPhraseAdapter = new DetailPhraseAdapter(itemPhraseList);
        detailSentenceAdapter = new DetailSentenceAdapter(itemSentenceList);
        recyclerPhrase.setAdapter(detailPhraseAdapter);
        recyclerSentence.setAdapter(detailSentenceAdapter);
        cardSentence = findViewById(R.id.card_wd_sentence);
        cardEnglish = findViewById(R.id.card_wd_ie);
        textEnglish = findViewById(R.id.text_wd_ie);
        cardPhrase = findViewById(R.id.card_wd_phrase);
        cardPic = findViewById(R.id.card_wd_pic);
        imgPic = findViewById(R.id.img_wd_pic);
        cardRemark = findViewById(R.id.card_wd_remark);
        textRemark = findViewById(R.id.text_wd_remark);
        textContinue = findViewById(R.id.text_wd_continue);
        imgStar = findViewById(R.id.img_wd_star);
        imgDelete = findViewById(R.id.img_wd_delete);
        layoutPicCustom = findViewById(R.id.layout_wd_pic);
        layoutPicCustom.setOnClickListener(this);
        imgPicCustom = findViewById(R.id.img_wd_pic_custom);
        cardPicCustom = findViewById(R.id.img_detail_pic_custom);
        layoutFolder = findViewById(R.id.layout_wd_folder);
        layoutFolder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_wd_continue:
                if (currentType == TYPE_GENERAL) {
                    onBackPressed();
                } else {
                    LearnWordActivity.needUpdate = true;
                    onBackPressed();
                }
                break;
            case R.id.layout_wd_delete:
                if (currentWord.getIsEasy() == 1) {
                    Log.d(TAG, "是简单词了");
                    Glide.with(this).load(R.drawable.icon_ash).into(imgDelete);
                    Word word = new Word();
                    word.setToDefault("isEasy");
                    word.updateAll("wordId = ?", wordId + "");
                    currentWord = LitePal.where("wordId = ?", wordId + "").find(Word.class).get(0);
                } else {
                    Log.d(TAG, "不是简单词了");
                    Glide.with(this).load(R.drawable.icon_ash_fill).into(imgDelete);
                    Word word = new Word();
                    word.setIsEasy(1);
                    word.updateAll("wordId = ?", wordId + "");
                    currentWord = LitePal.where("wordId = ?", wordId + "").find(Word.class).get(0);
                }
                if (currentType == TYPE_LEARN) {
                    WordController.removeOneWord(wordId);
                    ActivityCollector.startOtherActivity(WordDetailActivity.this, LearnWordActivity.class);
                }
                break;
            case R.id.layout_wd_voice:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaHelper.play(words.get(0).getWord());
                    }
                }).start();
                break;
            case R.id.layout_wd_pic:
                Intent intent = new Intent(WordDetailActivity.this, PicCustomActivity.class);
                intent.putExtra(PicCustomActivity.TYPE_WORD_ID, currentWord.getWordId());
                startActivity(intent);
                break;
            case R.id.layout_wd_star:
                if (currentWord.getIsCollected() == 1) {
                    Glide.with(this).load(R.drawable.icon_star).into(imgStar);
                    Word word = new Word();
                    word.setToDefault("isCollected");
                    word.updateAll("wordId = ?", wordId + "");
                    currentWord = LitePal.where("wordId = ?", wordId + "").find(Word.class).get(0);
                } else {
                    Glide.with(this).load(R.drawable.icon_star_fill).into(imgStar);
                    Word word = new Word();
                    word.setIsCollected(1);
                    word.updateAll("wordId = ?", wordId + "");
                    currentWord = LitePal.where("wordId = ?", wordId + "").find(Word.class).get(0);
                }
                break;
            case R.id.layout_wd_more:
                final AlertDialog.Builder builder = new AlertDialog.Builder(WordDetailActivity.this);
                builder.setTitle("请选择操作")
                        .setSingleChoiceItems(mores, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // 延迟500毫秒取消对话框
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        Intent intent = new Intent(WordDetailActivity.this, UpdateActivity.class);
                                        switch (which) {
                                            // 更改释义
                                            case 0:
                                                intent.putExtra(UpdateActivity.MODE_NAME, UpdateActivity.MODE_MEANS);
                                                intent.putExtra(UpdateActivity.WORD_ID_NAME, currentWord.getWordId());
                                                startActivity(intent);
                                                break;
                                            // 增加例句
                                            case 1:
                                                intent.putExtra(UpdateActivity.MODE_NAME, UpdateActivity.MODE_SENTENCES);
                                                intent.putExtra(UpdateActivity.WORD_ID_NAME, currentWord.getWordId());
                                                startActivity(intent);
                                                break;
                                            // 增加备注
                                            case 2:
                                                intent.putExtra(UpdateActivity.MODE_NAME, UpdateActivity.MODE_REMARKS);
                                                intent.putExtra(UpdateActivity.WORD_ID_NAME, currentWord.getWordId());
                                                startActivity(intent);
                                                break;
                                            // 自定义图片
                                            case 3:
                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(WordDetailActivity.this);
                                                builder2.setTitle("提示")
                                                        .setMessage("若当前单词已设置自定义图片，则该操作会覆盖原图片")
                                                        .setPositiveButton("继续上传", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //在这里跳转到手机系统相册里面
                                                                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                                                            }
                                                        })
                                                        .setNegativeButton("取消", null)
                                                        .show();
                                        }

                                    }
                                }, 200);
                            }
                        }).show();
                break;
            case R.id.layout_wd_phone_uk:
                MediaHelper.play(MediaHelper.ENGLISH_VOICE, words.get(0).getWord());
                break;
            case R.id.layout_wd_phone_usa:
                MediaHelper.play(MediaHelper.AMERICA_VOICE, words.get(0).getWord());
                break;
            case R.id.layout_wd_folder:
                final List<WordFolder> wordFolders = LitePal.findAll(WordFolder.class);
                if (wordFolders.isEmpty())
                    Toast.makeText(this, "暂无单词夹", Toast.LENGTH_SHORT).show();
                else {
                    String[] folderNames = new String[wordFolders.size()];
                    for (int i = 0; i < wordFolders.size(); ++i) {
                        folderNames[i] = wordFolders.get(i).getName();
                    }
                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(WordDetailActivity.this);
                    builder2.setTitle("存入单词夹")
                            .setSingleChoiceItems(folderNames, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    // 延迟500毫秒取消对话框
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            List<FolderLinkWord> folderLinkWords = LitePal.where("wordId = ? and folderId = ?", currentWord.getWordId() + "", wordFolders.get(which).getId() + "").find(FolderLinkWord.class);
                                            if (folderLinkWords.isEmpty()) {
                                                FolderLinkWord folderLinkWord = new FolderLinkWord();
                                                folderLinkWord.setFolderId(wordFolders.get(which).getId());
                                                folderLinkWord.setWordId(currentWord.getWordId());
                                                folderLinkWord.save();
                                                Toast.makeText(WordDetailActivity.this, "添加成功！", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(WordDetailActivity.this, "该单词已经在此单词夹中了哦", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, 200);
                                }
                            }).show();
                }
                break;
        }
    }

    private void setData() {
        words = LitePal.where("wordId = ?", wordId + "").find(Word.class);
        currentWord = words.get(0);
        // 设置收藏
        if (currentWord.getIsCollected() == 1)
            Glide.with(this).load(R.drawable.icon_star_fill).into(imgStar);
        else
            Glide.with(this).load(R.drawable.icon_star).into(imgStar);
        if (currentWord.getIsEasy() == 1)
            Glide.with(this).load(R.drawable.icon_ash_fill).into(imgDelete);
        else
            Glide.with(this).load(R.drawable.icon_ash).into(imgDelete);
        // 设置名称
        textWordName.setText(currentWord.getWord());
        // 设置英音
        if (currentWord.getUkPhone() != null) {
            layoutPhoneUk.setVisibility(View.VISIBLE);
            textPhoneUk.setText(currentWord.getUkPhone());
        } else {
            layoutPhoneUk.setVisibility(View.GONE);
        }
        // 设置美音
        if (currentWord.getUsPhone() != null) {
            layoutPhoneUs.setVisibility(View.VISIBLE);
            textPhoneUs.setText(currentWord.getUsPhone());
        } else {
            layoutPhoneUs.setVisibility(View.GONE);
        }
        // 设置中文/英文释义
        List<Interpretation> interpretationList = LitePal.where("wordId = ?", wordId + "").find(Interpretation.class);
        StringBuilder chinese = new StringBuilder();
        StringBuilder english = new StringBuilder();
        ArrayList<String> chsMeans = new ArrayList<>();
        ArrayList<String> enMeans = new ArrayList<>();
        for (int i = 0; i < interpretationList.size(); ++i) {
            chsMeans.add(interpretationList.get(i).getWordType() + ". " + interpretationList.get(i).getCHSMeaning());
            if (interpretationList.get(i).getENMeaning() != null) {
                enMeans.add(interpretationList.get(i).getWordType() + ". " + interpretationList.get(i).getENMeaning());
            }
        }
        for (int i = 0; i < chsMeans.size(); ++i) {
            if (i != chsMeans.size() - 1)
                chinese.append(chsMeans.get(i) + "\n");
            else
                chinese.append(chsMeans.get(i));
        }
        textInterpretation.setText(chinese.toString());
        if (enMeans.size() > 0) {
            cardEnglish.setVisibility(View.VISIBLE);
            for (int i = 0; i < enMeans.size(); ++i) {
                if (i != enMeans.size() - 1)
                    english.append(enMeans.get(i) + "\n");
                else
                    english.append(enMeans.get(i));
            }
            textEnglish.setText(english.toString());
        } else {
            cardEnglish.setVisibility(View.GONE);
        }
        // 设置巧记
        if (currentWord.getRemMethod() != null) {
            textRemMind.setText(currentWord.getRemMethod());
            cardRemMind.setVisibility(View.VISIBLE);
        } else {
            cardRemMind.setVisibility(View.GONE);
        }
        // 设置例句
        List<Sentence> sentenceList = LitePal.where("wordId = ?", wordId + "").find(Sentence.class);
        if (!sentenceList.isEmpty()) {
            cardSentence.setVisibility(View.VISIBLE);
            setSentenceData(sentenceList);
        } else {
            cardSentence.setVisibility(View.GONE);
        }
        // 设置词组
        List<Phrase> phraseList = LitePal.where("wordId = ?", wordId + "").find(Phrase.class);
        if (!phraseList.isEmpty()) {
            cardPhrase.setVisibility(View.VISIBLE);
            setPhraseData(phraseList);
        } else {
            cardPhrase.setVisibility(View.GONE);
        }

        // 设置图片记忆
        if (currentWord.getPicAddress() != null) {
            Glide.with(this).load(currentWord.getPicAddress()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgPic);
            Log.d(TAG, currentWord.getPicAddress());
            cardPic.setVisibility(View.VISIBLE);
        } else {
            cardPic.setVisibility(View.GONE);
        }

        if (currentWord.getPicCustom() != null) {
            cardPicCustom.setVisibility(View.VISIBLE);
        } else {
            cardPicCustom.setVisibility(View.GONE);
        }

        // 设置备注
        if (currentWord.getRemark() != null) {
            cardRemark.setVisibility(View.VISIBLE);
            textRemark.setText(currentWord.getRemark());
        } else {
            cardRemark.setVisibility(View.GONE);
        }
    }

    private void setSentenceData(List<Sentence> sentenceList) {
        itemSentenceList.clear();
        for (Sentence sentence : sentenceList) {
            itemSentenceList.add(new ItemSentence(sentence.getChsSentence(), sentence.getEnSentence()));
        }
        detailSentenceAdapter.notifyDataSetChanged();
    }

    private void setPhraseData(List<Phrase> phraseList) {
        itemPhraseList.clear();
        for (Phrase phrase : phraseList) {
            itemPhraseList.add(new ItemPhrase(phrase.getChsPhrase(), phrase.getEnPhrase()));
        }
        detailPhraseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ListActivity.isUpdate = true;
        LearnWordActivity.needUpdate = true;
        MediaHelper.releasePlayer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //在相册里面选择好相片之后调回到现在的这个activity中
        switch (requestCode) {
            case IMAGE_REQUEST_CODE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String path = cursor.getString(columnIndex);  //获取照片路径
                        cursor.close();
                        bitmap = BitmapFactory.decodeFile(path);
                        showProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                imgByte = FileUtil.bitmapCompress(bitmap, 1000);
                                Message message = new Message();
                                message.what = FINISH;
                                handler.sendMessage(message);
                            }
                        }).start();
                    } catch (Exception e) {
                        // TODO Auto-generatedcatch block
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private void showProgressDialog() {
        progressDialog = new ProgressDialog(WordDetailActivity.this);
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage("图片正在压缩中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

}
