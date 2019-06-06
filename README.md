>> 古人云：工欲善其事，必先利其器

android性能分析绕不开方法耗时分析的工作。没有数据统计支撑的性能分析是耍流氓的行为。
对于耗时方法分析统计。第一反应体力活是也，做法如下：

例子1：
```
public void methodA(){
    long start = SystemClock.elapsedRealtime();
    doingSomeThing();
    Log.d("===time==", "#methodA#Cost:" + (SystemClock.elapsedRealtime() -start);
}
```

有没有更好的办法呢??

###### 整体思路：
1. 思路：在app编译是，通过AOP字节码注入的方式实现对每一个方法插入耗时统计的代码，如例子1所示。
2. 实现方案， 开发gradle插件

有了以上的思路+几天构思和coding，有了这个开源的插件。好了不卖关子，插件奉上：

com.ifog:time-plugin:1.0.1

使用说明：

###### 配置:
1. project
```
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.ifog:time-plugin:1.0.1'
    }
}
```

2. 添加依赖和配置

```
apply plugin: 'time-plugin'
android{
    
}

timeExt{
    runVariant = 'DEBUG'
    whiteList = ['包名过列表'] // 例如['com.ifog']
}

dependencies{
    implementation 'com.ifog:PerformanceDebuger:1.0.2'
}
```
- runVariant 目前支持'DEBUG', "ANNOTATION", "NONE" 三种模式

##### 区别:

- 1).'DEBUG'模式下，对所有的方式会插入统计耗时代码
- 2). "ANNOTATION"模式，只正对 @DebugTime注解的方法出啊如统计耗时方法
- 3). "NONE"模式下，不会插入统计耗时代码


3. 在MainApplication中初始化
```
publc class MainApplication extends Application{
    @overrid
    public void onCreate(){
        
        // 自定义自己喜欢的log输出方式
        TimeDebugerManager.setLogger(new ITimeLogger() {
            @Override
            public void logger(String method, long cost) {
                if (cost > 16){
                    Log.e("=======AntiTime=======", "#" + method + ":cost:" + cost);
                }
            }
        });
    }
}

```


4. 后续会继续维护，增加log本地保存以及log分析的工具，敬请关注！
5. 可参考文章https://www.jianshu.com/p/54368f6fdda7
