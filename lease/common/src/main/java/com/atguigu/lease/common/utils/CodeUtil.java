package com.atguigu.lease.common.utils;

import java.util.Random;

public class CodeUtil {

    public static String generateCode(Integer length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
             builder.append(num);

        }
        return builder.toString();
    }
}
