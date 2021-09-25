# SwipeMenuRecyclerView

中文 | [English](README.en.md)

SwipeMenuRecyclerView 是一个轻量级的侧滑菜单库，可以在列表中使用，也可以单独使用。

- 支持多种样式的菜单（Classic, Overlay, Parallax），并可以轻松的自定义新的样式
- 支持长菜单，在菜单上也可以滑动
- 可以在布局预览器中轻松的构建，和布局一个TextView一样简单

https://user-images.githubusercontent.com/14817735/134755455-47c0fbc3-54b0-4899-9dba-dd54f3ddf851.mp4

## 内容列表

- [声明依赖项](#声明依赖项)
- [使用方式](#使用方式)
  - [SwipeLayout 的自定义属性](#swipelayout-的自定义属性)
  - [监听菜单事件](#监听菜单事件)
- [使用 Designer 自定义菜单样式](#使用-designer-自定义菜单样式)

## 声明依赖项

1.添加 JitPack 仓库

在项目根目录的 build.gradle 中新增

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

如果你的项目是使用新的AndroidStudio Arctic Fox 创建的，配置仓库的位置可能在 settings.gradle

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2.添加依赖

```groovy
dependencies {
    implementation 'com.github.aitsuki:SwipeMenuRecyclerView:2.0.0'
}
```

## 使用方式

在布局文件中编写：

```xml
<com.aitsuki.swipe.SwipeLayout
    app:autoClose="true"
    app:designer="@string/classic_designer"
    app:preview="none">

    <!-- This is the left menu, because it specifies `layout_gravity=start` -->
    <TextView
        ...
        android:layout_gravity="start" />

    <!-- This is the right menu -->
    <TextView
        ...
        android:layout_gravity="end" />

    <!-- This is the content, because it dows not specify `layout_gravity` -->
    <TextView />

</com.aitsuki.swipe.SwipeLayout>
```

就是如此简单，你不需要编写任何代码（当然你还是需要通过代码给menu和content设置点击事件），现在你可以在App上看到一个侧滑菜单了。

如果在列表中使用，需要使用`SwipeMenuRecyclerView` 代替 `RecyclerView`。

> `SwipeMenuRecyclerView` 几乎和 `RecyclerView` 没有区别，它只是稍微的的干预了一下事件分发，让 `SwipeLayout` 在 `RecyclerView` 滚动的时候自动关闭菜单，并防止多点触控时打开多个菜单。  
> 另外，如果你是主动调用 `RecyclerView` 的 `scrollxxx()` 方法去滚动列表，那么`SwipeMenuRecyclerView` 不会帮你关闭菜单，因为它不确定你是否真的需要这个行为。  
> 这时候你可以通过 `SwipeMenuRecyclerView` 的 `closeMenus()` 方法关闭菜单。

### SwipeLayout 的自定义属性

- `preview="none|left|right"`：default `none`。  
  这个属性可以帮助布局，因为菜单默认关闭的，布局的时候无法查看菜单的样式，通过 `preview` 可以控制这个行为。

- `autoClose="true|false"`：default `false`。  
  菜单展开后，点击菜单后是否自动关闭菜单。如果`false`，你需要手动调用`SwipeLayout`的公开方法去关闭它。

- `designer="@string/classic_designer|overlay...|parallax..."`: default `classic`  
  设置菜单的样式，设置方式类似于 [CoordinateLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout) 的 `Behavior`。  
  更多介绍请查看 [Designer 自定义菜单样式](#designer-自定义菜单样式)

### 监听菜单事件

```kotlin
swipeLayout.addListener(object : SwipeLayout.Listener {
    override fun onSwipe(menuView: View, swipeOffset: Float) {
    }

    override fun onSwipeStateChanged(menuView: View, newState: Int) {
    }

    override fun onMenuOpened(menuView: View) {
    }

    override fun onMenuClosed(menuView: View) {
    }
})
```

- `menuView` : 当前操作的菜单，如果你同时使用了 `leftMenu` 和 `rightMenu` ，你需要自行判断当前操作的是哪个，例如你可以通过 `id` 或者 `gravity` 判断。

- `swipeOffset` ：当前菜单显示的百分比，范围是 `0.0f ~ 1.0f`。

- `state` : 当前菜单的状态 `STATE_IDLE` , `STATE_DRAGGING`, `STATE_SETTLING`

## 使用 Designer 自定义菜单样式

目前 `SwipeLayout` 内置了三种样式：

- Classic : 菜单跟随内容滚动

- Overlay ：菜单不会滚动，被内容遮盖

- Parallax ： 菜单跟随内容滚动，并提供视差样式

你可以通过实现 `SwipeLayout.Designer` 实现更多的菜单样式。

```kotlin
class ClassicDesigner : Designer {

    private var leftMenu: View? = null

    override fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?) {
        this.leftMenu = leftMenu
        leftMenu?.visibility = View.INVISIBLE
        rightMenu?.visibility = View.INVISIBLE
    }

    override fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int) {
        menuView.visibility = if (right - left > 0) VISIBLE else INVISIBLE
        if (menuView == leftMenu) {
            menuView.layout(right - menuView.width, menuView.top, right, menuView.bottom)
        } else {
            menuView.layout(left, menuView.top, left + menuView.width, menuView.bottom)
        }
    }
}
```

- `onInit()` 发生在 `SwipeLayout` 的 `onMeasure` 方法之后，并且只会执行一次。

- `onLayout()` 发生在 `SwipeLayout` 的 `onLayout` 方法之后，当用户滑动菜单时也会触发此回调。

其中 `onLayout` 方法的 `left, top, right, bottom` 表示当前菜单的可观测区域，你可以将其理解为 `VisibleRect`。

![IMG_0074](https://user-images.githubusercontent.com/14817735/134761136-c8dfea17-d7e1-4618-8ec3-7b8b60831c0e.PNG)
