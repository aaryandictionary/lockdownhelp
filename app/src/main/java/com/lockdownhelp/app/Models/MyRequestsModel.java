package com.lockdownhelp.app.Models;

public class MyRequestsModel {
    String requestId,requestStatus,requestTitle,requestStatusMessage,categoryIconUrl,requestViewType;
    long requestTimeStamp;

    public MyRequestsModel() {
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestViewType() {
        return requestViewType;
    }

    public void setRequestViewType(String requestViewType) {
        this.requestViewType = requestViewType;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public String getRequestStatusMessage() {
        return requestStatusMessage;
    }

    public void setRequestStatusMessage(String requestStatusMessage) {
        this.requestStatusMessage = requestStatusMessage;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }
}
