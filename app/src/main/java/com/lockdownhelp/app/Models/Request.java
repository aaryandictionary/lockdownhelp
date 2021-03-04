package com.lockdownhelp.app.Models;

import java.util.List;

public class Request {
    String requestId,foodPacketCount,prescriptionUrl,requestMessage,requestStatus,requestStatusHeading,requestStatusMessage,requestTitle,requestViewType,shelterCount,userId,userLocation,userName,volunteerId,volunteerLocation,shelterPersonCount,requestReason,requestConfirmMessage,categoryIconUrl;
    long requestTimeStamp,requestCancelTime;
    List<RequestItemListType> requestItemListTypeList;

    List<ViewPagerModel>viewPagerModels;

    List<SelectedImageModel>selectedImageModels;

    RequestLocationDetails requestLocationDetails;

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public long getRequestCancelTime() {
        return requestCancelTime;
    }

    public void setRequestCancelTime(long requestCancelTime) {
        this.requestCancelTime = requestCancelTime;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    public Request() {
    }

    public RequestLocationDetails getRequestLocationDetails() {
        return requestLocationDetails;
    }

    public void setRequestLocationDetails(RequestLocationDetails requestLocationDetails) {
        this.requestLocationDetails = requestLocationDetails;
    }

    public String getRequestConfirmMessage() {
        return requestConfirmMessage;
    }

    public void setRequestConfirmMessage(String requestConfirmMessage) {
        this.requestConfirmMessage = requestConfirmMessage;
    }

    public String getShelterPersonCount() {
        return shelterPersonCount;
    }

    public void setShelterPersonCount(String shelterPersonCount) {
        this.shelterPersonCount = shelterPersonCount;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public List<SelectedImageModel> getSelectedImageModels() {
        return selectedImageModels;
    }

    public void setSelectedImageModels(List<SelectedImageModel> selectedImageModels) {
        this.selectedImageModels = selectedImageModels;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFoodPacketCount() {
        return foodPacketCount;
    }

    public void setFoodPacketCount(String foodPacketCount) {
        this.foodPacketCount = foodPacketCount;
    }

    public String getPrescriptionUrl() {
        return prescriptionUrl;
    }

    public void setPrescriptionUrl(String prescriptionUrl) {
        this.prescriptionUrl = prescriptionUrl;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestStatusHeading() {
        return requestStatusHeading;
    }

    public void setRequestStatusHeading(String requestStatusHeading) {
        this.requestStatusHeading = requestStatusHeading;
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

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public String getRequestViewType() {
        return requestViewType;
    }

    public void setRequestViewType(String requestViewType) {
        this.requestViewType = requestViewType;
    }

    public String getShelterCount() {
        return shelterCount;
    }

    public void setShelterCount(String shelterCount) {
        this.shelterCount = shelterCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getVolunteerLocation() {
        return volunteerLocation;
    }

    public void setVolunteerLocation(String volunteerLocation) {
        this.volunteerLocation = volunteerLocation;
    }

    public List<RequestItemListType> getRequestItemListTypeList() {
        return requestItemListTypeList;
    }

    public void setRequestItemListTypeList(List<RequestItemListType> requestItemListTypeList) {
        this.requestItemListTypeList = requestItemListTypeList;
    }

    public List<ViewPagerModel> getViewPagerModels() {
        return viewPagerModels;
    }

    public void setViewPagerModels(List<ViewPagerModel> viewPagerModels) {
        this.viewPagerModels = viewPagerModels;
    }
}
