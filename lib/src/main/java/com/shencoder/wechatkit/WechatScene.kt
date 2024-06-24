package com.shencoder.wechatkit

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX


/**
 *
 * @author Shenben
 * @date 2024/4/18 11:34
 * @description
 * @since
 */
sealed class WechatScene(val scene: Int) {
    /**
     * 对话场景
     */
    data object Session : WechatScene(SendMessageToWX.Req.WXSceneSession)

    /**
     * 朋友圈场景
     */
    data object Timeline : WechatScene(SendMessageToWX.Req.WXSceneTimeline)

    /**
     * 收藏场景
     */
    data object Favorite : WechatScene(SendMessageToWX.Req.WXSceneFavorite)

    /**
     * 指定联系人
     */
    data object SpecifiedContact : WechatScene(SendMessageToWX.Req.WXSceneSpecifiedContact)
}