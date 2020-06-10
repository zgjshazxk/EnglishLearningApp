package com.usts.englishlearning.activity.index;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.OCRActivity;
import com.usts.englishlearning.activity.load.LoadGameActivity;
import com.usts.englishlearning.activity.review.MatchActivity;
import com.usts.englishlearning.activity.review.SpeedActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemMatch;
import com.usts.englishlearning.listener.CallBackListener;
import com.usts.englishlearning.object.JsonBaidu;
import com.usts.englishlearning.object.JsonBaiduWords;
import com.usts.englishlearning.util.BaiduHelper;
import com.usts.englishlearning.util.Base64Util;
import com.usts.englishlearning.util.FileUtil;
import com.usts.englishlearning.util.MyApplication;
import com.usts.englishlearning.util.NumberController;
import com.usts.englishlearning.util.WordController;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class FragmentReview extends Fragment implements View.OnClickListener {

    private RelativeLayout layoutPhoto, layoutGame;

    private LinearLayout layoutSpeed, layoutMatch;

    private CircleImageView imgHead;

    private final int REQUEST_CODE_TAKE_PICTURE = 1000;

    private final int IMAGE_REQUEST_CODE = 2000;

    private static final String TAG = "FragmentReview";

    private ProgressDialog progressDialog;

    private final int FINISH = 1;
    private final int WRONG = 2;
    private final int LOAD_DONE = 3;
    private final int LOAD_SPEED = 4;

    final String items[] = {"拍照", "从手机相册选择"};

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    progressDialog.dismiss();
                    Intent intent = new Intent(getActivity(), OCRActivity.class);
                    startActivity(intent);
                    break;
                case WRONG:
                    progressDialog.dismiss();
                    Toast.makeText(MyApplication.getContext(), "发生了小错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_DONE:
                    progressDialog.dismiss();
                    Intent intent2 = new Intent();
                    intent2.setClass(MyApplication.getContext(), MatchActivity.class);
                    startActivity(intent2, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    break;
                case LOAD_SPEED:
                    progressDialog.dismiss();
                    Intent intent3 = new Intent();
                    intent3.setClass(MyApplication.getContext(), SpeedActivity.class);
                    startActivity(intent3, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();

        // 设置头像
        List<User> userList = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(User.class);
        Glide.with(MyApplication.getContext()).load(userList.get(0).getUserProfile()).into(imgHead);

    }

    private void init() {
        layoutPhoto = getActivity().findViewById(R.id.layout_re_photo);
        layoutPhoto.setOnClickListener(this);
        layoutSpeed = getActivity().findViewById(R.id.layout_re_speed);
        layoutSpeed.setOnClickListener(this);
        layoutMatch = getActivity().findViewById(R.id.layout_re_match);
        layoutMatch.setOnClickListener(this);
        layoutGame = getActivity().findViewById(R.id.layout_re_game);
        layoutGame.setOnClickListener(this);
        imgHead = getActivity().findViewById(R.id.img_me_head);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (v.getId()) {
            case R.id.layout_re_speed:
                showProgressDialog("数据准备中...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        prepareData(ConfigData.getSpeedNum());
                        Message message = new Message();
                        message.what = LOAD_SPEED;
                        handler.sendMessage(message);
                    }
                }).start();
                break;
            case R.id.layout_re_match:
                showProgressDialog("数据准备中...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadMatchData();
                        Message message = new Message();
                        message.what = LOAD_DONE;
                        handler.sendMessage(message);
                    }
                }).start();
                break;
            case R.id.layout_re_game:
                intent.setClass(MyApplication.getContext(), LoadGameActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                break;
            case R.id.layout_re_photo:
                // 通过builder 构建器来构造
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("请选择方式")
                        .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                final int num = which;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (num) {
                                            case 0:
                                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
                                                break;
                                            case 1:
                                                Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(intent2, IMAGE_REQUEST_CODE);
                                                break;
                                        }
                                        dialog.dismiss();
                                    }
                                }, 200);

                            }
                        })
                        .show();
                break;
        }

    }

    private void showProgressDialog(String content) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage(content);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Intent intent = data;
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PICTURE:
                    // 解析返回的图片成bitmap
                    if (data.getExtras() != null) {
                        showProgressDialog("正在识别分析中");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                                BaiduHelper.analysePicture(Base64Util.encode(FileUtil.bitmapCompress(bmp, 3999)), new CallBackListener() {

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Message message = new Message();
                                        message.what = WRONG;
                                        handler.sendMessage(message);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String s = response.body().string();
                                        Log.d(TAG, s);
                                        Gson gson = new Gson();
                                        try {
                                            analyseJson(s, gson);
                                            Message message = new Message();
                                            message.what = FINISH;
                                            handler.sendMessage(message);
                                        } catch (Exception e) {
                                            Message message = new Message();
                                            message.what = WRONG;
                                            handler.sendMessage(message);
                                        }
                                    }
                                });

                            }
                        }).start();
                    }
                    break;
                case IMAGE_REQUEST_CODE:
                    showProgressDialog("正在识别分析中");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Uri selectedImage = intent.getData(); //获取系统返回的照片的Uri
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            Cursor cursor = MyApplication.getContext().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String path = cursor.getString(columnIndex);  //获取照片路径
                            cursor.close();
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            BaiduHelper.analysePicture(Base64Util.encode(FileUtil.bitmapCompress(bitmap, 2000)), new CallBackListener() {

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Message message = new Message();
                                    message.what = WRONG;
                                    handler.sendMessage(message);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String s = response.body().string();
                                    Log.d(TAG, s);
                                    Gson gson = new Gson();
                                    try {
                                        analyseJson(s, gson);
                                        Message message = new Message();
                                        message.what = FINISH;
                                        handler.sendMessage(message);
                                    } catch (Exception e) {
                                        Message message = new Message();
                                        message.what = WRONG;
                                        handler.sendMessage(message);
                                    }
                                }

                            });

                        }
                    }).start();
                    break;
            }
        } else {
            // 取消了
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void analyseJson(String s, Gson gson) {
        JsonBaidu jsonBaidu = gson.fromJson(s.toLowerCase(), JsonBaidu.class);
        if (jsonBaidu != null) {
            List<JsonBaiduWords> jsonBaiduWordsResult = jsonBaidu.getWords_result();
            StringBuilder stringBuilder = new StringBuilder();
            for (JsonBaiduWords jsonBaiduWords : jsonBaiduWordsResult) {
                stringBuilder.append(jsonBaiduWords.getWords() + " ");
            }
            Log.d(TAG, stringBuilder.toString());
            String[] result = stringBuilder.toString().split(" ");
            Log.d(TAG, Arrays.toString(result));
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
            }
        }
    }

    private void loadMatchData() {
        MatchActivity.allMatches.clear();
        if (!MatchActivity.wordList.isEmpty())
            MatchActivity.wordList.clear();
        if (!MatchActivity.matchList.isEmpty())
            MatchActivity.matchList.clear();
        List<Word> words = LitePal.select("wordId", "word").find(Word.class);
        int[] randomId = NumberController.getRandomNumberList(0, words.size() - 1, ConfigData.getMatchNum());
        for (int i = 0; i < randomId.length; ++i) {
            MatchActivity.matchList.add(new ItemMatch(randomId[i], words.get(randomId[i]).getWord(), false, false));
            MatchActivity.allMatches.add(new ItemMatch(words.get(randomId[i]).getWordId(), words.get(randomId[i]).getWord(), false, false));
            Log.d(TAG, "单词：" + words.get(randomId[i]).getWord());
            List<Interpretation> interpretations = LitePal.where("wordId = ?", words.get(randomId[i]).getWordId() + "").find(Interpretation.class);
            Log.d(TAG, "size: " + interpretations.size());
            StringBuilder stringBuilder = new StringBuilder();
            for (int ii = 0; ii < interpretations.size(); ++ii) {
                if (ii != (interpretations.size() - 1))
                    stringBuilder.append(interpretations.get(ii).getWordType() + ". " + interpretations.get(ii).getCHSMeaning() + "\n");
                else
                    stringBuilder.append(interpretations.get(ii).getWordType() + ". " + interpretations.get(ii).getCHSMeaning());
            }
            Log.d(TAG, "意思: " + stringBuilder.toString());
            MatchActivity.matchList.add(new ItemMatch(randomId[i], stringBuilder.toString(), false, false));
        }
        Collections.shuffle(MatchActivity.matchList);

    }

    private void prepareData(int num) {
        if (!SpeedActivity.wordList.isEmpty())
            SpeedActivity.wordList.clear();
        // 准备单词数据
        List<Word> words = LitePal.select("wordId", "word").find(Word.class);
        // 随机匹配单词ID
        int[] randomId = NumberController.getRandomNumberList(0, words.size() - 1, num);
        for (int i = 0; i < num; ++i) {
            // 添加数据
            SpeedActivity.wordList.add(words.get(randomId[i]));
        }
    }

}
