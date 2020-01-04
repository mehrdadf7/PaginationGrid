package com.github.mehrdadf7.paginationgrid;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW     = 0;
    private static final int ITEM_PROGRESS = 1;

    private List<NewsDataModel> items = new ArrayList<>();

    private OnLoadMoreListener         onLoadMoreListener;
    private RecyclerView.LayoutManager layoutManager;

    private boolean isLoading;
    private int     totalItemCount, visibleTotalCount, lastVisibleItem;

    public NewsAdapter(RecyclerView recyclerView) {
        this.initRecyclerViewListener(recyclerView);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_VIEW) {
            View binding = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_news_row, parent, false);

            return new ItemViewHolder(binding);
        }

        return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_progress_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.bind(getItem(position));
        } else if (viewHolder instanceof ProgressViewHolder) {
            // When we use the staggered grid layout manager, change span size because show progressView single row
            if (layoutManager != null && layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isLoading);
            }

            ProgressViewHolder progressViewHolder = (ProgressViewHolder) viewHolder;
            if (isLoading) progressViewHolder.show();
            else progressViewHolder.hide();
        }
    }



    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? ITEM_PROGRESS : ITEM_VIEW;
    }

    public void remove(int position) {
        if (this.items.isEmpty()) {
            return;
        }
        this.items.remove(position);
        this.notifyItemRemoved(position);
    }

    public void remove(String... item) {
        if (this.items.isEmpty()) {
            return;
        }
        this.items.removeAll(new ArrayList<>(Arrays.asList(item)));
        this.notifyDataSetChanged();
    }

    public void removeAll() {
        if (this.items.isEmpty()) {
            return;
        }
        this.items.clear();
        this.notifyDataSetChanged();
    }

    public void addItem(NewsDataModel... item) {
        this.hiddenLoading();
        List<NewsDataModel> items = new ArrayList<>(Arrays.asList(item));
        this.items.addAll(items);
        notifyItemRangeChanged(getItemCount() - items.size(), getItemCount());
    }

    public void addItems(List<NewsDataModel> items) {
        this.hiddenLoading();
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void endLessScrolled(RecyclerView recyclerView) {
        initRecyclerViewListener(recyclerView);
    }

    public List<NewsDataModel> getItems() {
        return this.items;
    }


    public void hiddenLoading() {

        if (getItemCount() != 0 && isLoading) {
            this.items.remove(getItemCount() - 1);
            this.notifyItemRemoved(getItemCount());
        }
        isLoading = false;

        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            });
        }

    }


    private void initRecyclerViewListener(RecyclerView recyclerView) {

        if (recyclerView == null) return;
        this.layoutManager = recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (onLoadMoreListener == null) return;
                if (layoutManager == null) return;

                totalItemCount = layoutManager.getItemCount();
                visibleTotalCount = layoutManager.getChildCount();


                if (layoutManager instanceof GridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                } else if (layoutManager instanceof LinearLayoutManager) {
                    lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;

                    int   spanCount     = staggeredGridLayoutManager.getSpanCount();
                    int[] lastPositions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(new int[spanCount]);
                    lastVisibleItem = Math.max(lastPositions[0], lastPositions[1]);
                }


                if (totalItemCount <= visibleTotalCount) {
                    return;
                }

                if (!isLoading && (lastVisibleItem + visibleTotalCount) >= totalItemCount) {
                    onLoadMoreListener.onLoadMore();
                    isLoading = true;
                }
            }
        });
    }

    public NewsDataModel getItem(int position) {
        if (items.isEmpty()) return null;
        return items.get(position);
    }

    public void showLoading() {
        this.items.add(null);
        final int index = items.size() - 1;
        this.notifyItemInserted(index);
        this.isLoading = true;
        handledShowProgressViewRow(index);
    }

    private void handledShowProgressViewRow(final int index) {
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
            final int         spanCount         = gridLayoutManager.getSpanCount();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isLoading && position == index && items.get(index) == null) {
                        return spanCount;
                    }
                    return 1;
                }
            });
        }
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView newsImage;
        private AppCompatTextView newsTitle, newsDescription, newsPublishAt;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage       = itemView.findViewById(R.id.iv_news_image);
            newsTitle       = itemView.findViewById(R.id.tv_news_title);
            newsDescription = itemView.findViewById(R.id.tv_news_description);
            newsPublishAt   = itemView.findViewById(R.id.tv_news_publishAt);
        }

        public void bind(NewsDataModel item) {
            if (item.getPublishedAt() != null) {
                newsPublishAt.setText(item.getPublishedAt());
            }
            if (item.getTitle() != null) {
                newsTitle.setText(item.getTitle());
            }
            if (item.getDescription() != null) {
                newsDescription.setText(item.getDescription());
            }
            if (item.getUrlToImage() != null) {
                newsImage.setImageURI(Uri.parse(item.getUrlToImage()));
            }
        }
    }


    class ProgressViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout progressParent;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            progressParent = itemView.findViewById(R.id.progress_parent);
        }

        public void show() {
            progressParent.setVisibility(View.VISIBLE);
        }


        public void hide() {
            progressParent.setVisibility(View.GONE);
        }
    }

}
