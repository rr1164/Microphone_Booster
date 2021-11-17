package com.Mic.microphonebooster;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecorderViewModel extends ViewModel {
    public MutableLiveData<String> recordNameMutableLiveData;
    public void setrecordname(String recordname) {
        this.recordNameMutableLiveData.setValue(recordname);
    }
    public LiveData<String> getrecordname() {
        if (recordNameMutableLiveData == null) {
            recordNameMutableLiveData = new MutableLiveData<>();
        }
        return recordNameMutableLiveData;
    }
    private MutableLiveData<Boolean> recordstatus;
    public void setrecordstatus(boolean status) {
        this.recordstatus.setValue(status);
    }
    public LiveData<Boolean> getrecordstatus() {
        if (recordstatus == null) {
            recordstatus = new MutableLiveData<>();
        }
        return recordstatus;
    }
    public RecorderViewModel()
    {
        recordNameMutableLiveData = new MutableLiveData<>();
    }
}
