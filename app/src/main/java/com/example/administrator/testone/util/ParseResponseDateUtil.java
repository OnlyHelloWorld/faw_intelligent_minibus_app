package com.example.administrator.testone.util;

import android.text.TextUtils;

import com.example.administrator.testone.Order;
import com.example.administrator.testone.OrderManageActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/7/23.
 */

   /*
    * 解析服务器返回的数据(json格式)
    * */
public class ParseResponseDateUtil {
    /*
    * 解析服务器返回的订单数据
    * */
    public static boolean handleOrderResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allOrder = new JSONArray(response);
                for (int i = 0; i < allOrder.length(); i++) {
                    JSONObject orderObject = allOrder.getJSONObject(i);
                    Order order = new Order();
                    order.setBeginTime(orderObject.getString("beginTime"));
                    order.setStartPoint(orderObject.getString("startPoint"));
                    order.setEndPoint(orderObject.getString("endPoint"));
                    order.setOrderStation(orderObject.getString("orderStation"));
                    //加入到orderList中,在订单管理界面显示,显示完毕时清空orderList
                    OrderManageActivity.orderList.add(order);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}

