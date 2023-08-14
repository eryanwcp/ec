# 使用说明

#svg生成
1、将svg源文件放置在docs/icons/svg目录下，可按模块划分

2、生成目标文件，执行app/test/java/com/eryansky/utils/SvgUtils.java，

结果保存在：

    docs/icons/target

另外还将自动生成：

    app/src/main/resources/static/js/json/icon-app.json

3、在网站生成字体文件，https://www.iconfont.cn/
将生成的字体文件放置在app/src/main/resources/static/iconfont目录下

### PC端使用


拷贝以下目录或文件至项目。

    app/src/main/resources/static/iconfont/*
    app/src/main/resources/static/js/json/icon-app.json

