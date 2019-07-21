package com.comleoneo.myrestaurantstaffapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.comleoneo.myrestaurantstaffapp.Common.Common;
import com.comleoneo.myrestaurantstaffapp.Model.RestaurantOwnerModel;
import com.comleoneo.myrestaurantstaffapp.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurantstaffapp.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UpdateInformationActivity extends AppCompatActivity {

    private static final String TAG = UpdateInformationActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_update)
    Button btn_udpate;
    @BindView(R.id.edt_user_name)
    EditText edt_user_name;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        toolbar.setTitle("Update Information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_udpate.setOnClickListener(v -> {
            mDialog.show();

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {

                    mCompositeDisposable.add(mIMyRestaurantAPI.updateRestaurantOwnerModel(Common.API_KEY,
                            account.getPhoneNumber().toString(),
                            TextUtils.isEmpty(edt_user_name.getText().toString()) ? "Unknown Name" : edt_user_name.getText().toString(),
                            account.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(updateRestaurantOwnerModel -> {

                                if (updateRestaurantOwnerModel.isSuccess()) {

                                    mCompositeDisposable.add(mIMyRestaurantAPI.getRestaurantOwner(Common.API_KEY,
                                            account.getId())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(restaurantOwnerModel -> {

                                                if (restaurantOwnerModel.isSuccess()) {
                                                    Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                    if (Common.currentRestaurantOwner.isStatus()) {
                                                        startActivity(new Intent(UpdateInformationActivity.this, HomeActivity.class));
                                                        finish();
                                                    }
                                                    else {
                                                        Toast.makeText(UpdateInformationActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                    }

                                                    mDialog.dismiss();
                                                }
                                                else {
                                                    mDialog.dismiss();
                                                    Toast.makeText(UpdateInformationActivity.this, ""+restaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                            }, throwable -> {
                                                mDialog.dismiss();
                                                Toast.makeText(UpdateInformationActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));

                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(UpdateInformationActivity.this, updateRestaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }


                            }, throwable -> {
                                Toast.makeText(UpdateInformationActivity.this, "[UPDATE]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));

                }

                @Override
                public void onError(AccountKitError accountKitError) {
                    Toast.makeText(UpdateInformationActivity.this, "[Account Kit]" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }
}
