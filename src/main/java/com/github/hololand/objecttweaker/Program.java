package com.github.hololand.objecttweaker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

public class Program {
    public static List<GameObject> gameObjects= new ArrayList<>();
    public static Map< String,Integer> objectClasses = new HashMap<>();
    private SimpleFeatureSource currentShape;
    private String fileName;
    private String workingDirectory;
    private String fileExtension;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.bulenkov.darcula.DarculaLaf");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException lafException) {
            View.infoBox("Failed to load LAF / Your stuck in ugly mode!", "Warning!");
        }
        Program programInst = new Program();
        JFrame window = new View("Object Tweaker - v1.4", programInst);
        URL iconURL = Program.class.getResource("/favicon40.png");
        ImageIcon icon = new ImageIcon(iconURL);
        window.setIconImage(icon.getImage());
        window.setVisible(true);
    }

    public void updateTable(DefaultTableModel tbl){
        DefaultTableModel tblmdl = tbl;
        String header[] = {"Object Class", "Position X", "Position Y", "Yaw", "Pitch", "Roll", "Scale", "Elevation (relative"};
        tblmdl.setColumnIdentifiers(header);
        tblmdl.getDataVector().removeAllElements();
        tblmdl.fireTableDataChanged();
        for (GameObject obj: gameObjects) {
            Object[] row = new Object[]{
                    obj.Name, obj.getPosX(), obj.getPosY(), obj.getYaw(), obj.getPitch(), obj.getRoll(), obj.getScale(), String.format("%.6f", obj.getPosZ())
            };
            tblmdl.addRow(row);
        }
        tblmdl.fireTableDataChanged();
    }

    public DefaultComboBoxModel<String> updateClassList(DefaultComboBoxModel<String> cmdl) {
        cmdl.removeAllElements();
        List<String> objClasses = new ArrayList<>();
        for (Map.Entry<String, Integer> element: objectClasses.entrySet()) {
            String temp = element.getKey() + " (" + element.getValue() + ")";
            objClasses.add(temp);
        }
        cmdl.addAll(objClasses);
        return cmdl;
    }

    public void loadFile(String filePath, DefaultTableModel tbl) {

        try {
            gameObjects.clear();
            objectClasses.clear();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                GameObject temp = new GameObject(line);
                gameObjects.add(temp);
                if (!objectClasses.containsKey(temp.Name)) {
                    objectClasses.put(temp.Name, 1);
                }
                else {
                    objectClasses.put(temp.Name, objectClasses.get(temp.Name) + 1);
                }
            }
            br.close();

            fileName = filePath.substring(filePath.lastIndexOf('\\') + 1, filePath.lastIndexOf('.'));
            fileExtension = filePath.substring(filePath.lastIndexOf('.'));
            workingDirectory = filePath.substring(0,filePath.lastIndexOf('\\') + 1);
            updateTable(tbl);
        }
        catch (IOException loadFileException) {
            View.infoBox("Failed to open / read file", "Error!");
        }
    }

    public void exportFile() throws IOException {
        int i = 0;
        if (workingDirectory != null) {
            try {
                String saveDirectory = workingDirectory + fileName + "_tweaked" + fileExtension;
                while(new File(saveDirectory).isFile()) {
                    i++;
                    saveDirectory = workingDirectory + fileName + "_tweaked_" + i + fileExtension;
                }
                List<String> lines = new ArrayList<>();
                for(GameObject obj: gameObjects) {
                    String line = "\"" + obj.Name + "\";"
                            + (String.format("%.6f", obj.getPosX())) + ";"
                            + (String.format("%.6f", obj.getPosY())) + ";"
                            + (String.format("%.6f", obj.getYaw())) + ";"
                            + (String.format("%.6f", obj.getPitch())) + ";"
                            + (String.format("%.6f", obj.getRoll())) + ";"
                            + (String.format("%.6f", obj.getScale())) + ";"
                            + (String.format("%.6f", obj.getPosZ())) + ";";
                    lines.add(line);
                }
                String lineListStr = String.join("\r\n", lines);
                Files.write(Paths.get(saveDirectory), lineListStr.getBytes(Charset.forName("UTF-8")));
                if(new File(saveDirectory).isFile()){
                    View.infoBox("Exported Successfully!", "Success!");
                }
            }
            catch(IOException exportFail) {
                View.infoBox("Failed to export!", "Error!");
            }
        }
        else {
            View.infoBox("You must open a file first!", "Error!");
        }



    }

    public boolean setScale(String scaleMin, String scaleMax, String selectedClass, Boolean delete) {
        int sigFig = 4;
        try{
            List<GameObject> toRemove = new ArrayList<>();
            String classFormatted = selectedClass.substring(0, selectedClass.indexOf('(') - 1);
            OptionalDouble rawMin = stringToDouble(scaleMin, sigFig);
            OptionalDouble rawMax = stringToDouble(scaleMax, sigFig);
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0) {
                    View.infoBox("Scale must be positive!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(GameObject obj: gameObjects) {
                        if (obj.Name.equals(classFormatted)) {
                            if (obj.getScale() < min) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setScale(min);
                                }
                            }
                            if (obj.getScale() > max) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setScale(max);
                                }
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }

    public boolean setScaleTable(String scaleMin, String scaleMax, int[] tableSelection, Boolean delete) {
        int sigFig = 4;
        try{
            List<GameObject> toRemove = new ArrayList<>();
            OptionalDouble rawMin = stringToDouble(scaleMin, sigFig);
            OptionalDouble rawMax = stringToDouble(scaleMax, sigFig);
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0) {
                    View.infoBox("Scale must be positive!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(int i : tableSelection) {
                        if (gameObjects.get(i).getScale() < min) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setScale(min);
                            }
                        }
                        if (gameObjects.get(i).getScale() > max) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setScale(max);
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }


    public boolean setPitch(String pitchMin, String pitchMax, String selectedClass, Boolean delete) {
        int sigFig = 9;
        try {
            String classFormatted = selectedClass.substring(0, selectedClass.indexOf('(') - 1);
            OptionalDouble rawMin = stringToDouble(pitchMin, sigFig);
            OptionalDouble rawMax = stringToDouble(pitchMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0 || min > 360 || max > 360) {
                    View.infoBox("Pitch must be between 0-360!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(GameObject obj: gameObjects) {
                        if (obj.Name.equals(classFormatted)) {
                            if (obj.getPitch() < min) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setPitch(min);
                                }

                            }
                            if (obj.getPitch() > max) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setPitch(max);
                                }
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }

    public boolean setPitchTable(String pitchMin, String pitchMax, int[] tableSelection, Boolean delete) {
        int sigFig = 9;
        try{
            OptionalDouble rawMin = stringToDouble(pitchMin, sigFig);
            OptionalDouble rawMax = stringToDouble(pitchMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0) {
                    View.infoBox("Scale must be positive!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(int i : tableSelection) {
                        if (gameObjects.get(i).getPitch() < min) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setPitch(min);
                            }

                        }
                        if (gameObjects.get(i).getPitch() > max) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setPitch(max);
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }

    public boolean setYaw(String yawMin, String yawMax, String selectedClass, Boolean delete) {
        int sigFig = 9;
        try{
            String classFormatted = selectedClass.substring(0, selectedClass.indexOf('(') - 1);
            OptionalDouble rawMin = stringToDouble(yawMin, sigFig);
            OptionalDouble rawMax = stringToDouble(yawMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0 || min > 360 || max > 360) {
                    View.infoBox("Yaw must be between 0-360!", "Warning!");
                    return false;
                }
                else {
                    for(GameObject obj: gameObjects) {
                        if (obj.Name.equals(classFormatted)) {
                            if (obj.getYaw() < min) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setYaw(min);
                                }
                            }
                            if (obj.getYaw() > max) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setYaw(max);
                                }
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }
    }

    public boolean setYawTable(String yawMin, String yawMax, int[] tableSelection, Boolean delete) {
        int sigFig = 9;
        try{
            OptionalDouble rawMin = stringToDouble(yawMin, sigFig);
            OptionalDouble rawMax = stringToDouble(yawMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0) {
                    View.infoBox("Scale must be positive!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(int i : tableSelection) {
                        if (gameObjects.get(i).getYaw() < min) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setYaw(min);
                            }

                        }
                        if (gameObjects.get(i).getYaw() > max) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setYaw(max);
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }

    public boolean setRoll(String rollMin, String rollMax, String selectedClass, Boolean delete) {
        int sigFig = 9;
        try{
            String classFormatted = selectedClass.substring(0, selectedClass.indexOf('(') - 1);
            OptionalDouble rawMin = stringToDouble(rollMin, sigFig);
            OptionalDouble rawMax = stringToDouble(rollMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0 || min > 360 || max > 360) {
                    View.infoBox("Roll must be between 0-360!", "Warning!");
                    return false;
                }
                else {
                    for(GameObject obj: gameObjects) {
                        if (obj.Name.equals(classFormatted)) {
                            if (obj.getRoll() < min) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setRoll(min);
                                }
                            }
                            if (obj.getRoll() > max) {
                                if (delete) {
                                    toRemove.add(obj);
                                }
                                else {
                                    obj.setRoll(max);
                                }
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }
    }

    public boolean setRollTable(String rollMin, String rollMax, int[] tableSelection, Boolean delete) {
        int sigFig = 9;
        try{
            OptionalDouble rawMin = stringToDouble(rollMin, sigFig);
            OptionalDouble rawMax = stringToDouble(rollMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                if (min < 0 || max < 0) {
                    View.infoBox("Scale must be positive!", "Warning!");
                    return false;
                }
                if (min > max) {
                    View.infoBox("Max must be greater than min!", "Warning!");
                    return false;
                }
                else {
                    for(int i : tableSelection) {
                        if (gameObjects.get(i).getRoll() < min) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setRoll(min);
                            }
                        }
                        if (gameObjects.get(i).getRoll() > max) {
                            if (delete) {
                                toRemove.add(gameObjects.get(i));
                            }
                            else {
                                gameObjects.get(i).setRoll(max);
                            }
                        }
                    }
                    gameObjects.removeAll(toRemove);
                    return true;
                }
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }

    }

    public boolean setElevation(String elevMin, String elevMax, String selectedClass, Boolean delete) {
        int sigFig = 7;
        try{
            String classFormatted = selectedClass.substring(0, selectedClass.indexOf('(') - 1);
            OptionalDouble rawMin = stringToDouble(elevMin, sigFig);
            OptionalDouble rawMax = stringToDouble(elevMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                for(GameObject obj: gameObjects) {
                    if (obj.Name.equals(classFormatted)) {
                        if (obj.getPosZ() < min) {
                            if (delete) {
                                toRemove.add(obj);
                            }
                            else {
                                obj.setPosZ(min);
                            }

                        }
                        if (obj.getPosZ() > max) {
                            if (delete) {
                                toRemove.add(obj);
                            }
                            else {
                                obj.setPosZ(max);
                            }
                        }
                    }
                }
                gameObjects.removeAll(toRemove);
                return true;
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }
    }

    public boolean setElevationTable(String elevMin, String elevMax, int[] tableSelection, Boolean delete) {
        int sigFig = 7;
        try{
            OptionalDouble rawMin = stringToDouble(elevMin, sigFig);
            OptionalDouble rawMax = stringToDouble(elevMax, sigFig);
            List<GameObject> toRemove = new ArrayList<>();
            if (rawMin != OptionalDouble.empty() && rawMax != OptionalDouble.empty()) {
                double min = rawMin.getAsDouble();
                double max = rawMax.getAsDouble();
                for(int i : tableSelection) {
                    if (gameObjects.get(i).getPosZ() < min) {
                        if (delete) {
                            toRemove.add(gameObjects.get(i));
                        }
                        else {
                            gameObjects.get(i).setPosZ(min);
                        }
                    }
                    if (gameObjects.get(i).getPosZ() > max) {
                        if (delete) {
                            toRemove.add(gameObjects.get(i));
                        }
                        else {
                            gameObjects.get(i).setPosZ(max);
                        }
                    }
                }
                gameObjects.removeAll(toRemove);
                return true;
            }
            View.infoBox("Enter a valid value!", "Warning!");
            return false;
        }
        catch (NullPointerException e) {
            View.infoBox("No class or rows selected!", "Warning!");
            return false;
        }
    }

    public boolean cullDuplicateObjects() {
        if (fileName == null) {
            View.infoBox("Load a file first!", "Warning!");
            return false;
        }
        int culledObjCount = 0;
        Set<GameObject> culledObjs = new HashSet<>();
        for (int i = 0; i < gameObjects.size(); i++) {
            culledObjs.add(gameObjects.get(i));
        }
        culledObjCount = gameObjects.size() - culledObjs.size();
        if (culledObjCount > 0) {
            gameObjects.clear();
            for (GameObject obj : culledObjs) {
                gameObjects.add(obj);
            }
            sortGameObjects();
            View.infoBox("Culled " + culledObjCount + " Duplicate objects", "Success!");
            return true;
        }
        View.infoBox("Culled " + culledObjCount + " Duplicate objects", "Success!");
        return false;
    }

    public boolean cullOutsideMap() {
        if (fileName == null) {
            View.infoBox("Load a file first!", "Warning!");
            return false;
        }
        String size = JOptionPane.showInputDialog("Enter terrain size: e.g. 20480");
        int easting = 200000;
        int removeCount = 0;
        try {
            int mapSize = Integer.parseInt(size);

            Iterator<GameObject> iter = gameObjects.iterator();
            while(iter.hasNext()) {
                GameObject obj = iter.next();
                if (obj.getPosX() > easting + mapSize || obj.getPosX() < easting) {
                    iter.remove();
                    removeCount++;
                }
                else if (obj.getPosY() < 0 || obj.getPosY() > mapSize) {
                    iter.remove();
                    removeCount++;
                }
                // 20480x20480
                // pos X = foo + 200000
                // pos Y = foo
            }
            View.infoBox("Culled " + removeCount + " Objects outside of map", "Success!");
            return true;
        }
        catch (NumberFormatException badMapSize){
            View.infoBox("Enter a valid map size!", "Warning!");
            return false;
        }
    }
    // Helper function for cullOutsideShape because the geotools API is crap
    public static File showOpenFile(String extension, File initialDir, Component parent) throws HeadlessException {
        JFileDataStoreChooser dialog = new JFileDataStoreChooser(extension);
        dialog.setPreferredSize(new Dimension(700,500));
        if (initialDir != null) {
            if (initialDir.isDirectory()) {
                dialog.setCurrentDirectory(initialDir);
            } else {
                dialog.setCurrentDirectory(initialDir.getParentFile());
            }
        }

        return dialog.showOpenDialog(parent) == 0 ? dialog.getSelectedFile() : null;
    }

    public boolean cullOutsideShape() {
        currentShape = null;
        List<GameObject> toKeep = new ArrayList<>();
        if (fileName == null) {
            View.infoBox("Load a file first!", "Warning!");
            return false;
        }
        try {
            File file = showOpenFile("shp", (File) null, null);
            if (file == null) {
                return false;
            }
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            currentShape = store.getFeatureSource();
            SimpleFeatureCollection collection = currentShape.getFeatures();
            SimpleFeatureIterator iterator = collection.features();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry shpGeo = (Geometry) feature.getDefaultGeometry();
                for (GameObject obj : gameObjects) {
                    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                    Coordinate coord = new Coordinate(obj.getPosX(), obj.getPosY());
                    Point objPos = geometryFactory.createPoint(coord);
                    if (shpGeo.contains(objPos)) {
                        toKeep.add(obj);
                    }
                }
            }
            iterator.close();
            int removeCount = gameObjects.size() - toKeep.size();
            gameObjects = toKeep;
            View.infoBox("Culled " + removeCount + " Objects outside of shape", "Success!");
            return true;
        }
        catch (IOException shapeLoadError) {
            View.infoBox("Failed to load shape!", "Warning!");
            return false;
        }

    }

    public boolean cullInsideShape() {
        currentShape = null;
        List<GameObject> toKeep = new ArrayList<>();
        if (fileName == null) {
            View.infoBox("Load a file first!", "Warning!");
            return false;
        }
        try {
            File file = showOpenFile("shp", (File) null, null);
            if (file == null) {
                return false;
            }
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            currentShape = store.getFeatureSource();
            SimpleFeatureCollection collection = currentShape.getFeatures();
            SimpleFeatureIterator iterator = collection.features();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry shpGeo = (Geometry) feature.getDefaultGeometry();
                for (GameObject obj : gameObjects) {
                    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                    Coordinate coord = new Coordinate(obj.getPosX(), obj.getPosY());
                    Point objPos = geometryFactory.createPoint(coord);
                    if (!(shpGeo.contains(objPos))) {
                        toKeep.add(obj);
                    }
                }
            }
            iterator.close();
            int removeCount = gameObjects.size() - toKeep.size();
            gameObjects = toKeep;
            View.infoBox("Culled " + removeCount + " Objects Inside of shape", "Success!");
            return true;
        }
        catch (IOException shapeLoadError) {
            View.infoBox("Failed to load shape!", "Warning!");
            return false;
        }

    }

    public void sortGameObjects() {
        Collections.sort(gameObjects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject o1, GameObject o2) {
                return o1.Name.compareTo(o2.Name);
            }
        });
    }

    public OptionalDouble stringToDouble(String str, int SF){
        try {
            Double value = Double.parseDouble(str);
            BigDecimal rounding = new BigDecimal(value).round(new MathContext(SF));
            return OptionalDouble.of(rounding.doubleValue());
        }
        catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }
}

