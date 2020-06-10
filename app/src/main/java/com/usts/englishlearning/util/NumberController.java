package com.usts.englishlearning.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NumberController {

    // 得到区间里的一个随机数
    // 两个端点都能取到
    public static int getRandomNumber(int min, int max) {
        if (min != max) {
            Random random = new Random();
            return random.nextInt(max) % (max - min + 1) + min;
        } else return min;
    }

    // 得到区间里的N个随机数
    // 参数n必须大于0
    public static int[] getRandomNumberList(int min, int max, int n) {
        //判断是否已经达到索要输出随机数的个数
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n]; //用于存放结果的数组
        int count = 0;
        while (count < n) {
            int num = getRandomNumber(min, max);
            boolean flag = true;
            for (int j = 0; j < count; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    // 得到区间里的N个随机数
    // 参数n必须大于0
    public static int[] getRandomExceptList(int min, int max, int n, int except) {
        //判断是否已经达到索要输出随机数的个数
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n]; //用于存放结果的数组
        int count = 0;
        while (count < n) {
            int num = getRandomNumber(min, max);
            while (num == except) {
                num = getRandomNumber(min, max);
            }
            boolean flag = true;
            for (int j = 0; j < count; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

}
