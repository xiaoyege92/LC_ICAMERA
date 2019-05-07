package com.example.administrator.lc_dvr.common.utils.observer;

import com.example.administrator.lc_dvr.common.utils.observer.ObserverListener;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/15
 *   desc   : 被观察者接口
 *  version :
 * </pre>
 */
public interface SubjectListener {
    void add(ObserverListener observerListener);
    void notifyObserver(String content);
    void remove(ObserverListener observerListener);
}
