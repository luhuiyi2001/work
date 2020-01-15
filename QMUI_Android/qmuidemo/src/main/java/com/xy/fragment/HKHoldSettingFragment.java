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
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.utilcode.util.TimeUtils;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.xy.common.TRuntime;
import com.xy.util.TUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIGroupListView} 的使用示例。
 * Created by Kayo on 2016/11/21.
 */

@Widget(name = "设置", iconRes = R.mipmap.icon_grid_group_list_view)
public class HKHoldSettingFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.groupListView)
    QMUIGroupListView mGroupListView;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initGroupListView();

        return root;
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

    private void initGroupListView() {
        //---------------------------- Start Date ----------------------------
        final QMUICommonListItemView startDateItem = mGroupListView.createItemView("开始日期");
        startDateItem.setOrientation(QMUICommonListItemView.VERTICAL);
        startDateItem.setDetailText(TimeUtils.millis2String(TRuntime.getStartTime(), TUtils.SDF_CN_DATE));
        startDateItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(TRuntime.getStartTime());
        View.OnClickListener startDateOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        TRuntime.setStartTime(calendar.getTimeInMillis());
                        startDateItem.setDetailText(TimeUtils.millis2String(TRuntime.getStartTime(), TUtils.SDF_CN_DATE));
                    }
                }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        };

        //---------------------------- End Date ----------------------------
        final QMUICommonListItemView endDateItem = mGroupListView.createItemView("结束日期");
        endDateItem.setOrientation(QMUICommonListItemView.VERTICAL);
        endDateItem.setDetailText(TimeUtils.millis2String(TRuntime.getEndTime(), TUtils.SDF_CN_DATE));
        endDateItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(TRuntime.getEndTime());
        View.OnClickListener endDateOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        TRuntime.setEndTime(calendar.getTimeInMillis());
                        endDateItem.setDetailText(TimeUtils.millis2String(TRuntime.getEndTime(), TUtils.SDF_CN_DATE));
                    }
                }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        };

        //---------------------------- Exchange ----------------------------
        final QMUICommonListItemView exchangeItem = mGroupListView.createItemView("交易所");
        exchangeItem.setOrientation(QMUICommonListItemView.VERTICAL);
        exchangeItem.setDetailText(TUtils.getExchageName(TRuntime.getExchage()));
        exchangeItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        View.OnClickListener exchangeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"沪深股通", "沪股通", "深股通", "创业板"};
                new QMUIDialog.CheckableDialogBuilder(getActivity())
                        .setCheckedIndex(TRuntime.getExchage())
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TRuntime.setExchage(which);
                                exchangeItem.setDetailText(TUtils.getExchageName(TRuntime.getExchage()));
                                dialog.dismiss();
                            }
                        })
                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
            }
        };


        int size = QMUIDisplayHelper.dp2px(getContext(), 20);
        QMUIGroupListView.newSection(getContext())
                .setTitle("设置港股通参数")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(startDateItem, startDateOnClickListener)
                .addItemView(endDateItem, endDateOnClickListener)
                .addItemView(exchangeItem, exchangeOnClickListener)
                .addTo(mGroupListView);

    }

}
