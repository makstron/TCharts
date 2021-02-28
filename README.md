# TChart
[![API](https://img.shields.io/badge/API-19%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![](https://jitpack.io/v/makstron/TCharts.svg)](https://jitpack.io/#makstron/TCharts)

Simple and fast charts.

Current version Beta 0.9.1

## Preview
![](https://raw.githubusercontent.com/makstron/TCharts/main/info/preview.webp)<br /><br />

## Import

### jitpack.io
#### gradle
```gradle
allprojects {
  repositories {
    ....
    maven { url 'https://jitpack.io' }
  }
}
```
```gradle
dependencies {
  implementation 'com.github.makstron:TCharts:0.9'
}
```
#### maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
```xml
<dependency>
  <groupId>com.github.makstron</groupId>
  <artifactId>TCharts</artifactId>
  <version>0.9.1</version>
</dependency>
```

## Usage

### Sample project
See `app` directory. Sample project is under construction. Not all features are covered yet.

### Usage in code
#### kotlin
```kotlin
val tChart = TChart(context)
val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
tChart.layoutParams = layoutParams
tChart.id = View.generateViewId()
tChart.setData(chartData, true)
tChart.setTitle(String.format("Chart #%d", i))
layout.addView(tChart)
```

### Usage in XML
```xml
<com.klim.tcharts.TChart
  android:id="@+id/tchart"
  android:layout_width="match_parent"
  android:layout_height="300dp"
  android:layout_marginTop="8dp"
  android:padding="16dp"
  app:backgroundColor="#785630"
  app:detailDivisionColor="#fff"
  app:detailLabelsFontColor="#BA1B289E"
  app:detailLineSelectedPosition="#871893"
  app:infoWindowBackground="#6338C6"
  app:infoWindowShadowColor="#980A16"
  app:infoWindowTitleColor="#117F8E"
  app:navBordersColor="#000000"
  app:navViewFillColor="#BA7C3838"
  app:showTitle="true"
  app:title="Title"
  app:titleFontColor="#339728"
  app:titleFontSize="16dp" />
```

### Properties
--------

| Properties                   | Default White theme                                                | Default Dark theme                                                  |
| ---------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------- |
| backgroundColor              | ![](https://via.placeholder.com/15/FFFFFF/FFFFFF?text=+)#FFFFFF    | ![](https://via.placeholder.com/15/1D2733/1D2733?text=+)#1D2733     |
| showTitle                    | true                                                               | true                                                                |
| title                        |                                                                    |                                                                     |
| titleFontSize                | 16sp                                                               | 16sp                                                                |
| titleFontColor               | ![](https://via.placeholder.com/15/3896D4/3896D4?text=+)#3896D4    | ![](https://via.placeholder.com/15/7BC4FB/7BC4FB?text=+)#7BC4FB     |
| detailLabelsFontColor        | ![](https://via.placeholder.com/15/506372/506372?text=+)#506372    | ![](https://via.placeholder.com/15/506372/506372?text=+)#506372     |
| detailDivisionColor          | ![](https://via.placeholder.com/15/efeff0/efeff0?text=+)#efeff0    | ![](https://via.placeholder.com/15/151e2a/151e2a?text=+)#151e2a     |
| detailLineSelectedPosition   | ![](https://via.placeholder.com/15/C9D8E3/C9D8E3?text=+)#73C9D8E3  | ![](https://via.placeholder.com/15/0E1721/0E1721?text=+)#AD0E1721   |
| infoWindowBackground         | ![](https://via.placeholder.com/15/FFFFFF/FFFFFF?text=+)#FFFFFFFF  | ![](https://via.placeholder.com/15/202b38/202b38?text=+)#FF202b38   |
| infoWindowShadowColor        | ![](https://via.placeholder.com/15/000000/000000?text=+)#5C000000  | ![](https://via.placeholder.com/15/000000/000000?text=+)#5C000000   |
| infoWindowTitleColor         | ![](https://via.placeholder.com/15/222222/222222?text=+)#222222    | ![](https://via.placeholder.com/15/FFFFFF/FFFFFF?text=+)#FFFFFF     |
| navViewFillColor             | ![](https://via.placeholder.com/15/F5F8F9/F5F8F9?text=+)#BAF5F8F9  | ![](https://via.placeholder.com/15/19232E/19232E?text=+)#BD19232E   |
| navBordersColor              | ![](https://via.placeholder.com/15/7DA9CA/7DA9CA?text=+)#477DA9CA  | ![](https://via.placeholder.com/15/7DA9CA/7DA9CA?text=+)#477DA9CA   |
| navTapCircle                 | ![](https://via.placeholder.com/15/D3E3F0/D3E3F0?text=+)#92D3E3F0  | ![](https://via.placeholder.com/15/43515c/43515c?text=+)#8043515c   |
