package io.github.pn11.submit2slack

import android.content.Context
import android.view.Gravity
import android.widget.Toast


object Utils {

    fun showToast(context: Context, message: String, x: Int = 0, y: Int = 0) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, x, y)
        toast.show()
    }
}

