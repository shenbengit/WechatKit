package com.shencoder.wechatkitdemo

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.shencoder.wechatkit.WechatUtils


/**
 *
 * @author Shenben
 * @date 2024/6/24 10:01
 * @description
 * @since
 */
class App : Application() {

    private companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()

        WechatUtils.initConfig(object : WechatUtils.InitWechatConfig {
            override val application: Application
                get() = this@App

            override val appId: String
                get() = "xxxxx"

            override val appTag: String
                get() = getString(R.string.app_name)

            override fun onWechatNotInstalled() {
                Log.w(TAG, "onWechatNotInstalled")
            }

            override fun logI(tag: String, message: String) {
                Log.i(tag, message)
            }

            override fun logD(tag: String, message: String) {
                Log.d(tag, message)
            }

            override fun logW(tag: String, message: String) {
                Log.w(tag, message)
            }

            override fun logE(tag: String, message: String) {
                Log.e(tag, message)
            }

            override fun toast(message: String) {
                Toast.makeText(this@App, message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}