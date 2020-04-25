# SwipeMenuRecyclerView

![](http://upload-images.jianshu.io/upload_images/2202079-68f9a97838a05c53.gif?imageMogr2/auto-orient/strip)

## Usage

<b>Step 1. Add the JitPack repository to your build file</b>
Add it in your root `build.gradle` at the end of repositories:

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

<b>Step 2. Add the dependency</b>

```
dependencies {
	        compile 'com.github.AItsuki:SwipeMenuRecyclerView:1.1.4'
	}
```

# 存在问题

## 点击事件bug

`SwipeItemLayout`默认设置了`clickable`为true，会拦截掉parent的点击事件。所以如果SwipeItemLayout不是RecyclerView的Item的根布局，那么通过`ViewHolder.itemView.setOnClickListener`设置的点击时间将不起作用。需要将点击事件设置到`SwipeItemLayout`上。


