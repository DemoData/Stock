package com.hitales.common.support;

import java.util.HashMap;
import java.util.Map;

public class ProgressCount {
    private static Map<String, Integer> progressMap = new HashMap<>();

    public static void putProgress(String key, Integer count) {
        synchronized (ProgressCount.class){
            Integer value = progressMap.get(key);
            if (value == null || count > value) {
                progressMap.put(key, count);
            }
        }
    }

    public static Integer getProgress(String key) {
        return progressMap.get(key);
    }
}
