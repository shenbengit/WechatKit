package com.shencoder.wechatkit



/**
 * 微信支付信息
 *
 * @property nonceStr
 * @property packageValue 取固定值Sign=WXPay
 * @property partnerId
 * @property prepayId
 * @property sign
 * @property timestamp
 */
data class WechatPayInfo(
    val nonceStr: String,
    val packageValue: String,
    val partnerId: String,
    val prepayId: String,
    val sign: String,
    val timestamp: String
)