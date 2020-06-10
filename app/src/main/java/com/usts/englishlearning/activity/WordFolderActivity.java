package com.usts.englishlearning.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.usts.englishlearning.R;
import com.usts.englishlearning.adapter.WordFolderAdapter;
import com.usts.englishlearning.database.FolderLinkWord;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.WordFolder;
import com.usts.englishlearning.entity.ItemWordFolder;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class WordFolderActivity extends BaseActivity {

    private RecyclerView recyclerView;

    private List<ItemWordFolder> wordFolderList = new ArrayList<>();

    private ImageView imgAdd;

    private WordFolderAdapter wordFolderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_folder);

        windowExplode();

        init();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        wordFolderAdapter = new WordFolderAdapter(wordFolderList);
        recyclerView.setAdapter(wordFolderAdapter);

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WordFolderActivity.this, AddFolderActivity.class);
                startActivity(intent);
            }
        });

    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_wf);
        imgAdd = findViewById(R.id.img_wf_add);
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<WordFolder> wordFolders = LitePal.findAll(WordFolder.class);
        if (!wordFolders.isEmpty()) {
            wordFolderList.clear();
            for (WordFolder w : wordFolders) {
                List<FolderLinkWord> folderLinkWords = LitePal.where("folderId = ?", w.getId() + "").find(FolderLinkWord.class);
                wordFolderList.add(new ItemWordFolder(w.getId(), folderLinkWords.size(), w.getName(), w.getRemark()));
            }
            wordFolderAdapter.notifyDataSetChanged();
        }
    }
}
