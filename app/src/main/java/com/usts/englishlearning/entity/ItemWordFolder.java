package com.usts.englishlearning.entity;

public class ItemWordFolder {

    private int folderId;

    private int wordNum;

    private String folderName;

    private String folderRemark;

    public ItemWordFolder(int folderId, int wordNum, String folderName, String folderRemark) {
        this.folderId = folderId;
        this.wordNum = wordNum;
        this.folderName = folderName;
        this.folderRemark = folderRemark;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getWordNum() {
        return wordNum;
    }

    public void setWordNum(int wordNum) {
        this.wordNum = wordNum;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderRemark() {
        return folderRemark;
    }

    public void setFolderRemark(String folderRemark) {
        this.folderRemark = folderRemark;
    }
}
