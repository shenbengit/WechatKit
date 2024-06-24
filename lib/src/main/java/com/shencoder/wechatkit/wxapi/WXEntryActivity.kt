package com.shencoder.wechatkit.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.shencoder.wechatkit.WechatUtils
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * 微信登录、分享
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {

    private companion object {
        private const val TAG = "WXEntryActivity"
    }

    private val wxApi by lazy {
        WXAPIFactory.createWXAPI(this, WechatUtils.wechatAppId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wxApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        wxApi.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {
    }

    override fun onResp(resp: BaseResp) {
        WechatUtils.initWechatConfig.logI(
            TAG,
            "onResp errCode: ${resp.errCode}, errStr: ${resp.errStr}, transaction: ${resp.transaction}, resp: ${resp::class.java}"
        )
        val transaction = resp.transaction ?: ""
        val success = resp.errCode == BaseResp.ErrCode.ERR_OK
        when (resp) {
            is SendAuth.Resp -> {
                // 微信授权登录
                var code: String? = null
                if (success) {
                    code = resp.code
                }
                WechatUtils.dispatchAuthCodeResult(transaction, success, code)
            }

            is SendMessageToWX.Resp -> {
                // 发送消息到微信
                WechatUtils.dispatchSendMessageResult(transaction, success)
            }

            is WXLaunchMiniProgram.Resp -> {
                // 加载小程序
                val extMsg = resp.extMsg // 对应小程序组件 <button open-type="launchApp"> 中的 app-parameter 属性
                WechatUtils.dispatchLaunchMiniProgramResult(transaction, success, extMsg)
            }
        }

        finish()
    }
}