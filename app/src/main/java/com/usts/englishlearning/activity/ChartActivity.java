package com.usts.englishlearning.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.config.ConstantData;
import com.usts.englishlearning.database.LearnTime;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.UserConfig;
import com.usts.englishlearning.database.Word;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends BaseActivity {

    private PieChart pieChart;

    private BarChart barChartWord, barChartTime;

    private ArrayList<PieEntry> pieEntries = new ArrayList<>();

    private ArrayList<BarEntry> learnData = new ArrayList<>();

    private ArrayList<BarEntry> reviewData = new ArrayList<>();

    private ArrayList<BarEntry> timeData = new ArrayList<>();

    private TextView textTodayLearn, textTodayReview, textNoLearn, textLittle, textDeep, textAlready, textLearnTime;

    private float toadyLearnWord, todayReviewWord, noLearn, littleWord, deepWord, alreadyWord, todayLearnTime;

    private final int FINISH = 1;

    private ProgressDialog progressDialog;

    private static final String TAG = "ChartActivity";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case FINISH:
                    configBarTimeData();
                    configBarWordData();
                    configPieData();
                    textAlready.setText((int) alreadyWord + "");
                    textDeep.setText((int) deepWord + "");
                    textLittle.setText((int) littleWord + "");
                    textNoLearn.setText((int) noLearn + "");
                    textTodayLearn.setText((int) toadyLearnWord + "");
                    textTodayReview.setText((int) todayReviewWord + "");
                    if (todayLearnTime != 0) {
                        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                        String p = decimalFormat.format(todayLearnTime);
                        if (p.indexOf(".") == 0)
                            textLearnTime.setText("0" + p);
                        else
                            textLearnTime.setText(p);
                    } else
                        textLearnTime.setText("0");
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        init();

        initBarChart();

        initPieChart();

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWordData();
                readPieData();
                loadTimeData();
                Message message = new Message();
                message.what = FINISH;
                handler.sendMessage(message);
            }
        }).start();

    }

    private void init() {
        pieChart = findViewById(R.id.chart_scale);
        pieChart.setNoDataText("暂无数据");
        barChartWord = findViewById(R.id.chart_word);
        barChartWord.setNoDataText("暂无数据");
        textAlready = findViewById(R.id.text_chart_already);
        textDeep = findViewById(R.id.text_chart_deep);
        textLittle = findViewById(R.id.text_chart_little);
        textNoLearn = findViewById(R.id.text_chart_noLearn);
        textTodayLearn = findViewById(R.id.text_chart_todayLearn);
        textTodayReview = findViewById(R.id.text_chart_todayReview);
        barChartTime = findViewById(R.id.chart_time);
        textLearnTime = findViewById(R.id.text_chart_todayLearnTime);
    }

    private void initBarChart() {
        barChartWord.setDrawBarShadow(false);
        barChartTime.setDrawBarShadow(false);
        barChartWord.setDrawValueAboveBar(true);
        barChartTime.setDrawValueAboveBar(true);
        barChartWord.getDescription().setEnabled(false);
        barChartTime.getDescription().setEnabled(false);
        Legend legend = barChartWord.getLegend();
        Legend legend2 = barChartTime.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend2.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend2.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        // 集双指缩放
        barChartWord.setPinchZoom(false);
        barChartTime.setPinchZoom(false);
        // 动画
        barChartWord.animateY(2000);
        barChartTime.animateY(2000);

        XAxis xAxis = barChartWord.getXAxis();
        XAxis xAxis2 = barChartTime.getXAxis();
        xAxis.setDrawLabels(true);//是否显示x坐标的数据
        xAxis2.setDrawLabels(true);//是否显示x坐标的数据
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x坐标数据的位置
        xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x坐标数据的位置
        xAxis.setDrawGridLines(false);//是否显示网格线中与x轴垂直的网格线
        xAxis2.setDrawGridLines(false);//是否显示网格线中与x轴垂直的网格线

        final List<String> xValue = new ArrayList<>();
        xValue.add("zero");//index = 0 的位置的数据是否显示，跟barChart.groupBars中的第一个参数有关。
        xValue.add(TimeController.getPastDate(5));
        xValue.add(TimeController.getPastDate(4));
        xValue.add(TimeController.getPastDate(3));
        xValue.add(TimeController.getPastDate(2));
        xValue.add(TimeController.getPastDate(1));
        xValue.add(TimeController.getPastDate(0));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValue));//设置x轴标签格式化器
        xAxis2.setValueFormatter(new IndexAxisValueFormatter(xValue));//设置x轴标签格式化器

        YAxis rightYAxis = barChartWord.getAxisRight();
        YAxis rightYAxis2 = barChartTime.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis2.setDrawGridLines(false);
        rightYAxis.setEnabled(true);//设置右侧的y轴是否显示。包括y轴的那一条线和上面的标签都不显示
        rightYAxis2.setEnabled(true);//设置右侧的y轴是否显示。包括y轴的那一条线和上面的标签都不显示
        rightYAxis.setDrawLabels(false);//设置y轴右侧的标签是否显示。只是控制y轴处的标签。控制不了那根线。
        rightYAxis2.setDrawLabels(false);//设置y轴右侧的标签是否显示。只是控制y轴处的标签。控制不了那根线。
        rightYAxis.setDrawAxisLine(false);//这个方法就是专门控制坐标轴线的
        rightYAxis2.setDrawAxisLine(false);//这个方法就是专门控制坐标轴线的

        YAxis leftYAxis = barChartWord.getAxisLeft();
        YAxis leftYAxis2 = barChartTime.getAxisLeft();
        leftYAxis.setEnabled(true);
        leftYAxis2.setEnabled(true);
        leftYAxis.setDrawLabels(true);
        leftYAxis2.setDrawLabels(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis2.setDrawAxisLine(true);
        leftYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftYAxis2.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftYAxis.setDrawGridLines(false);//只有左右y轴标签都设置不显示水平网格线，图形才不会显示网格线
        leftYAxis2.setDrawGridLines(false);//只有左右y轴标签都设置不显示水平网格线，图形才不会显示网格线
        leftYAxis.setDrawGridLinesBehindData(true);//设置网格线是在柱子的上层还是下一层（类似Photoshop的层级）
        leftYAxis2.setDrawGridLinesBehindData(true);//设置网格线是在柱子的上层还是下一层（类似Photoshop的层级）
        leftYAxis.setGranularity(1f);//设置最小的间隔，防止出现重复的标签。这个得自己尝试一下就知道了。
        leftYAxis2.setGranularity(1f);//设置最小的间隔，防止出现重复的标签。这个得自己尝试一下就知道了。
        leftYAxis.setAxisMinimum(0);//设置左轴最小值的数值。如果IndexAxisValueFormatter自定义了字符串的话，那么就是从序号为2的字符串开始取值。
        leftYAxis2.setAxisMinimum(0);//设置左轴最小值的数值。如果IndexAxisValueFormatter自定义了字符串的话，那么就是从序号为2的字符串开始取值。
        leftYAxis.setSpaceBottom(0);//左轴的最小值默认占有10dp的高度，如果左轴最小值为0，一般会去除0的那部分高度
        leftYAxis2.setSpaceBottom(0);//左轴的最小值默认占有10dp的高度，如果左轴最小值为0，一般会去除0的那部分高度
    }

    private int getReviewData(String dayTime) {
        String[] days = dayTime.split("-");
        List<MyDate> myDates = LitePal.where("year = ? and month = ? and date = ?", days[0], days[1], days[2]).find(MyDate.class);
        if (myDates.isEmpty())
            return 0;
        else
            return myDates.get(0).getWordReviewNumber();
    }

    private int getLearnWordData(String dayTime) {
        String[] days = dayTime.split("-");
        List<MyDate> myDates = LitePal.where("year = ? and month = ? and date = ?", days[0], days[1], days[2]).find(MyDate.class);
        if (myDates.isEmpty())
            return 0;
        else
            return myDates.get(0).getWordLearnNumber();
    }

    private float getLearnTimeData(String dayTime) {
        List<LearnTime> learnTimes = LitePal.where("date = ?", dayTime).find(LearnTime.class);
        if (learnTimes.isEmpty())
            return 0;
        else {
            Log.d(TAG, "getLearnTimeData: " + learnTimes.get(0).getTime());
            return Float.parseFloat((double) Long.parseLong(learnTimes.get(0).getTime()) / (1000 * 60) + "");
        }
    }

    private void initPieChart() {
        pieChart.setUsePercentValues(false);//这货，是否使用百分比显示，但是我还是没操作出来。
        /*Description description = pieChart.getDescription();
        description.setText(""); //设置描述的文字*/
        pieChart.setDragDecelerationEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHighlightPerTapEnabled(true); //设置piecahrt图表点击Item高亮是否可用
        pieChart.animateX(2000);

        pieChart.setDrawEntryLabels(true); // 设置entry中的描述label是否画进饼状图中
        pieChart.setEntryLabelColor(Color.GRAY);//设置该文字是的颜色
        pieChart.setEntryLabelTextSize(10f);//设置该文字的字体大小

        pieChart.setDrawHoleEnabled(true);//设置圆孔的显隐，也就是内圆
        pieChart.setHoleRadius(28f);//设置内圆的半径。外圆的半径好像是不能设置的，改变控件的宽度和高度，半径会自适应。
        pieChart.setHoleColor(Color.WHITE);//设置内圆的颜色
        pieChart.setDrawCenterText(true);//设置是否显示文字
        List<UserConfig> userConfigs = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(UserConfig.class);
        pieChart.setCenterText(ConstantData.typeById(userConfigs.get(0).getCurrentBookId()));//设置饼状图中心的文字
        pieChart.setCenterTextSize(10f);//设置文字的消息
        pieChart.setCenterTextColor(Color.RED);//设置文字的颜色
        pieChart.setTransparentCircleRadius(31f);//设置内圆和外圆的一个交叉园的半径，这样会凸显内外部的空间
        pieChart.setTransparentCircleColor(Color.BLACK);//设置透明圆的颜色
        pieChart.setTransparentCircleAlpha(50);//设置透明圆你的透明度

        Legend legend = pieChart.getLegend();//图例
        legend.setEnabled(true);//是否显示
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//对齐
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);//对齐
        legend.setForm(Legend.LegendForm.DEFAULT);//设置图例的图形样式,默认为圆形
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);//设置图例的排列走向:vertacal相当于分行
        legend.setFormSize(6f);//设置图例的大小
        legend.setTextSize(7f);//设置图注的字体大小
        legend.setFormToTextSpace(3f);//设置图例到图注的距离
        legend.setDrawInside(true);//不是很清楚
        legend.setWordWrapEnabled(false);//设置图列换行(注意使用影响性能,仅适用legend位于图表下面)，我也不知道怎么用的
        legend.setTextColor(Color.BLACK);
    }

    // 耗时操作
    private void readPieData() {
        List<Word> noNeedWords = LitePal.where("isLearned = ?", 0 + "").select("wordId").find(Word.class);
        noLearn = noNeedWords.size();
        if (!noNeedWords.isEmpty())
            pieEntries.add(new PieEntry(noNeedWords.size(), "未学"));
        List<Word> littleWords = LitePal.where("masterDegree < ? and isLearned = ?", 10 + "", 1 + "").select("wordId").find(Word.class);
        littleWord = littleWords.size();
        if (!littleWords.isEmpty())
            pieEntries.add(new PieEntry(littleWords.size(), "轻度复习"));
        List<Word> deepWords = LitePal.where("masterDegree = ? and deepMasterTimes < ?", 10 + "", 3 + "").select("wordId").find(Word.class);
        deepWord = deepWords.size();
        if (!deepWords.isEmpty())
            pieEntries.add(new PieEntry(deepWords.size(), "深度复习"));
        List<Word> alreadyWords = LitePal.where("masterDegree = ? and deepMasterTimes = ?", 10 + "", 3 + "").select("wordId").find(Word.class);
        alreadyWord = alreadyWords.size();
        if (!alreadyWords.isEmpty())
            pieEntries.add(new PieEntry(alreadyWords.size(), "复习完毕"));

    }

    private void configPieData() {
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(Color.parseColor("#FFDEC9"),
                Color.parseColor("#FFB44A"),
                Color.parseColor("#FFD191"),
                Color.parseColor("#FFF1DE"));
        pieDataSet.setSliceSpace(3f);//设置每块饼之间的空隙
        pieDataSet.setSelectionShift(10f);//点击某个饼时拉长的宽度

        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Log.d(TAG, "getFormattedValue: ");
                return (int) value + "";
            }
        });

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    // 耗时操作
    private void loadWordData() {
        learnData.add(new BarEntry(1, getLearnWordData(TimeController.getPastDateWithYear(5))));
        learnData.add(new BarEntry(2, getLearnWordData(TimeController.getPastDateWithYear(4))));
        learnData.add(new BarEntry(3, getLearnWordData(TimeController.getPastDateWithYear(3))));
        learnData.add(new BarEntry(4, getLearnWordData(TimeController.getPastDateWithYear(2))));
        learnData.add(new BarEntry(5, getLearnWordData(TimeController.getPastDateWithYear(1))));
        toadyLearnWord = getLearnWordData(TimeController.getPastDateWithYear(0));
        learnData.add(new BarEntry(6, toadyLearnWord));

        reviewData.add(new BarEntry(1, getReviewData(TimeController.getPastDateWithYear(5))));
        reviewData.add(new BarEntry(2, getReviewData(TimeController.getPastDateWithYear(4))));
        reviewData.add(new BarEntry(3, getReviewData(TimeController.getPastDateWithYear(3))));
        reviewData.add(new BarEntry(4, getReviewData(TimeController.getPastDateWithYear(2))));
        reviewData.add(new BarEntry(5, getReviewData(TimeController.getPastDateWithYear(1))));
        todayReviewWord = getReviewData(TimeController.getPastDateWithYear(0));
        reviewData.add(new BarEntry(6, todayReviewWord));
    }

    // 耗时操作
    private void loadTimeData() {
        timeData.add(new BarEntry(1, getLearnTimeData(TimeController.getPastDateWithYear(5))));
        timeData.add(new BarEntry(2, getLearnTimeData(TimeController.getPastDateWithYear(4))));
        timeData.add(new BarEntry(3, getLearnTimeData(TimeController.getPastDateWithYear(3))));
        timeData.add(new BarEntry(4, getLearnTimeData(TimeController.getPastDateWithYear(2))));
        timeData.add(new BarEntry(5, getLearnTimeData(TimeController.getPastDateWithYear(1))));
        todayLearnTime = getLearnTimeData(TimeController.getPastDateWithYear(0));
        timeData.add(new BarEntry(6, todayLearnTime));
    }

    private void configBarWordData() {
        BarDataSet barDataSet1 = new BarDataSet(learnData, "当日学习");
        barDataSet1.setColor(Color.parseColor("#4cb4e7"));
        BarDataSet barDataSet2 = new BarDataSet(reviewData, "当日复习");
        barDataSet2.setColor(Color.parseColor("#1F6FB5"));
        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<>();

        iBarDataSets.add(barDataSet1);
        iBarDataSets.add(barDataSet2);

        BarData barData = new BarData(iBarDataSets);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });
        barData.setDrawValues(true);//是否显示柱子的数值
        barData.setValueTextSize(10f);//柱子上面标注的数值的字体大小
        barData.setBarWidth(0.3f);//每个柱子的宽度
        barChartWord.setData(barData);
        barChartWord.invalidate();
        //如果不设置组直接的距离的话，那么两个柱子会公用一个空间，即发生重叠；另外，设置了各种距离之后，X轴方向会自动调整距离，以保持“两端对齐”
        barChartWord.groupBars(0.45f/*从X轴哪个位置开始显示，这个参数具体啥意思。。。*/, 0.32f/*组与组之间的距离*/, 0.05f/*组中每个柱子之间的距离*/);
    }

    private void configBarTimeData() {
        BarDataSet barDataSet1 = new BarDataSet(timeData, "当日学习时间（分钟）");
        barDataSet1.setColor(Color.parseColor("#79e8d0"));
        ArrayList<IBarDataSet> iBarDataTimes = new ArrayList<>();

        iBarDataTimes.add(barDataSet1);

        BarData barData = new BarData(iBarDataTimes);
        barData.setDrawValues(true);//是否显示柱子的数值
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String p = decimalFormat.format(value);
                if (p.indexOf(".") == 0)
                    return "0" + p;
                else
                    return p;
            }
        });
        barData.setValueTextSize(10f);//柱子上面标注的数值的字体大小
        barData.setBarWidth(0.5f);//每个柱子的宽度
        barChartTime.setData(barData);
        barChartTime.invalidate();
        //如果不设置组直接的距离的话，那么两个柱子会公用一个空间，即发生重叠；另外，设置了各种距离之后，X轴方向会自动调整距离，以保持“两端对齐”
        //barChartTime.groupBars(0.45f/*从X轴哪个位置开始显示，这个参数具体啥意思。。。*/, 0.32f/*组与组之间的距离*/, 0.05f/*组中每个柱子之间的距离*/);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(ChartActivity.this);
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage("数据正在加载中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

}
