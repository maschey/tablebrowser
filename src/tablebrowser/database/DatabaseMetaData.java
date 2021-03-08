package tablebrowser.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMetaData {
    static Statement statement;

    public static String[] getTableViewName(Connection connection, String... types) throws SQLException {
        //Get all table/view from Database
        String connectionCatalog = connection.getCatalog();
        String connectionSchema = connection.getSchema();
        java.sql.DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(connectionCatalog, connectionSchema, null, types);

        //Variables
        String[] tables = new String[getRowCount(resultSet)];

        //Convert ResultSet to Array
        int i = 0;
        while (resultSet.next()) {
            tables[i++] = resultSet.getString("TABLE_NAME");
        }

        return tables;
    }

    public static int getRowCount(ResultSet resultSet) throws SQLException {
        //Get Row Count
        resultSet.last();
        int row = resultSet.getRow();
        resultSet.beforeFirst();

        return row;
    }

    public static String[][] getDescriptionTableView(Connection connection, String table, String[] header) throws SQLException {
        //Get table/view Column
        ResultSet resultSet = getColumns(connection, table);

        //Variables
        int rowCount = getRowCount(resultSet);
        int columnCount = header.length;
        String[][] descriptionTable = new String[rowCount + 1][columnCount];
        String[][] primaryKeys = getPrimaryKeys(connection, table);
        String[][] foreignKeys = getForeignKeys(connection, table);

        //Add Header to array
        for (int i = 0; i < columnCount; i++) {
            descriptionTable[0][i] = header[i];
            i++;
        }

        //Add Column Info to array
        int row = 1;
        while (resultSet.next()) {
            descriptionTable[row][0] = resultSet.getString("COLUMN_NAME");
            descriptionTable[row][1] = getColumnTypeString(resultSet.getString("TYPE_NAME"), resultSet.getInt("COLUMN_SIZE"), resultSet.getInt("DECIMAL_DIGITS"));
            descriptionTable[row][2] = resultSet.getString("IS_NULLABLE");
            descriptionTable[row][3] = resultSet.getString("COLUMN_DEF") == null ? "NULL" : resultSet.getString("COLUMN_DEF");
            descriptionTable[row][4] = contains(primaryKeys, foreignKeys, resultSet.getString("COLUMN_NAME"));
            descriptionTable[row][5] = resultSet.getString("IS_AUTOINCREMENT").equals("YES") ? "auto_increment" : "";
            row++;
        }

        return descriptionTable;
    }

    public static ResultSet getColumns(Connection connection, String table) throws SQLException {
        //Get Table Column
        String connectionCatalog = connection.getCatalog();
        String connectionSchema = connection.getSchema();
        java.sql.DatabaseMetaData connectionMetaData = connection.getMetaData();

        return connectionMetaData.getColumns(connectionCatalog, connectionSchema, table, null);
    }

    public static String[][] getPrimaryKeys(Connection connection, String table) throws SQLException {
        //Get Primary Key
        String connectionCatalog = connection.getCatalog();
        String connectionSchema = connection.getSchema();
        java.sql.DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getPrimaryKeys(connectionCatalog, connectionSchema, table);

        //Add to array
        String[][] primaryKeys = new String[getRowCount(resultSet)][2];
        int i = 0;
        while (resultSet.next()) {
            primaryKeys[i][0] = resultSet.getString("COLUMN_NAME");
            primaryKeys[i++][1] = resultSet.getString("KEY_SEQ");
        }

        return primaryKeys;
    }

    public static String[][] getForeignKeys(Connection connection, String table) throws SQLException {
        //Get Foreign Key
        String connectionCatalog = connection.getCatalog();
        String connectionSchema = connection.getSchema();
        java.sql.DatabaseMetaData connectionMetaData = connection.getMetaData();
        ResultSet resultSet = connectionMetaData.getImportedKeys(connectionCatalog, connectionSchema, table);

        //Add to array
        String[][] foreignKeys = new String[getRowCount(resultSet)][4];
        int i = 0;
        while (resultSet.next()) {
            foreignKeys[i][0] = resultSet.getString("FK_NAME");
            foreignKeys[i][1] = resultSet.getString("FKCOLUMN_NAME");
            foreignKeys[i][2] = resultSet.getString("PKTABLE_NAME");
            foreignKeys[i++][3] = resultSet.getString("PKCOLUMN_NAME");
        }

        return foreignKeys;
    }

    public static String getColumnTypeString(String type, int size, int decimal) {
        //Add Column Type and Size to string
        StringBuilder buf = new StringBuilder();
        buf.append(type);

        if (size > 0) {
            buf.append("(");
            buf.append(size);
            if (decimal > 0) {
                buf.append(",");
                buf.append(decimal);
            }
            buf.append(")");
        }

        return buf.toString();
    }

    public static String contains(String[][] primaryKes, String[][] foreignKeys, String columnName) {
        //Variables
        boolean isPrimaryKeyFound = false;
        boolean isForeignKeyFound = false;
        String returnValue = "";

        //Check Column if Primary Key or Foreign Key
        for (String[] pk : primaryKes) {
            if (pk[0].equals(columnName)) {
                isPrimaryKeyFound = true;
                break;
            }
        }
        for (String[] fk : foreignKeys) {
            if (fk[1].equals(columnName)) {
                isForeignKeyFound = true;
                break;
            }
        }

        //Add to string
        if (isPrimaryKeyFound) {
            returnValue = "PRIMARY KEY";
        } else if (isForeignKeyFound) {
            returnValue = "FOREIGN KEY";
        }

        return returnValue;
    }

    public static boolean execute(Connection connection, String query) throws SQLException {
        //execute()
        statement = connection.createStatement();
        return statement.execute(query);
    }

    public static String[][] getForeignKeyTable(Connection connection, String table, String[] header) throws SQLException {
        //Variables
        int rowCount = getForeignKeys(connection, table).length;
        int columnCount = header.length;
        String[][] foreignKeyTable = new String[rowCount + 1][columnCount];
        String[][] foreignKeys = getForeignKeys(connection, table);

        //Add Header to array
        for (int i = 0; i < columnCount; i++) {
            foreignKeyTable[0][i] = header[i];
            i++;
        }

        //Add Foreign Key Info to array
        int row = 1;
        for (String[] s : foreignKeys) {
            foreignKeyTable[row][0] = s[0];
            foreignKeyTable[row][1] = s[1];
            foreignKeyTable[row][2] = s[2];
            foreignKeyTable[row++][3] = s[3];
        }

        return foreignKeyTable;
    }

    public static String[][] getCreateTable(Connection connection, String table) throws SQLException {
        //Get Table Column
        ResultSet resultSet = getColumns(connection, table);

        //Variables
        int rowCount = getRowCount(resultSet);
        String[][] createTable = new String[2][1];
        StringBuilder createCommand = new StringBuilder("CREATE TABLE '" + table + "' (");
        String[][] primaryKeys = getPrimaryKeys(connection, table);
        String[][] foreignKeys = getForeignKeys(connection, table);
        String[][] indexes = getIndexes(connection, table);

        //Add Column to string
        int row = 0;
        String[] temp = new String[rowCount];
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            String columnType = getColumnTypeString(resultSet.getString("TYPE_NAME"), resultSet.getInt("COLUMN_SIZE"), resultSet.getInt("DECIMAL_DIGITS"));
            String columnNullable = resultSet.getString("IS_NULLABLE").equals("NO") ? " NOT NULL" : "";
            String columnDefault = resultSet.getString("COLUMN_DEF") == null ? "" : " DEFAULT '" + resultSet.getString("COLUMN_DEF") + "'";
            String columnAutoIncrement = resultSet.getString("IS_AUTOINCREMENT").equals("YES") ? " AUTO_INCREMENT" : "";
            temp[row++] = columnName + " " + columnType + columnNullable + columnDefault + columnAutoIncrement;
        }
        for (String s : temp) {
            createCommand.append(" ").append(s).append(",");
        }

        //Add Primary Key to string
        createCommand.append(" PRIMARY KEY (");
        for (String[] primaryKey : primaryKeys) {
            createCommand.append("'").append(primaryKey[0]).append("', ");
        }
        createCommand = new StringBuilder(createCommand.substring(0, createCommand.length() - 2) + "),");

        //Add Index to string
        for (String[] index : indexes) {
            if (index[0] != null && index[1] != null) {
                createCommand.append(" INDEX '").append(index[0]).append("' ('").append(index[1]).append("'),");
            }
        }

        //Add Foreign Key to string
        for (String[] foreignKey : foreignKeys) {
            createCommand.append(" CONSTRAINT '").append(foreignKey[0]).append("' FOREIGN KEY ('").append(foreignKey[1]).append("') REFERENCES '").append(foreignKey[2]).append("' ('").append(foreignKey[3]).append("'),");
        }

        createCommand.append(createCommand.substring(0, createCommand.length() - 1)).append(");");

        //Add to array
        createTable[0][0] = "CREATE code";
        createTable[1][0] = createCommand.toString();

        return createTable;
    }

    public static String[][] getIndexes(Connection connection, String table) throws SQLException {
        //Get Indexes
        String connectionCatalog = connection.getCatalog();
        String connectionSchema = connection.getSchema();
        java.sql.DatabaseMetaData connectionMetaData = connection.getMetaData();
        ResultSet resultSet = connectionMetaData.getIndexInfo(connectionCatalog, connectionSchema, table, false, false);

        //Add to array
        String[][] iks = new String[getRowCount(resultSet)][2];
        int i = 0;
        while (resultSet.next()) {
            if (!resultSet.getString("INDEX_NAME").equals("PRIMARY")) {
                iks[i][0] = resultSet.getString("INDEX_NAME");
                iks[i++][1] = resultSet.getString("COLUMN_NAME");
            }
        }
        return iks;
    }

    public static String[][] getSelectTable(Connection connection, String selectQuery) throws SQLException {
        //Execute Query
        ResultSet resultSet = executeQuery(connection, selectQuery);

        //Variables
        int rowCount = getRowCount(resultSet);
        int columnCount = getColumnCount(resultSet);
        String[][] selectTable = new String[rowCount + 1][columnCount];

        //Add Header to array
        for (int col = 1; col <= columnCount; col++) {
            selectTable[0][col - 1] = resultSet.getMetaData().getColumnLabel(col);
        }

        //Add Value to array
        int row = 1;
        while (resultSet.next()) {
            for (int col = 1; col <= columnCount; col++) {
                final Object o = resultSet.getObject(col);
                selectTable[row][col - 1] = o == null ? "null" : o.toString();
            }
            row++;
        }

        return selectTable;
    }

    public static ResultSet executeQuery(Connection connection, String query) throws SQLException {
        //executeQuery()
        statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public static int getColumnCount(ResultSet resultSet) throws SQLException {
        //Get Column Count
        return resultSet.getMetaData().getColumnCount();
    }

    public static int executeUpdate(Connection connection, String query) throws SQLException {
        //executeUpdate()
        statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
}
