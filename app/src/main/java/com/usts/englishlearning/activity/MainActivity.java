package com.usts.englishlearning.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.index.FragmentMe;
import com.usts.englishlearning.activity.index.FragmentReview;
import com.usts.englishlearning.activity.index.FragmentWord;
import com.usts.englishlearning.util.ActivityCollector;

public class MainActivity extends BaseActivity {

    private Fragment fragWord, fragReview, fragMe;

    private Fragment[] fragments;

    //用于记录上个选择的Fragment
    public static int lastFragment;

    private BottomNavigationView bottomNavigationView;

    private LinearLayout linearLayout;

    private static final String TAG = "MainActivity";

    public static boolean needRefresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");

        init();

        if (needRefresh) {

            TranslateAnimation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f
            );
            animation.setDuration(2000);
            //bottomNavigationView.startAnimation(animation);
        }

        initFragment();

    }

    // 初始化控件
    private void init() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        linearLayout = findViewById(R.id.linear_frag_container);
    }

    // 初始化initFragment
    private void initFragment() {
        fragWord = new FragmentWord();
        fragReview = new FragmentReview();
        fragMe = new FragmentMe();
        fragments = new Fragment[]{fragWord, fragReview, fragMe};
        switch (lastFragment) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.linear_frag_container, fragWord).show(fragWord).commit();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.linear_frag_container, fragReview).show(fragReview).commit();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.linear_frag_container, fragMe).show(fragMe).commit();
                break;
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bnavigation_word:
                        if (lastFragment != 0) {
                            switchFragment(lastFragment, 0);
                            lastFragment = 0;
                        }
                        return true;
                    case R.id.bnavigation_review:
                        if (lastFragment != 1) {
                            switchFragment(lastFragment, 1);
                            lastFragment = 1;
                        }
                        return true;
                    case R.id.bnavigation_me:
                        if (lastFragment != 2) {
                            switchFragment(lastFragment, 2);
                            lastFragment = 2;
                        }
                        return true;
                }
                return true;
            }
        });
    }

    private void switchFragment(int lastIndex, int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //隐藏上个Fragment
        transaction.hide(fragments[lastIndex]);
        if (fragments[index].isAdded() == false) {
            transaction.add(R.id.linear_frag_container, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示")
                .setMessage("今天不再背单词了吗？")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        needRefresh = true;
                        ActivityCollector.finishAll();
                    }
                })
                .setNegativeButton("再看看", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
