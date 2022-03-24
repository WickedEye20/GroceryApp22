package com.example.groceryapp.models;

public class ModelOrderUser {
    String orderId, orderTime, orderCost, orderBy, OrderFrom, orderStatus, paymentMode, Delivery;

    public ModelOrderUser() {
    }

    public String getDelivery() {
        return Delivery;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getorderFrom() {
        return OrderFrom;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public ModelOrderUser(String orderId, String orderTime, String orderCost, String orderBy, String orderFrom, String orderStatus, String paymentMode, String Delivery) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.orderCost = orderCost;
        this.orderBy = orderBy;
        this.OrderFrom = orderFrom;
        this.orderStatus = orderStatus;
        this.paymentMode = paymentMode;
        this.Delivery = Delivery;
    }
}
