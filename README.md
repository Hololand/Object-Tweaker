# ObjectTweaker

## Developers

### Compiling
Linux/Mac
```shell script
$ ./gradlew clean shadowJar
```


Windows
```shell script
> gradlew.bat clean shadowJar
```



### Distributing
Linux/Mac
```shell script
$ ./gradlew clean jpackage
```
Windows
```shell script
> gradlew.bat clean jpackage
```


# Object Tweaker v1.0 - Manual
Report any bugs or issues to Hololand
##Installation
1. Run ObjectTweaker-1.0 msi
2. The Program will be installed to "C:\Program Files\ObjectTweaker\ObjectTweaker.exe"
3. A shortcut will be added to your start menu and desktop automatically
## Manual

### Loading and Exporting files

### Loading
Object Tweaker uses files from Terrain Builders "Object Export". To export from Terrain Builder correctly
ensure that *Threshold Options* Are un-ticked and *Record Format* is set to Terrain Builder format. An 
example export would follow the format
 `"ObjectClassName";PositionX;PositionY;Yaw;Pitch;Roll;Elevation(relative);` 
 Object tweaker will automatically account for an easting of `200,000` so no need to change that.
#### Exporting
 Files will be exported to the same directory from which they are opened with the suffix `"_tweaked"` a number may 
 be added if that name already exists.
### Features and Functions
Once you have opened a file items can be selected in 2 ways, You can either select the table rows you wish to edit
or select a class at the top to apply changes to all objects of that class. There are two main panels for tweaking
Note that the `Object Cleanup` Panel is not affected by your selection. We will now look at these two panels
#### Obect Tweaker panel
This panel is where you can select either objects in the table or a class to edit. Once you have made a selection 
then you can set the minimum and maximum values for the scale, pitch, yaw, roll and elevation of each object. Any
object in the selection that falls outside of these bounds will be set to the minimum or maximum accordingly.
#### Object Cleanup panel
##### Cull Duplicate Objects
This button will delete any duplicate objects in the file
##### Cull Outside Shape
You will be prompted to load a `shape.shp` file and any objects outside of this shape will be deleted
##### Cull Outside Map
You will be prompted to enter the Terrain size of your map and any objects outside of this will be deleted
