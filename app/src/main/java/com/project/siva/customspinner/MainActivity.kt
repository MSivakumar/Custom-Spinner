package com.project.siva.customspinner

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinner: CustomSpinner = findViewById<View>(R.id.spinner) as CustomSpinner
        val itemsList = listOf("0","1","2","3","4","5","6","7","8","9")
        spinner.setItems(itemsList)
        spinner.setOnItemSelectedListener(object : CustomSpinner.OnItemSelectedListener<Any> {
            override fun onItemSelected(view: CustomSpinner, position: Int, id: Long, item: Any) {
                Log.e("item selected", item.toString())
            }
        })
    }
}
