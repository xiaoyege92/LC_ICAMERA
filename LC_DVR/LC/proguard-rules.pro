-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
# EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-ignorewarnings