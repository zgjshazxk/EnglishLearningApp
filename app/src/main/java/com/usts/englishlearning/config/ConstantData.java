package com.usts.englishlearning.config;

import com.usts.englishlearning.R;

// 存放不变的数据（常量）
public class ConstantData {

    // 数据存放目录（外置存储卡）
    public static final String DIR_TOTAL = "englishLearning";
    // 解压后的数据目录
    public static final String DIR_AFTER_FINISH = "json";

    // 书默认ID
    public static final int CET4_WORDBOOK = 1;
    public static final int CET6_WORDBOOK = 2;
    public static final int KAOYAN_WORDBOOK = 3;
    public static final int CET6ALL = 4;
    public static final int KAOYANALL = 5;

    // 背景图API
    public static final String IMG_API = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
    public static final String IMG_API_BEFORE = "https://www.bing.com";

    // 每日一句API
    public static final String DAILY_SENTENCE_API = "https://open.iciba.com/dsapi/";

    // 有道英音发音
    public static final String YOU_DAO_VOICE_EN = "https://dict.youdao.com/dictvoice?type=1&audio=";

    // 有道美音发音
    public static final String YOU_DAO_VOICE_USA = "https://dict.youdao.com/dictvoice?type=0&audio=";

    // raw资源正确提示音
    public static final int RIGHT_SIGN = R.raw.right;
    // raw资源错误提示音
    public static final int WRONG_SIGN = R.raw.wrong;

    // 通知渠道ID
    public static final String channelId = "default";
    public static final String channelId2 = "default2";
    // 通知渠道名称
    public static final String channelName = "默认通知";
    public static final String channelName2 = "默认通知2";

    // 提示句子集合
    public static final String[] phrases = {
            "马行软地易失蹄，人贪安逸易失志",
            "每天告诉自己一次：我真的很不错",
            "没有热忱，世间便无进步",
            "有志者，事竟成，破釜沉舟，百二秦关终属楚",
            "有心人，天不负，卧薪尝胆，三千越甲可吞吴",
            "风尘三尺剑，社稷一戎衣",
            "只要站起来的次数比倒下去的次数多，那就是成功",
            "收拾一下心情，开始下一个新的开始",
            "你配不上自己的野心，也辜负了曾经历的苦难",
            "现实很近又很冷，梦想很远却很温暖",
            "前方无绝路，希望在转角",
            "没有人会让我输，除非我不想赢",
            "追踪着鹿的猎人是看不见山的",
            "有志始知蓬莱近，无为总觉咫尺远",
            "业精于勤而荒于嬉，行成于思而毁于随",
            "没有所谓失败，除非你不再尝试"};

    // 根据书ID获取该书的单词总量
    public static int wordTotalNumberById(int bookId) {
        int num = 0;
        switch (bookId) {
            case CET4_WORDBOOK:
                num = 1162;
                break;
            case CET6_WORDBOOK:
                num = 1228;
                break;
            case KAOYAN_WORDBOOK:
                num = 1341;
                break;
            case CET6ALL:
                num = 2078;
                break;
            case KAOYANALL:
                num = 4533;
        }
        return num;
    }

    // 根据书ID获取该书的书名
    public static String bookNameById(int bookId) {
        String name = "";
        switch (bookId) {
            case CET4_WORDBOOK:
                name = "英语四级核心词";
                break;
            case CET6_WORDBOOK:
                name = "英语六级核心词";
                break;
            case KAOYAN_WORDBOOK:
                name = "考研必考词汇";
                break;
            case CET6ALL:
                name = "六级英语大纲";
                break;
            case KAOYANALL:
                name = "考研英语大纲";
                break;
        }
        return name;
    }

    // 根据书ID获取该书的类型
    public static String typeById(int bookId) {
        String name = "";
        switch (bookId) {
            case CET4_WORDBOOK:
                name = "四级";
                break;
            case CET6_WORDBOOK:
            case CET6ALL:
                name = "六级";
                break;
            case KAOYAN_WORDBOOK:
            case KAOYANALL:
                name = "考研";
                break;
        }
        return name;
    }

    // 根据书ID获取该书的图片
    public static String bookPicById(int bookId) {
        String picAddress = "";
        switch (bookId) {
            case CET4_WORDBOOK:
                picAddress = "https://nos.netease.com/ydschool-online/1496632727200CET4luan_1.jpg";
                break;
            case CET6_WORDBOOK:
                picAddress = "https://nos.netease.com/ydschool-online/1496655382926CET6luan_1.jpg";
                break;
            case KAOYAN_WORDBOOK:
                picAddress = "https://nos.netease.com/ydschool-online/1496632762670KaoYanluan_1.jpg";
                break;
            case CET6ALL:
                picAddress = "https://nos.netease.com/ydschool-online/youdao_CET6_2.jpg";
                break;
            case KAOYANALL:
                picAddress = "https://nos.netease.com/ydschool-online/youdao_KaoYan_2.jpg";
                break;
        }
        return picAddress;
    }

    // 根据书ID获取该书的下载地址
    public static String bookDownLoadAddressById(int bookId) {
        String picAddress = "";
        switch (bookId) {
            case CET4_WORDBOOK:
                picAddress = "http://ydschool-online.nos.netease.com/1523620217431_CET4luan_1.zip";
                break;
            case CET6_WORDBOOK:
                picAddress = "http://ydschool-online.nos.netease.com/1521164660466_CET6luan_1.zip";
                break;
            case KAOYAN_WORDBOOK:
                picAddress = "http://ydschool-online.nos.netease.com/1521164661106_KaoYanluan_1.zip";
                break;
            case CET6ALL:
                picAddress = "http://ydschool-online.nos.netease.com/1524052554766_CET6_2.zip";
                break;
            case KAOYANALL:
                picAddress = "http://ydschool-online.nos.netease.com/1521164654696_KaoYan_2.zip";
                break;
        }
        return picAddress;
    }

    // 根据书ID获取该书的下载后的文件名
    public static String bookFileNameById(int bookId) {
        String picAddress = "";
        switch (bookId) {
            case CET4_WORDBOOK:
                picAddress = "CET4luan_1.zip";
                break;
            case CET6_WORDBOOK:
                picAddress = "CET6luan_1.zip";
                break;
            case KAOYAN_WORDBOOK:
                picAddress = "KaoYanluan_1.zip";
                break;
            case CET6ALL:
                picAddress = "CET6_2.zip";
                break;
            case KAOYANALL:
                picAddress = "KaoYan_2.zip";
                break;
        }
        return picAddress;
    }

}
