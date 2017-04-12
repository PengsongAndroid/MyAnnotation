package com.ps.api;

import android.app.Activity;
import android.view.View;

/**
 * Created by PS on 2017/3/31.
 */

public class ViewBinder {

    private final static String SUFFIX = "$$ViewBinder";

    public static void bind(Activity activity){
        ViewBind proxyActivity = findProxyActivity(activity);
        proxyActivity.inject(activity, activity);
    }

    public static void injectView(Object object, View view)
    {
        ViewBind proxyActivity = findProxyActivity(object);
        proxyActivity.inject(object, view);
    }

    private static ViewBind findProxyActivity(Object activity){
        try {
            Class clazz = activity.getClass();
            Class viewBindClazz = Class.forName(clazz.getName() + SUFFIX);
            return (ViewBind) viewBindClazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s, something error when compiler.", activity.getClass().getSimpleName() + SUFFIX));
    }

}
