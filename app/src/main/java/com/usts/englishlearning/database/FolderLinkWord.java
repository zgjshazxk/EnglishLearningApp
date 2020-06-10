package com.usts.englishlearning.database;

import org.litepal.crud.LitePalSupport;

public class FolderLinkWord extends LitePalSupport {

    private int wordId;

    private int folderId;

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }
}
