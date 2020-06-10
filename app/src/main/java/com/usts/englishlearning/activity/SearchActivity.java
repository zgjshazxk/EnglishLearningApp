package com.usts.englishlearning.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.SearchAdapter;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemSearch;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private RecyclerView recyclerSearch;

    private EditText editWord;

    private TextView textCancel;

    private RelativeLayout layoutNothing;

    private List<ItemSearch> itemSearches = new ArrayList<>();

    private SearchAdapter searchAdapter;

    private final int FINISH = 1;

    private static final String TAG = "SearchActivity";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerSearch.setLayoutManager(linearLayoutManager);

        searchAdapter = new SearchAdapter(itemSearches);
        recyclerSearch.setAdapter(searchAdapter);

        editWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {
                    layoutNothing.setVisibility(View.VISIBLE);
                    recyclerSearch.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, s.toString().trim());
                    setData(s.toString().trim());
                }
            }
        });

        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void init() {
        recyclerSearch = findViewById(R.id.recycler_search);
        editWord = findViewById(R.id.edit_search);
        textCancel = findViewById(R.id.text_search_cancel);
        layoutNothing = findViewById(R.id.layout_search_nothing);
    }

    private void setData(String s) {
        itemSearches.clear();
        List<Word> words = LitePal.where("word like ?", s + "%").select("wordId", "word", "usPhone").limit(10).find(Word.class);
        if (!words.isEmpty()) {
            for (Word word : words) {
                List<Interpretation> interpretations = LitePal.where("wordId = ?", word.getWordId() + "").select("wordType", "CHSMeaning").find(Interpretation.class);
                StringBuilder stringBuilder = new StringBuilder();
                for (Interpretation interpretation : interpretations) {
                    stringBuilder.append(interpretation.getWordType() + ". " + interpretation.getCHSMeaning() + " ");
                }
                itemSearches.add(new ItemSearch(word.getWordId(), word.getWord(), word.getUsPhone(), stringBuilder.toString()));
            }
            layoutNothing.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
        } else {
            layoutNothing.setVisibility(View.VISIBLE);
            recyclerSearch.setVisibility(View.GONE);
        }
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
