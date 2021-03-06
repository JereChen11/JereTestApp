package com.wanandroid.java.ui.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wanandroid.java.R;
import com.wanandroid.java.data.bean.Article;
import com.wanandroid.java.ui.customview.PullUpRefreshView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author jere
 */
public class ProjectItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ARTICLE_TYPE = 0;
    private static final int BOTTOM_PROMPT_TYPE = 1;
    private ArrayList<Article> articleData;
    private WeakReference<Context> weakReference;
    private boolean isLoadAllArticleData = false;

    public interface AdapterItemClickListener {
        void onPositionClicked(View v, int position);
    }

    public AdapterItemClickListener itemClickListener;

    ProjectItemListAdapter(Context context,
                           ArrayList<Article> articles,
                           AdapterItemClickListener adapterItemClickListener) {
        weakReference = new WeakReference<>(context);
        this.articleData = articles;
        this.itemClickListener = adapterItemClickListener;
    }

    public void setData(ArrayList<Article> articles) {
        this.articleData = articles;
        notifyDataSetChanged();
    }

    public void setIsLoadAllArticleData(boolean isLoadAll) {
        this.isLoadAllArticleData = isLoadAll;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == articleData.size()) {
            return BOTTOM_PROMPT_TYPE;
        }
        return ARTICLE_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == BOTTOM_PROMPT_TYPE) {
            return new BottomPromptViewHolder(
                    layoutInflater.inflate(R.layout.recycler_list_item_view_article_bottom_prompt_view,
                            parent,
                            false));
        }
        return new ProjectArticleViewHolder(layoutInflater.inflate(R.layout.recycle_list_item_view_project_item_list,
                parent,
                false),
                itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ARTICLE_TYPE) {
            ProjectArticleViewHolder projectArticleViewHolder = (ProjectArticleViewHolder) holder;
            Article article = articleData.get(position);
            if (weakReference != null && weakReference.get() != null) {
                Glide.with(weakReference.get())
                        .load(article.getEnvelopePic())
                        .into(projectArticleViewHolder.envelopIv);
                projectArticleViewHolder.titleTv.setText(article.getTitle());
                projectArticleViewHolder.describeContentTv.setText(article.getDesc());
                projectArticleViewHolder.shareDateTv.setText(article.getNiceShareDate());
                projectArticleViewHolder.authorTv.setText(article.getAuthor());
            }
        } else {
            BottomPromptViewHolder bottomPromptViewHolder = (BottomPromptViewHolder) holder;
            if (isLoadAllArticleData) {
                bottomPromptViewHolder.pullUpRefreshView.setProgressBarStatus(View.GONE);
                bottomPromptViewHolder.pullUpRefreshView.setPromptTv("所有文章都已被加载");
            } else {
                bottomPromptViewHolder.pullUpRefreshView.setProgressBarStatus(View.VISIBLE);
                bottomPromptViewHolder.pullUpRefreshView.setPromptTv("加载中");
            }
        }
    }

    @Override
    public int getItemCount() {
        return articleData.size() + 1;
    }

    static class ProjectArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView envelopIv;
        private TextView titleTv;
        private TextView describeContentTv;
        private TextView shareDateTv;
        private TextView authorTv;
        private AdapterItemClickListener adapterItemClickListener;

        ProjectArticleViewHolder(@NonNull View itemView, AdapterItemClickListener adapterItemClickListener) {
            super(itemView);
            envelopIv = itemView.findViewById(R.id.projectItemListEnvelopIv);
            titleTv = itemView.findViewById(R.id.projectItemListTitleTv);
            describeContentTv = itemView.findViewById(R.id.projectItemListDescribeContentTv);
            shareDateTv = itemView.findViewById(R.id.projectItemListShareDateTv);
            authorTv = itemView.findViewById(R.id.projectItemListAuthorTv);

            this.adapterItemClickListener = adapterItemClickListener;

            envelopIv.setOnClickListener(this);
            titleTv.setOnClickListener(this);
            describeContentTv.setOnClickListener(this);
            shareDateTv.setOnClickListener(this);
            authorTv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            adapterItemClickListener.onPositionClicked(v, getAdapterPosition());
        }
    }

    static class BottomPromptViewHolder extends RecyclerView.ViewHolder {
        private PullUpRefreshView pullUpRefreshView;

        public BottomPromptViewHolder(@NonNull View itemView) {
            super(itemView);
            pullUpRefreshView = itemView.findViewById(R.id.articleBottomPullUpRefreshView);
        }
    }

}
