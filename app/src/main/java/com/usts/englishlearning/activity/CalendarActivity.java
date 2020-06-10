package com.usts.englishlearning.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.usts.englishlearning.R;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.util.NormalSpan;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends BaseActivity {

    private MaterialCalendarView materialCalendarView;

    private TextView textDate, textWord, textRemark, textSign;

    private LinearLayout layoutRemark, layoutDate, layoutWord;

    private CardView cardInfor;

    private ImageView imgSign;

    private static final String TAG = "CalendarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        init();

        final List<MyDate> myDateList = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(MyDate.class);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDate = calendar.get(Calendar.DAY_OF_MONTH);

        materialCalendarView.setDateSelected(CalendarDay.from(currentYear, currentMonth, currentDate), true);
        materialCalendarView.setWeekDayLabels(new String[]{"SUN", "MON", "TUS", "WED", "THU", "FRI", "SAT"});
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);

        updateData(CalendarDay.from(currentYear, currentMonth, currentDate));

        materialCalendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                for (MyDate myDate : myDateList) {
                    if (day.getDay() == myDate.getDate() && day.getMonth() == (myDate.getMonth() - 1) && day.getYear() == myDate.getYear())
                        return true;
                }
                return false;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new NormalSpan());
            }
        });

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                updateData(date);
            }
        });

    }

    private void init() {
        materialCalendarView = findViewById(R.id.calendar);
        textDate = findViewById(R.id.text_cal_date);
        textWord = findViewById(R.id.text_cal_word);
        textRemark = findViewById(R.id.text_cal_remark);
        textSign = findViewById(R.id.text_cal_sign);
        layoutRemark = findViewById(R.id.layout_cal_remark);
        layoutWord = findViewById(R.id.layout_cal_word);
        layoutDate = findViewById(R.id.layout_cal_date);
        layoutRemark = findViewById(R.id.layout_cal_remark);
        cardInfor = findViewById(R.id.card_cal_infor);
        imgSign = findViewById(R.id.img_cal_sign);
    }

    private void updateData(CalendarDay date) {
        List<MyDate> myDates = LitePal.where("date = ? and month = ? and year = ? and userId = ?", date.getDay() + "", (date.getMonth() + 1) + "", date.getYear() + "", ConfigData.getSinaNumLogged() + "").find(MyDate.class);
        if (myDates.isEmpty()) {
            layoutDate.setVisibility(View.GONE);
            layoutRemark.setVisibility(View.GONE);
            layoutWord.setVisibility(View.GONE);
            Glide.with(CalendarActivity.this).load(R.drawable.icon_no_done).into(imgSign);
            textSign.setText("该日学习计划未完成");
            textSign.setTextColor(getColor(R.color.colorLightBlack));
            cardInfor.setCardBackgroundColor(getColor(R.color.colorBgWhite));
        } else {
            layoutDate.setVisibility(View.VISIBLE);
            layoutWord.setVisibility(View.VISIBLE);
            Glide.with(CalendarActivity.this).load(R.drawable.icon_done).into(imgSign);
            textSign.setText("该日学习计划已完成");
            textSign.setTextColor(getColor(R.color.colorMainBlue));
            if (ConfigData.getIsNight())
                cardInfor.setCardBackgroundColor(getColor(R.color.colorBgWhite));
            else
                cardInfor.setCardBackgroundColor(getColor(R.color.colorLittleBlue));
            textDate.setText(myDates.get(0).getYear() + "年" + myDates.get(0).getMonth() + "月" + myDates.get(0).getDate() + "日" + "");
            textWord.setText((myDates.get(0).getWordLearnNumber() + myDates.get(0).getWordReviewNumber()) + "");

            if (myDates.get(0).getRemark() != null) {
                if (!myDates.get(0).getRemark().isEmpty()) {
                    layoutRemark.setVisibility(View.VISIBLE);
                    textRemark.setText(myDates.get(0).getRemark());
                }
            } else {
                layoutRemark.setVisibility(View.GONE);
            }
        }
    }

}
