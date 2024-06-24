package com.shencoder.wechatkit

import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram


/**
 *
 * @author Shenben
 * @date 2024/6/24 10:24
 * @description
 * @since
 */
sealed class MiniProgramType(val type: Int) {
    /**
     * 生产
     */
    data object Release : MiniProgramType(WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE)

    /**
     * 测试
     */
    data object Test : MiniProgramType(WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_TEST)

    /**
     * 预览
     */
    data object Preview : MiniProgramType(WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW)
}