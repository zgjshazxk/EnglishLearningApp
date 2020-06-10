package com.usts.englishlearning.util;

import android.util.Log;

import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.database.Word;

import org.litepal.LitePal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*背单词流程的控制器*/
public class WordController {

    // 新学模式
    public static final int NEW_LEARN = 1;
    // 及时复习模式
    public static final int REVIEW_AT_TIME = 2;
    // 一般复习模式（浅度&深度）
    public static final int REVIEW_GENERAL = 3;
    // 学习完毕
    public static final int TODAY_MASK_DONE = 4;

    // 今天复习总共的单词量
    public static int wordReviewNum;

    // 当前ID
    public static int currentWordId;
    // 当前模式
    public static int currentMode;

    public static final String TAG = "WordController";

    // 候选需要学习的单词
    public static List<Integer> needLearnWords = new ArrayList<>();

    // 候选需要复习的单词
    public static List<Integer> needReviewWords = new ArrayList<>();

    // 用于存放本轮刚学习过的单词，以便及时复习
    public static List<Integer> justLearnedWords = new ArrayList<>();

    // 生成候选需学习的单词
    public static void generateDailyLearnWords(long lastStartTime) {

        needLearnWords.clear();
        justLearnedWords.clear();

        // 获得准备数据
        List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        // 需要学习的，但是并没有在指定时间去学习
        List<Word> wordNeedLearnList = LitePal.where("isNeedLearned = ? and justLearned = ? and needLearnDate <= ?", "1", "0", TimeController.getCurrentDateStamp() + "")
                .select("wordId").find(Word.class);
        // 根本没有学习过的单词（这是供下面用来分配单词的库）
        // 不需要学习的单词有可能是需要复习的单词，需要将其排除
        List<Word> wordNoNeedLearnList = LitePal.where("isNeedLearned = ? and isLearned = ?", "0", "0").select("wordId").find(Word.class);
        // 得到每天需要的学习量
        int needWordTotal = userConfigs.get(0).getWordNeedReciteNum();

        // 说明再点击按钮的时候已经是新的一天了，这时候就要重新分配单词了
        if (!TimeController.isTheSameDay(lastStartTime, TimeController.getCurrentTimeStamp())) {
            Log.d(TAG, "a new day");
            // 分情况
            // 情况1.说明需要的单词是差了一些的，需要再分配一点
            if (wordNeedLearnList.size() < needWordTotal) {
                // 说明需要再分配differ个学习的单词
                int differ = needWordTotal - wordNeedLearnList.size();
                Log.d(TAG, "wordNoNeedLearnList=" + wordNoNeedLearnList.size());
                // 在未学习的列表里随机分配
                int[] stillLearnList = NumberController.getRandomNumberList(0, wordNoNeedLearnList.size() - 1, differ);
                Log.d(TAG, "still" + Arrays.toString(stillLearnList));
                // 把这些数据划给候选单词列表
                // 并且给它们标记为需要学习
                for (int i : stillLearnList) {
                    needLearnWords.add(wordNoNeedLearnList.get(i).getWordId());
                    // 更新数据
                    Word word = new Word();
                    word.setIsNeedLearned(1);
                    word.setNeedLearnDate(TimeController.getCurrentDateStamp());
                    word.updateAll("wordId = ?", wordNoNeedLearnList.get(i).getWordId() + "");
                }
                // 最后把之前需要学习但是在规定时间未学习的单词也一并划入候选列表中
                if (!wordNeedLearnList.isEmpty()) {
                    for (Word word : wordNeedLearnList) {
                        needLearnWords.add(word.getWordId());
                    }
                }
            } else {
                // 说明之前需要学习但是在规定时间未学习的单词量已经足够今天所学习了，不需要再分配更多了，直接把之前的再拿来用就可以了
                int i = 0;
                for (Word word : wordNeedLearnList) {
                    ++i;
                    if (i <= needWordTotal)
                        needLearnWords.add(word.getWordId());
                    else
                        break;
                }
            }
        } else {
            Log.d(TAG, "the same day");
            // 这时候说明还是同一天，直接分配未学习的单词就可以了
            int i = 0;
            for (Word word : wordNeedLearnList) {
                ++i;
                if (i <= needWordTotal)
                    needLearnWords.add(word.getWordId());
                else
                    break;
            }
        }
        Log.d(TAG, "generateDailyLearnWords: ");
        Log.d(TAG, needLearnWords.toString());
    }

    // 生成候选需复习的单词
    // 这就不需要判断是否是当天了，因为每天复习的列表都是一致的
    public static void generateDailyReviewWords() {
        Log.d(TAG, "开始运行了");
        // 防止重复添加
        needReviewWords.clear();
        justLearnedWords.clear();
        // 获得准备数据
        // 已经初学过，但是并未及时复习的单词
        List<Word> notReviewAtTimeList = LitePal.where("justLearned = ? and isLearned = ?", "1", "0").select("wordId").find(Word.class);
        // 浅度复习候选条件：已经学习过的并且单词掌握程度未到10的单词
        List<Word> littleReviewList = LitePal.where("isLearned = ? and masterDegree < ?", "1", "10").select("wordId").find(Word.class);
        // 深度复习候选条件：单词掌握程度达到10并且达到了单词复习的时间
        List<Word> deepReviewList = LitePal.where("masterDegree = ?", "10").select("wordId").find(Word.class);
        // (1).先找哪些单词未及时深度复习或者已经到了深度学习的阶段，找到的同时加入到候选复习单词列表
        for (Word word : deepReviewList) {
            switch (word.getDeepMasterTimes()) {
                case 0:
                    try {
                        // 说明未及时深度复习
                        if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) > 4) {
                            Word newWord = new Word();
                            newWord.setMasterDegree(8);
                            newWord.updateAll("wordId = ?", word.getWordId() + "");
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        } else if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) == 4) {
                            // 说明已经到了深度复习的那一天
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        // 说明未及时深度复习
                        if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) > 3) {
                            Word newWord = new Word();
                            newWord.setMasterDegree(8);
                            newWord.updateAll("wordId = ?", word.getWordId() + "");
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        } else if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) == 3) {
                            // 说明已经到了深度复习的那一天
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        // 说明未及时深度复习
                        if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) > 8) {
                            Word newWord = new Word();
                            newWord.setMasterDegree(8);
                            newWord.updateAll("wordId = ?", word.getWordId() + "");
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        } else if (TimeController.daysInternal(word.getLastMasterTime(), TimeController.getCurrentDateStamp()) == 8) {
                            // 说明已经到了深度复习的那一天
                            // 加入复习列表
                            needReviewWords.add(word.getWordId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        // (2).把需浅度复习的单词也一并加入到候选复习单词列表
        for (Word word : littleReviewList) {
            needReviewWords.add(word.getWordId());
        }
        // (3).把需及时复习的单词也一并加入到候选复习单词列表
        for (Word word : notReviewAtTimeList) {
            needReviewWords.add(word.getWordId());
        }
        Log.d(TAG, "generateDailyReviewWords: ");
        Log.d(TAG, needReviewWords.toString());
    }

    // 学习单词
    public static int learnNewWord() {
        Log.d(TAG, "learnNewWord--------------------------------------------------------");
        Log.d(TAG, "learnNewWord" + "最后是我返回了值");
        Log.d(TAG, "needLearnedWords.size=" + needLearnWords.size());
        if (!needLearnWords.isEmpty()) {
            int index = NumberController.getRandomNumber(0, needLearnWords.size() - 1);
            return needLearnWords.get(index);
        } else
            return -1;
    }

    // 认识/不认识都算已初学完这个单词
    // 已初学完该单词（未及时复习）
    public static void learnNewWordDone(int wordId) {
        Log.d(TAG, "learnNewWordDone--------------------------------------------------------");
        // 移除
        Log.d(TAG, "before　size:" + needLearnWords.size());
        for (int i = 0; i < needLearnWords.size(); ++i) {
            if (needLearnWords.get(i) == wordId) {
                needLearnWords.remove(i);
                break;
            }
        }
        Log.d(TAG, "after size:" + needLearnWords.size());
        // 放进临时需要复习的列表里
        justLearnedWords.add(wordId);
        // 更新数据库数据
        Word word = new Word();
        word.setJustLearned(1);
        word.setToDefault("isNeedLearned");
        word.updateAll("wordId = ?", wordId + "");
    }

    // 及时复习单词
    public static int reviewNewWord() {
        Log.d(TAG, "reviewNewWord: 最后是我返回了值");
        if (!justLearnedWords.isEmpty()) {
            Log.d(TAG, "-1=?" + justLearnedWords.size());
            int index = NumberController.getRandomNumber(0, justLearnedWords.size() - 1);
            return justLearnedWords.get(index);
        } else
            return -1;
    }

    // 及时复习完单词
    public static void reviewNewWordDone(int wordId, boolean isAnswerRight) {
        List<Word> words = LitePal.where("wordId = ?", wordId + "").select("wordId", "word", "masterDegree", "ExamRightNum", "DeepMasterTimes").find(Word.class);
        if (isAnswerRight) {
            // 移除
            for (int i = 0; i < justLearnedWords.size(); ++i) {
                if (justLearnedWords.get(i) == wordId) {
                    justLearnedWords.remove(i);
                    break;
                }
            }
            // 更新数据库数据
            Word word = new Word();
            word.setIsLearned(1);
            // 如果回答正确，加2点掌握度，回答错误，不加掌握程度，继续背
            if (words.get(0).getMasterDegree() < 10) {
                // 掌握程度+2
                if (words.get(0).getMasterDegree() != 8)
                    word.setMasterDegree(words.get(0).getMasterDegree() + 2);
                else
                    word.setMasterDegree(10);
                word.updateAll("wordId = ?", wordId + "");
            } else {
                word.setDeepMasterTimes(words.get(0).getDeepMasterTimes() + 1);
                word.setLastMasterTime(TimeController.getCurrentDateStamp());
            }
            word.setLastReviewTime(TimeController.getCurrentTimeStamp());
            word.updateAll("wordId = ?", wordId + "");
        }
    }

    // 复习单词（随机抽取）
    public static int reviewOneWord() {
        Log.d(TAG, "reviewOneWord: 最后是我返回了值");
        if (!needReviewWords.isEmpty()) {
            int index = NumberController.getRandomNumber(0, needReviewWords.size() - 1);
            return needReviewWords.get(index);
        } else
            return -1;
    }

    // 复习完该单词
    public static void reviewOneWordDone(int wordId, boolean isAnswerRight) {
        List<Word> words = LitePal.where("wordId = ?", wordId + "").select("wordId", "word", "masterDegree", "ExamRightNum", "DeepMasterTimes").find(Word.class);
        if (isAnswerRight) {
            // 移除需要复习列表
            for (int i = 0; i < needReviewWords.size(); ++i) {
                if (needReviewWords.get(i) == wordId) {
                    needReviewWords.remove(i);
                    break;
                }
            }
            // 说明只是浅度复习
            if (words.get(0).getMasterDegree() < 10) {
                Word word = new Word();
                // 测试正确次数+1
                word.setExamRightNum(1 + words.get(0).getExamRightNum());
                // 掌握程度+2
                if (words.get(0).getMasterDegree() != 8)
                    word.setMasterDegree(words.get(0).getMasterDegree() + 2);
                else
                    word.setMasterDegree(10);
                word.updateAll("wordId = ?", wordId + "");
            } else {
                // 说明已是处于深度复习阶段
                Word word = new Word();
                word.setDeepMasterTimes(words.get(0).getDeepMasterTimes() + 1);
                word.setLastMasterTime(TimeController.getCurrentDateStamp());
                word.updateAll("wordId = ?", wordId + "");
            }
        }
        // 更新测试次数
        Word word = new Word();
        word.setExamNum(words.get(0).getExamNum() + 1);
        word.updateAll("wordId = ?", wordId + "");
    }

    /*-----------------------------*/

    // 随机来决定接下来是新学还是及时复习
    public static int isNewOrReviewAtTime() {
        return NumberController.getRandomNumber(NEW_LEARN, REVIEW_AT_TIME);
    }

    public static void removeOneWord(int wordId) {
        Log.d(TAG, "removeOneWord: " + wordId);
        Log.d(TAG, "之前");
        Log.d(TAG, needLearnWords.toString());
        Log.d(TAG, needReviewWords.toString());
        for (int i = 0; i < needLearnWords.size(); ++i) {
            if (wordId == needLearnWords.get(i))
                needLearnWords.remove(i);
        }
        for (int i = 0; i < needReviewWords.size(); ++i) {
            if (wordId == needReviewWords.get(i))
                needReviewWords.remove(i);
        }
        for (int i = 0; i < justLearnedWords.size(); ++i) {
            if (wordId == justLearnedWords.get(i))
                justLearnedWords.remove(i);
        }
        Log.d(TAG, "之后");
        Log.d(TAG, needLearnWords.toString());
        Log.d(TAG, needReviewWords.toString());
    }

    // 决定学习顺序
    public static int whatToDo() {
        Log.d(TAG, "needLearnWordsSize=" + needLearnWords.size());
        Log.d(TAG, "justLearnedWords=" + justLearnedWords.size());
        Log.d(TAG, "needReViewWords=" + needReviewWords.size());
        if (!needLearnWords.isEmpty()) {
            if (justLearnedWords.size() > (needLearnWords.size() / 2)) {
                Log.d(TAG, "接下来是随机模式");
                int nowMode = isNewOrReviewAtTime();
                Log.d(TAG, "whatToDo: " + nowMode);
                return nowMode;
            } else {
                Log.d(TAG, "接下来是新学模式");
                return NEW_LEARN;
            }
        } else {
            if (!justLearnedWords.isEmpty()) {
                Log.d(TAG, "接下来是及时复习模式");
                return REVIEW_AT_TIME;
            } else {
                if (!needReviewWords.isEmpty()) {
                    Log.d(TAG, "接下来是一般复习模式");
                    return REVIEW_GENERAL;
                } else {
                    Log.d(TAG, "done");
                    return TODAY_MASK_DONE;
                }
            }
        }
    }

}
