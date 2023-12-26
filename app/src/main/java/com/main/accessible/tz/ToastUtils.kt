package com.main.accessible.tz

import android.widget.Toast
import com.main.accessible.tz.app.MyApp

object  ToastUtils {

    fun showToast(str : String){
        Toast.makeText(MyApp.instance,str,Toast.LENGTH_SHORT);
    }


}