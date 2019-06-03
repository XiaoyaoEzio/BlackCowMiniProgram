package com.superyc.heiniu.bean;

import java.util.Date;

public class Device {
    private Integer id;

    private String deviceName;

    private String deviceNumber;

    private Integer enable;

    private Integer firstProxyId;

    private Integer secondProxyId;

    private Date creationTime;

    private Date modificationTime;

    private Integer proxyMode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName == null ? null : deviceName.trim();
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber == null ? null : deviceNumber.trim();
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public Integer getFirstProxyId() {
        return firstProxyId;
    }

    public void setFirstProxyId(Integer firstProxyId) {
        this.firstProxyId = firstProxyId;
    }

    public Integer getSecondProxyId() {
        return secondProxyId;
    }

    public void setSecondProxyId(Integer secondProxyId) {
        this.secondProxyId = secondProxyId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Integer getProxyMode() {
        return proxyMode;
    }

    public void setProxyMode(Integer proxyMode) {
        this.proxyMode = proxyMode;
    }
}