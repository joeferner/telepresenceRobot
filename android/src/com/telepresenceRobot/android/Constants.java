package com.telepresenceRobot.android;

public class Constants {
    private static final String LOG = "telepresenceRobot";

    public static String getLogTag(Class clazz) {
        return LOG + "-" + clazz.getName();
    }
}
