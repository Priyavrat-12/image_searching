package com.dev.imagesearching.models;

import com.google.gson.annotations.SerializedName;

public class ImagesResponse {

    @SerializedName("success")
    boolean success;

    @SerializedName("status")
    int status;

    @SerializedName("data")
    List<Data> dataList;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }
}
