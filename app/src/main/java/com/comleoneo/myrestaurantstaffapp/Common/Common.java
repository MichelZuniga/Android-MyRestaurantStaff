package com.comleoneo.myrestaurantstaffapp.Common;

import com.comleoneo.myrestaurantstaffapp.Model.RestaurantOwner;

public class Common {

    public static final String API_RESTAURANT_ENDPOINT = "http://192.168.0.13:3000/";
    public static final String API_KEY = "1234";

    public static RestaurantOwner currentRestaurantOwner;

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }
}
