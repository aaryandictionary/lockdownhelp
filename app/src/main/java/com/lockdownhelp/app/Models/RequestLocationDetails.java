package com.lockdownhelp.app.Models;

public class RequestLocationDetails {
    UserLocationDetails userLocationDetails;
    AdminLocationDetails adminLocationDetails;
    Double distance;

    public RequestLocationDetails() {
    }

    public UserLocationDetails getUserLocationDetails() {
        return userLocationDetails;
    }

    public void setUserLocationDetails(UserLocationDetails userLocationDetails) {
        this.userLocationDetails = userLocationDetails;
    }

    public AdminLocationDetails getAdminLocationDetails() {
        return adminLocationDetails;
    }

    public void setAdminLocationDetails(AdminLocationDetails adminLocationDetails) {
        this.adminLocationDetails = adminLocationDetails;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public static class UserLocationDetails{
        Double lat,lng;

        public UserLocationDetails() {
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }

    public static class AdminLocationDetails{
        Double lat,lng;
        String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public AdminLocationDetails() {
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}


