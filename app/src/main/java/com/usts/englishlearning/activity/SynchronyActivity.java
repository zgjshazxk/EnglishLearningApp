package com.usts.englishlearning.activity;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.config.ServerData;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.LearnTime;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.Phrase;
import com.usts.englishlearning.database.Sentence;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.util.FileUtil;
import com.usts.englishlearning.util.TimeController;
import com.usts.englishlearning.util.ZipUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SynchronyActivity extends BaseActivity {

    private TextView textSyn, textRecover;

    private ProgressDialog progressDialog;

    private ImageView imgMore;

    private String fileTypeName, filePath, zipPath;

    private final int START_SYN_CLOUD = 0;
    private final int FINISH_SYN_CLOUD = 1;
    private final int FINISH_SYN_LOCAL = 2;
    private final int START_RECOVER_CLOUD = 3;
    private final int FAIL_RECOVER_CLOUD = 4;
    private final int FINISH_RECOVER_LOCAL = 5;

    private static final String TAG = "SynchronyActivity";

    public static String TYPE_NAME = "TYPENAME";

    public boolean canSave;

    private String currentBookName;

    private String downLoadPath;

    private String[] currentBookLists;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case FAIL_RECOVER_CLOUD:
                    progressDialog.dismiss();
                    Toast.makeText(SynchronyActivity.this, "云端暂无数据，请备份再重试", Toast.LENGTH_SHORT).show();
                    break;
                case FINISH_SYN_LOCAL:
                    progressDialog.dismiss();
                    Toast.makeText(SynchronyActivity.this, "备份完毕", Toast.LENGTH_SHORT).show();
                    break;
                case FINISH_RECOVER_LOCAL:
                    progressDialog.dismiss();
                    Toast.makeText(SynchronyActivity.this, "恢复成功", Toast.LENGTH_SHORT).show();
                    break;
                case FINISH_SYN_CLOUD:
                    progressDialog.dismiss();
                    Toast.makeText(SynchronyActivity.this, "云端备份完毕", Toast.LENGTH_SHORT).show();
                    break;
                case START_RECOVER_CLOUD:
                    try {
                        ZipUtil.unzip(getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/cloudFiles/" + ConfigData.getSinaNumLogged() + ".zip",
                                getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/cloudFiles/" + ConfigData.getSinaNumLogged());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentBookLists = FileUtil.allFilesInOne(downLoadPath + "/books");
                    if (currentBookLists != null) {
                        if (currentBookLists.length != 0) {
                            progressDialog.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(SynchronyActivity.this);
                            builder.setTitle("请选择以下书单进行恢复")
                                    .setItems(currentBookLists, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String currentName = currentBookLists[which];
                                            dialog.dismiss();
                                            AlertDialog.Builder timeBuilder = new AlertDialog.Builder(SynchronyActivity.this);
                                            timeBuilder.setTitle("提示")
                                                    .setMessage("书单名称：" + currentBookLists[which] + "\n备份时间：" + FileUtil.readFileToString(downLoadPath + "/books/" + currentBookLists[which], "更新时间" + fileTypeName))
                                                    .setPositiveButton("确定恢复", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            showProgressDialog("数据已准备完毕，正在恢复中...");
                                                            //showProgressDialog("数据恢复中，请勿强行关闭软件");
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Log.d(TAG, currentName);
                                                                    downLoadPath = getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/cloudFiles/" + ConfigData.getSinaNumLogged();
                                                                    recoverFromLocal(downLoadPath, currentName);
                                                                    RequestBody formBody = new FormBody.Builder()
                                                                            .add(ServerData.TYPE_NAME, ServerData.RECOVER_TYPE)
                                                                            .add(ServerData.LOGIN_SINA_NUM, ConfigData.getSinaNumLogged() + "")
                                                                            .build();
                                                                    Request request = new Request.Builder()
                                                                            .post(formBody)
                                                                            .url(ServerData.SERVER_UPLOAD_RECORD_ADDRESS)
                                                                            .build();

                                                                    OkHttpClient okHttpClient = new OkHttpClient();
                                                                    okHttpClient.newCall(request)
                                                                            .enqueue(new okhttp3.Callback() {
                                                                                @Override
                                                                                public void onFailure(Call call, IOException e) {

                                                                                }

                                                                                @Override
                                                                                public void onResponse(Call call, Response response) throws IOException {
                                                                                }
                                                                            });
                                                                }
                                                            }).start();
                                                        }
                                                    })
                                                    .setNegativeButton("取消", null)
                                                    .show();
                                        }
                                    })
                                    .show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SynchronyActivity.this, "云端暂无数据，请备份再重试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case START_SYN_CLOUD:
                    progressDialog.setMessage("准备完毕，正在上传...");
                    uploadInfoFiles(ConfigData.getSinaNumLogged() + "");
                    RequestBody formBody = new FormBody.Builder()
                            .add(ServerData.TYPE_NAME, ServerData.UPLOAD_TYPE)
                            .add(ServerData.LOGIN_SINA_NUM, ConfigData.getSinaNumLogged() + "")
                            .build();
                    Request request = new Request.Builder()
                            .post(formBody)
                            .url(ServerData.SERVER_UPLOAD_RECORD_ADDRESS)
                            .build();

                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request)
                            .enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Message message = new Message();
                                    message.what = FINISH_SYN_CLOUD;
                                    handler.sendMessage(message);
                                }
                            });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchro);

        canSave = getIntent().getBooleanExtra(TYPE_NAME, true);

        downLoadPath = getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/cloudFiles/" + ConfigData.getSinaNumLogged();

        List<UserConfig> currentConfig = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        currentBookName = ConstantData.bookNameById(currentConfig.get(0).getCurrentBookId());

        fileTypeName = ".database";
        filePath = getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/" + ConfigData.getSinaNumLogged();
        zipPath = getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/zip";

        init();

        textSyn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("数据正在准备中...");
                if (canSave) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveToLocal(true);
                        }
                    }).start();
                } else {
                    Toast.makeText(SynchronyActivity.this, "抱歉，此时无法进行备份操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("数据准备中...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody formBody = new FormBody.Builder()
                                .add(ServerData.LOGIN_SINA_NUM, ConfigData.getSinaNumLogged() + "")
                                .build();
                        Request request = new Request.Builder()
                                .post(formBody)
                                .url(ServerData.SERVER_RETURN_BOOKS_ADDRESS)
                                .build();
                        OkHttpClient okHttpClient = new OkHttpClient();
                        okHttpClient.newCall(request)
                                .enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Message message = new Message();
                                        message.what = FAIL_RECOVER_CLOUD;
                                        handler.sendMessage(message);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String responseContent = response.body().string();
                                        Log.d(TAG, "onResponse: " + responseContent);
                                        if (responseContent.contains("no_exist") || responseContent.contains("not found")) {
                                            // Toast.makeText(SynchronyActivity.this, "服务器中无备份数据", Toast.LENGTH_SHORT).show();
                                            Message message = new Message();
                                            message.what = FAIL_RECOVER_CLOUD;
                                            handler.sendMessage(message);
                                        } else {
                                            Request request = new Request.Builder()
                                                    .url(ServerData.SERVER_ADDRESS + "/upload/" + ConfigData.getSinaNumLogged() + "/" + ConfigData.getSinaNumLogged() + ".zip")
                                                    .build();
                                            OkHttpClient okHttpClient = new OkHttpClient();
                                            okHttpClient.newCall(request)
                                                    .enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {

                                                        }

                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            FileUtil.getFileByBytes(response.body().bytes(), getFilesDir() + "/" + ConstantData.DIR_TOTAL + "/cloudFiles", ConfigData.getSinaNumLogged() + ".zip");
                                                            Message message = new Message();
                                                            message.what = START_RECOVER_CLOUD;
                                                            handler.sendMessage(message);
                                                        }
                                                    });
                                            // 下载完毕
                                            // 恢复
                                        }
                                    }
                                });
                    }
                }).start();
            }
        });

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] choices = {"本地备份", "本地恢复"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(SynchronyActivity.this);
                builder.setTitle("请选择操作")
                        .setItems(choices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    dialog.dismiss();
                                    if (canSave) {
                                        showProgressDialog("数据正在备份中...");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                saveToLocal(false);
                                            }
                                        }).start();
                                    } else {
                                        Toast.makeText(SynchronyActivity.this, "抱歉，此时无法进行备份操作", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    dialog.dismiss();
                                    final String[] bookLists = FileUtil.allFilesInOne(filePath + "/books");
                                    if (bookLists != null) {
                                        if (bookLists.length != 0) {
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(SynchronyActivity.this);
                                            builder.setTitle("请选择以下书单进行恢复")
                                                    .setItems(bookLists, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            final String currentName = bookLists[which];
                                                            dialog.dismiss();
                                                            AlertDialog.Builder timeBuilder = new AlertDialog.Builder(SynchronyActivity.this);
                                                            timeBuilder.setTitle("提示")
                                                                    .setMessage("书单名称：" + bookLists[which] + "\n备份时间：" + FileUtil.readFileToString(filePath + "/books/" + bookLists[which], "更新时间" + fileTypeName))
                                                                    .setPositiveButton("确定恢复", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                            showProgressDialog("数据恢复中，请勿强行关闭软件");
                                                                            new Thread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    Log.d(TAG, currentName);
                                                                                    recoverFromLocal(filePath, currentName);
                                                                                }
                                                                            }).start();
                                                                        }
                                                                    })
                                                                    .setNegativeButton("取消", null)
                                                                    .show();
                                                        }
                                                    })
                                                    .show();
                                        } else {
                                            Toast.makeText(SynchronyActivity.this, "抱歉，暂无文件", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(SynchronyActivity.this, "本地无备份文件，请先进行备份再恢复", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .show();
            }
        });

    }

    private void init() {
        textSyn = findViewById(R.id.text_syno);
        textRecover = findViewById(R.id.text_recover);
        imgMore = findViewById(R.id.img_local_more);
    }

    private void showProgressDialog(String content) {
        progressDialog = new ProgressDialog(SynchronyActivity.this);
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage(content);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void saveToLocal(boolean isCloud) {
        // 备份释义
        List<Interpretation> interpretations = LitePal.findAll(Interpretation.class);
        JSONArray interpretationArray = new JSONArray();
        try {
            for (Interpretation interpretation : interpretations) {
                JSONObject interpretationJson = new JSONObject();
                interpretationJson.put("wordType", interpretation.getWordType());
                interpretationJson.put("CHSMeaning", interpretation.getCHSMeaning());
                interpretationJson.put("ENMeaning", interpretation.getENMeaning());
                interpretationJson.put("wordId", interpretation.getWordId());
                interpretationArray.put(interpretationJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileUtil.saveStringToFile(interpretationArray.toString(), filePath + "/books/" + currentBookName, "Interpretation" + fileTypeName);
        // 备份单词
        List<Word> words = LitePal.findAll(Word.class);
        JSONArray wordArray = new JSONArray();
        try {
            for (Word word : words) {
                JSONObject wordJson = new JSONObject();
                wordJson.put("wordId", word.getWordId());
                wordJson.put("word", word.getWord());
                wordJson.put("ukPhone", word.getUkPhone());
                wordJson.put("usPhone", word.getUsPhone());
                wordJson.put("remMethod", word.getRemMethod());
                wordJson.put("picAddress", word.getPicAddress());
                wordJson.put("picCustom", word.getPicCustom());
                wordJson.put("remark", word.getRemark());
                wordJson.put("belongBook", word.getBelongBook());
                wordJson.put("isCollected", word.getIsCollected());
                wordJson.put("isEasy", word.getIsEasy());
                wordJson.put("justLearned", word.getJustLearned());
                wordJson.put("isNeedLearned", word.getIsNeedLearned());
                wordJson.put("needLearnDate", word.getNeedLearnDate());
                wordJson.put("needReviewDate", word.getNeedReviewDate());
                wordJson.put("isLearned", word.getIsLearned());
                wordJson.put("examNum", word.getExamNum());
                wordJson.put("examRightNum", word.getExamRightNum());
                wordJson.put("lastMasterTime", word.getLastMasterTime());
                wordJson.put("lastReviewTime", word.getLastReviewTime());
                wordJson.put("masterDegree", word.getMasterDegree());
                wordJson.put("deepMasterTimes", word.getDeepMasterTimes());
                wordArray.put(wordJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(wordArray.toString(), filePath + "/books/" + currentBookName, "Word" + fileTypeName);
        // 备份短语
        List<Phrase> phrases = LitePal.findAll(Phrase.class);
        JSONArray phraseArray = new JSONArray();
        try {
            for (Phrase phrase : phrases) {
                JSONObject phraseJson = new JSONObject();
                phraseJson.put("wordId", phrase.getWordId());
                phraseJson.put("chsPhrase", phrase.getChsPhrase());
                phraseJson.put("enPhrase", phrase.getEnPhrase());
                phraseArray.put(phraseJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(phraseArray.toString(), filePath + "/books/" + currentBookName, "Phrase" + fileTypeName);
        // 备份例句
        List<Sentence> sentences = LitePal.findAll(Sentence.class);
        JSONArray sentenceArray = new JSONArray();
        try {
            for (Sentence sentence : sentences) {
                JSONObject sentenceJson = new JSONObject();
                sentenceJson.put("wordId", sentence.getWordId());
                sentenceJson.put("chsSentence", sentence.getChsSentence());
                sentenceJson.put("enSentence", sentence.getEnSentence());
                sentenceArray.put(sentenceJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(sentenceArray.toString(), filePath + "/books/" + currentBookName, "Sentence" + fileTypeName);
        // 备份单词夹
        List<WordFolder> wordFolders = LitePal.findAll(WordFolder.class);
        JSONArray folderArray = new JSONArray();
        try {
            for (WordFolder wordFolder : wordFolders) {
                JSONObject folderJson = new JSONObject();
                folderJson.put("id", wordFolder.getId());
                folderJson.put("createTime", wordFolder.getCreateTime());
                folderJson.put("name", wordFolder.getName());
                folderJson.put("remark", wordFolder.getRemark());
                folderArray.put(folderJson);
            }
        } catch (Exception e) {

        }
        List<FolderLinkWord> folderLinkWords = LitePal.findAll(FolderLinkWord.class);
        JSONArray folderLinksArray = new JSONArray();
        try {
            for (FolderLinkWord folderLinkWord : folderLinkWords) {
                JSONObject folderLinkJson = new JSONObject();
                folderLinkJson.put("wordId", folderLinkWord.getWordId());
                folderLinkJson.put("folderId", folderLinkWord.getFolderId());
                folderLinksArray.put(folderLinkJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(folderLinksArray.toString(), filePath + "/books/" + currentBookName, "FolderLinkWord" + fileTypeName);
        FileUtil.saveStringToFile(folderArray.toString(), filePath + "/books/" + currentBookName, "WordFolder" + fileTypeName);
        // 备份用户数据
        List<User> users = LitePal.findAll(User.class);
        JSONArray userArray = new JSONArray();
        try {
            for (User user : users) {
                JSONObject userJson = new JSONObject();
                userJson.put("userId", user.getUserId());
                userJson.put("userProfile", user.getUserProfile());
                userJson.put("userName", user.getUserName());
                userJson.put("userWordNumber", user.getUserWordNumber());
                userJson.put("userMoney", user.getUserMoney());
                userArray.put(userJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(userArray.toString(), filePath, "User" + fileTypeName);
        // 备份用户配置数据
        List<UserConfig> userConfigs = LitePal.findAll(UserConfig.class);
        JSONArray configArray = new JSONArray();
        try {
            for (UserConfig userConfig : userConfigs) {
                JSONObject configJson = new JSONObject();
                configJson.put("id", userConfig.getId());
                configJson.put("currentBookId", userConfig.getCurrentBookId());
                Log.d(TAG, "currentBookId" + userConfig.getCurrentBookId());
                configJson.put("wordNeedReciteNum", userConfig.getWordNeedReciteNum());
                configJson.put("userId", userConfig.getUserId());
                configJson.put("lastStartTime", userConfig.getLastStartTime());
                configArray.put(configJson);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(configArray.toString(), filePath + "/books/" + currentBookName, "UserConfig" + fileTypeName);
        // 备份时间相关数据
        List<MyDate> myDates = LitePal.findAll(MyDate.class);
        JSONArray dateArray = new JSONArray();
        try {
            for (MyDate myDate : myDates) {
                JSONObject dateObject = new JSONObject();
                dateObject.put("id", myDate.getId());
                dateObject.put("year", myDate.getYear());
                dateObject.put("month", myDate.getMonth());
                dateObject.put("date", myDate.getDate());
                dateObject.put("wordLearnNumber", myDate.getWordLearnNumber());
                dateObject.put("wordReviewNumber", myDate.getWordReviewNumber());
                dateObject.put("remark", myDate.getRemark());
                dateObject.put("userId", myDate.getUserId());
                dateArray.put(dateObject);
            }
        } catch (Exception e) {

        }
        FileUtil.saveStringToFile(dateArray.toString(), filePath, "MyDate" + fileTypeName);
        List<LearnTime> learnTimes = LitePal.findAll(LearnTime.class);
        JSONArray timeArray = new JSONArray();
        try {
            for (LearnTime learnTime : learnTimes) {
                JSONObject timeObject = new JSONObject();
                timeObject.put("date", learnTime.getDate());
                timeObject.put("time", learnTime.getTime());
                timeArray.put(timeObject);
            }
        } catch (Exception e) {

        }
        Log.d(TAG, timeArray.toString());
        FileUtil.saveStringToFile(timeArray.toString(), filePath, "LearnTime" + fileTypeName);
        // 最后写入配置信息
        FileUtil.saveStringToFile(TimeController.getStringDateDetail(TimeController.getCurrentTimeStamp()), filePath + "/books/" + currentBookName, "更新时间" + fileTypeName);
        if (isCloud)
            // 压缩文件夹
            try {
                ZipUtil.zip(filePath, zipPath + "/" + ConfigData.getSinaNumLogged() + ".zip");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage().trim());
            }
        Message message = new Message();
        if (!isCloud)
            message.what = FINISH_SYN_LOCAL;
        else
            message.what = START_SYN_CLOUD;
        handler.sendMessage(message);
    }

    private void recoverFromLocal(String currentPath, String currentName) {
        // 清空本地数据
        LitePal.deleteAll(FolderLinkWord.class);
        LitePal.deleteAll(Interpretation.class);
        LitePal.deleteAll(LearnTime.class);
        LitePal.deleteAll(MyDate.class);
        LitePal.deleteAll(Phrase.class);
        LitePal.deleteAll(Sentence.class);
        LitePal.deleteAll(User.class);
        LitePal.deleteAll(UserConfig.class);
        LitePal.deleteAll(Word.class);
        LitePal.deleteAll(WordFolder.class);
        Log.d(TAG, "删除完了");
        // 恢复用户数据
        Gson gson = new Gson();
        // 恢复学习数据
        String learnTimeContent = FileUtil.readFileToString(currentPath, "LearnTime" + fileTypeName);
        Log.d(TAG, learnTimeContent);
        List<LearnTime> learnTimes = gson.fromJson(learnTimeContent, new TypeToken<List<LearnTime>>() {
        }.getType());
        if (learnTimes != null)
            for (LearnTime learnTime : learnTimes) {
                LearnTime newLearnTime = new LearnTime();
                newLearnTime.setTime(learnTime.getTime());
                newLearnTime.setDate(learnTime.getDate());
                newLearnTime.save();
            }
        // 恢复打卡日历
        String myDateContent = FileUtil.readFileToString(currentPath, "MyDate" + fileTypeName);
        Log.d(TAG, myDateContent);
        List<MyDate> myDates = gson.fromJson(myDateContent, new TypeToken<List<MyDate>>() {
        }.getType());
        if (myDates != null)
            for (MyDate myDate : myDates) {
                MyDate newDate = new MyDate();
                newDate.setMonth(myDate.getMonth());
                newDate.setYear(myDate.getYear());
                newDate.setUserId(myDate.getUserId());
                newDate.setDate(myDate.getDate());
                newDate.setRemark(myDate.getRemark());
                newDate.setWordReviewNumber(myDate.getWordReviewNumber());
                newDate.setWordLearnNumber(myDate.getWordLearnNumber());
                newDate.setId(myDate.getId());
                newDate.save();
            }
        // 恢复用户信息
        String userContent = FileUtil.readFileToString(currentPath, "User" + fileTypeName);
        List<User> users = gson.fromJson(userContent, new TypeToken<List<User>>() {
        }.getType());
        Log.d(TAG, userContent);
        if (users != null)
            for (User user : users) {
                User newUser = new User();
                newUser.setUserMoney(user.getUserMoney());
                newUser.setUserWordNumber(user.getUserWordNumber());
                newUser.setUserId(user.getUserId());
                newUser.setUserName(user.getUserName());
                newUser.setUserProfile(user.getUserProfile());
                newUser.save();
            }
        // 恢复书本信息
        String detailPath = currentPath + "/books/" + currentName;
        Log.d(TAG, "书目路径：" + detailPath);
        // 恢复用户配置信息
        String configContent = FileUtil.readFileToString(detailPath, "UserConfig" + fileTypeName);
        List<UserConfig> userConfigs = gson.fromJson(configContent, new TypeToken<List<UserConfig>>() {
        }.getType());
        Log.d(TAG, configContent);
        if (userConfigs != null)
            for (UserConfig userConfig : userConfigs) {
                UserConfig newConfig = new UserConfig();
                newConfig.setCurrentBookId(userConfig.getCurrentBookId());
                newConfig.setLastStartTime(userConfig.getLastStartTime());
                newConfig.setWordNeedReciteNum(userConfig.getWordNeedReciteNum());
                newConfig.setUserId(userConfig.getUserId());
                newConfig.setId(userConfig.getId());
                newConfig.save();
            }
        // 恢复单词夹
        String folderLinkContent = FileUtil.readFileToString(detailPath, "FolderLinkWord" + fileTypeName);
        List<FolderLinkWord> folderLinkWords = gson.fromJson(folderLinkContent, new TypeToken<List<FolderLinkWord>>() {
        }.getType());
        if (folderLinkWords != null) {
            for (FolderLinkWord folderLinkWord : folderLinkWords) {
                FolderLinkWord newLinks = new FolderLinkWord();
                newLinks.setWordId(folderLinkWord.getWordId());
                newLinks.setFolderId(folderLinkWord.getFolderId());
                newLinks.save();
            }
        }
        String folderContent = FileUtil.readFileToString(detailPath, "WordFolder" + fileTypeName);
        List<WordFolder> wordFolders = gson.fromJson(folderContent, new TypeToken<List<WordFolder>>() {
        }.getType());
        if (wordFolders != null) {
            for (WordFolder wordFolder : wordFolders) {
                WordFolder newFolder = new WordFolder();
                newFolder.setId(wordFolder.getId());
                newFolder.setRemark(wordFolder.getRemark());
                newFolder.setName(wordFolder.getName());
                newFolder.setCreateTime(wordFolder.getCreateTime());
                newFolder.save();
            }
        }
        Log.d(TAG, "run: ");
        // 恢复单词
        String wordContent = "";
        try {
            wordContent = FileUtil.readFileToString(detailPath, "Word" + fileTypeName);
        } catch (Exception e) {
            Log.d(TAG, "错误：" + e.getMessage());
        }
        Log.d(TAG, wordContent);
        List<Word> words = gson.fromJson(wordContent, new TypeToken<List<Word>>() {
        }.getType());
        if (words != null) {
            Log.d(TAG, "不是空的");
            for (Word word : words) {
                Word newWord = new Word();
                newWord.setPicCustom(word.getPicCustom());
                newWord.setRemark(word.getRemark());
                newWord.setIsCollected(word.getIsCollected());
                newWord.setIsEasy(word.getIsEasy());
                newWord.setWord(word.getWord());
                newWord.setUkPhone(word.getUkPhone());
                newWord.setLastReviewTime(word.getLastReviewTime());
                newWord.setIsNeedLearned(word.getIsNeedLearned());
                newWord.setExamNum(word.getExamNum());
                newWord.setLastMasterTime(word.getLastMasterTime());
                newWord.setDeepMasterTimes(word.getDeepMasterTimes());
                newWord.setMasterDegree(word.getMasterDegree());
                newWord.setExamRightNum(word.getExamRightNum());
                newWord.setIsLearned(word.getIsLearned());
                newWord.setJustLearned(word.getJustLearned());
                newWord.setNeedLearnDate(word.getNeedLearnDate());
                newWord.setBelongBook(word.getBelongBook());
                newWord.setNeedReviewDate(word.getNeedReviewDate());
                newWord.setPicAddress(word.getPicAddress());
                newWord.setRemMethod(word.getRemMethod());
                newWord.setUsPhone(word.getUsPhone());
                newWord.setWordId(word.getWordId());
                newWord.save();
            }
        }
        // 恢复句子
        String senContent = FileUtil.readFileToString(detailPath, "Sentence" + fileTypeName);
        List<Sentence> sentences = gson.fromJson(senContent, new TypeToken<List<Sentence>>() {
        }.getType());
        if (sentences != null) {
            for (Sentence sentence : sentences) {
                Sentence newSen = new Sentence();
                newSen.setEnSentence(sentence.getEnSentence());
                newSen.setChsSentence(sentence.getChsSentence());
                newSen.setWordId(sentence.getWordId());
                newSen.save();
            }
        }
        // 恢复短语
        String phraseContent = FileUtil.readFileToString(detailPath, "Phrase" + fileTypeName);
        List<Phrase> phrases = gson.fromJson(phraseContent, new TypeToken<List<Phrase>>() {
        }.getType());
        if (phrases != null)
            for (Phrase phrase : phrases) {
                Phrase newPhrase = new Phrase();
                newPhrase.setWordId(phrase.getWordId());
                newPhrase.setChsPhrase(phrase.getEnPhrase());
                newPhrase.setEnPhrase(phrase.getEnPhrase());
                newPhrase.save();
            }
        // 恢复释义
        String meansContent = FileUtil.readFileToString(detailPath, "Interpretation" + fileTypeName);
        List<Interpretation> interpretations = gson.fromJson(meansContent, new TypeToken<List<Interpretation>>() {
        }.getType());
        if (interpretations != null)
            for (Interpretation interpretation : interpretations) {
                Interpretation mean = new Interpretation();
                mean.setWordId(interpretation.getWordId());
                mean.setWordType(interpretation.getWordType());
                mean.setENMeaning(interpretation.getENMeaning());
                mean.setCHSMeaning(interpretation.getCHSMeaning());
                mean.save();
            }
        Message message = new Message();
        message.what = FINISH_RECOVER_LOCAL;
        handler.sendMessage(message);
    }

    private void uploadInfoFiles(String fileName) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put(ServerData.UPLOAD_FILE, new File(zipPath + "/" + fileName + ".zip"));
            params.put(ServerData.LOGIN_SINA_NUM, ConfigData.getSinaNumLogged());
        } catch (Exception e) {
            Log.d(TAG, "wrong");
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        httpClient.post(ServerData.SERVER_UPLOAD_INFO_ADDRESS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "onSuccess: ");
                try {
                    Log.d(TAG, new String(responseBody, "gbk"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

}
