package com.digital_tent.cow_marks.product_spinner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.digital_tent.cow_marks.R
import com.squareup.picasso.Picasso

class ProductSpinnerAdapter(context: Context, private val list: List<ProductSpinnerItem>):
    ArrayAdapter<ProductSpinnerItem>(context, 0, list) {

    private var layoutInflater = LayoutInflater.from(context)
    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = layoutInflater.inflate(R.layout.product_item, null, true)
        return view(view, position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        if (cv == null) {
            cv = layoutInflater.inflate(R.layout.product_item, parent, false)
        }
        return view(cv!!, position)
    }

    private fun view(view: View, position: Int): View {
        val products: ProductSpinnerItem = getItem(position) ?: return view
        val productName = view.findViewById<TextView>(R.id.spinner_item_text)
        val productImageView = view.findViewById<ImageView>(R.id.spinner_item_imageView)

        productName.text = products.product
        Picasso.get().load("http://172.16.16.239/static/images/${products.imageUrl}.jpg")
            .resize(75,75)
            .into(productImageView)

        return view
    }
}