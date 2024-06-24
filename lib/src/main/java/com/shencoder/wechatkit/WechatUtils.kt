package com.shencoder.wechatkit

import android.app.Application
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage.IMediaObject
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.utils.ILog
import com.tencent.mm.opensdk.utils.Log
import java.lang.ref.WeakReference

/**
 * 微信sdk使用工具类
 * 使用这些方法应在用户同意隐私政策之后
 *
 * [sdk](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Access_Guide/Android.html)
 *
 * @date    2023/10/24 20:05
 *
 */
object WechatUtils {

    private const val TAG = "WechatUtils"

    private val onSendMessageResultCallbackMap: MutableMap<String, WeakReference<OnSendMessageResultCallback>> =
        mutableMapOf()
    private val onAuthCodeResultCallbackMap: MutableMap<String, WeakReference<OnAuthCodeResultCallback>> =
        mutableMapOf()
    private val onPayResultCallbackMap: MutableMap<String, WeakReference<OnPayResultCallback>> =
        mutableMapOf()
    private val onLaunchMiniProgramResultCallbackMap: MutableMap<String, WeakReference<OnLaunchMiniProgramResultCallback>> =
        mutableMapOf()

    private var _initWechatConfig: InitWechatConfig? = null
    val initWechatConfig: InitWechatConfig
        get() {
            val config = _initWechatConfig
            checkNotNull(config) {
                "you must be call initConfig() first."
            }
            return config
        }

    @JvmStatic
    val wechatAppId: String
        get() = initWechatConfig.appId

    private val wxApi by lazy {
        WXAPIFactory.createWXAPI(initWechatConfig.application, wechatAppId)
    }

    /**
     * 初始化配置
     * 越早调用越好
     * 推荐在[Application.onCreate]
     *
     * @param config
     */
    @JvmStatic
    fun initConfig(config: InitWechatConfig) {
        _initWechatConfig = config
        Log.setLogImpl(object : ILog {
            override fun v(tag: String, message: String) {
                config.logD(tag, message)
            }

            override fun d(tag: String, message: String) {
                config.logD(tag, message)
            }

            override fun i(tag: String, message: String) {
                config.logI(tag, message)
            }

            override fun w(tag: String, message: String) {
                config.logW(tag, message)
            }

            override fun e(tag: String, message: String) {
                config.logE(tag, message)
            }
        })
    }

    /**
     * 注册app
     * 再用户同意隐私协议后才能调用
     */
    @JvmStatic
    fun registerApp() {
        val result = wxApi.registerApp(wechatAppId)
        val api = wxApi.wxAppSupportAPI
        initWechatConfig.logI(TAG, "registerApp: result: $result, supportApi: $api")
    }

    /**
     * 微信app是否安装
     */
    @JvmStatic
    val isWXAppInstalled: Boolean
        get() = wxApi.isWXAppInstalled

    /**
     * 分享网页
     *
     * @param url
     * @param title
     * @param description
     * @param thumbData 图片的二进制数据，bitmap转成byteArray
     * @param scene
     * @param callback
     * @return 校验参数是否合法
     */
    @JvmStatic
    @JvmOverloads
    fun wxShareWeb(
        url: String,
        title: String,
        description: String,
        thumbData: ByteArray? = null,
        scene: WechatScene = WechatScene.Session,
        callback: OnSendMessageResultCallback
    ): Boolean {
        if (scene == WechatScene.Timeline) {
            if (wxApi.wxAppSupportAPI < Build.TIMELINE_SUPPORTED_SDK_INT) {
                // 当前微信版本不支持分享到朋友圈
                initWechatConfig.logW(
                    TAG,
                    "wxShareWeb to Timeline is failed, because wechat version too low"
                )
                return false
            }
        }
        val webPage = WXWebpageObject()
        webPage.webpageUrl = url
        return sendMessage(
            webPage,
            title,
            description,
            buildTransaction("WXWebpageObject"),
            thumbData,
            scene,
            callback
        )
    }

    /**
     * 分享文本
     * @param text
     * @param scene
     * @return 校验参数是否合法
     */
    @JvmStatic
    @JvmOverloads
    fun shareText(
        text: String,
        scene: WechatScene = WechatScene.Session,
        callback: OnSendMessageResultCallback
    ): Boolean {
        val textObject = WXTextObject(text)
        return sendMessage(
            textObject,
            "",
            text,
            buildTransaction("WXTextObject"),
            scene = scene,
            callback = callback
        )
    }

    @JvmStatic
    @JvmOverloads
    fun sendMessage(
        mediaObject: IMediaObject,
        title: String?,
        description: String?,
        transaction: String,
        thumbData: ByteArray? = null,
        scene: WechatScene = WechatScene.Session,
        callback: OnSendMessageResultCallback
    ): Boolean {
        if (!isWXAppInstalled) {
            initWechatConfig.onWechatNotInstalled()
            initWechatConfig.logW(TAG, "wechat is not installed.")
            return false
        }
        val wxMsg = WXMediaMessage()
        wxMsg.mediaObject = mediaObject
        wxMsg.title = title
        wxMsg.description = description
        wxMsg.thumbData = thumbData

        val req = SendMessageToWX.Req()
        req.transaction =
            "SendMessageToWX_$transaction" // 对应该请求的事务 ID，通常由 Req 发起，回复 Resp 时应填入对应事务 ID
        req.message = wxMsg
        req.scene = scene.scene
        if (!req.checkArgs()) {
            return false
        }
        onSendMessageResultCallbackMap[req.transaction] = WeakReference(callback)
        wxApi.sendReq(req)
        return true
    }

    /**
     * 微信登录
     *
     * @param callback
     */
    @JvmStatic
    fun wxLogin(callback: OnAuthCodeResultCallback) {
        if (!isWXAppInstalled) {
            initWechatConfig.onWechatNotInstalled()
            initWechatConfig.logW(TAG, "wechat is not installed.")
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = initWechatConfig.appTag
        req.transaction = buildTransaction("SendAuthReq")
        onAuthCodeResultCallbackMap[req.transaction] = WeakReference(callback)
        wxApi.sendReq(req)
    }

    /**
     * 微信支付
     *
     * @param bean
     * @param callback
     * @return
     */
    @JvmStatic
    fun wxPay(bean: WechatPayInfo, callback: OnPayResultCallback): Boolean {
        if (!isWXAppInstalled) {
            initWechatConfig.onWechatNotInstalled()
            initWechatConfig.logW(TAG, "wechat is not installed.")
            return false
        }
        val payReq = PayReq().apply {
            appId = wechatAppId
            partnerId = bean.partnerId
            prepayId = bean.prepayId
            packageValue = bean.packageValue
            nonceStr = bean.nonceStr
            timeStamp = bean.timestamp
            sign = bean.sign
        }
        payReq.transaction = buildTransaction("PayReq")
        if (!payReq.checkArgs()) {
            return false
        }
        onPayResultCallbackMap[payReq.transaction] = WeakReference(callback)
        wxApi.sendReq(payReq)
        return true
    }

    /**
     * app 打开小程序
     *
     * @param userName 填小程序原始id
     * @param path 拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     * @param type 开发版，体验版和正式版
     * @param callback
     * @return
     */
    @JvmStatic
    @JvmOverloads
    fun launchMiniProgram(
        userName: String,
        path: String = "",
        type: MiniProgramType = MiniProgramType.Release,
        callback: OnLaunchMiniProgramResultCallback
    ): Boolean {
        if (!isWXAppInstalled) {
            initWechatConfig.onWechatNotInstalled()
            initWechatConfig.logW(TAG, "wechat is not installed.")
            return false
        }
        val req = WXLaunchMiniProgram.Req()
        req.userName = userName
        req.path = path
        req.miniprogramType = type.type
        req.transaction = buildTransaction("LaunchMiniProgramReq")
        if (!req.checkArgs()) {
            return false
        }
        onLaunchMiniProgramResultCallbackMap[req.transaction] = WeakReference(callback)
        wxApi.sendReq(req)
        return true
    }

    internal fun dispatchSendMessageResult(transaction: String, success: Boolean) {
        onSendMessageResultCallbackMap.remove(transaction)?.get()?.onResult(success)
    }

    internal fun dispatchAuthCodeResult(transaction: String, success: Boolean, code: String?) {
        onAuthCodeResultCallbackMap.remove(transaction)?.get()?.onResult(success, code)
    }

    internal fun dispatchPayResult(transaction: String, success: Boolean) {
        onPayResultCallbackMap.remove(transaction)?.get()?.onResult(success)
    }

    internal fun dispatchLaunchMiniProgramResult(
        transaction: String,
        success: Boolean,
        extMsg: String?
    ) {
        onLaunchMiniProgramResultCallbackMap.remove(transaction)?.get()?.onResult(success, extMsg)
    }

    private fun buildTransaction(type: String) = "${type}_${System.currentTimeMillis()}"

    interface InitWechatConfig {
        val application: Application

        /**
         * appId
         */
        val appId: String
        val appTag: String

        /**
         * 微信未安装时回调
         */
        fun onWechatNotInstalled()
        fun logI(tag: String, message: String)
        fun logD(tag: String, message: String)
        fun logW(tag: String, message: String)
        fun logE(tag: String, message: String)
        fun toast(message: String)
    }

    fun interface OnSendMessageResultCallback {
        fun onResult(success: Boolean)
    }

    fun interface OnAuthCodeResultCallback {
        fun onResult(success: Boolean, code: String?)
    }

    fun interface OnPayResultCallback {
        fun onResult(success: Boolean)
    }

    fun interface OnLaunchMiniProgramResultCallback {
        fun onResult(success: Boolean, extMsg: String?)
    }
}