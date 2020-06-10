package com.usts.englishlearning.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.UpdateSentenceAdapter;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Sentence;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemUpdateSen;
import com.usts.englishlearning.entity.UpdateMeans;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateActivity extends BaseActivity {

    private String currentMode;

    private int currentWordId;

    public static final String MODE_NAME = "updateMode";
    public static final String WORD_ID_NAME = "updateWordId";

    public static final String MODE_MEANS = "meansMode";

    public static final String MODE_REMARKS = "remarksMode";

    public static final String MODE_SENTENCES = "sentencesMode";

    private Spinner spinnerMeansType;

    private int currentMeansType = 0;

    private String[] meansTypes = {
            "名词 [n]",
            "代词 [pron]",
            "形容词 [adj]",
            "副词 [adv]",
            "动词 [v]",
            "及物动词 [vt]",
            "不及物动词 [vi]",
            "数词 [num]",
            "冠词 [art]",
            "介词 [prep]",
            "连词 [conj]",
            "感叹词 [interj]"};

    private String[] types = {
            "n",
            "pron",
            "adj",
            "adv",
            "v",
            "vt",
            "vi",
            "num",
            "art",
            "prep",
            "conj",
            "interj"};

    private EditText editMeansChs, editMeansEn;

    private Map<Integer, UpdateMeans> meansMap = new HashMap<>();

    private CardView cardMeans;

    private CardView cardRemark;

    private EditText editRemark;

    private TextView textDone;

    private LinearLayout layoutTip;

    private RecyclerView recyclerSentences;

    private UpdateSentenceAdapter updateSentenceAdapter;

    private List<ItemUpdateSen> updateSenList = new ArrayList<>();

    private FloatingActionButton btnAdd;

    private static final String TAG = "UpdateActivity";

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        currentMode = getIntent().getStringExtra(MODE_NAME);
        currentWordId = getIntent().getIntExtra(WORD_ID_NAME, 0);

        init();

        switch (currentMode) {
            case MODE_MEANS:
                cardRemark.setVisibility(View.GONE);
                cardMeans.setVisibility(View.VISIBLE);
                btnAdd.setVisibility(View.GONE);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, meansTypes);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMeansType.setAdapter(arrayAdapter);
                // 初始化
                setMeansData();
                setMeansEdit(0);
                // 及时保存数据
                editMeansChs.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        Log.d(TAG, "beforeTextChanged: " + s.toString());
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        UpdateMeans updateMeans = meansMap.get(currentMeansType);
                        if (!TextUtils.isEmpty(s.toString().trim()))
                            updateMeans.setChsMeans(s.toString().trim());
                        else
                            updateMeans.setChsMeans("@null");
                        meansMap.put(currentMeansType, updateMeans);
                        Log.d(TAG, "onTextChanged: " + s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d(TAG, "afterTextChanged: " + s.toString());
                        Log.d(TAG, "currentType" + meansTypes[currentMeansType]);
                    }
                });
                editMeansEn.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        UpdateMeans updateMeans = meansMap.get(currentMeansType);
                        if (!TextUtils.isEmpty(s.toString().trim()))
                            updateMeans.setEnMeans(s.toString().trim());
                        else
                            updateMeans.setEnMeans("@null");
                        meansMap.put(currentMeansType, updateMeans);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                spinnerMeansType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentMeansType = position;
                        setMeansEdit(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
            case MODE_REMARKS:
                btnAdd.setVisibility(View.GONE);
                cardRemark.setVisibility(View.VISIBLE);
                cardMeans.setVisibility(View.GONE);
                List<Word> words = LitePal.where("wordId = ?", currentWordId + "").select("wordId","word","remark").find(Word.class);
                if (words.get(0).getRemark() != null) {
                    editRemark.setText(words.get(0).getRemark());
                }
                break;
            case MODE_SENTENCES:
                updateSenList.clear();
                btnAdd.setVisibility(View.VISIBLE);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerSentences.setLayoutManager(linearLayoutManager);
                List<Sentence> sentenceList = LitePal.where("wordId = ?", currentWordId + "").find(Sentence.class);
                for (Sentence sentence : sentenceList) {
                    updateSenList.add(new ItemUpdateSen(sentence.getChsSentence(), sentence.getEnSentence()));
                }
                updateSentenceAdapter = new UpdateSentenceAdapter(updateSenList);
                recyclerSentences.setAdapter(updateSentenceAdapter);
                break;
        }

        textDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentMode) {
                    case MODE_MEANS:
                        for (int i = 0; i < types.length; ++i) {
                            UpdateMeans updateMeans = meansMap.get(i);
                            Log.d(TAG, meansTypes[i] + ": " + updateMeans.getChsMeans() + updateMeans.getEnMeans());
                            Interpretation interpretation = new Interpretation();
                            if (!updateMeans.getChsMeans().equals("@null")) {
                                interpretation.setCHSMeaning(updateMeans.getChsMeans());
                            } else {
                                interpretation.setToDefault("CHSMeaning");
                            }
                            if (!updateMeans.getEnMeans().equals("@null")) {
                                interpretation.setENMeaning(updateMeans.getEnMeans());
                            } else {
                                interpretation.setToDefault("ENMeaning");
                            }
                            if (!LitePal.where("wordId = ? and wordType = ?", currentWordId + "", types[i]).find(Interpretation.class).isEmpty()) {
                                if (!updateMeans.getEnMeans().equals("@null") && !updateMeans.getChsMeans().equals("@null")) {
                                    interpretation.updateAll("wordId = ? and wordType = ?", currentWordId + "", types[i]);
                                } else {
                                    // 删除
                                    LitePal.deleteAll(Interpretation.class, "wordId = ? and wordType = ?", currentWordId + "", types[i]);
                                }
                            } else {
                                // 当两者都不为空的时候，再增加
                                if (!(updateMeans.getEnMeans().equals("@null") && updateMeans.getChsMeans().equals("@null"))) {
                                    interpretation.setWordType(types[i]);
                                    interpretation.setWordId(currentWordId);
                                    interpretation.save();
                                }
                            }
                        }
                        Toast.makeText(UpdateActivity.this, "更新完毕", Toast.LENGTH_SHORT).show();

                        onBackPressed();
                        break;
                    case MODE_REMARKS:
                        Word word = new Word();
                        if (TextUtils.isEmpty(editRemark.getText().toString()))
                            word.setToDefault("remark");
                        else
                            word.setRemark(editRemark.getText().toString());
                        word.updateAll("wordId = ?", currentWordId + "");
                        Toast.makeText(UpdateActivity.this, "更新完毕", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        break;
                    case MODE_SENTENCES:
                        LitePal.deleteAll(Sentence.class, "wordId = ?", currentWordId + "");
                        for (ItemUpdateSen itemUpdateSen : updateSenList) {
                            if (!TextUtils.isEmpty(itemUpdateSen.getEnSentences()) && !TextUtils.isEmpty(itemUpdateSen.getChsSentences())) {
                                Sentence sentence = new Sentence();
                                sentence.setWordId(currentWordId);
                                sentence.setChsSentence(itemUpdateSen.getChsSentences());
                                sentence.setEnSentence(itemUpdateSen.getEnSentences());
                                sentence.save();
                            }
                        }
                        Toast.makeText(UpdateActivity.this, "更新完毕", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        break;
                }
            }
        });

        layoutTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tip = "";
                switch (currentMode) {
                    case MODE_MEANS:
                        tip = "1. 中英文释义必须填写其中一项，若当前选择词性的中英文释义均无，则默认没有该词性的意思\n2. 数据无法恢复，请谨慎修改";
                        break;
                    case MODE_REMARKS:
                        tip = "备注方便用自己的方式去理解记忆，若不填，则默认没有备注，在详情页就不显示备注信息";
                        break;
                    case MODE_SENTENCES:
                        tip = "1. 中英文释义必须都填写，若当前项例句的中英文释义均无，则默认不添加该例句\n2. 数据无法恢复，请谨慎修改";
                        break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                builder.setTitle("提示")
                        .setMessage(tip)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSenList.add(new ItemUpdateSen("", ""));
                updateSentenceAdapter.notifyDataSetChanged();
            }
        });

    }

    private void init() {
        spinnerMeansType = findViewById(R.id.spinner_update_type);
        editMeansChs = findViewById(R.id.edit_update_means_chs);
        editMeansEn = findViewById(R.id.edit_update_means_en);
        editRemark = findViewById(R.id.edit_update_remark);
        cardMeans = findViewById(R.id.card_update_means);
        cardRemark = findViewById(R.id.card_update_remark);
        textDone = findViewById(R.id.text_update_done);
        recyclerSentences = findViewById(R.id.recycler_update_sentences);
        layoutTip = findViewById(R.id.layout_update_tip);
        btnAdd = findViewById(R.id.btn_update_add);
    }

    private void setMeansData() {
        meansMap.clear();
        for (int i = 0; i < types.length; ++i) {
            List<Interpretation> interpretations = LitePal.where("wordType = ? and wordId = ?", types[i], currentWordId + "").find(Interpretation.class);
            if (!interpretations.isEmpty()) {
                Log.d(TAG, "setMeansData: " + interpretations.get(0).getCHSMeaning() + "\n" + interpretations.get(0).getENMeaning());
                UpdateMeans updateMeans = new UpdateMeans();
                if (interpretations.get(0).getCHSMeaning() != null)
                    updateMeans.setChsMeans(interpretations.get(0).getCHSMeaning());
                else
                    updateMeans.setChsMeans("@null");
                if (interpretations.get(0).getENMeaning() != null)
                    updateMeans.setEnMeans(interpretations.get(0).getENMeaning());
                else
                    updateMeans.setEnMeans("@null");
                meansMap.put(i, updateMeans);
            } else {
                UpdateMeans updateMeans = new UpdateMeans();
                updateMeans.setChsMeans("@null");
                updateMeans.setEnMeans("@null");
                meansMap.put(i, updateMeans);
            }
        }
    }

    private void setMeansEdit(int position) {
        if (!meansMap.get(position).getChsMeans().equals("@null"))
            editMeansChs.setText(meansMap.get(position).getChsMeans());
        else
            editMeansChs.setText("");
        if (!meansMap.get(position).getEnMeans().equals("@null"))
            editMeansEn.setText(meansMap.get(position).getEnMeans());
        else
            editMeansEn.setText("");
    }

}
