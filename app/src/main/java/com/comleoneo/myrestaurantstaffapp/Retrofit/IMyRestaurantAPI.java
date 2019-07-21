package com.comleoneo.myrestaurantstaffapp.Retrofit;

import com.comleoneo.myrestaurantstaffapp.Model.MaxOrderModel;
import com.comleoneo.myrestaurantstaffapp.Model.OrderDetailModel;
import com.comleoneo.myrestaurantstaffapp.Model.OrderModel;
import com.comleoneo.myrestaurantstaffapp.Model.RestaurantOwnerModel;
import com.comleoneo.myrestaurantstaffapp.Model.UpdateRestaurantOwnerModel;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IMyRestaurantAPI {

    @GET("restaurantowner")
    Observable<RestaurantOwnerModel> getRestaurantOwner(@Query("key") String key,
                                                        @Query("fbid") String fbid);

    @POST("restaurantowner")
    @FormUrlEncoded
    Observable<UpdateRestaurantOwnerModel> updateRestaurantOwnerModel(@Field("key") String key,
                                                                      @Field("userPhone") String userPhone,
                                                                      @Field("userName") String userName,
                                                                      @Field("fbid") String fbid);

    @GET("orderbyrestaurant")
    Observable<OrderModel> getOrder(@Query("key") String key,
                                    @Query("restaurantId") String restaurantId,
                                    @Query("from") int from,
                                    @Query("to") int to);
    @GET("maxorderbyrestaurant")
    Observable<MaxOrderModel> getMaxOrder(@Query("key") String key,
                                       @Query("restaurantId") String restaurantId);

    @GET("orderdetailbyrestaurant")
    Observable<OrderDetailModel> getOrderDetailModel(@Query("key") String key,
                                                     @Query("orderId") int orderId);
}
