package com.example.hm_soft_test;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DataFactory extends ViewModelProvider.NewInstanceFactory {

    @NonNull
    private final Application application;

    public DataFactory(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == DataViewModel.class) {
            return (T) new DataViewModel(application);
        }
        return null;
    }
}
