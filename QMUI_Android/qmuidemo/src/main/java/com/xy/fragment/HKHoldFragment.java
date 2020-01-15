/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xy.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.google.utilcode.util.TimeUtils;
import com.google.utilcode.util.ToastUtils;
import com.google.utilcode.util.Utils;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchTestFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.xy.activity.HKHoldActivity;
import com.xy.adapter.CountStatAdapter;
import com.xy.bean.CountStatic;
import com.xy.bean.HkHold;
import com.xy.common.TRuntime;
import com.xy.sever.StaticFmSeverData;
import com.xy.util.TUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.qmuiteam.qmuidemo.QDApplication.getContext;

/**
 * {@link HKHoldFragment} 的使用示例。
 * Created by Kayo on 2016/11/21.
 */
@Widget(name = "沪深股通", iconRes = R.mipmap.icon_grid_tip_dialog)
public class HKHoldFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.startDate)
    Button mStartDateBtn;
    @BindView(R.id.endDate) Button mEndDateBtn;
    @BindView(R.id.query) Button mQueryBtn;
    @BindView(R.id.listview)
    QMUIAnimationListView mListView;
    private MyAdapter mAdapter;
    private List<CountStatic> mData = new ArrayList<>();

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.xy_activity_hkhold, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initView();
        initListView();

        return root;
    }

    private void initListView() {
        mAdapter = new MyAdapter(getContext(), mData);
        mListView.setAdapter(mAdapter);
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
        mTopBar.setTitle(TUtils.getExchageName(TRuntime.getExchage()) /*+ " ( " + startDate + " ~ " + endDate + " )"*/);

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_about, R.id.topbar_right_about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HKHoldSettingFragment fragment = new HKHoldSettingFragment();
                startFragment(fragment);
            }
        });
    }

    private void initView() {
        mStartDateBtn.setText(String.format(getResources().getString(R.string.start_date), TimeUtils.millis2String(TRuntime.getStartTime(), TUtils.SDF_DATE)));
        mEndDateBtn.setText(String.format(getResources().getString(R.string.end_date), TimeUtils.millis2String(TRuntime.getEndTime(), TUtils.SDF_DATE)));
        mQueryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StaticFmSeverData.loadDataFormServer();
                        doStatic();
                        Utils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                mTipDialog.dismiss();
                            }
                        });
                    }
                }).start();
                Utils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTipDialog("Loading from server, and stat data");
                    }
                });
            }
        });

    }

    public void doStatic() {
        String exchange = TUtils.getExchageTypeName(TRuntime.getExchage());
        String startDate = TimeUtils.millis2String(TRuntime.getStartTime(), TUtils.SDF_DATE);
        String endDate = TimeUtils.millis2String(TRuntime.getEndTime(), TUtils.SDF_DATE);
        Log.d("luhuiyi", "doStatic [" + startDate + " - " + endDate + "]");
        List<HkHold> allData = StaticFmSeverData.readFormLocal(exchange, startDate, endDate);
        if (allData == null || allData.size() == 0) {
            Log.d("luhuiyi", "allData is null.");
            ToastUtils.showLong("readFormLocal[" + startDate + " - " + endDate + "] is null!");
            return;
        }

        Map<String, List<HkHold>> tsDataMap = StaticFmSeverData.groupByTsCode(allData);
        if (tsDataMap == null || tsDataMap.size() == 0) {
            Log.d("luhuiyi", "tsDataMap is null.");
            ToastUtils.showLong("groupByTsCode[" + startDate + " - " + endDate + "] is null!");
            return;
        }
        StaticFmSeverData.sortByTsCode(tsDataMap);
        List<CountStatic> countStaticList = StaticFmSeverData.buyTheMostDayNum(tsDataMap);
        mData.clear();
        mData.addAll(countStaticList);
        Log.d("luhuiyi", "End Static [" + startDate + " - " + endDate + "]");
    }

    private static class MyAdapter extends CountStatAdapter {
        public MyAdapter(Context context, List<CountStatic> data) {
            super(context, data);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private QMUITipDialog mTipDialog;
    private void showTipDialog(String text) {
        if (mTipDialog != null && mTipDialog.isShowing()) {
            mTipDialog.dismiss();
        }
        mTipDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(text)
                .create();
        mTipDialog.show();
    }

}
