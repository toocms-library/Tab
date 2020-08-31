<p align="center">
  <img src="https://avatars3.githubusercontent.com/u/38806334?s=400&u=b20d7b719e126e45e3d45c0ff04d0597ae3ed703&v=4" width="220" height="220" alt="Banner" />
</p>

# Tab框架集成文档

[![](https://jitpack.io/v/toocms-library/Tab.svg)](https://jitpack.io/#toocms-library/Tab)&#160;&#160;&#160;&#160;&#160;![Support](https://img.shields.io/badge/API-19+-4BC51D.svg)&#160;&#160;&#160;&#160;&#160;[![Tab Update](https://img.shields.io/badge/更新-记录-4BC51D.svg)](https://github.com/toocms-library/Tab/releases)&#160;&#160;&#160;&#160;&#160;![Author](https://img.shields.io/badge/Author-Zero-4BC51D.svg)

## 添加Gradle依赖
- 在项目根目录的build.gradle文件中添加
```
buildscript {
    repositories {
        ...
        maven { url 'https://dl.bintray.com/umsdk/release' }
    }
}
```
```
allprojects {
     repositories {
        ...
        maven { url "https://jitpack.io" }
        maven { url 'https://dl.bintray.com/umsdk/release' }
    }
}
```
- 在模块目录下的build.gradle文件的dependencies添加
```
dependencies {
    //  主框架包
    implementation 'com.github.toocms-library:Tab:5.1.6'
    // 兼容
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.0.0'
    //  图片加载
    implementation 'com.github.bumptech.glide:glide:4.11.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
    implementation 'com.github.bumptech.glide:okhttp3-integration:4.11.0'
    //  图片选择
    implementation 'com.github.LuckSiege.PictureSelector:picture_library:v2.5.8'
    //  网络请求
    implementation 'com.lzy.net:okgo:3.0.4'
    //  屏幕适配
    implementation 'me.jessyan:autosize:1.2.1'
    // json解析
    implementation 'com.google.code.gson:gson:2.8.6'
    //  View注入
    implementation 'com.jakewharton:butterknife:10.2.3'
    annotationProcessor 'com.jakewharton:butterknife-compiler:10.2.3'
    // 友盟
    implementation 'com.umeng.umsdk:common:9.1.0'
    implementation 'com.umeng.umsdk:asms:1.1.3'
    implementation 'com.umeng.umsdk:crash:0.0.4'
    implementation 'com.umeng.umsdk:oaid_lenovo:1.0.0'
    implementation 'com.umeng.umsdk:oaid_mi:1.0.0'
    implementation 'com.umeng.umsdk:oaid_oppo:1.0.4'
    implementation 'com.umeng.umsdk:oaid_vivo:1.0.0.1'
    // 分包
    implementation 'androidx.multidex:multidex:2.0.1'
}
```
## 集成方法
- 包名下创建BaseAty抽象类，继承BaseActivity类，该类中可实现一些针对项目通用的方法，其他Activity类继承BaseAty类
- 包名下创建config包
- 在config包下新建AppConfig类，实现IAppConfig接口，并实现其方法
- 在config包下新建Urls类，实现IUrls接口，并实现其方法，此类为URL集合类，所有URL都需写在此类中以便管理
- 在config包下新建User类，实现IUser接口，此类为用户信息实体类，框架中通过该实体类存储和读取用户信息
- 在config包下新建DataSet类，继承BaseDataSet抽象类，把AppConfig、Urls、User当做元素传入，并实现其中抽象方法
- 以上所述可参考原型库中的写法以单例模式实现
- 按项目需要在colors.xml文件中加上如下颜色值，并做相应修改
```
<!--APP主色调，主要影响加载条颜色、版本更新图片颜色，界面中的一些按钮、文字颜色需要指定该颜色方便以后修改-->
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
- 按项目需要在dimens.xml文件中加上如下尺寸值，并做相应修改
```
<!--标题栏标题文字字号-->
<dimen name="action_title_size">18sp</dimen>
<!--标题栏菜单文字字号-->
<dimen name="action_menu_size">16sp</dimen>
```
