package com.github.mehrdadf7.paginationgrid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String token = "4c035feb054741cd8d6c64497ea9a714";
    private RecyclerView rvItems;
    private NewsAdapter newsAdapter;
    private int page = 1;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        rvItems = findViewById(R.id.rv_items);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        rvItems.setLayoutManager(new GridLayoutManager(this, 1));
        newsAdapter = new NewsAdapter(rvItems);
        newsAdapter.endLessScrolled(rvItems);
        getNews(page);
        rvItems.setAdapter(newsAdapter);

        newsAdapter.setOnLoadMoreListener(() -> {
            newsAdapter.showLoading();
            page += 1;
            getNews(page);
        });


    }

    private void getNews(int newsPage) {
        Log.e(TAG, "getNews: " + newsPage);
        apiService.getNews("sports", token, 10, newsPage)
                .enqueue(new Callback<NewsApiResponse>() {
                    @Override
                    public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                        NewsApiResponse newsApiResponse = response.body();
                        if (response.isSuccessful() && newsApiResponse != null) {
                            newsAdapter.addItems(newsApiResponse.getArticles());
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

}
