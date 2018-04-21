# TabView for Android

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[ ![Download](https://api.bintray.com/packages/dsiner/maven/tabview/images/download.svg) ](https://bintray.com/dsiner/maven/tabview/_latestVersion)

## Demos
![](https://github.com/Dsiner/Resouce/blob/master/lib/TabView/tabview.gif)

## Setup
Maven:
```xml
<dependency>
  <groupId>com.dsiner.lib</groupId>
  <artifactId>tabview</artifactId>
  <version>1.0.1</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.dsiner.lib:tabview:1.0.1'
```


## Usage
```xml
    <com.d.lib.tabview.TabView
        android:id="@+id/tabv_tab"
        android:layout_width="365dp"
        android:layout_height="36dp"
        app:tabv_colorMain="#4577B7"
        app:tabv_colorSub="#ffffff"
        app:tabv_duration="250"
        app:tabv_padding="2px"
        app:tabv_textSize="14dp"
        app:tabv_title="TAB1;TAB2;TAB3;TAB4" />
```

#### Operation
###### app:tabv_title指定标题（或代码中设置标题，如下）
```java
        tabView.setTitle(new String[]{"TAB1", "TAB2", "TAB3", "TAB4"});
```
###### 其他参数都具有默认值(可选)

#### SetListener(可选)
```java
        tabView.setOnTabSelectedListener(new TabView.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                
            }
        });
```

#### Parameter
| Attrs        | Type           | Function  |
| ------------- |:-------------:| -----:|
| tabv_title      | string | 标题(各个标题以";"分隔) |
| tabv_textSize      | dimension      |   标题文字大小 |
| tabv_colorMain | color      |    主颜色 |
| tabv_colorSub | color      |    辅颜色 |
| tabv_padding | dimension      |    边框宽度 |
| tabv_paddingSide | dimension      |    两端预留间距 |
| tabv_duration | integer      |    动画时长(ms) |

More usage see [Demo](app/src/main/java/com/d/slidelayout/MainActivity.java)


## Licence

```txt
Copyright 2017 D

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
