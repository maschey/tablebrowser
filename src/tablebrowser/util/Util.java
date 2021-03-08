package tablebrowser.util;

import tablebrowser.database.DatabaseConnection;
import tablebrowser.database.DatabaseMetaData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Util {
    public static DefaultTableModel getMetaData(Connection connection, String selectedTableView, DefaultTableModel tableModelData) throws SQLException {
        //Set Header und get Meta Data of table
        String[] tableMetaDataHeader = {"Column Name", "Datatype", "Allow Null", "Default", "Key", "Extra"};
        String[][] tableMetaDataTemp = tablebrowser.database.DatabaseMetaData.getDescriptionTableView(connection, selectedTableView, tableMetaDataHeader);

        return convertFromArray(tableMetaDataTemp, emptyTableModel(tableModelData));
    }

    public static DefaultTableModel convertFromArray(String[][] stringArray, DefaultTableModel defaultTableModel) {
        int x = 0;
        for (String[] s : stringArray) {
            if (x == 0) {
                //Add Header to DefaultTableModel
                for (String s1 : s) {
                    defaultTableModel.addColumn(s1);
                }
            } else {
                //Add Cell to DefaultTableModel
                defaultTableModel.addRow(s);
            }
            x++;
        }

        return defaultTableModel;
    }

    public static DefaultTableModel emptyTableModel(DefaultTableModel defaultTableModel) {
        //Empty current DefaultTableModel
        int row = defaultTableModel.getRowCount();
        for (int i = 0; i < row; i++) {
            defaultTableModel.removeRow(0);
        }
        defaultTableModel.setColumnCount(0);

        return defaultTableModel;
    }

    public static DefaultTableModel getValueData(Connection connection, String selectedTableView, DefaultTableModel tableModelData) throws SQLException {
        //Set SQL Query und get Value of table/view
        String sqlQuery = "SELECT * FROM " + selectedTableView;
        String[][] valueDateTemp = tablebrowser.database.DatabaseMetaData.getSelectTable(connection, sqlQuery);

        return convertFromArray(valueDateTemp, emptyTableModel(tableModelData));
    }

    public static DefaultTableModel getTableFKData(Connection connection, String selectedTableView, DefaultTableModel tableModelData) throws SQLException {
        //Set Header und get Foreign Key List of table
        String[] tableFKDataHeader = {"Foreign Key Name", "Column", "Reference Table", "Foreign Column"};
        String[][] tableFKDataTemp = tablebrowser.database.DatabaseMetaData.getForeignKeyTable(connection, selectedTableView, tableFKDataHeader);

        return convertFromArray(tableFKDataTemp, emptyTableModel(tableModelData));
    }

    public static DefaultTableModel getTableCreateData(Connection connection, String selectedTableView, DefaultTableModel tableModelData) throws SQLException {
        //Get Create Code of Table
        String[][] tableCreateDataTemp = tablebrowser.database.DatabaseMetaData.getCreateTable(connection, selectedTableView);

        return convertFromArray(tableCreateDataTemp, emptyTableModel(tableModelData));
    }

    public static void editTree(JTree tree, int startingIndex, int rowCount) {
        //Open all Nodes
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            editTree(tree, rowCount, tree.getRowCount());
        }
    }

    public static void addDatabase(DefaultListModel<String> propertiesData) {
        //Remove existing .properties File
        propertiesData.removeAllElements();

        //Get all .properties File
        String[] propertiesDataTemp = DatabaseConnection.getProperties();
        for (String s : propertiesDataTemp) {
            propertiesData.addElement(s);
        }
    }

    public static void addTableView(Connection connection, DefaultMutableTreeNode root) throws SQLException {
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode subNode;

        //Add Table
        node = new DefaultMutableTreeNode("Table");
        root.add(node);

        String[] tableTemp = tablebrowser.database.DatabaseMetaData.getTableViewName(connection, "TABLE");
        for (String s : tableTemp) {
            subNode = new DefaultMutableTreeNode(s);
            node.add(subNode);
        }

        String[] viewTemp = tablebrowser.database.DatabaseMetaData.getTableViewName(connection, "VIEW");
        for (String s : viewTemp) {
            subNode = new DefaultMutableTreeNode(s);
            node.add(subNode);
        }
    }

    public static void executeSQLQuery(Connection connection, String sqlQuery, JTabbedPane tabbedPane) {
        //Remove all Tab
        removeAllTabs(tabbedPane);
        int sqlQueryCount = 1;

        //Split SQL Queries after semicolon
        sqlQuery = sqlQuery.replace("\n", "");
        String[] sqlQueryTemp = sqlQuery.split(";");
        String[][] result;

        for (String s : sqlQueryTemp) {
            try {
                if (s.startsWith("SELECT")) {
                    //Execute SELECT SQL Query
                    result = tablebrowser.database.DatabaseMetaData.getSelectTable(connection, s);
                } else if (s.startsWith("INSERT") || s.startsWith("DELETE") || s.startsWith("UPDATE")) {
                    //Execute INSERT/DELETE/UPDATE SQL Query
                    int i = DatabaseMetaData.executeUpdate(connection, s);
                    result = new String[][]{{"Rows Updated"}, {Integer.toString(i)}};
                } else if (s.startsWith("CREATE") || s.startsWith("ALTER") || s.startsWith("DROP")) {
                    //Execute CREATE/ALTER/DROP SQL Query
                    result = new String[][]{{"Query successful"}};
                } else {
                    //Print Error Text
                    result = new String[][]{{"Error"}, {"No correct SQL Query"}};
                }
            } catch (SQLException sqlException) {
                //Print Error Text
                result = new String[][]{{"Error"}, {sqlException.toString()}};
            }

            //Print Result to Tab
            addTab(tabbedPane, convertFromArray(result, new DefaultTableModel()), sqlQueryCount);
            sqlQueryCount++;
        }
    }

    public static void removeAllTabs(JTabbedPane tabbedPane) {
        //Remove all Tabs
        int count = tabbedPane.getTabCount();
        for (int i = 0; i < count; i++) {
            tabbedPane.remove(i);
        }
    }

    public static void addTab(JTabbedPane tabbedPane, DefaultTableModel tableModel, int sqlQueryCount) {
        //Create JTable and JScrollPane
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(table);
        editTable(table);

        //Add JTable to Tab
        tabbedPane.addTab("Result: " + sqlQueryCount, scrollPane);
    }

    public static void editTable(JTable table) {
        //Add space left and right of Cell
        table.setIntercellSpacing(new Dimension(5, 0));

        //Edit column width (min 100px)
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
}
