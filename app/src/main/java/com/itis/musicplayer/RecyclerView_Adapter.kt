package com.itis.musicplayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class RecyclerViewAdapter(list: List<Audio>) : RecyclerView.Adapter<ViewHolder>() {

    internal var list = emptyList<Audio>()

    init {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(v)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.text = list[position].title
    }

    override fun getItemCount(): Int {
        //returns the number of elements the RecyclerView will display
        return list.size
    }

}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var title: TextView = itemView.findViewById(R.id.tv_title)

}