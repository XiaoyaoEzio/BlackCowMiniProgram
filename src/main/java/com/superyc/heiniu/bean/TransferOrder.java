package com.superyc.heiniu.bean;

import java.util.Date;

public class TransferOrder {
    private Integer id;

    private Integer proxyId;

    private String orderNumber;

    private Date creationTime;

    private Date finishedTime;

    private Integer amount;

    private String wxPaymentNumber;

    private Integer commissionAmount;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProxyId() {
        return proxyId;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber == null ? null : orderNumber.trim();
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Date finishedTime) {
        this.finishedTime = finishedTime;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getWxPaymentNumber() {
        return wxPaymentNumber;
    }

    public void setWxPaymentNumber(String wxPaymentNumber) {
        this.wxPaymentNumber = wxPaymentNumber == null ? null : wxPaymentNumber.trim();
    }

    public Integer getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(Integer commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}