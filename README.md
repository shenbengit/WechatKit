# WechatKit
微信SDK使用工具类，包括分享、支付、登录、拉起小程序功能

## 引入
### 将JitPack存储库添加到您的项目中(项目根目录下build.gradle文件)
```gradle
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```
### 添加依赖
> 在您引入项目的build.gradle中添加
```gradle
dependencies {
    implementation 'io.github.shenbengit:wechatkit:1.0.0'
}
```

## 使用事例
### 初始化配置
推荐在[Application.onCreate]调用

``` kotlin
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

```
### 使用方法
使用这些方法应在用户同意隐私政策之后
#### 注册app
```kotlin
// 用户同意隐私政策之后调用
WechatUtils.registerApp() 
```
#### 其他方法
[详见代码方法](https://github.com/shenbengit/WechatKit/blob/master/lib/src/main/java/com/shencoder/wechatkit/WechatUtils.kt)

## 官方文档
[微信sdk文档](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Access_Guide/Android.html)

## 作者其他的开源项目
- 基于RecyclerView实现网格分页布局：[PagerGridLayoutManager](https://github.com/shenbengit/PagerGridLayoutManager)
- 基于Netty封装UDP收发工具：[UdpNetty](https://github.com/shenbengit/UdpNetty)
- Android端基于JavaCV实现人脸检测功能：[JavaCV-FaceDetect](https://github.com/shenbengit/JavaCV-FaceDetect)
- 使用Kotlin搭建Android MVVM快速开发框架：[MVVMKit](https://github.com/shenbengit/MVVMKit)

# [License](https://github.com/shenbengitWechatKit/blob/master/LICENSE) 
