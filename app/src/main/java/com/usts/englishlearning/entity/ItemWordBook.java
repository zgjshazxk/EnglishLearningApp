package com.usts.englishlearning.entity;

public class ItemWordBook {

    // 单词书ID
    private int bookId;

    // 单词书名称
    private String bookName;

    // 单词书单词总数
    private int bookWordNum;

    // 单词书数据来源
    private String bookSource;

    // 单词书图片
    private String bookImg;

    public ItemWordBook(int bookId, String bookName, int bookWordNum, String bookSource, String bookImg) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookWordNum = bookWordNum;
        this.bookSource = bookSource;
        this.bookImg = bookImg;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getBookWordNum() {
        return bookWordNum;
    }

    public void setBookWordNum(int bookWordNum) {
        this.bookWordNum = bookWordNum;
    }

    public String getBookSource() {
        return bookSource;
    }

    public void setBookSource(String bookSource) {
        this.bookSource = bookSource;
    }

    public String getBookImg() {
        return bookImg;
    }

    public void setBookImg(String bookImg) {
        this.bookImg = bookImg;
    }
}
