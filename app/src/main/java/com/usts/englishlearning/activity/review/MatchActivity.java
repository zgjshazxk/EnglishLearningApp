package com.usts.englishlearning.activity.review;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.BaseActivity;
import com.usts.englishlearning.adapter.MatchAdapter;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.entity.ItemMatch;
import com.usts.englishlearning.util.NumberController;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MatchActivity extends BaseActivity {

    public static List<Word> wordList = new ArrayList<>();

    public static ArrayList<ItemMatch> matchList = new ArrayList<>();

    public static ArrayList<ItemMatch> allMatches = new ArrayList<>();

    private RecyclerView recyclerView;

    private static final String TAG = "MatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        init();

        windowExplode();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        MatchAdapter matchAdapter = new MatchAdapter(matchList);
        recyclerView.setAdapter(matchAdapter);

    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_mt);
    }

}
