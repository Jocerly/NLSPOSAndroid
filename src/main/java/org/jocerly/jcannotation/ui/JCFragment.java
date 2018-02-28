/*
 * Copyright (c) 2014,JCFrameForAndroid Open Source Project,Jocerly.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jocerly.jcannotation.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.SoftReference;


/**
 * Fragment's framework<br>
 * <b>创建时间</b> 2014-3-1 <br>
 * <b>最后修改时间</b> 2014-5-30<br>
 *
 * @author Jocerly
 * @version 1.6
 */
@SuppressLint("NewApi")
public abstract class JCFragment extends Fragment implements View.OnClickListener {

    public static final int WHICH_MSG = 0X37211;

    protected View fragmentRootView;
    private ThreadDataCallBack callback;
    private JCFragmentHandle threadHandle = new JCFragmentHandle(this);

    /**
     * 一个私有回调类，线程中初始化数据完成后的回调
     */
    private interface ThreadDataCallBack {
        void onSuccess();
    }


    private static class JCFragmentHandle extends Handler {
        private final SoftReference<JCFragment> mOuterInstance;

        JCFragmentHandle(JCFragment outer) {
            mOuterInstance = new SoftReference<JCFragment>(outer);
        }

        // 当线程中初始化的数据初始化完成后，调用回调方法
        @Override
        public void handleMessage(android.os.Message msg) {
            JCFragment JCFragment = mOuterInstance.get();
            if (msg.what == WHICH_MSG && JCFragment != null) {
                JCFragment.callback.onSuccess();
            }
        }
    }

    protected abstract View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle);

    /**
     * initialization widget, you should look like parentView.findviewbyid(id);
     * call method
     *
     * @param parentView 根View
     */
    protected void initWidget(View parentView) {
    }

    /**
     * initialization data
     */
    protected void initData() {
    }

    /**
     * 当通过changeFragment()显示时会被调用(类似于onResume)
     */
    public void onChange() {
    }

    /**
     * initialization data. And this method run in background thread, so you
     * shouldn't change ui<br>
     * on initializated, will call threadDataInited();
     */
    protected void initDataFromThread() {
        callback = new ThreadDataCallBack() {
            @Override
            public void onSuccess() {
                threadDataInited();
            }
        };
    }

    /**
     * 如果调用了initDataFromThread()，则当数据初始化完成后将回调该方法。
     */
    protected void threadDataInited() {
    }

    /**
     * widget click method
     */
    protected void widgetClick(View v) {
    }

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentRootView = inflaterView(inflater, container, savedInstanceState);
        AnnotateUtil.initBindView(this, fragmentRootView);
        initData();
        initWidget(fragmentRootView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDataFromThread();
                threadHandle.sendEmptyMessage(WHICH_MSG);
            }
        }).start();
        return fragmentRootView;
    }
}