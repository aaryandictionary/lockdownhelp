package com.lockdownhelp.app.Models;

import android.net.Uri;

public class SelectedImageModel implements Cloneable {
    String fileId,fileName,fileUrl,fileUploadTime,fileExtension;
    Uri uri;
    long fileSize;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public SelectedImageModel() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileUploadTime() {
        return fileUploadTime;
    }

    public void setFileUploadTime(String fileUploadTime) {
        this.fileUploadTime = fileUploadTime;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public SelectedImageModel clone() throws CloneNotSupportedException
    {
        SelectedImageModel selectedImageModel = (SelectedImageModel) super.clone();
        selectedImageModel.setUri(null);
        return selectedImageModel;
    }
}
