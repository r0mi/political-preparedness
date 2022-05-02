package com.example.android.politicalpreparedness.election.adapter

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.fadeIn
import com.example.android.politicalpreparedness.fadeOut
import com.example.android.politicalpreparedness.network.models.Election

@BindingAdapter("liveData")
fun setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<Election>>?) {
    items?.value?.let { itemList ->
        (recyclerView.adapter as ElectionListAdapter).submitList(itemList)
    }
}

@BindingAdapter("fadeVisible")
fun setFadeVisible(view: View, visible: Boolean? = true) {
    if (view.tag == null) {
        view.tag = true
        view.visibility = if (visible == true) View.VISIBLE else View.GONE
    } else {
        view.animate().cancel()
        if (visible == true) {
            if (view.visibility == View.GONE)
                view.fadeIn()
        } else {
            if (view.visibility == View.VISIBLE)
                view.fadeOut()
        }
    }
}
