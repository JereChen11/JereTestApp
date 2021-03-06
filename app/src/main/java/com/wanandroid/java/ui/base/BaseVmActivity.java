package com.wanandroid.java.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wanandroid.java.MyApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import kotlin.TypeCastException;

/**
 * @author jere
 */
public abstract class BaseVmActivity<VM extends ViewModel, B extends ViewDataBinding> extends AppCompatActivity {

    public VM viewModel;
    public B dataBinding;

    protected final String TAG = getClass().getName();

    protected abstract void initView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        try {
            Method method = getDataBindingClass().getDeclaredMethod("inflate", LayoutInflater.class);
            dataBinding = (B) method.invoke(null, getLayoutInflater());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        setContentView(dataBinding.getRoot());

        initView();
    }

    private Class<VM> getViewModelClass() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (type == null) {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.Class<VM>");
        } else {
            return (Class<VM>) type;
        }
    }

    private Class<B> getDataBindingClass() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        if (type == null) {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.Class<B>");
        } else {
            return (Class<B>) type;
        }
    }

    public void showToast(String textContent) {
        Toast.makeText(MyApplication.getInstance(), textContent, Toast.LENGTH_SHORT).show();
    }

}
