package com.jere.test.article.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jere.test.R;
import com.jere.test.article.modle.beanfiles.completeproject.ProjectItemList;
import com.jere.test.article.view.completeproject.CompleteProjectArticleFragment;
import com.jere.test.article.viewmodel.completeproject.ProjectItemListViewModel;
import com.jere.test.databinding.ActivityProjectItemListBinding;
import com.jere.test.util.RecyclerItemClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author jere
 */
public class ProjectItemListActivity extends AppCompatActivity {
    private static final String TAG = "ProjectItemListActivity";

    private ArrayList<ProjectItemList.DataBean.DatasBean> mProjectItemDataList = new ArrayList<>();
    private ActivityProjectItemListBinding mBinding;
    private MyAdapter adapter;
    private int pageNumber = 0;

    private Observer<ProjectItemList> observer = new Observer<ProjectItemList>() {
        @Override
        public void onChanged(@Nullable ProjectItemList projectItemList) {
            if (projectItemList != null) {
                mProjectItemDataList.addAll(projectItemList.getData().getDatas());
                adapter.setData(mProjectItemDataList);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_project_item_list);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_project_item_list);

        final int clickItemProjectId = getIntent().getIntExtra(CompleteProjectArticleFragment.PROJECT_ITEM_ID_KEY, -1);

        final ProjectItemListViewModel projectItemListVm = new ViewModelProvider(this).get(ProjectItemListViewModel.class);
        if (clickItemProjectId > -1) {
            projectItemListVm.setProjectItemListLd(pageNumber, clickItemProjectId);
        }
        adapter = new MyAdapter(ProjectItemListActivity.this, mProjectItemDataList);
        mBinding.projectItemListRcv.setAdapter(adapter);
        projectItemListVm.getProjectItemListLd().observe(this, observer);

        mBinding.projectItemListRcv.addOnItemTouchListener(new RecyclerItemClickListener(this,
                mBinding.projectItemListRcv,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ProjectItemList.DataBean.DatasBean data = mProjectItemDataList.get(position);
                        String link = data.getLink();

                        Intent articleDetailWebViewIntent = new Intent(ProjectItemListActivity.this, ArticleDetailWebViewActivity.class);
                        articleDetailWebViewIntent.putExtra(ArticleDetailWebViewActivity.ARTICLE_DETAIL_WEB_LINK_KEY, link);
                        startActivity(articleDetailWebViewIntent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));

        mBinding.projectItemListRcv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    pageNumber++;
                    projectItemListVm.setProjectItemListLd(pageNumber, clickItemProjectId);
                }
            }
        });
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<ProjectItemList.DataBean.DatasBean> dataList;
        private WeakReference<ProjectItemListActivity> weakReference;

        MyAdapter(ProjectItemListActivity activity, ArrayList<ProjectItemList.DataBean.DatasBean> projectItemList) {
            weakReference = new WeakReference<>(activity);
            this.dataList = projectItemList;
        }

        public void setData(ArrayList<ProjectItemList.DataBean.DatasBean> projectItemList) {
            this.dataList = projectItemList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycle_list_item_view_project_item_list, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            ProjectItemList.DataBean.DatasBean item = dataList.get(i);
            if (weakReference != null && weakReference.get() != null && !weakReference.get().isFinishing()) {
                Glide.with(weakReference.get())
                        .load(item.getEnvelopePic())
                        .into(myViewHolder.envelopIv);
                myViewHolder.titleTv.setText(item.getTitle());
                myViewHolder.describeContentTv.setText(item.getDesc());
                myViewHolder.shareDateTv.setText(item.getNiceShareDate());
                myViewHolder.authorTv.setText(item.getAuthor());
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        static class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView envelopIv;
            private TextView titleTv;
            private TextView describeContentTv;
            private TextView shareDateTv;
            private TextView authorTv;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                envelopIv = itemView.findViewById(R.id.project_item_list_envelop_iv);
                titleTv = itemView.findViewById(R.id.project_item_list_title_tv);
                describeContentTv = itemView.findViewById(R.id.project_item_list_describe_content_tv);
                shareDateTv = itemView.findViewById(R.id.project_item_list_share_date_tv);
                authorTv = itemView.findViewById(R.id.project_item_list_author_tv);
            }
        }
    }
}
