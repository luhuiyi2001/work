package com.xy.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.utilcode.util.TimeUtils;
import com.google.utilcode.util.ToastUtils;
import com.google.utilcode.util.Utils;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.adaptor.QDSimpleAdapter;
import com.qmuiteam.qmuidemo.base.BaseActivity;
import com.qmuiteam.qmuidemo.fragment.lab.QDAnimationListViewFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchTestFragment;
import com.suntek.commonlibrary.utils.PermissionUtils;
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

import static com.qmuiteam.qmuidemo.QDApplication.getContext;

public class HKHoldActivity extends BaseActivity {
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 100001;

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.startDate) Button mStartDateBtn;
    @BindView(R.id.endDate) Button mEndDateBtn;
    @BindView(R.id.query) Button mQueryBtn;
    @BindView(R.id.listview) QMUIAnimationListView mListView;

    private MyAdapter mAdapter;
    private String mStartDate = "20191101";
    private String mEndDate = "20191130";
    private List<CountStatic> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = LayoutInflater.from(this).inflate(R.layout.xy_activity_hkhold, null);
        ButterKnife.bind(this, root);
        initTopBar();
        initView();
        initListView();
        setContentView(root);
        initPermission();
    }

    /**
     * 跳转到权限设置页面返回后再次检查
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        initPermission();
    }

    private void initListView() {
//        StaticFmSeverData.loadDataFormServer(mStartDate, mEndDate);
//        doStatic();
        mAdapter = new MyAdapter(getContext(), mData);
        mListView.setAdapter(mAdapter);
    }
    private void initTopBar() {
        mTopBar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_color_theme_4));
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            }
        });
        mTopBar.setTitle("Arch Test");
        QDArchTestFragment.injectEntrance(mTopBar);
    }

    private void initView() {
        mStartDateBtn.setText(String.format(getResources().getString(R.string.start_date), mStartDate));
        mStartDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(HKHoldActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mStartDate = TimeUtils.date2String(calendar.getTime(), new SimpleDateFormat("yyyyMMdd", Locale.getDefault()));
                        mStartDateBtn.setText(String.format(getResources().getString(R.string.start_date), mStartDate));
                    }
                }, 2019, 10, 1);
                datePicker.show();
            }
        });
        mEndDateBtn.setText(String.format(getResources().getString(R.string.end_date), mEndDate));
        mEndDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker=new DatePickerDialog(HKHoldActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mEndDate = TimeUtils.date2String(calendar.getTime(), new SimpleDateFormat("yyyyMMdd", Locale.getDefault()));
                        mEndDateBtn.setText(String.format(getResources().getString(R.string.end_date), mEndDate));
                    }
                }, 2019, 10, 30);
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
                        doStatic("CN");
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

    /**
     * 检查，申请权限
     */
    private void initPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has){
                PermissionUtils.requestPermissions(this,PERMISSION_REQUEST_CODE,
                        BASIC_PERMISSIONS);
            }
        }
    }

    /**
     * 权限授予结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            PermissionUtils.dealPermissionResult(HKHoldActivity.this, permissions, grantResults,
                    new PermissionUtils.RequestPermissionCallBack() {
                        @Override
                        public void onGrant(String... permissions) {
                            ToastUtils.showLong("PERMISSION Grant");
                        }

                        @Override
                        public void onDenied(String... permissions) {
                            ToastUtils.showLong("PERMISSION Denied");
                            finish();
                        }

                        @Override
                        public void onDeniedAndNeverAsk(String... permissions) {
                            ToastUtils.showLong("PERMISSION onDeniedAndNeverAsk");
                        }
                    });
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

