package com.example.administrator.testone;

/**
 * Created by Administrator on 2018/7/22.
 */

public class Order {
    private String beginTime;
    private String startPoint;
    private String endPoint;
    private String orderStation;

    public Order() {
    }

    public Order(String beginTime, String startPoint, String endPoint, String orderStation) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.beginTime = beginTime;
        this.orderStation = orderStation;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public void setOrderStation(String orderStation) {
        this.orderStation = orderStation;
    }



    public void setEndPoint(String endPoint) {

        this.endPoint = endPoint;
    }

    public void setStartPoint(String startPoint) {

        this.startPoint = startPoint;
    }

    public String getOrderStation() {

        return orderStation;
    }


    public String getEndPoint() {

        return endPoint;
    }

    public String getStartPoint() {

        return startPoint;
    }
}
