package com.project.siva.customspinner

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import kotlin.math.max

class CustomSpinner(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {

    private var onNothingSelectedListener: OnNothingSelectedListener? = null
    private var onItemSelectedListener: OnItemSelectedListener<Any>? = null
    private var adapter: CustomSpinnerBaseAdapter? = null
    private var popupWindow: PopupWindow? = null
    private var listView: ListView? = null
    private var arrowDrawable: Drawable? = null
    private var hideArrow = false
    private var nothingSelected = false
    private var popupWindowMaxHeight = 0
    private var popupWindowHeight = 0
    private var selectedIndex = 0
    private var backgroundColor1 = 0
    private var backgroundSelector = 0
    private var arrowColor = 0
    private var arrowColorDisabled = 0
    private var textColor1 = 0
    private var hintColor = 0
    private var popupPaddingTop = 0
    private var popupPaddingLeft = 0
    private var popupPaddingBottom = 0
    private var popupPaddingRight = 0
    private var hintText: String? = null

    init {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CustomSpinner)
        val defaultColor = textColors.defaultColor
        val rtl = (context!!.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL)

        val paddingLeft: Int
        val paddingTop: Int
        val paddingRight: Int
        val paddingBottom: Int
        val defaultPaddingLeft = 12
        val defaultPaddingTop = 12
        val defaultPaddingRight = 12
        val defaultPaddingBottom = 12
        val defaultPopupPaddingLeft = 24
        val defaultPopupPaddingTop = 12
        val defaultPopupPaddingRight = 24
        val defaultPopupPaddingBottom = 12

        backgroundColor1 = typedArray?.getColor(R.styleable.CustomSpinner_cs_background_color, Color.WHITE)!!
        backgroundSelector = typedArray.getResourceId(R.styleable.CustomSpinner_cs_background_selector, 0)
        textColor1 = typedArray.getColor(R.styleable.CustomSpinner_cs_text_color, defaultColor)
        hintColor = typedArray.getColor(R.styleable.CustomSpinner_cs_hint_color, defaultColor)
        arrowColor = typedArray.getColor(R.styleable.CustomSpinner_cs_arrow_tint, textColor1)
        hideArrow = typedArray.getBoolean(R.styleable.CustomSpinner_cs_hide_arrow, false)
        hintText = if (typedArray.getString(R.styleable.CustomSpinner_cs_hint) == null) "" else typedArray.getString(R.styleable.CustomSpinner_cs_hint)
        popupWindowMaxHeight = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_dropdown_max_height, 0)
        popupWindowHeight = typedArray.getLayoutDimension(R.styleable.CustomSpinner_cs_dropdown_height, WindowManager.LayoutParams.WRAP_CONTENT)
        paddingTop = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_padding_top, defaultPaddingTop)
        paddingLeft = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_padding_left, defaultPaddingLeft)
        paddingBottom = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_padding_bottom, defaultPaddingBottom)
        paddingRight = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_padding_right, defaultPaddingRight)
        popupPaddingTop = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_popup_padding_top, defaultPopupPaddingTop)
        popupPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_popup_padding_left, defaultPopupPaddingLeft)
        popupPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_popup_padding_bottom, defaultPopupPaddingBottom)
        popupPaddingRight = typedArray.getDimensionPixelSize(R.styleable.CustomSpinner_cs_popup_padding_right, defaultPopupPaddingRight)
        val factor = 0.8f
        val red = ((Color.red(arrowColor) * (1 - factor) / 255 + factor) * 255).toInt()
        val green = ((Color.green(arrowColor) * (1 - factor) / 255 + factor) * 255).toInt()
        val blue = ((Color.blue(arrowColor) * (1 - factor) / 255 + factor) * 255).toInt()
        arrowColorDisabled = Color.argb(Color.alpha(arrowColor), red, green, blue)

        nothingSelected = true
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
        isClickable = true
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        setBackgroundResource(R.drawable.ms__selector)
        if (rtl) {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            textDirection = View.TEXT_DIRECTION_RTL
        }

        if (!hideArrow) {
            arrowDrawable = context.getDrawable(R.drawable.ms__arrow)
            val drawables = compoundDrawables
            if (rtl) {
                drawables[0] = arrowDrawable
            } else {
                drawables[2] = arrowDrawable
            }
            setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
        }
        listView = ListView(context)
        listView!!.id = id
        listView!!.divider = null
        listView!!.itemsCanFocus = true
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, p, id ->
            var position = p
            if (position >= selectedIndex && position < adapter?.count!! && adapter?.getItems()?.size != 1 && TextUtils.isEmpty(hintText)) {
                position++
            }
            selectedIndex = position
            nothingSelected = false
            val item: Any = adapter?.get(position)!!
            adapter?.notifyItemSelected(position)
            setTextColor(textColor1)
            text = item.toString()
            collapse()
            onItemSelectedListener?.onItemSelected(this, position, id, item)
        }

        popupWindow = PopupWindow(context)
        popupWindow?.contentView = listView
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow?.elevation = 16f
            popupWindow?.setBackgroundDrawable(context.getDrawable(R.drawable.ms__drawable))
        } else {
            popupWindow?.setBackgroundDrawable(context.getDrawable(R.drawable.ms__drop_down_shadow))
        }
        if (backgroundColor1 != Color.WHITE) {
            setBackgroundColor(backgroundColor1)
        } else if (backgroundSelector != 0) {
            setBackgroundResource(backgroundSelector)
        }
        if (textColor1 != defaultColor) {
            setTextColor(textColor1)
        }

        popupWindow!!.setOnDismissListener {
            if (nothingSelected && onNothingSelectedListener != null) {
                onNothingSelectedListener!!.onNothingSelected(this)
            }
            if (!hideArrow) {
                animateArrow(false)
            }
        }
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        popupWindow?.width = MeasureSpec.getSize(widthMeasureSpec)
        popupWindow?.height = calculatePopupWindowHeight()
        if (adapter != null) {
            val currentText = text
            var longestItem = currentText.toString()
            for (i in 0 until adapter?.count!!) {
                val itemText: String = adapter?.getItemText(i)!!
                if (itemText.length > longestItem.length) {
                    longestItem = itemText
                }
            }
            text = longestItem
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            text = currentText
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (isEnabled && isClickable) {
                if (!popupWindow!!.isShowing) {
                    expand()
                } else {
                    collapse()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor1 = color
        val background = background
        if (background is StateListDrawable) {
            try {
                val getStateDrawable =
                    StateListDrawable::class.java.getDeclaredMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
                if (!getStateDrawable.isAccessible) getStateDrawable.isAccessible = true
                val colors = intArrayOf(Color.argb(Color.alpha(color), max((Color.red(color) * 0.85f).toInt(), 0), max((Color.green(color) * 0.85f).toInt(), 0), max((Color.blue(color) * 0.85f).toInt(), 0)))
                for (i in colors.indices) {
                    val drawable = getStateDrawable.invoke(background, i) as ColorDrawable
                    drawable.color = colors[i]
                }
            } catch (e: Exception) {
                Log.e("CustomSpinner", "Error setting background color", e)
            }
        }
    }

    override fun setTextColor(color: Int) {
        textColor1 = color
        if (adapter != null) {
            adapter?.setTextColor(textColor1)
            adapter?.notifyDataSetChanged()
        }
        super.setTextColor(color)
    }

    private fun setHintColor(color: Int) {
        hintColor = color
        super.setTextColor(color)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("state", super.onSaveInstanceState())
        bundle.putInt("selected_index", selectedIndex)
        bundle.putBoolean("nothing_selected", nothingSelected)
        if (popupWindow != null) {
            bundle.putBoolean("is_popup_showing", popupWindow!!.isShowing)
            collapse()
        } else {
            bundle.putBoolean("is_popup_showing", false)
        }
        return bundle
    }

    override fun onRestoreInstanceState(savedState1: Parcelable?) {
        var savedState = savedState1
        if (savedState is Bundle) {
            val bundle = savedState
            selectedIndex = bundle.getInt("selected_index")
            nothingSelected = bundle.getBoolean("nothing_selected")
            if (adapter != null) {
                if (nothingSelected && !TextUtils.isEmpty(hintText)) {
                    setHintColor(hintColor)
                    text = hintText
                } else {
                    setTextColor(textColor1)
                    text = adapter?.get(selectedIndex).toString()
                }
                adapter?.notifyItemSelected(selectedIndex)
            }
            if (bundle.getBoolean("is_popup_showing")) {
                if (popupWindow != null) { // Post the show request into the looper to avoid bad token exception
                    post { expand() }
                }
            }
            savedState = bundle.getParcelable("state")
        }
        super.onRestoreInstanceState(savedState)
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<Any>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    fun setOnNothingSelectedListener(onNothingSelectedListener: OnNothingSelectedListener) {
        this.onNothingSelectedListener = onNothingSelectedListener
    }

    private fun calculatePopupWindowHeight(): Int {
        if (adapter == null) {
            return WindowManager.LayoutParams.WRAP_CONTENT
        }
        val itemHeight = 48
        val listViewHeight: Float = (adapter?.count!! * itemHeight).toFloat()
        if (popupWindowMaxHeight > 0 && listViewHeight > popupWindowMaxHeight) {
            return popupWindowMaxHeight
        } else if (popupWindowHeight != WindowManager.LayoutParams.MATCH_PARENT && popupWindowHeight != WindowManager.LayoutParams.WRAP_CONTENT && popupWindowHeight <= listViewHeight
        ) {
            return popupWindowHeight
        } else if (listViewHeight == 0f && adapter?.getItems()?.size == 1) {
            return itemHeight
        }
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    private fun canShowPopup(): Boolean {
        if (getActivity() == null || getActivity()!!.isFinishing) {
            return false
        }
        return isLaidOut
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
    private fun animateArrow(shouldRotateUp: Boolean) {
        val start = if (shouldRotateUp) 0 else 10000
        val end = if (shouldRotateUp) 10000 else 0
        val animator: ObjectAnimator = ObjectAnimator.ofInt(arrowDrawable!!, "level", start, end)
        animator.start()
    }

    private fun expand() {
        if (canShowPopup()) {
            if (!hideArrow) {
                animateArrow(true)
            }
            nothingSelected = true
            popupWindow!!.showAsDropDown(this)
        }
    }

    private fun collapse() {
        if (!hideArrow) {
            animateArrow(false)
        }
        popupWindow!!.dismiss()
    }

    fun setItems(items: List<Any>) {
        adapter = CustomSpinnerAdapter(context, items)
            .setPopupPadding(popupPaddingLeft, popupPaddingTop, popupPaddingRight, popupPaddingBottom)
            ?.setBackgroundSelector(backgroundSelector)
            ?.setTextColor(textColor1)
        setAdapterInternal(adapter!!)
    }

    private fun setAdapterInternal(adapter: CustomSpinnerBaseAdapter) {
        val shouldResetPopupHeight = listView!!.adapter != null
        adapter.setHintEnabled(!TextUtils.isEmpty(hintText))
        listView!!.adapter = adapter
        if (selectedIndex >= adapter.count) {
            selectedIndex = 0
        }
        if (adapter.getItems().isNotEmpty()) {
            if (nothingSelected && !TextUtils.isEmpty(hintText)) {
                text = hintText
                setHintColor(hintColor)
            } else {
                setTextColor(textColor1)
                text = adapter[selectedIndex].toString()
            }
        } else {
            text = ""
        }
        if (shouldResetPopupHeight) {
            popupWindow!!.height = calculatePopupWindowHeight()
        }
    }

    interface OnItemSelectedListener<Any> {
        fun onItemSelected(view: CustomSpinner, position: Int, id: Long, item: Any)
    }

    interface OnNothingSelectedListener {
        fun onNothingSelected(spinner: CustomSpinner)
    }
}