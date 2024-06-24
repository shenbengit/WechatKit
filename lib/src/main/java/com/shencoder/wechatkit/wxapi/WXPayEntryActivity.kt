package com.shencoder.wechatkit.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.shencoder.wechatkit.WechatUtils
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * 微信支付
 */
class WXPayEntryActivity : Activity(), IWXAPIEventHandler {

    private companion object {
        private const val TAG = "WXPayEntryActivity"
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
        val success = resp.errCode == BaseResp.ErrCode.ERR_OK
        val transaction = resp.transaction ?: ""
        when (resp) {
            is PayResp -> {
                // 微信支付
                WechatUtils.dispatchPayResult(transaction, success)
            }
        }

        finish()
    }
}