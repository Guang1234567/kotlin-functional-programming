package com.functional.programming

import android.app.Application
import android.content.Context
import android.util.Log

import androidx.multidex.MultiDex

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class DemoApp : Application() {

    companion object {

        const val TAG = "DemoApp"

        val processName: String? by lazy {
            try {
                val file = File("/proc/" + android.os.Process.myPid() + "/" + "cmdline")
                val mBufferedReader = BufferedReader(FileReader(file))
                val processName = mBufferedReader.readLine().trim { it <= ' ' }
                mBufferedReader.close()
                processName
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }

        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "current processName = $processName")

        /*
        if (!TextUtils.isEmpty(processName) && processName.endsWith(":navi")) {//判断进程名，保证只有主进程运行
            Log.w(TAG,"current processName = " + processName);
        }
        */
    }
}