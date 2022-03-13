# SwipeMenuRecyclerView

SwipeMenuRecyclerView is a lightweight sliding menu library，it can be use in the list or on its own.

- Support multiple styles of menus (Classic, Overlay, Parallax), and easily customize your new styles.
- Support long menus, you can also slide on menu button.
- Easily build in layout editor, as simple as build a TextView.

https://user-images.githubusercontent.com/14817735/134755455-47c0fbc3-54b0-4899-9dba-dd54f3ddf851.mp4

## Table of Contents

- [Declaring dependencies](#declaring-dependencies)
- [ProGuard](#proguard)
- [Usage](#usage)
  - [Declared attributes for SwipeLayout](#declared-attributes-for-swipelayout)
  - [Listener for SwipeLayout](#listener-for-swipelayout)
- [Designer](#designer)
- [Contributing](#contributing)
- [License](#license)

## Declaring dependencies

1.Add JitPack repository

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

If your project is created by AndroidStudio-Arctic-Fox, the repositoryies configuration may be in settings.gradle:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2.Add the dependency

```groovy
dependencies {
    implementation 'com.github.aitsuki:SwipeMenuRecyclerView:2.1.1'
}
```

## ProGuard

Depending on your ProGuard config and usage, you may to include the following lines in your proguard configuration file.

```groovy
-keep class * implements com.aitsuki.swipe.SwipeLayout$Designer
```

## Usage

In XML layout file:

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

If use it as RecyclerView’s item, need to use `SwipeMenuRecyclerView` instead of  `RecyclerView`.

> `SwipeMenuRecyclerView` is almost the same as `RecyclerView`. It only slightly interferes with `dispatchTouchEvent()` to allow `SwipeLayout` automatically close menu when user scrolled the `RecyclerView`, and prevent open multiple menus during multi-touch.
>
> But if user manually calls the `RecyclerView.scroollxxx()` method to scoll the list, `SwipeMenuRecyclerView` will not automatically close  menu, because it doesn’t know whether the user needs this behavior.
>
> In this case, you can use the `SwipeMenuRecyclerView.closeMenus()` method.

### Declared attributes for SwipeLayout

- `preview="none|left|right"`：default `none`.  
  This attribute is helps build menus when the content is covers it.

- `autoClose="true|false"`：default `false`.
  If menu is opened, whether to automatically close the menu when click it.
- `designer="@string/classic_designer|overlay...|parallax..."`: default `classic`  
  Setting the style of menu, is similar to `Behavior` of [CoordinateLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout).  
  See [Designer](#designer)

### Listener for SwipeLayout

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

- `menuView` : For the currently operating menu, if you use both `leftMenu` and `rightMenu` at the same time, you need to determine which one is currently operating. For example, you can judge by `id` or `gravity`.

- `swipeOffset` ：The percentage of current menu display, the range is `0.0f ~ 1.0f`.

- `state` : `STATE_IDLE` , `STATE_DRAGGING`, `STATE_SETTLING`

## Designer

`SwipeLayout` has three build-in styles：

- Classic : Menu scrolls with content.

- Overlay ：Menu does not scroll, and the content covers the menu.

- Parallax ： Menu scrolls with content，and provides parallax styles.

You can implement `SwipeLayout.Designer` to customize more styles.

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

- `onInit()` is called after `onMeasure` of `SwipeLayout` method，and it will only be called once.

- `onLayout()` is called after `onLayout` of `SwipeLayout` method，and is called back every time when user sliding.

Among them, the `left, top, right, bottom` of the `onLayout` method represent the visible area of the current menu, which can be understood as `VisibleRect`.

![IMG_0074](https://user-images.githubusercontent.com/14817735/134761136-c8dfea17-d7e1-4618-8ec3-7b8b60831c0e.PNG)

## Contributing

Feel free to dive in! [Open an issue](https://github.com/aitsuki/SwipeMenuRecyclerView/issues/new) or submit PRs.

## License

[MIT](LICENSE) © Aitsuki
