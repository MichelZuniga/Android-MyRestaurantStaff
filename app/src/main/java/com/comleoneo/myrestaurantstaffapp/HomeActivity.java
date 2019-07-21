package com.comleoneo.myrestaurantstaffapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurantstaffapp.Adapter.MyOrderAdapter;
import com.comleoneo.myrestaurantstaffapp.Common.Common;
import com.comleoneo.myrestaurantstaffapp.Interface.ILoadMore;
import com.comleoneo.myrestaurantstaffapp.Model.Order;
import com.comleoneo.myrestaurantstaffapp.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurantstaffapp.Retrofit.RetrofitClient;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ILoadMore {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @BindView(R.id.rv_order)
    RecyclerView rv_order;

    private LayoutAnimationController mLayoutAnimationController;
    private int maxData = 0;
    private MyOrderAdapter mAdapter;
    private List<Order> mOrderList;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        initView();

        getMaxOrder();
    }

    private void getMaxOrder() {
        Log.d(TAG, "getMaxOrder: called!!");
        mDialog.show();

        mCompositeDisposable.add(mIMyRestaurantAPI.getMaxOrder(Common.API_KEY,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxOrderModel -> {

                    if (maxOrderModel.isSuccess()) {
                        maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                        mDialog.dismiss();

                        getAllOrder(0, 10);
                    }

                }, throwable -> {
                    mDialog.dismiss();
                    Toast.makeText(this, "[GET MAX ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void getAllOrder(int from, int to) {
        Log.d(TAG, "getAllOrder: called!!");
        mDialog.show();

        mCompositeDisposable.add(mIMyRestaurantAPI.getOrder(Common.API_KEY,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()), from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {

                    if (orderModel.isSuccess()) {
                        if (orderModel.getResult().size() > 0) {
                            if (mAdapter == null) {
                                mOrderList = new ArrayList<>();
                                mOrderList = orderModel.getResult();
                                mAdapter = new MyOrderAdapter(this, mOrderList, rv_order);
                                mAdapter.setILoadMore(this);

                                rv_order.setAdapter(mAdapter);
                                rv_order.setLayoutAnimation(mLayoutAnimationController);
                            }
                            else {
                                mOrderList.remove(mOrderList.size() - 1);
                                mOrderList = orderModel.getResult();
                                mAdapter.addItem(mOrderList);
                            }
                        }

                        mDialog.dismiss();

                    }

                }, throwable -> {
                    mDialog.dismiss();
                    Toast.makeText(this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_order.setLayoutManager(layoutManager);
        rv_order.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        mLayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Restaurant Order");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLoadMore() {
        if (mAdapter.getItemCount() < maxData) {
            // Add null to show loading progressbar
            mOrderList.add(null);
            mAdapter.notifyItemInserted(mOrderList.size()-1);

            // Get next 10 item
            getAllOrder(mAdapter.getItemCount()+1, mAdapter.getItemCount()+10);

            mAdapter.notifyDataSetChanged();
            mAdapter.setLoaded();
        }
        else {
            Toast.makeText(this, "Max Data to load", Toast.LENGTH_SHORT).show();
        }
    }
}
