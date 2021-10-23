package com.github.hololand.objecttweaker;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class View extends JFrame implements ActionListener {
    // Crap that the form will handle
    public Program programInstance;
    private JPanel mainPanel;
    private JButton loadButton;
    private JButton exportButton;
    private JComboBox objClassSelect;
    private JTable objectData;
    private JPanel scaleRange;
    private JTextField scaleMin;
    private JTextField scaleMax;
    private JButton setScaleButton;
    private JPanel pitchRange;
    private JTextField pitchMin;
    private JTextField pitchMax;
    private JButton setPitchButton;
    private JTextField yawMin;
    private JTextField yawMax;
    private JButton setYawButton;
    private JPanel yawRange;
    private JPanel rollRange;
    private JTextField rollMin;
    private JTextField rollMax;
    private JButton setRollButton;
    private JTextField elevMin;
    private JTextField elevMax;
    private JButton setElevationButton;
    private JPanel elevationRange;
    private JPanel objectTweaker;
    private JScrollPane graphScrollPane;
    private JToolBar toolBar;
    private JButton cullDuplicateObjectsButton;
    private JButton cullOutsideShapeButton;
    private JButton cullOutsideMapButton;
    private JPanel objectCleanup;
    private JButton cullInsideShapeButton;
    private final JFileChooser fileSelect = new JFileChooser();
    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor normCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    public View(String title, Program inst) {
        // Initialise GUI
        super(title);
        programInstance = inst;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setSize(1200, 760);
        this.setLocationRelativeTo(null);
        fileSelect.setPreferredSize(new Dimension(700, 500));
        objClassSelect.setMaximumRowCount(30);
        loadButton.addActionListener(this);
        exportButton.addActionListener(this);
        setScaleButton.addActionListener(this);
        setPitchButton.addActionListener(this);
        setYawButton.addActionListener(this);
        setRollButton.addActionListener(this);
        setElevationButton.addActionListener(this);
        cullOutsideShapeButton.addActionListener(this);
        cullDuplicateObjectsButton.addActionListener(this);
        cullOutsideMapButton.addActionListener(this);
        cullInsideShapeButton.addActionListener(this);
    }

    //
    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, "      " + infoMessage,
                "Object Tweaker: " + titleBar,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Program.class.getResource("/favicon40.png")));
    }

    public static String optionBox(String titleBar) {
        Object[] options = {"Class Selection", "Table Selection"};
        Object choice = JOptionPane.showInputDialog(null, "Make edit using",
                "Object Tweaker: " + titleBar,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Program.class.getResource("/favicon40.png")),
                options,
                options[0]);
        if (choice instanceof String) {
            return (String) choice;
        } else {
            return "noSelect";
        }
    }

    public static String optionBox2(String titleBar) {
        Object[] options = {"Set to limit", "Delete"};
        Object choice = JOptionPane.showInputDialog(null, "Choose operation for objects out of bounds",
                "Object Tweaker: " + titleBar,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Program.class.getResource("/favicon40.png")),
                options,
                options[0]);
        if (choice instanceof String) {
            return (String) choice;
        } else {
            return "noSelect";
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean setSuccess = false;
        int[] tableSelection = objectData.getSelectedRows();

        if (e.getSource() == loadButton) {
            int file = fileSelect.showOpenDialog(this);
            if (file == JFileChooser.APPROVE_OPTION) {
                try {
                    programInstance.loadFile(fileSelect.getSelectedFile().getAbsolutePath(), (DefaultTableModel) objectData.getModel());
                    objectData.getColumnModel().getColumn(0).setPreferredWidth(150);
                    objectData.getTableHeader().setReorderingAllowed(false);
                    objClassSelect.setModel(programInstance.updateClassList((DefaultComboBoxModel<String>) objClassSelect.getModel()));
                } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException loadFail) {
                    View.infoBox("Incorrect file format!", "Error!");
                }
            }
        } else if (e.getSource() == exportButton) {
            try {
                this.setCursor(waitCursor);
                programInstance.exportFile();
            } catch (IOException exportException) {
                View.infoBox("Failed to export!", "Error!");
            }

        } else if (e.getSource() == setScaleButton) {
            Boolean toDelete = false;
            String action = View.optionBox2("Set Scale");
            if (action.equals("Delete")) {
                toDelete = true;
            }
            if (tableSelection.length > 0 && (!(objClassSelect.getSelectedIndex() == -1))) {
                if (scaleMin.getText().equals("") || scaleMax.getText().equals("")) {
                    View.infoBox("Enter a valid value!", "Warning!");
                } else {
                    String choice = View.optionBox("Set Scale");
                    if (choice.equals("Class Selection")) {
                        if (programInstance.setScale(scaleMin.getText(), scaleMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    } else if (choice.equals("Table Selection")) {
                        if (programInstance.setScaleTable(scaleMin.getText(), scaleMax.getText(), objectData.getSelectedRows(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    }
                }
            } else if (tableSelection.length > 0) {
                if (programInstance.setScaleTable(scaleMin.getText(), scaleMax.getText(), objectData.getSelectedRows(), toDelete)) {
                    setSuccess = true;
                    View.infoBox("Changes applied!", "Success!");
                }
            } else if (programInstance.setScale(scaleMin.getText(), scaleMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                setSuccess = true;
                View.infoBox("Changes applied!", "Success!");
            }


        } else if (e.getSource() == setPitchButton) {
            Boolean toDelete = false;
            String action = View.optionBox2("Set Pitch");
            if (action.equals("Delete")) {
                toDelete = true;
            }
            if (tableSelection.length > 0 && (!(objClassSelect.getSelectedIndex() == -1))) {
                if (pitchMin.getText().equals("") || pitchMax.getText().equals("")) {
                    View.infoBox("Enter a valid value!", "Warning!");
                } else {
                    String choice = View.optionBox("Set Pitch");
                    if (choice.equals("Class Selection")) {
                        if (programInstance.setPitch(pitchMin.getText(), pitchMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    } else if (choice.equals("Table Selection")) {
                        if (programInstance.setPitchTable(pitchMin.getText(), pitchMax.getText(), objectData.getSelectedRows(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    }
                }
            } else if (tableSelection.length > 0) {
                if (programInstance.setPitchTable(pitchMin.getText(), pitchMax.getText(), objectData.getSelectedRows(), toDelete)) {
                    setSuccess = true;
                    View.infoBox("Changes applied!", "Success!");
                }
            } else if (programInstance.setPitch(pitchMin.getText(), pitchMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                setSuccess = true;
                View.infoBox("Changes applied!", "Success!");
            }


        } else if (e.getSource() == setYawButton) {
            Boolean toDelete = false;
            String action = View.optionBox2("Set Yaw");
            if (action.equals("Delete")) {
                toDelete = true;
            }
            if (tableSelection.length > 0 && (!(objClassSelect.getSelectedIndex() == -1))) {
                if (yawMin.getText().equals("") || yawMax.getText().equals("")) {
                    View.infoBox("Enter a valid value!", "Warning!");
                } else {
                    String choice = View.optionBox("Set Yaw");
                    if (choice.equals("Class Selection")) {
                        if (programInstance.setYaw(yawMin.getText(), yawMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    } else if (choice.equals("Table Selection")) {
                        if (programInstance.setYawTable(yawMin.getText(), yawMax.getText(), objectData.getSelectedRows(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    }
                }
            } else if (tableSelection.length > 0) {
                if (programInstance.setYawTable(yawMin.getText(), yawMax.getText(), objectData.getSelectedRows(), toDelete)) {
                    setSuccess = true;
                    View.infoBox("Changes applied!", "Success!");
                }
            } else if (programInstance.setYaw(yawMin.getText(), yawMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                setSuccess = true;
                View.infoBox("Changes applied!", "Success!");
            }


        } else if (e.getSource() == setRollButton) {
            Boolean toDelete = false;
            String action = View.optionBox2("Set Roll");
            if (action.equals("Delete")) {
                toDelete = true;
            }
            if (tableSelection.length > 0 && (!(objClassSelect.getSelectedIndex() == -1))) {
                if (rollMin.getText().equals("") || rollMax.getText().equals("")) {
                    View.infoBox("Enter a valid value!", "Warning!");
                } else {
                    String choice = View.optionBox("Set Roll");
                    if (choice.equals("Class Selection")) {
                        if (programInstance.setRoll(rollMin.getText(), rollMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    } else if (choice.equals("Table Selection")) {
                        if (programInstance.setRollTable(rollMin.getText(), rollMax.getText(), objectData.getSelectedRows(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    }
                }
            } else if (tableSelection.length > 0) {
                if (programInstance.setRollTable(rollMin.getText(), rollMax.getText(), objectData.getSelectedRows(), toDelete)) {
                    setSuccess = true;
                    View.infoBox("Changes applied!", "Success!");
                }
            } else if (programInstance.setRoll(rollMin.getText(), rollMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                setSuccess = true;
                View.infoBox("Changes applied!", "Success!");
            }


        } else if (e.getSource() == setElevationButton) {
            Boolean toDelete = false;
            String action = View.optionBox2("Set Elevation");
            if (action.equals("Delete")) {
                toDelete = true;
            }
            if (tableSelection.length > 0 && (!(objClassSelect.getSelectedIndex() == -1))) {
                if (elevMin.getText().equals("") || elevMax.getText().equals("")) {
                    View.infoBox("Enter a valid value!", "Warning!");
                } else {
                    String choice = View.optionBox("Set Elevation");
                    if (choice.equals("Class Selection")) {
                        if (programInstance.setElevation(elevMin.getText(), elevMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    } else if (choice.equals("Table Selection")) {
                        if (programInstance.setElevationTable(elevMin.getText(), elevMax.getText(), objectData.getSelectedRows(), toDelete)) {
                            setSuccess = true;
                            View.infoBox("Changes applied!", "Success!");
                        }
                    }
                }
            } else if (tableSelection.length > 0) {
                if (programInstance.setElevationTable(elevMin.getText(), elevMax.getText(), objectData.getSelectedRows(), toDelete)) {
                    setSuccess = true;
                    View.infoBox("Changes applied!", "Success!");
                }
            } else if (programInstance.setElevation(elevMin.getText(), elevMax.getText(), (String) objClassSelect.getSelectedItem(), toDelete)) {
                setSuccess = true;
                View.infoBox("Changes applied!", "Success!");
            }


        } else if (e.getSource() == cullDuplicateObjectsButton) {
            this.setCursor(waitCursor);
            if (programInstance.cullDuplicateObjects()) {
                setSuccess = true;
            }


        } else if (e.getSource() == cullOutsideMapButton) {
            this.setCursor(waitCursor);
            if (programInstance.cullOutsideMap()) {
                setSuccess = true;
            }


        } else if (e.getSource() == cullOutsideShapeButton) {
            this.setCursor(waitCursor);
            if (programInstance.cullOutsideShape()) {
                setSuccess = true;
            }
        }
        else if (e.getSource() == cullInsideShapeButton) {
            this.setCursor(waitCursor);
            if (programInstance.cullInsideShape()) {
                setSuccess = true;
            }
        }
        if (setSuccess) {
            programInstance.updateTable((DefaultTableModel) objectData.getModel());
            objectData.getColumnModel().getColumn(0).setPreferredWidth(150);
            objectData.getTableHeader().setReorderingAllowed(false);
        }
        this.setCursor(normCursor);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        mainPanel.add(toolBar, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        loadButton = new JButton();
        loadButton.setText("Load Objects");
        toolBar.add(loadButton);
        exportButton = new JButton();
        exportButton.setText("Export");
        toolBar.add(exportButton);
        final JLabel label1 = new JLabel();
        label1.setText("Select Object Class: ");
        toolBar.add(label1);
        objClassSelect = new JComboBox();
        toolBar.add(objClassSelect);
        graphScrollPane = new JScrollPane();
        mainPanel.add(graphScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        objectData = new JTable();
        objectData.setAutoResizeMode(4);
        objectData.setShowHorizontalLines(false);
        graphScrollPane.setViewportView(objectData);
        objectTweaker = new JPanel();
        objectTweaker.setLayout(new GridLayoutManager(10, 1, new Insets(0, 0, 0, 20), -1, -1));
        mainPanel.add(objectTweaker, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Object Tweaker");
        objectTweaker.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaleRange = new JPanel();
        scaleRange.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(scaleRange, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaleMin = new JTextField();
        scaleRange.add(scaleMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        scaleRange.add(spacer1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scaleMax = new JTextField();
        scaleRange.add(scaleMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Max");
        scaleRange.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Min");
        scaleRange.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setScaleButton = new JButton();
        setScaleButton.setText("Set Scale");
        scaleRange.add(setScaleButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pitchRange = new JPanel();
        pitchRange.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(pitchRange, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pitchMin = new JTextField();
        pitchRange.add(pitchMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        pitchRange.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pitchMax = new JTextField();
        pitchRange.add(pitchMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Min");
        pitchRange.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Max");
        pitchRange.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setPitchButton = new JButton();
        setPitchButton.setText("Set Pitch");
        pitchRange.add(setPitchButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yawRange = new JPanel();
        yawRange.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(yawRange, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        yawMin = new JTextField();
        yawRange.add(yawMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        yawRange.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        yawMax = new JTextField();
        yawRange.add(yawMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        setYawButton = new JButton();
        setYawButton.setText("Set Yaw");
        yawRange.add(setYawButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Min");
        yawRange.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Max");
        yawRange.add(label8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rollRange = new JPanel();
        rollRange.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(rollRange, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        rollMin = new JTextField();
        rollRange.add(rollMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        rollRange.add(spacer4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        rollMax = new JTextField();
        rollRange.add(rollMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        setRollButton = new JButton();
        setRollButton.setText("Set Roll");
        rollRange.add(setRollButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Min");
        rollRange.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Max");
        rollRange.add(label10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        elevationRange = new JPanel();
        elevationRange.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(elevationRange, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        elevMin = new JTextField();
        elevationRange.add(elevMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer5 = new Spacer();
        elevationRange.add(spacer5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        elevMax = new JTextField();
        elevationRange.add(elevMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        setElevationButton = new JButton();
        setElevationButton.setText("Set Elevation");
        elevationRange.add(setElevationButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Min");
        elevationRange.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Max");
        elevationRange.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        objectTweaker.add(separator1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        objectCleanup = new JPanel();
        objectCleanup.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        objectTweaker.add(objectCleanup, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cullDuplicateObjectsButton = new JButton();
        cullDuplicateObjectsButton.setText("Cull Duplicate Objects");
        objectCleanup.add(cullDuplicateObjectsButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Object Cleanup");
        objectCleanup.add(label13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cullOutsideShapeButton = new JButton();
        cullOutsideShapeButton.setText("Cull Outside Shape");
        objectCleanup.add(cullOutsideShapeButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cullOutsideMapButton = new JButton();
        cullOutsideMapButton.setText("Cull Outside Map");
        objectCleanup.add(cullOutsideMapButton, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cullInsideShapeButton = new JButton();
        cullInsideShapeButton.setText("Cull Inside Shape");
        objectCleanup.add(cullInsideShapeButton, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        objectTweaker.add(separator2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
