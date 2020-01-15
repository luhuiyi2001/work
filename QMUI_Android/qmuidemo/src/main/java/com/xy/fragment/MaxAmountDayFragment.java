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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.utilcode.util.TimeUtils;
import com.google.utilcode.util.ToastUtils;
import com.google.utilcode.util.Utils;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.xy.adapter.CountStatAdapter;
import com.xy.bean.CountStatic;
import com.xy.bean.HkHold;
import com.xy.sever.StaticFmSeverData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link MaxAmountDayFragment} 的使用示例。
 * Created by Kayo on 2016/11/21.
 */
@Widget(widgetClass = MaxAmountDayFragment.class, iconRes = R.mipmap.icon_grid_tip_dialog, name = "沪深股通")
public class MaxAmountDayFragment extends BaseFragment {

    private static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.startDate)
    Button mStartDateBtn;
    @BindView(R.id.endDate) Button mEndDateBtn;
    @BindView(R.id.query) Button mQueryBtn;
    @BindView(R.id.listview)
    QMUIAnimationListView mListView;

    private MyAdapter mAdapter;
    private String mStartDate = "20200101";
    private String mEndDate = "20200131";
    private List<CountStatic> mData = new ArrayList<>();

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.xy_activity_max_amount_day, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initView();
        initListView();

        return root;
    }

    private void initListView() {
//        StaticFmSeverData.loadDataFormServer(mStartDate, mEndDate);
//        doStatic();
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

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    private void initView() {
        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mStartDate = TimeUtils.date2String(startCalendar.getTime(), SDF_DATE);
        mStartDateBtn.setText(String.format(getResources().getString(R.string.start_date), mStartDate));
        mStartDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mStartDate = TimeUtils.date2String(calendar.getTime(), SDF_DATE);
                        mStartDateBtn.setText(String.format(getResources().getString(R.string.start_date), mStartDate));
                    }
                }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });

        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(endCalendar.getTimeInMillis() - 24 * 60 * 60 * 1000);
        mEndDate = TimeUtils.date2String(endCalendar.getTime(), SDF_DATE);

        mEndDateBtn.setText(String.format(getResources().getString(R.string.end_date), mEndDate));
        mEndDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mEndDate = TimeUtils.date2String(calendar.getTime(), SDF_DATE);
                        mEndDateBtn.setText(String.format(getResources().getString(R.string.end_date), mEndDate));
                    }
                }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });

        mQueryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StaticFmSeverData.loadDataFormServer();
                        doStatic(getExchange());
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

    public void doStatic(String exchange) {
        Log.d("luhuiyi", "doStatic [" + mStartDate + " - " + mEndDate + "]");
        List<HkHold> allData = StaticFmSeverData.readFormLocal(exchange, mStartDate, mEndDate);
        if (allData == null || allData.size() == 0) {
            Log.d("luhuiyi", "allData is null.");
            ToastUtils.showLong("readFormLocal[" + mStartDate + " - " + mEndDate + "] is null!");
            return;
        }

        Map<String, List<HkHold>> tsDataMap = StaticFmSeverData.groupByTsCode(allData);
        if (tsDataMap == null || tsDataMap.size() == 0) {
            Log.d("luhuiyi", "tsDataMap is null.");
            ToastUtils.showLong("groupByTsCode[" + mStartDate + " - " + mEndDate + "] is null!");
            return;
        }
        StaticFmSeverData.sortByTsCode(tsDataMap);
        List<CountStatic> countStaticList = StaticFmSeverData.buyTheMostDayNum(tsDataMap);
        mData.clear();
        mData.addAll(countStaticList);
        Log.d("luhuiyi", "End Static [" + mStartDate + " - " + mEndDate + "]");
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

    protected String getExchange() {
        return "CN";
    }

}
