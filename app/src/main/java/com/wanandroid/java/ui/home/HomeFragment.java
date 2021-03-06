package com.wanandroid.java.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wanandroid.java.R;
import com.wanandroid.java.data.bean.Article;
import com.wanandroid.java.data.bean.ArticleData;
import com.wanandroid.java.data.bean.HomeBanner;
import com.wanandroid.java.databinding.FragmentHomeBinding;
import com.wanandroid.java.ui.adapter.ArticleListViewAdapter;
import com.wanandroid.java.ui.base.BaseVmFragment;
import com.wanandroid.java.ui.login.RegisterLoginActivity;
import com.wanandroid.java.ui.web.ArticleDetailWebViewActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author jere
 */
public class HomeFragment extends BaseVmFragment<HomeViewModel, FragmentHomeBinding> {
    private static final String TAG = "HomeFragment";

    private ViewPager2 mBannerVp2;
    private MyBannerVpAdapter mBannerVpAdapter;
    private View[] indicateViews;
    private ArrayList<HomeBanner> homeBannerDataList = new ArrayList<>();
    private BannerHandler mBannerHandler;
    private ScheduledExecutorService mBannerScheduledExecutorService;
    private final ArrayList<Article> homeArticles = new ArrayList<>();
    private ArticleListViewAdapter articleListViewAdapter;
    private int homeArticlePage = 0;

    private final Observer<List<HomeBanner>> bannerListDataObserver = new Observer<List<HomeBanner>>() {
        @Override
        public void onChanged(List<HomeBanner> homeBannerList) {
            if (homeBannerList != null) {
                homeBannerDataList = new ArrayList<>();
                homeBannerDataList.addAll(homeBannerList);
                mBannerVpAdapter = new MyBannerVpAdapter(HomeFragment.this, homeBannerDataList);
                mBannerVpAdapter.notifyDataSetChanged();
                mBannerVp2.setAdapter(mBannerVpAdapter);
                mBannerVp2.setCurrentItem(1);
            }
        }
    };

    private final Observer<ArticleData> articleDataObserver = new Observer<ArticleData>() {
        @Override
        public void onChanged(ArticleData articleData) {
            if (articleData != null) {
                homeArticles.addAll(articleData.getArticles());
                articleListViewAdapter.setData(homeArticles);
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    protected void initView() {
        articleListViewAdapter = new ArticleListViewAdapter(homeArticles,
                new ArticleListViewAdapter.AdapterItemClickListener() {
                    @Override
                    public void onPositionClicked(View v, int position) {
                        String link = homeArticles.get(position).getLink();
                        Intent intent = new Intent(getActivity(), ArticleDetailWebViewActivity.class);
                        intent.putExtra(ArticleDetailWebViewActivity.ARTICLE_DETAIL_WEB_LINK_KEY, link);
                        startActivity(intent);
                    }

                    @Override
                    public void onLongClicked(View v, int position) {
                        Toast.makeText(getContext(), "long clcik position : " + position, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void unLoginBeforeCollect() {
                        startActivity(new Intent(getActivity(), RegisterLoginActivity.class));
                    }
                });
        dataBinding.homeArticleListRecycleView.setAdapter(articleListViewAdapter);


        viewModel.getHomeBannerListLd().observe(getViewLifecycleOwner(), bannerListDataObserver);
//        homeVm.setHomeBannerListLd();
        viewModel.setRxJava2HomeBannerListLd();
        viewModel.getHomeArticleDataLd().observe(getViewLifecycleOwner(), articleDataObserver);
        viewModel.setHomeArticleDataLd(homeArticlePage);

        dataBinding.homeArticleListRecycleView.setNestedScrollingEnabled(false);
        dataBinding.homeNsv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!v.canScrollVertically(1)) {
                    homeArticlePage++;
                    viewModel.setHomeArticleDataLd(homeArticlePage);
                }
            }
        });


        initBannerVew();
        mBannerHandler = new BannerHandler(this);
        startAutoLoopBanner();
    }

    private void initBannerVew() {
        mBannerVp2 = dataBinding.homeBannerVp2;
        indicateViews = new View[]{
                dataBinding.firstIndicateView,
                dataBinding.secondIndicateView,
                dataBinding.thirdIndicateView,
                dataBinding.fourthIndicateView};
        mBannerVpAdapter = new MyBannerVpAdapter(this, homeBannerDataList);
        mBannerVp2.setAdapter(mBannerVpAdapter);
        mBannerVp2.setCurrentItem(1);
        mBannerVp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //hard code, Banner只有4张图
                if (position == 5) {
                    mBannerVp2.setCurrentItem(1, false);
                } else if (position == 0) {
                    mBannerVp2.setCurrentItem(4, false);
                }
                position = toRealPosition(position, 4);
                for (int i = 0; i < 4; i++) {
                    if (position == i) {
                        indicateViews[i].setBackgroundResource(R.drawable.banner_navigation_point_highlight_shape);
                    } else {
                        indicateViews[i].setBackgroundResource(R.drawable.banner_navigation_point_gray_shape);
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    mBannerScheduledExecutorService.shutdownNow();
                }
            }
        });
    }

    private void startAutoLoopBanner() {
        mBannerScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mBannerScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                mBannerHandler.sendMessage(msg);
            }
        }, 2, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: mBannerScheduledExecutorService.isShutdown() = " + mBannerScheduledExecutorService.isShutdown());
        if (!mBannerScheduledExecutorService.isShutdown()) {
            mBannerScheduledExecutorService.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static class BannerHandler extends Handler {
        private WeakReference<HomeFragment> weakReference;

        BannerHandler(HomeFragment homeFragment) {
            weakReference = new WeakReference<>(homeFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HomeFragment homeFragment = weakReference.get();
            if (msg.what == 1) {
                if (homeFragment != null && !homeFragment.isHidden()) {
                    int currentItem = homeFragment.mBannerVp2.getCurrentItem();
                    if (currentItem == 4) {
                        homeFragment.mBannerVp2.setCurrentItem(1);
                    } else {
                        homeFragment.mBannerVp2.setCurrentItem(currentItem + 1);
                    }
                }
            }
        }
    }

    public int toRealPosition(int position, int realCount) {
        int realPosition = 0;
        if (realCount > 1) {
            realPosition = (position - 1) % realCount;
        }
        if (realPosition < 0) {
            realPosition += realCount;
        }
        return realPosition;
    }

    class MyBannerVpAdapter extends RecyclerView.Adapter<MyBannerVpAdapter.MyViewHolder> {
        private final ArrayList<HomeBanner> bannerDataList;
        private final WeakReference<HomeFragment> weakReference;

        MyBannerVpAdapter(HomeFragment fragment, ArrayList<HomeBanner> dataList) {
            this.weakReference = new WeakReference<>(fragment);
            this.bannerDataList = dataList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_view_page_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            HomeBanner homeBanner = bannerDataList.get(toRealPosition(position, getRealCount()));
            String imageUrl = homeBanner.getImagePath();
            final HomeFragment homeFragment = weakReference.get();
            if (!TextUtils.isEmpty(imageUrl) && homeFragment != null
                    && !homeFragment.isHidden()
                    && !TextUtils.isEmpty(imageUrl)) {
                Glide.with(homeFragment).load(imageUrl).into(holder.bannerItemIv);
            }

            final String articleDetailUrl = homeBanner.getUrl();
            holder.bannerItemIv.setOnClickListener(v -> {
                Log.e(TAG, "onClick: bannerItemIv");
                Intent intent = new Intent(homeFragment.getActivity(), ArticleDetailWebViewActivity.class);
                intent.putExtra(ArticleDetailWebViewActivity.ARTICLE_DETAIL_WEB_LINK_KEY, articleDetailUrl);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            if (bannerDataList.size() > 0) {
                //为了实现Banner循环轮播，需要额外多两张图片，分别放置列表首尾。
                return bannerDataList.size() + 2;
            }
            return bannerDataList.size();
        }

        public int getRealCount() {
            return bannerDataList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView bannerItemIv;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                bannerItemIv = itemView.findViewById(R.id.banner_item_iv);
            }
        }
    }
}
