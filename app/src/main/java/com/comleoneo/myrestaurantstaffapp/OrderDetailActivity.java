package com.comleoneo.myrestaurantstaffapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.comleoneo.myrestaurantstaffapp.Adapter.MyOrderDetailAdapter;
import com.comleoneo.myrestaurantstaffapp.Common.Common;
import com.comleoneo.myrestaurantstaffapp.Model.Status;
import com.comleoneo.myrestaurantstaffapp.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurantstaffapp.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = OrderDetailActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @BindView(R.id.tv_order_number)
    TextView tv_order_number;
    @BindView(R.id.spinner_status)
    AppCompatSpinner spinner_status;
    @BindView(R.id.rv_order_detail)
    RecyclerView rv_order_detail;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.order_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_order_detail.setLayoutManager(layoutManager);
        rv_order_detail.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        tv_order_number.setText(new StringBuilder("Order Number: #").append(Common.currentOrder.getOrderId()));

        initStatusSpinner();

        loadOrderDetail();
    }

    private void loadOrderDetail() {
        Log.d(TAG, "loadOrderDetail: called!!");
        mDialog.show();

        mCompositeDisposable.add(mIMyRestaurantAPI.getOrderDetailModel(Common.API_KEY,
                Common.currentOrder.getOrderId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(orderDetailModel -> {

            if (orderDetailModel.isSuccess()) {
                if (orderDetailModel.getResult().size() > 0) {
                    MyOrderDetailAdapter adapter = new MyOrderDetailAdapter(this, orderDetailModel.getResult());
                    rv_order_detail.setAdapter(adapter);
                }
            }

            mDialog.dismiss();

        }, throwable -> {
            mDialog.dismiss();
            Toast.makeText(this, "[GET ORDER DETAIL]", Toast.LENGTH_SHORT).show();
        }));
    }

    private void initStatusSpinner() {
        Log.d(TAG, "initStatusSpinner: called!!");
        List<Status> statusList = new ArrayList<Status>();

        statusList.add(new Status(0, "Placed"));
        statusList.add(new Status(1, "Shipping"));
        statusList.add(new Status(2, "Shipped"));
        statusList.add(new Status(-1, "Cancelled"));

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_status.setAdapter(adapter);
        spinner_status.setSelection(Common.convertStatusToIndex(Common.currentOrder.getOrderStatus()));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        else if (id == R.id.action_save) {
            Toast.makeText(this, "[SAVE TEST]", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
