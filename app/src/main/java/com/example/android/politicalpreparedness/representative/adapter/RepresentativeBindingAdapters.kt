package com.example.android.politicalpreparedness.representative.adapter

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.representative.model.Representative

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        Glide.with(view.context)
            .load(uri)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(view)
    }
}

@BindingAdapter("stateValue")
fun Spinner.setNewValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

@BindingAdapter("selectedValueAttrChanged")
fun Spinner.setListener(selectedItemChange: InverseBindingListener) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            selectedItemChange.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

@InverseBindingAdapter(attribute = "stateValue", event = "selectedValueAttrChanged")
fun Spinner.getSelectedValue() = this.selectedItem as String

@Suppress("UNCHECKED_CAST")
inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T> {
    return adapter as ArrayAdapter<T>
}

@BindingAdapter("liveData")
fun setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<Representative>>?) {
    items?.value?.let { itemList ->
        (recyclerView.adapter as RepresentativeListAdapter).submitList(itemList)
    }
}