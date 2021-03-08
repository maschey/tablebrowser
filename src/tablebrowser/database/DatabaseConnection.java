package tablebrowser.database;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class DatabaseConnection {
    private static final String PROPERTIES_FILE_ENDING = ".properties";
    private static final String PROPERTIES_PATH = "src/properties/";


    public static String[] getProperties() {
        //Define Path
        File f = new File("src/properties.");
        File[] dirFiles = f.listFiles();

        //Add File to ArrayList
        ArrayList<String> propertiesTemp = new ArrayList<>();
        for (int i = 0; i < Objects.requireNonNull(dirFiles).length; i++) {
            if (dirFiles[i].getName().endsWith((PROPERTIES_FILE_ENDING))) {
                propertiesTemp.add(dirFiles[i].getName().substring(0, dirFiles[i].getName().length() - 11));
            }
        }

        //Convert ArrayList to Array
        return propertiesTemp.toArray(new String[0]);
    }

    public static String[] readPropertiesFile(String database) throws IOException {
        //Define .properties File
        File dir = new File(PROPERTIES_PATH + database + PROPERTIES_FILE_ENDING);

        //Get Data from file
        String[] connectionData = new String[4];
        if (dir.exists()) {
            try (FileReader fileReader = new FileReader(PROPERTIES_PATH + database + PROPERTIES_FILE_ENDING)) {
                Properties prop = new Properties();
                prop.load(fileReader);

                connectionData[0] = prop.getProperty("databaseUrl");
                connectionData[1] = prop.getProperty("database");
                connectionData[2] = prop.getProperty("databaseUser");
                connectionData[3] = prop.getProperty("databasePassword");
            }
        }

        return connectionData;
    }

    public static void savePropertiesFile(String database, String databaseUrl, String databaseUser, String databasePassword) throws IOException {
        //Define .properties File
        FileWriter fileWriter = new FileWriter(PROPERTIES_PATH + database + PROPERTIES_FILE_ENDING);
        try (BufferedWriter out = new BufferedWriter(fileWriter)) {

            //Write Data into File
            out.write("databaseUrl=" + databaseUrl + "\n");
            out.write("database=" + database + "\n");
            out.write("databaseUser=" + databaseUser + "\n");
            out.write("databasePassword=" + databasePassword + "\n");
        }
        fileWriter.close();
    }

    public static void renamePropertiesFile(String oldDatabase, String newDatabase) {
        //Define .properties Files
        File fileStreamOld = new File(PROPERTIES_PATH + oldDatabase + PROPERTIES_FILE_ENDING);
        File fileStreamNew = new File(PROPERTIES_PATH + newDatabase + PROPERTIES_FILE_ENDING);

        //Rename File
        if (!fileStreamOld.renameTo(fileStreamNew)) {
            System.err.println("Error. Not Renamed");
        }
    }

    public static void deletePropertiesFile(String database) {
        //Define .properties File
        File fileStream = new File(PROPERTIES_PATH + database + PROPERTIES_FILE_ENDING);

        //Delete File
        if (!fileStream.delete()) {
            System.err.println("Error. Not Deleted");
        }
    }

    public static Connection getDatabaseConnection(String databaseUrl, String database, String databaseUser, String databasePassword, String databaseDriver) throws ClassNotFoundException, SQLException {
        //Make Connection
        Class.forName(databaseDriver);

        return DriverManager.getConnection(databaseUrl + "/" + database + "?autoReconnect=true&useSSL=false", databaseUser, databasePassword);
    }
}
