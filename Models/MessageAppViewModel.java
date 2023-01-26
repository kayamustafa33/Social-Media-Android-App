package com.mustafa.message_app.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MessageAppViewModel extends ViewModel {

    private MutableLiveData<String> username = new MutableLiveData<>();
    private MutableLiveData<String> requestName = new MutableLiveData<>();

    public void setUsername(String name){
        username.setValue(name);
    }

    public LiveData<String> getUsername(){
        return username;
    }

    public void setRequestName(String name){
        requestName.setValue(name);
    }

    public LiveData<String> getRequestName(){
        return requestName;
    }
}
