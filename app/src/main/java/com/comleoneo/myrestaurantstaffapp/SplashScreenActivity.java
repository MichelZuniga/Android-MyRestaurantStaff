package com.comleoneo.myrestaurantstaffapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.comleoneo.myrestaurantstaffapp.Common.Common;
import com.comleoneo.myrestaurantstaffapp.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurantstaffapp.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started!!");

        init();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {

                                mDialog.show();

                                mCompositeDisposable.add(mIMyRestaurantAPI.getRestaurantOwner(Common.API_KEY, 
                                        account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(restaurantOwnerModel -> {

                                    if (restaurantOwnerModel.isSuccess()) {
                                        // If user already in database
                                        // Check permission of user
                                        Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                        if (Common.currentRestaurantOwner.isStatus()) {
                                            startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(SplashScreenActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                    else {
                                        // If user is new
                                        startActivity(new Intent(SplashScreenActivity.this, UpdateInformationActivity.class));
                                        finish();
                                    }

                                    mDialog.dismiss();

                                }, throwable -> {
                                    Toast.makeText(SplashScreenActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));

                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {
                                // If not logged, just ask login
                                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                finish();
                            }
                        });

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SplashScreenActivity.this, "You must enable this permission to user our app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();

    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }
}
