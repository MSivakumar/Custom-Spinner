package com.project.siva.customspinner

import android.content.Context

class CustomSpinnerAdapter(context: Context, private val itemsList: List<Any>) : CustomSpinnerBaseAdapter(context) {
    override fun getItem(position: Int): Any {
        return if (isHintEnabled()) {
            itemsList[position]
        } else if (position >= getSelectedIndex() && itemsList.size != 1) {
            itemsList[position + 1]
        } else {
            itemsList[position]
        }
    }

    override fun getCount(): Int {
        val size: Int = itemsList.size
        return if (size == 1 || isHintEnabled()) size else size - 1
    }

    override fun get(position: Int): Any {
        return itemsList[position]
    }

    override fun getItems(): List<Any> {
        return itemsList
    }

}