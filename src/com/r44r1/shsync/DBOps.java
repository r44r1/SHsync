/*
 * © Copyright 2018 r44r1 [claie@aol.jp]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.r44r1.shsync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DBOps {
	//Use single DB handler instance
	public static volatile DBOps instance;
	public static DBOps getInstance() {
		DBOps lInstance = instance;
		if (lInstance == null) {
			synchronized (DBOps.class) {
				lInstance = instance;
				if (lInstance == null) {
					instance = lInstance = new DBOps();
				}
			}
		}
		return lInstance;
	}

	public static Statement attachDB(Shell shell) {
		Connection db = null;
		Statement dbQuery = null;
		try {
			String path = "jdbc:sqlite:"+FileOps.getConnectorPath();
			db = DriverManager.getConnection(path);
			dbQuery = db.createStatement();
			dbQuery.setQueryTimeout(15);
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setText("Success");
			messageBox.setMessage("Database attached.");
			messageBox.open();
		} catch (SQLException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Error");
			messageBox.setMessage("Unable to open database file.\n\n"+e.getMessage());
			messageBox.open();
		}
		return dbQuery;
	}
	
/*	public static String getAll (Statement db) {
		String out = new String();
		try {
			ResultSet result = db.executeQuery("select * from songinfo");
			while(result.next())
			{
				out += result.getString("DB_Song_RootPath") + "\n";
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return out;
	}
*/
}
