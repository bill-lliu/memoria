package com.example.memoria

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FormViewModel : ViewModel() {
    val postMade: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val currentPosts: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    fun makePost() {
        postMade.value = true
    }

}