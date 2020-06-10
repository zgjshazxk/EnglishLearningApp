package com.usts.englishlearning.database;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class Word extends LitePalSupport {

    // ID
    @Column(unique = true)
    private int wordId;

    // 单词
    private String word;

    // 英国音标
    private String ukPhone;

    // 美国音标
    private String usPhone;

    // 巧记
    private String remMethod;

    // 图片(网址)
    private String picAddress;

    // 自定义照片
    private byte[] picCustom;

    // 自定义备注
    private String remark;

    // 设置归属
    private String belongBook;

    // 是否收藏
    @Column(defaultValue = "0")
    private int isCollected;

    // 是否是简单词
    @Column(defaultValue = "0")
    private int isEasy;

    // 是否是刚学过
    @Column(defaultValue = "0")
    private int justLearned;

    /*
     * 以下是学习复习专用的
     */

    // 是否需要学习
    @Column(defaultValue = "0")
    private int isNeedLearned;

    // 需要学习的时间（以天为单位）
    private long needLearnDate;

    // 需要复习的时间（以天为单位）
    private long needReviewDate;

    // 是否学习过
    @Column(defaultValue = "0")
    private int isLearned;

    // 总计检验次数
    @Column(defaultValue = "0")
    private int examNum;

    // 总计检验答对次数
    @Column(defaultValue = "0")
    private int examRightNum;

    // 上次已掌握时间（时间戳）
    @Column(defaultValue = "0")
    private long lastMasterTime;

    // 上次复习的时间（时间戳）
    @Column(defaultValue = "0")
    private long lastReviewTime;

    // 掌握程度（总计10分）
    @Column(defaultValue = "0")
    private int masterDegree;

    // 深度掌握次数
    /*
     * 前提：掌握程度已达到10
     * 当深度次数为0时，记下次复习时间=上次已掌握时间+4天，若及时复习，更新上次已掌握时间
     * 当深度次数为1时，记下次复习时间=上次已掌握时间+3天，若及时复习，更新上次已掌握时间
     * 当深度次数为2时，记下次复习时间=上次已掌握时间+8天，若及时复习，更新上次已掌握时间
     * 当深度次数为3时，记已经完全掌握
     *
     * 检测哪些单词未及时深度复习：
     * 首先单词必须掌握程度=10，其次单词上次掌握的时间与现在的时间进行对比
     * （1）要是深度次数为0，且两者时间之差为大于4天，说明未深度复习
     * （2）要是深度次数为1，且两者时间之差为大于3天，说明未深度复习
     * （3）要是深度次数为2，且两者时间之差为大于8天，说明未深度复习
     * （#）若未及时深度复习，一律将其单词掌握程度-2（10→8）
     *
     * */
    @Column(defaultValue = "0")
    private int deepMasterTimes;

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUkPhone() {
        return ukPhone;
    }

    public void setUkPhone(String ukPhone) {
        this.ukPhone = ukPhone;
    }

    public String getUsPhone() {
        return usPhone;
    }

    public void setUsPhone(String usPhone) {
        this.usPhone = usPhone;
    }

    public String getRemMethod() {
        return remMethod;
    }

    public void setRemMethod(String remMethod) {
        this.remMethod = remMethod;
    }

    public String getPicAddress() {
        return picAddress;
    }

    public void setPicAddress(String picAddress) {
        this.picAddress = picAddress;
    }

    public byte[] getPicCustom() {
        return picCustom;
    }

    public void setPicCustom(byte[] picCustom) {
        this.picCustom = picCustom;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBelongBook() {
        return belongBook;
    }

    public void setBelongBook(String belongBook) {
        this.belongBook = belongBook;
    }

    public int getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(int isCollected) {
        this.isCollected = isCollected;
    }

    public int getIsEasy() {
        return isEasy;
    }

    public void setIsEasy(int isEasy) {
        this.isEasy = isEasy;
    }

    public int getJustLearned() {
        return justLearned;
    }

    public void setJustLearned(int justLearned) {
        this.justLearned = justLearned;
    }

    public int getIsNeedLearned() {
        return isNeedLearned;
    }

    public void setIsNeedLearned(int isNeedLearned) {
        this.isNeedLearned = isNeedLearned;
    }

    public long getNeedLearnDate() {
        return needLearnDate;
    }

    public void setNeedLearnDate(long needLearnDate) {
        this.needLearnDate = needLearnDate;
    }

    public long getNeedReviewDate() {
        return needReviewDate;
    }

    public void setNeedReviewDate(long needReviewDate) {
        this.needReviewDate = needReviewDate;
    }

    public int getIsLearned() {
        return isLearned;
    }

    public void setIsLearned(int isLearned) {
        this.isLearned = isLearned;
    }

    public int getExamNum() {
        return examNum;
    }

    public void setExamNum(int examNum) {
        this.examNum = examNum;
    }

    public int getExamRightNum() {
        return examRightNum;
    }

    public void setExamRightNum(int examRightNum) {
        this.examRightNum = examRightNum;
    }

    public long getLastMasterTime() {
        return lastMasterTime;
    }

    public void setLastMasterTime(long lastMasterTime) {
        this.lastMasterTime = lastMasterTime;
    }

    public long getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(long lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public int getMasterDegree() {
        return masterDegree;
    }

    public void setMasterDegree(int masterDegree) {
        this.masterDegree = masterDegree;
    }

    public int getDeepMasterTimes() {
        return deepMasterTimes;
    }

    public void setDeepMasterTimes(int deepMasterTimes) {
        this.deepMasterTimes = deepMasterTimes;
    }
}
