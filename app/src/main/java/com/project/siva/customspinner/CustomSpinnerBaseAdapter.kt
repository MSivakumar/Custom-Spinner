package com.project.siva.customspinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

abstract class CustomSpinnerBaseAdapter(private val context : Context) : BaseAdapter() {
    private var selectedIndex = 0
    private var textColor = 0
    private var backgroundSelector = 0
    private var popupPaddingTop = 0
    private var popupPaddingLeft = 0
    private var popupPaddingBottom = 0
    private var popupPaddingRight = 0
    private var isHintEnabled = false

    override fun getView(position: Int, convertView1: View?, parent: ViewGroup?): View? {
        var convertView = convertView1
        val textView: TextView
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.ms__list_item, parent, false)
            textView = convertView!!.findViewById<View>(R.id.tv_tinted_spinner) as TextView
            textView.setTextColor(textColor)
            textView.setPadding(
                popupPaddingLeft,
                popupPaddingTop,
                popupPaddingRight,
                popupPaddingBottom
            )
            if (backgroundSelector != 0) {
                textView.setBackgroundResource(backgroundSelector)
            }
            val config = context.resources.configuration
            if (config.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                textView.textDirection = View.TEXT_DIRECTION_RTL
            }
            convertView.tag = ViewHolder(textView)
        } else {
            textView = (convertView.tag as ViewHolder).textView
        }
        textView.text = getItemText(position)
        return convertView
    }

    open fun getItemText(position: Int): String? {
        return getItem(position).toString()
    }

    open fun getSelectedIndex(): Int {
        return selectedIndex
    }

    open fun notifyItemSelected(index: Int) {
        selectedIndex = index
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    abstract override fun getItem(position: Int): Any

    abstract override fun getCount(): Int

    abstract operator fun get(position: Int): Any

    abstract fun getItems(): List<Any>

    open fun setHintEnabled(isHintEnabled: Boolean) {
        this.isHintEnabled = isHintEnabled
    }

    open fun isHintEnabled(): Boolean {
        return isHintEnabled
    }

    open fun setTextColor(@ColorInt textColor: Int): CustomSpinnerBaseAdapter? {
        this.textColor = textColor
        return this
    }

    open fun setBackgroundSelector(@DrawableRes backgroundSelector: Int): CustomSpinnerBaseAdapter {
        this.backgroundSelector = backgroundSelector
        return this
    }

    open fun setPopupPadding(left: Int, top: Int, right: Int, bottom: Int): CustomSpinnerBaseAdapter? {
        popupPaddingLeft = left
        popupPaddingTop = top
        popupPaddingRight = right
        popupPaddingBottom = bottom
        return this
    }

    private class ViewHolder(val textView: TextView)
}