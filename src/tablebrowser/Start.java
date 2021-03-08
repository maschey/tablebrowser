package tablebrowser;

import tablebrowser.database.DatabaseConnection;
import tablebrowser.util.Util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static javax.swing.SwingConstants.TOP;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Start {
    private static final String PROJECT = "Browse Tables :-)";
    private static final EmptyBorder EMPTY_BORDER_PADDING = new EmptyBorder(5, 5, 5, 5);
    private static final LineBorder LINE_BORDER_PADDING = new LineBorder(new Color(240, 240, 240));
    private static final DefaultListModel<String> propertiesData = new DefaultListModel<>();
    private static final DefaultMutableTreeNode tableviewTree = new DefaultMutableTreeNode("Database");
    private static final DefaultComboBoxModel<String> changeMetaData = new DefaultComboBoxModel<>();
    private static final String SAVED_STRING = "connect";
    private static JFrame screen;
    private static JLayeredPane layeredPane;
    private static JPanel loginPanel;
    private static JPanel applicationPanel;
    private static String databaseUrl;
    private static String database;
    private static String databaseUser;
    private static String databasePassword;
    private static String databaseDriver = "com.mysql.cj.jdbc.Driver";
    private static int selectedDatabase = -1;
    private static Connection connection;
    private static DefaultTableModel tableviewData = new DefaultTableModel();
    private static DefaultTableModel valueData = new DefaultTableModel();
    private static String selectedTableView = "";
    private static String selectedTableViewType = "";

    public static void main(String[] args) throws SQLException {

        //Initialize the Window
        screen = new JFrame(PROJECT);
        screen.setDefaultCloseOperation(EXIT_ON_CLOSE);
        screen.setResizable(true);
        screen.toFront();

        //Layout
        screen.getContentPane().setLayout(null);
        layeredPane = new JLayeredPane();
        screen.getContentPane().add(layeredPane);
        layeredPane.setLayout(new CardLayout(0, 0));

        //Panels
        loginPanel = new JPanel();
        loginPanel.setLayout(null);
        applicationPanel = new JPanel();
        applicationPanel.setLayout(null);

        //Add to Login Panel

        JLabel urlLabel = new JLabel("URL");
        urlLabel.setBounds(180, 11, 100, 25);
        loginPanel.add(urlLabel);

        JLabel databaseLabel = new JLabel("Database");
        databaseLabel.setBounds(180, 46, 100, 25);
        loginPanel.add(databaseLabel);

        JLabel userLabel = new JLabel("User");
        userLabel.setBounds(180, 82, 100, 25);
        loginPanel.add(userLabel);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(180, 118, 100, 25);
        loginPanel.add(passwordLabel);

        JTextField urlTextField = new JTextField();
        urlTextField.setColumns(10);
        urlTextField.setBounds(290, 10, 184, 25);
        loginPanel.add(urlTextField);

        JTextField databaseTextField = new JTextField();
        databaseTextField.setColumns(10);
        databaseTextField.setBounds(290, 48, 184, 25);
        loginPanel.add(databaseTextField);

        JTextField userTextField = new JTextField();
        userTextField.setColumns(10);
        userTextField.setBounds(290, 84, 184, 25);
        loginPanel.add(userTextField);

        JPasswordField passwordTextField = new JPasswordField();
        passwordTextField.setColumns(10);
        passwordTextField.setBounds(290, 120, 184, 25);
        loginPanel.add(passwordTextField);

        JList<String> properties = new JList<>(propertiesData);
        JScrollPane scrollPaneProperties = new JScrollPane(properties);
        scrollPaneProperties.setBounds(10, 11, 160, 228);
        loginPanel.add(scrollPaneProperties);

        JButton newDatabase = new JButton("New");
        newDatabase.setBounds(10, 250, 75, 25);
        newDatabase.setBackground(Color.CYAN);
        newDatabase.setToolTipText("Add new Database.");
        loginPanel.add(newDatabase);

        JButton delete = new JButton("Delete");
        delete.setBounds(95, 250, 75, 25);
        delete.setBackground(Color.RED);
        delete.setToolTipText("Delete current selected Database properties.");
        loginPanel.add(delete);

        JButton save = new JButton("Save");
        save.setBounds(180, 250, 75, 25);
        save.setBackground(Color.GREEN);
        save.setToolTipText("Save current selected Database properties.");
        loginPanel.add(save);

        JButton connect = new JButton("Connect");
        connect.setBounds(385, 251, 89, 23);
        connect.setBackground(Color.GREEN);
        connect.setToolTipText("Connect to the selected Database.");
        loginPanel.add(connect);

        //Add to Application Panel
        JTree tableview = new JTree(tableviewTree);
        tableview.setBorder(BorderFactory.createCompoundBorder(LINE_BORDER_PADDING, EMPTY_BORDER_PADDING));
        JScrollPane scrollPaneTableView = new JScrollPane(tableview);
        scrollPaneTableView.setBounds(10, 11, 350, 664);
        applicationPanel.add(scrollPaneTableView);

        JTable valueData = new JTable(Start.valueData);
        valueData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        valueData.setEnabled(false);
        JScrollPane scrollPaneValue = new JScrollPane(valueData);
        scrollPaneValue.setBounds(370, 11, 750, 342);
        applicationPanel.add(scrollPaneValue);

        JTable tableViewData = new JTable(tableviewData);
        tableViewData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableViewData.setEnabled(false);
        JScrollPane scrollPaneTableViewData = new JScrollPane(tableViewData);
        scrollPaneTableViewData.setBounds(1130, 11, 299, 308);
        applicationPanel.add(scrollPaneTableViewData);

        JTextArea sqlQuery = new JTextArea();
        sqlQuery.setBorder(BorderFactory.createCompoundBorder(LINE_BORDER_PADDING, EMPTY_BORDER_PADDING));
        JScrollPane scrollPaneSqlQuery = new JScrollPane(sqlQuery);
        scrollPaneSqlQuery.setBounds(370, 364, 1059, 150);
        applicationPanel.add(scrollPaneSqlQuery);

        JTabbedPane tabbedPaneSqlOutput = new JTabbedPane(TOP);
        tabbedPaneSqlOutput.setBorder(null);
        tabbedPaneSqlOutput.setBounds(370, 525, 1059, 150);
        applicationPanel.add(tabbedPaneSqlOutput);

        JButton sqlQueryExecuteButton = new JButton("Execute SQL");
        sqlQueryExecuteButton.setBounds(1279, 330, 150, 23);
        sqlQueryExecuteButton.setToolTipText("Execute your SQL-Statement below.");
        sqlQueryExecuteButton.setBackground(Color.GREEN);
        applicationPanel.add(sqlQueryExecuteButton);

        changeMetaData.addElement("Basic");
        JComboBox<String> changeMeta = new JComboBox<>(changeMetaData);
        changeMeta.setBounds(1130, 330, 100, 22);
        changeMeta.setToolTipText("Select Metadata-Information");
        applicationPanel.add(changeMeta);

        //Add MouseListener to JList
        //Listening on Double Click on JList Element
        MouseListener mouseListenerOfProperties = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    //Draw Error Text
                    connect(urlTextField, databaseTextField, userTextField, tableview);
                }
            }
        };
        properties.addMouseListener(mouseListenerOfProperties);

        //Add ListSelectionListener to .properties
        properties.addListSelectionListener(e -> {
            int index = properties.getSelectedIndex();
            if (index >= 0) {
                try {
                    //Collect the data from Data from .properties File
                    databaseUrl = DatabaseConnection.readPropertiesFile(propertiesData.elementAt(index))[0];
                    database = DatabaseConnection.readPropertiesFile(propertiesData.elementAt(index))[1];
                    databaseUser = DatabaseConnection.readPropertiesFile(propertiesData.elementAt(index))[2];
                    databasePassword = DatabaseConnection.readPropertiesFile(propertiesData.elementAt(index))[3];
                    selectedDatabase = properties.getSelectedIndex();

                    //Write Data into JTextField
                    urlTextField.setText(databaseUrl);
                    databaseTextField.setText(database);
                    userTextField.setText(databaseUser);
                    passwordTextField.setText(databasePassword);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        //Add TreeSelectionListener to table/view Tree
        tableview.addTreeSelectionListener(e -> {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tableview.getLastSelectedPathComponent();
            String nodeInfo = node.toString();

            if (!node.isRoot() && !nodeInfo.equals("Table") && !nodeInfo.equals("View")) {
                try {
                    changeMetaData.removeAllElements();

                    // Add all needed elements to JComboBox
                    changeMetaData.addElement("Basic");
                    changeMeta.setSelectedIndex(0);

                    //Get Data from Tree Node
                    selectedTableView = nodeInfo;
                    selectedTableViewType = node.getParent().toString();

                    //Get Value of table/view
                    getSQLData("tableviewValue", valueData, null);

                    //Get Meta Data of table
                    getSQLData("tableviewMeta", tableViewData, null);

                    if (selectedTableViewType.equals("Table")) {
                        //Add to JComboBox
                        changeMetaData.addElement("Create");
                        changeMetaData.addElement("Foreign Key");
                    }
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        });

        //Add ActionListener to delete Button
        delete.addActionListener(e -> {
            try {
                if (selectedDatabase >= 0) {
                    //Delete .properties-File
                    DatabaseConnection.deletePropertiesFile(propertiesData.elementAt(selectedDatabase));
                    getSQLData(SAVED_STRING, null, null);
                    selectedDatabase = -1;

                    //Clear all Fields
                    clsFields(urlTextField, databaseTextField, userTextField, passwordTextField);
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

        //Add ActionListener to "save" Button
        save.addActionListener(e -> saveFields(urlTextField, databaseTextField, userTextField, passwordTextField, properties));

        //Add ActionListener to "new" Button
        newDatabase.addActionListener(e -> {
            //Deselect .properties-File
            selectedDatabase = -1;
            properties.clearSelection();

            //Clear all Fields
            clsFields(urlTextField, databaseTextField, userTextField, passwordTextField);
        });

        //Add ActionListener to "connect" Button
        connect.addActionListener(e -> {
            //Display Output||Error Text
            saveFields(urlTextField, databaseTextField, userTextField, passwordTextField, properties);
            connect(urlTextField, databaseTextField, userTextField, tableview);
        });

        //Add ActionListener to "sqlExecute" Button
        sqlQueryExecuteButton.addActionListener(e -> {
            if (!sqlQuery.getText().isEmpty()) {
                Util.executeSQLQuery(connection, sqlQuery.getText(), tabbedPaneSqlOutput);
            }
        });

        //Add ActionListener to Combo Box
        changeMeta.addActionListener(e -> {
            try {
                //Get selected JComboBox Element
                switch (changeMeta.getSelectedIndex()) {
                    case 0:
                        if (selectedTableViewType.equals("Table")) {
                            //Get Meta Data of table
                            getSQLData("tableMeta", tableViewData, null);
                        } else if (selectedTableViewType.equals("View")) {
                            //Get Meta Data of view
                            getSQLData("viewMeta", tableViewData, null);
                        }
                        break;
                    case 1:
                        //Get Create Code of table
                        getSQLData("tableCreate", tableViewData, null);
                        break;
                    case 2:
                        //Get Foreign Key of table
                        getSQLData("tableFK", tableViewData, null);
                        break;
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });

        switchUi(loginPanel, "loginPanel", null);
    }

    public static void switchUi(JPanel panel, String panelName, JTree tree) throws SQLException {
        //Deactivate old Panel and activate new panel
        layeredPane.removeAll();
        layeredPane.add(panel);
        layeredPane.repaint();
        layeredPane.revalidate();

        if ("loginPanel".equals(panelName)) {
            //Get all .properties Files
            getSQLData(SAVED_STRING, null, null);

            //Set Title and Size for Window
            screen.setTitle(PROJECT + ": Login");
            screen.setSize(500, 325);
            layeredPane.setBounds(0, 0, 484, 286);
        } else if ("applicationPanel".equals(panelName)) {//Get all tables and views
            getSQLData("database", null, tree);

            //Set Title and Size for Window when connected todo resizeable
            screen.setTitle(PROJECT + ": Connected to " + database);
            screen.setSize(1450, 725);
            layeredPane.setBounds(0, 0, 1439, 686);
        }

        //Edit screen properties
        screen.setLocationRelativeTo(null);
        screen.setVisible(true);
    }

    public static void getSQLData(String type, JTable table, JTree tree) throws SQLException {
        switch (type) {
            case SAVED_STRING:
                //Add all .properties Files to List
                Util.addDatabase(propertiesData);
                break;
            case "database":
                //Add all Table and View to Tree
                tableviewTree.setUserObject(database);
                Util.addTableView(connection, tableviewTree);
                Util.editTree(tree, 0, tree.getRowCount());
                break;
            case "tableviewMeta":
                //Print Meta Data of table
                tableviewData = Util.getMetaData(connection, selectedTableView, tableviewData);
                Util.editTable(table);
                break;
            case "tableviewValue":
                //Print Select Output of table
                valueData = Util.getValueData(connection, selectedTableView, valueData);
                Util.editTable(table);
                break;
            case "tableCreate":
                //Print Create Code of table
                tableviewData = Util.getTableCreateData(connection, selectedTableView, tableviewData);
                Util.editTable(table);
                break;
            case "tableFK":
                //Print Foreign Key List of table
                tableviewData = Util.getTableFKData(connection, selectedTableView, tableviewData);
                Util.editTable(table);
                break;
        }
    }

    private static void clsFields(JTextField urlTextField, JTextField databaseTextField, JTextField userTextField, JTextField passwordTextField) {
        //Clear Connection Variable
        databaseUrl = "";
        database = "";
        databasePassword = "";

        //Clear JTextField
        urlTextField.setText("");
        databaseTextField.setText("");
        userTextField.setText("");
        passwordTextField.setText("");
    }

    private static void saveFields(JTextField urlTextField,
                                   JTextField databaseTextField,
                                   JTextField userTextField,
                                   JTextField passwordTextField,
                                   JList<String> properties) {

        try {
            if (!databaseTextField.getText().isEmpty() || !urlTextField.getText().isEmpty() || !userTextField.getText().isEmpty()) {
                if (selectedDatabase >= 0) {
                    //Save in same .properties-File, if Database is same
                    if (databaseTextField.getText().equals(propertiesData.elementAt(selectedDatabase))) {
                        DatabaseConnection.savePropertiesFile(databaseTextField.getText(), urlTextField.getText(), userTextField.getText(), passwordTextField.getText());
                    } else {
                        //Check for existing Database .properties-File
                        boolean exist = false;
                        for (int i = 0; i < propertiesData.getSize(); i++) {
                            if (propertiesData.elementAt(i).equals(databaseTextField.getText())) {
                                exist = true;
                            }
                        }

                        if (!exist) {
                            //Rename File
                            DatabaseConnection.renamePropertiesFile(database, databaseTextField.getText());
                            DatabaseConnection.savePropertiesFile(databaseTextField.getText(), urlTextField.getText(), userTextField.getText(), passwordTextField.getText());
                        } else {
                            //Delete selected .properties-File, saved existing .properties-File
                            DatabaseConnection.deletePropertiesFile(database);
                            DatabaseConnection.savePropertiesFile(databaseTextField.getText(), urlTextField.getText(), userTextField.getText(), passwordTextField.getText());
                        }
                    }
                } else {
                    //Save new .properties-File
                    DatabaseConnection.savePropertiesFile(databaseTextField.getText(), urlTextField.getText(), userTextField.getText(), passwordTextField.getText());
                }
            }

            //Edit Connection Variable
            databaseUrl = urlTextField.getText();
            database = databaseTextField.getText();
            databaseUser = userTextField.getText();
            databasePassword = passwordTextField.getText();

            //Reload JList
            getSQLData(SAVED_STRING, null, null);

            //Edit selected Element
            for (int i = 0; i < propertiesData.getSize(); i++) {
                if (databaseTextField.getText().equals(propertiesData.elementAt(i))) {
                    selectedDatabase = i;
                    properties.setSelectedIndex(i);
                }
            }
        } catch (IOException | SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void connect(JTextField urlTextField, JTextField databaseTextField, JTextField userTextField, JTree tableView) {

        try {
            if (!databaseTextField.getText().isEmpty() || !urlTextField.getText().isEmpty() || !userTextField.getText().isEmpty()) {
                //Connect to Database
                connection = DatabaseConnection.getDatabaseConnection(databaseUrl, database, databaseUser, databasePassword, databaseDriver);

                if (connection != null) {

                    //Change to App Panel
                    switchUi(applicationPanel, "applicationPanel", tableView);
                }
            }
        } catch (ClassNotFoundException | SQLException e1) {
            System.out.println("Error: Couldn't connect to Database");
        }

    }
}
