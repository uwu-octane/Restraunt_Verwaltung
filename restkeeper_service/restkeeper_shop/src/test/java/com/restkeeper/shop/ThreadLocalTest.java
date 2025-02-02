package com.restkeeper.shop;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalTest {

    static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>(){
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }
    };

    public static void main(String[] args) {
        threadLocal.get().put("demo", "01");
        outInfo();

        new Thread(new Runnable() {
            @Override
            public void run() {
                threadLocal.get().put("demo", "02");
                outInfo();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                threadLocal.get().put("demo", "03");
                outInfo();
            }
        }).start();
    }

    private static void outInfo() {
        System.out.println(Thread.currentThread().getName() + " " + threadLocal.get());

    }

}
