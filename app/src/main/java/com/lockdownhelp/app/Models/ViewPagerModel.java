package com.lockdownhelp.app.Models;

import java.util.List;

public class ViewPagerModel {
    String questionIconUrl,question,selectionType,questionStatus,backgroundColor;
    List<String>responses;

    public ViewPagerModel() {
    }

    public String getQuestionStatus() {
        return questionStatus;
    }

    public void setQuestionStatus(String questionStatus) {
        this.questionStatus = questionStatus;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(String selectionType) {
        this.selectionType = selectionType;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public String getQuestionIconUrl() {
        return questionIconUrl;
    }

    public void setQuestionIconUrl(String questionIconUrl) {
        this.questionIconUrl = questionIconUrl;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
