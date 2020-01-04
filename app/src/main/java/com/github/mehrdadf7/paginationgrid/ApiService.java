package com.github.mehrdadf7.paginationgrid;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("top-headlines")
    Call<NewsApiResponse> getNews(
            @Query("category") String category,
            @Query("apiKey") String token,
            @Query("pageSize") int pageSize,
            @Query("page") int page
    );

}
