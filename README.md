<p align="center">
  <img src="https://avatars3.githubusercontent.com/u/38806334?s=400&u=b20d7b719e126e45e3d45c0ff04d0597ae3ed703&v=4" width="220" height="220" alt="Banner" />
</p>

# TAF框架集成文档

[![TAF Releases](https://img.shields.io/badge/Download-4.4.3-4BC51D.svg)](https://raw.githubusercontent.com/TooCMS-AIHP/TAFIntegrationAndUpdate/master/TooCMSAndroidFrame.aar)&#160;&#160;&#160;&#160;&#160;![Support](https://img.shields.io/badge/API-14+-4BC51D.svg)&#160;&#160;&#160;&#160;&#160;[![TAF Update](https://img.shields.io/badge/Update-Record-4BC51D.svg)](https://github.com/TooCMS-AIHP/TAFIntegrationAndUpdate/releases)&#160;&#160;&#160;&#160;&#160;![Author](https://img.shields.io/badge/Author-Zero-4BC51D.svg)

- 将TooCMSAndroidFrame.aar包放到libs目录下
- 在build.gradle文件下添加代码

```
android {

    /* 此处略过其他配置 */

    //添加aar包依赖，必要、固定
    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}
//  除支付和三方分享包是按需添加依赖外，剩余所有都是必要的
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    //  主框架包
    implementation(name: 'TooCMSAndroidFrame', ext: 'aar')
    //  支付包
    implementation(name: 'TooCMSAndroidPay', ext: 'aar')
    //  三方分享/登录包
    implementation(name: 'TooCMSAndroidShare', ext: 'aar')
    // 兼容
    implementation 'androidx.appcompat:appcompat:1.0.0'
    implementation 'com.google.android.material:material:1.0.0'
    //  图片加载
    implementation 'com.github.bumptech.glide:glide:4.10.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.10.0'
    implementation 'com.github.bumptech.glide:okhttp3-integration:4.10.0'
    //  网络请求
    implementation 'com.lzy.net:okgo:3.0.4'
    //  屏幕适配
    implementation 'me.jessyan:autosize:latest.integration'
    // json解析
    implementation 'com.google.code.gson:gson:2.8.2'
    //  View注入
    implementation 'com.jakewharton:butterknife:10.2.0'
    annotationProcessor 'com.jakewharton:butterknife-compiler:10.2.0'
    // 友盟
    implementation 'com.umeng.sdk:common:latest.integration'
    implementation 'com.umeng.sdk:analytics:latest.integration'
}
```

- 配置AndroidManifest.xml文件

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.toocms.frametesting">

    <!-- 这个权限用于进行网络定位-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!-- 写入扩展存储，向扩展卡写入数据，用于写入离线定位数据-->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- 访问网络，网络定位需要上网-->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- 这个权限用于获取wifi的获取权限，wifi信息会用来进行网络定位-->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <!-- 获取运营商信息，用于支持提供运营商信息相关的接口-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- 用于访问wifi网络信息，wifi信息会用于进行网络定位-->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <!--适配8.0请求安装权限-->
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

    <!-- 需指定name，必要 -->
    <!--此theme为默认，如需修改部分颜色转到color中修改即可-->
    <application
        android:name="tab.config.WeApplication"
        android:allowBackup="true"
		android:appComponentFactory="androidx.core.app.CoreComponentFactory"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme.CustomActionBar"
		tools:replace="android:appComponentFactory">

		<!-- 7.0适配，需更改包名 -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.toocms.frametesting.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

		<!-- 9.0适配 -->
        <uses-library
            android:name="org.apache.http.legacy"
            android:required="true" />

        <!-- 选取图片页面，按需、固定 -->
        <activity android:name="tab.ui.imageselector.SelectImageAty" />

        <!-- 裁剪图片页面，按需、固定 -->
        <activity
            android:name="cn.zero.android.common.view.ucrop.UCropActivity"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar" />
    </application>
</manifest>
```
- 创建必要类

   * 包名下创建BaseAty抽象类，继承BaseActivity类，该类中可实现一些针对项目通用的方法
   * 包名下创建config包
   * 在config包下新建AppConfig类，实现IAppConfig接口，并实现其方法
   * 在config包下新建Urls类，实现IUrls接口，并实现其方法，此类为URL集合类，所有URL都需写在此类中以便管理
   * 在config包下新建User类，实现IUser接口，此类为用户信息实体类，框架中通过该实体类存储和读取用户信息
   * 在config包下新建DataSet类，继承BaseDataSet抽象类，把AppConfig、Urls、User当做元素传入，并实现其中抽象方法
   * 以上所述可参考原型库中的写法以单例模式实现
   * 按项目需要在colors.xml文件中加上如下颜色值，并做相应修改

   ```
   <!--APP主色调-->
   <color name="clr_main">#FF5500</color>
   <!--页面背景色-->
   <color name="clr_bg">#F2F2F2</color>
   <!--Toolbar背景颜色-->
   <color name="action_bg">@color/white</color>
   <!--Toolbar标题文字颜色-->
   <color name="action_title_color">#323232</color>
   <!--Toolbar右侧菜单文字颜色-->
   <color name="action_menu_color">#323232</color>
   ```
   * 按项目需要在dimens.xml文件中加上如下尺寸值，并做相应修改
   ```
   <!--标题栏标题文字字号-->
   <dimen name="action_title_size">18sp</dimen>
   <!--标题栏菜单文字字号-->
   <dimen name="action_menu_size">16sp</dimen>
   ```
