package com.example.memoria

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FormViewModel : ViewModel() {
    val postMade: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun makePost(){
        postMade.value = true
    }

}