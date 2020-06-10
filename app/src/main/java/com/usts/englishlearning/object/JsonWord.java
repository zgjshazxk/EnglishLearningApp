package com.usts.englishlearning.object;

public class JsonWord {

    // 单词序号
    private int wordRank;

    // 单词名称
    private String headWord;

    // 单词内容
    private JsonWordTotal content;

    // 单词归属数目
    private String bookId;

    public int getWordRank() {
        return wordRank;
    }

    public void setWordRank(int wordRank) {
        this.wordRank = wordRank;
    }

    public String getHeadWord() {
        return headWord;
    }

    public void setHeadWord(String headWord) {
        this.headWord = headWord;
    }

    public JsonWordTotal getContent() {
        return content;
    }

    public void setContent(JsonWordTotal content) {
        this.content = content;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
