package com.usts.englishlearning.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.database.Interpretation;
import com.usts.englishlearning.database.Phrase;
import com.usts.englishlearning.database.Sentence;
import com.usts.englishlearning.object.JsonPhrase;
import com.usts.englishlearning.object.JsonSentence;
import com.usts.englishlearning.object.JsonTran;
import com.usts.englishlearning.object.JsonWord;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

// JSON工具类
public class JsonHelper {

    private static final String TAG = "JsonHelper";

    // 采用Google的GSON开源框架
    public static Gson gson = new Gson();

    // 解析默认词库数据文件，并存放到数据库中
    public static void analyseDefaultAndSave(String jsonContent) {
        if (!LitePal.findAll(Word.class).isEmpty()) {
            LitePal.deleteAll(Word.class);
            LitePal.deleteAll(Interpretation.class);
            LitePal.deleteAll(Phrase.class);
            LitePal.deleteAll(Sentence.class);
        }
        // 解析的数据格式
        List<JsonSentence> jsonSentences = new ArrayList<>();
        List<JsonTran> jsonTrans = new ArrayList<>();
        List<JsonPhrase> jsonPhrases = new ArrayList<>();
        List<JsonWord> jsonWordList = gson.fromJson(jsonContent, new TypeToken<List<JsonWord>>() {}.getType());
        for (JsonWord jsonWord : jsonWordList) {
            Word word = new Word();
            // 设置ID
            word.setWordId(jsonWord.getWordRank());
            // 设置单词
            word.setWord(jsonWord.getHeadWord());
            // 设置音标
            if (jsonWord.getContent().getWord().getContent().getUkphone() != null) {
                if (jsonWord.getContent().getWord().getContent().getUkphone().indexOf(";") == -1)
                    word.setUkPhone("[" + jsonWord.getContent().getWord().getContent().getUkphone() + "]");
                else
                    word.setUkPhone("[" + jsonWord.getContent().getWord().getContent().getUkphone().split(";")[0] + "]");
            }
            if (jsonWord.getContent().getWord().getContent().getUsphone() != null) {
                if (jsonWord.getContent().getWord().getContent().getUsphone().indexOf(";") == -1)
                    word.setUsPhone("[" + jsonWord.getContent().getWord().getContent().getUsphone() + "]");
                else
                    word.setUsPhone("[" + jsonWord.getContent().getWord().getContent().getUsphone().split(";")[0] + "]");
            }
            // 设置图片
            if (jsonWord.getContent().getWord().getContent().getPicture() != null)
                word.setPicAddress(jsonWord.getContent().getWord().getContent().getPicture());
            // 设置巧记
            if (jsonWord.getContent().getWord().getContent().getRemMethod() != null)
                word.setRemMethod(jsonWord.getContent().getWord().getContent().getRemMethod().getVal());
            // 设置归属书目
            word.setBelongBook(jsonWord.getBookId());
            // 保存
            word.save();
            /*至此，单词的基本内容已经保存，接下来把其他表的数据保存并绑定到这个单词上*/
            // 设置短语
            if (jsonWord.getContent().getWord().getContent().getPhrase() != null) {
                jsonPhrases = jsonWord.getContent().getWord().getContent().getPhrase().getPhrases();
                for (JsonPhrase jsonPhrase : jsonPhrases) {
                    Phrase phrase = new Phrase();
                    phrase.setChsPhrase(jsonPhrase.getpCn());
                    phrase.setEnPhrase(jsonPhrase.getpContent());
                    phrase.setWordId(jsonWord.getWordRank());
                    phrase.save();
                }
            }
            // 设置释义
            jsonTrans = jsonWord.getContent().getWord().getContent().getTrans();
            for (JsonTran jsonTran : jsonTrans) {
                Interpretation interpretation = new Interpretation();
                interpretation.setWordType(jsonTran.getPos());
                interpretation.setCHSMeaning(jsonTran.getTranCn().replace("；；", ";").replace(",","，"));
                interpretation.setENMeaning(jsonTran.getTranOther());
                interpretation.setWordId(jsonWord.getWordRank());
                interpretation.save();
            }
            // 设置例句
            if (jsonWord.getContent().getWord().getContent().getSentence() != null) {
                jsonSentences = jsonWord.getContent().getWord().getContent().getSentence().getSentences();
                for (JsonSentence jsonSentence : jsonSentences) {
                    Sentence sentence = new Sentence();
                    sentence.setChsSentence(jsonSentence.getsCn());
                    sentence.setEnSentence(jsonSentence.getsContent());
                    sentence.setWordId(jsonWord.getWordRank());
                    sentence.save();
                }
            }
            // 清空数据，防止重复
            jsonPhrases.clear();
            jsonSentences.clear();
            jsonTrans.clear();
        }
        Log.d(TAG, "analyseDefaultAndSave: ");
    }

}
