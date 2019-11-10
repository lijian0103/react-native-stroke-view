# react-native-stroke-view

## Getting started

`$ npm install react-native-stroke-view --save`

### Mostly automatic installation

`$ react-native link react-native-stroke-view`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import cn.cnlee.commons.hanzi.RNStrokeViewPackage;` to the imports at the top of the file
  - Add `new RNStrokeViewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-stroke-view'
  	project(':react-native-stroke-view').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-stroke-view/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-stroke-view')
  	```


## Usage
```javascript
import RNStrokeView from 'react-native-stroke-view';

// TODO: What to do with the module?
RNStrokeView;
```
  