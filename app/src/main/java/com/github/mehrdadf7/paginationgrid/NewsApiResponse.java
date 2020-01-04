package com.github.mehrdadf7.paginationgrid;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NewsApiResponse {

    @SerializedName("totalResults")
    @Expose
    private int totalResults;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("articles")
    @Expose
    private ArrayList<NewsDataModel> articles;

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<NewsDataModel> getArticles() {
        return articles;
    }

    public void setArticles(ArrayList<NewsDataModel> articles) {
        this.articles = articles;
    }
}
