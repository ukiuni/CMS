package test.org.ukiuni.report.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

public class DBTestUtil {
	public static String MODE_UNIT_TEST = "unitTest";
	public static String MODE_INTEGRATION_TEST = "integrationTest";

	public static IDatabaseConnection getConnection(String mode) throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:file:./test/db/" + mode + ".db", "test", "test");
		return new DatabaseConnection(conn);
	}

	public static IDataSet getDataSet(String xml) throws DataSetException, FileNotFoundException {
		return new FlatXmlDataSetBuilder().build(new FileInputStream("./test/cause/" + xml));
	}

	public static void setUpDbWithXML(String mode, String xml) {
		try {
			IDatabaseConnection connection = getConnection(mode);
			DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet(xml));
			connection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		IDatabaseConnection con = null;
		try {
			con = getConnection(MODE_UNIT_TEST);
			IDataSet dataset = con.createDataSet();
			FlatXmlDataSet.write(dataset, new FileOutputStream("./test/cause/export.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
