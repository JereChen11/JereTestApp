package com.jere.test.home.model;

import com.google.gson.Gson;
import com.jere.test.article.modle.api.AbstractRetrofitCallback;
import com.jere.test.article.modle.api.ApiService;
import com.jere.test.article.modle.api.ApiWrapper;
import com.jere.test.article.modle.api.GetWebDataListener;
import com.jere.test.home.model.beanfiles.HomeBannerListBean;

/**
 * @author jere
 */
public class HomeRepository {
    private static HomeRepository instance;

    public static HomeRepository newInstance() {
        if (instance == null) {
            synchronized (HomeRepository.class) {
                if (instance == null) {
                    instance = new HomeRepository();
                }
            }
        }
        return instance;
    }

    public void getHomeBannerList(final GetWebDataListener listener) {
        ApiService apiService = ApiWrapper.getRetrofitInstance().create(ApiService.class);
        apiService.getHomeBannerList().enqueue(new AbstractRetrofitCallback() {
            @Override
            public void getSuccessful(String responseBody) {
                Gson gson = new Gson();
                HomeBannerListBean homeBannerListBean = gson.fromJson(responseBody, HomeBannerListBean.class);
                listener.getDataSuccess(homeBannerListBean);
            }

            @Override
            public void getFailed(String failedMsg) {
                listener.getDataFailed(failedMsg);
            }
        });
    }

}