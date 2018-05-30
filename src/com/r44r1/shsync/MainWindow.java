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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;

import java.io.File;
import java.io.IOException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FillLayout;

public class MainWindow {

	protected Shell shell;
	protected static String phonePath;
	protected static Statement dbQuery;
//	private Table table;
	private Table fileListTable;
	protected String[] toDoFileList;
	protected static String ffBinary = "none";
	private static Text consoleBox;
	SashForm mainForm;
	private static final int minTableHeight = 350;
	private static final int barHeight = 50;

	/**
	 * Launch the application.
	 * @param args
	 */
	
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void redraw() {
		shell.layout();
		shell.redraw();
		shell.update();
	}
	
	public static void addLog(String log) {
		consoleBox.append(log);
	}
	
	public String[] updateValues (TableItem item, String status) {
	// Dumb but...
	// Get current values from columns 1-7
	String[] currentValues = new String [] {"","","","","","","",""};
	int i = 1;
	while (i<8) {
		currentValues[i] = item.getText(i);
		i++;
	}
	// Add result to head
	currentValues[0] = status;
	return currentValues;
}
	
	public void updateRowStatus (TableItem item, String status)
	{
		item.setText(updateValues(item,status));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		redraw();
	}		
	
	public static void debugPrint(String arg0) {
		// Why not?
		System.out.println(arg0);
	}
	
	public static String getPhonePath () {
		return phonePath;
	}
	
	public static Statement getQuery () {
		return dbQuery;
	}
	
	public static String getFF () {
		return ffBinary;
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		//display.timerExec(500, new Runnable( ) {
		//	public void run() {
				//fileListTable = new Table(mainForm, SWT.BORDER | SWT.FULL_SELECTION);
		//	}
		//});
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1279, 600);
		shell.setText("Docomo SHsync v0.1b");
		shell.setLayout(new FillLayout());
		mainForm = new SashForm(shell, SWT.VERTICAL);
		mainForm.setSashWidth(1);
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		// Menubar - File
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_2 = new Menu(mntmFile);
		mntmFile.setMenu(menu_2);
		
		// Menu - Open Phone
		MenuItem mntmOpenPhone = new MenuItem(menu_2, SWT.NONE);
		mntmOpenPhone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				phonePath = FileOps.openSD(shell);		
				try{
					dbQuery = FileOps.checkDB(shell);
				}
				catch (Exception x) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("Error");
					messageBox.setMessage("Error occured while creating database.\n"
							+ "Please, check permissions and try again.");
					messageBox.open();
				}
			}
		});
		mntmOpenPhone.setText("Open Phone");
		
		/* Filelist table */
		
		// Menu - Add Files
		MenuItem mntmAddFiles = new MenuItem(menu_2, SWT.NONE);
		mntmAddFiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.MULTI);
				dialog.setFilterNames(new String[] {"Windows Media Audio Files (*.wma)",
						"Audio Files (*.mp3, *.aac, *.wav, *.ac3, *.amr, *.m4a, *.ape, *.flac, *.wma, *.ogg,"
						+ " *.oga)","All Files (*.*)"});
					
				// Little care about case sensitive FS
				dialog.setFilterExtensions(new String[] {"*.wma;*.WMA","*.mp3;*.aac;*.wav;*.ac3;*.amr;*.m4a;"
						+ "*.ape;*.flac;*.wma;*.ogg;*.oga;*.MP3;*.AAC;*.WAV;*.AC3;*.AMR;*.M4A;"
						+ "*.APE;*.FLAC;*.WMA;*.OGG;*.OGA","*.*"});
			    if (dialog.open() != null) {
					String DirPath = dialog.getFilterPath();				
			    	String[] fList = dialog.getFileNames();
			    	for(int i=0; i<fList.length;i++) {
			    		fList[i] = DirPath+File.separator+fList[i];
			    	}
			    	FileOps.populateFileTable(fileListTable, fList);
			    }
			}
		});
		mntmAddFiles.setText("Add File(s)");
		
		fileListTable = new Table(mainForm, SWT.BORDER | SWT.FULL_SELECTION);
		fileListTable.setHeaderVisible(true);
		fileListTable.setLinesVisible(true);
		
		// Remove item by double click
		fileListTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				fileListTable.remove(fileListTable.getSelectionIndices());
			}
		});
		
		// Remove item by select+del
		fileListTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fileListTable.addKeyListener(new KeyListener() {
					public void keyReleased(KeyEvent event) {
						if (event.keyCode == SWT.DEL) fileListTable.remove(fileListTable.getSelectionIndices());
					}
					public void keyPressed(KeyEvent arg0) {						
					}
				});
				//
			}
		});
		
		TableColumn fileStatus = new TableColumn(fileListTable, SWT.NONE);
		fileStatus.setWidth(60);
		fileStatus.setText("Status");		
		
		final TableColumn fileFullPath = new TableColumn(fileListTable, SWT.NONE);
		fileFullPath.setWidth(499);
		fileFullPath.setText("File Path");
		
		TableColumn fileArtist = new TableColumn(fileListTable, SWT.NONE);
		fileArtist.setWidth(164);
		fileArtist.setText("Artist");
		
		TableColumn fileTitle = new TableColumn(fileListTable, SWT.NONE);
		fileTitle.setWidth(169);
		fileTitle.setText("Title");
		
		TableColumn fileAlbum = new TableColumn(fileListTable, SWT.NONE);
		fileAlbum.setWidth(164);
		fileAlbum.setText("Album");
		
		TableColumn fileRate = new TableColumn(fileListTable, SWT.NONE);
		fileRate.setWidth(60);
		fileRate.setText("BRate");
		
		TableColumn fileDuration = new TableColumn(fileListTable, SWT.NONE);
		fileDuration.setWidth(60);
		fileDuration.setText("Time");
		
		TableColumn fileSize = new TableColumn(fileListTable, SWT.NONE);
		fileSize.setWidth(80);
		fileSize.setText("Size");
		
		// Drag-n-Drop routine
		DropTarget dropTable = new DropTarget(fileListTable, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final FileTransfer fTrans = FileTransfer.getInstance();
		dropTable.setTransfer(fTrans);
		
		dropTable.addDropListener(new DropTargetAdapter() {
			
			public void drop(DropTargetEvent event) {
				if (fTrans.isSupportedType(event.currentDataType)) {
					String[] fList = (String[])event.data;
			    	FileOps.populateFileTable(fileListTable, fList);
				}
			}
		});
		
		/* End of filelist table */
		
		final ProgressBar progressBar = new ProgressBar(mainForm, SWT.NULL);
		consoleBox = new Text(mainForm, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		
		// Menu - Sync DB

		MenuItem mntmSyncDb = new MenuItem(menu_2, SWT.NONE);
		mntmSyncDb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( dbQuery != null ) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("Error");
					messageBox.setMessage("You should attach DB first!");
					messageBox.open();
				} else {
					String[] fileList = FileOps.getFileList(fileListTable);
					if (fileList.length == 0) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Error");
						messageBox.setMessage("You should add files first!");
						messageBox.open();
					} else {
						progressBar.setMinimum(0);
						progressBar.setMaximum(fileList.length);
						// Clear, in case of re-click
						progressBar.setSelection(0);
						TableItem[] items = fileListTable.getItems();
						for(int i=0; i<fileList.length;i++) {
							items[i].setText(updateValues(items[i], ""));
						}
						redraw();
						// Begin sync
						for(int i=0; i<fileList.length; i++) {
							//fileListTable.setTopIndex(i);
							//fileListTable.showSelection();
							progressBar.setSelection(i+1);
							String status = new String("");
							String error = new String("");
							
							File SongFile = new File(fileList[i]);
							Song CurrentSong = new Song(SongFile);							
							debugPrint("Input file: "+fileList[i]);
							error = FileOps.encode(shell, SongFile, CurrentSong);							
							debugPrint("Encode status: "+error);
							
							// I hate myself for this, but too lazy to rewrite
							if (error.equals("fail")) {
								// Skips copy to phone - encoder crashed
								status = "FF ERR";
								updateRowStatus(items[i], status);
								continue;
							} else { 
								// Skips copy if file with same name (without extension) & tags present on SD
								// It will be the same file in most cases, result of previous encode or copy
								
								if (error.matches("deny")) {
									status = "CP SKP";
									debugPrint("Copy status: "+status+"\n");
									updateRowStatus(items[i], status);
									continue;
								} else {
									if (error.equals("copy")) {
										// Copy input file - only for WMA <256 absent on SD
										status = "FF SKP";
										updateRowStatus(items[i], status);
										error = FileOps.copyFile(CurrentSong, SongFile);
										if (error.equals("fail") ) status = "CP ERR";
											else status = "CP OK";
										debugPrint("Copy status: "+status+"\n");
										updateRowStatus(items[i], status);
									} else {
										// Copy encoded file
										status = "FF OK";
										updateRowStatus(items[i], status);
										// "error" should contain full path to encoded song here
										File newSongFile = new File(error);
										Song newCurrentSong = new Song(newSongFile);
										error = FileOps.copyFile(newCurrentSong, newSongFile);
										debugPrint(error);
										if (error.equals("fail")) status = "CP ERR";
											else status = "CP OK";
										debugPrint("Copy status: "+status+"\n");
										newSongFile.delete();
										updateRowStatus(items[i], status);
									}
								}
							}
							
							if (status.equals("CP OK")) {
								// now "error" store absolute path to copied song on SD
								
								//TODO: invoke DB updater
								// Statement dbQuery - connected to DB now
								updateRowStatus(items[i], status);
							}	
						}
					}
				}
			}
		});
		mntmSyncDb.setText("Sync DB");
		
		// FFmpeg path
		MenuItem mntmSetFFmpegPath = new MenuItem(menu_2, SWT.NONE);
		mntmSetFFmpegPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.MULTI);
				dialog.setFilterNames(new String[] {"Windows Executables (*.exe)","All Files (*.*)"});
				dialog.setFilterExtensions(new String[] {"*.exe;*.EXE","*.*"});
			    if (dialog.open() != null) {
					String DirPath = dialog.getFilterPath();				
			    	String Binary = dialog.getFileName();
			    	ffBinary = DirPath+File.separator+Binary;
			    }
			}
		});
		mntmSetFFmpegPath.setText("FFmpeg Path");
		
		// Menu - Exit
		MenuItem mntmExit = new MenuItem(menu_2, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		mntmExit.setText("Exit");
		
		// Menubar - Help
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_3 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_3);
		
		// Menu - HowTo
		MenuItem mntmHowToUse = new MenuItem(menu_3, SWT.NONE);
		mntmHowToUse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		        messageBox.setText("How to use");
		        messageBox.setMessage("Switch phone to microSD mode, connect it to PC via USB cable.\n\n"
		        		            + "Select 'Open Phone' from 'File' menu and point program to SD.\n\n"
		        		            + "Drop songs to window or select 'Add File(s)' from 'File' menu.\n\n"
		        		            + "To allow automatic re-encoding, download & install FFmpeg static\n"
		        		            + "binary from https://ffmpeg.zeranoe.com/builds/ and put it into\n"
		        		            + "program directory; or specify path to already existing installation\n"
		        		            + "by selecting 'FFmpeg Path' from 'File' menu\n\n"
		        		            + "Select 'Sync DB', wait a bit and enjoy your songs on your phone."
		        	);
		        messageBox.open();
			}
		});
		mntmHowToUse.setText("How to use");
		
		// Menu - About
		MenuItem mntmAbout = new MenuItem(menu_3, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		        messageBox.setText("About");
		        messageBox.setMessage("SHsync is software which allows you to sync music with your Docomo\n"
		        		            + "Symbian-based SH series mobile phone. Created as replacement for\n"
		        		            + "Docomo M-Sync by weaknespase, which don't work on modern OS.\n\n"
		        		            + "Source code can be found at: \n\n"
		        		            + "This software is distributed under Apache License 2.0.\n\n"
		        		            + "This software uses SQLite JDBC driver developed by Xerial.org,\n"
		        		            + "licensed under Apache License 2.0\n\n"
		        		            + "This software uses jAudiotagger library, developed by JThink Ltd.,\n"
		        		            + "licensed under LGPL v3 License and its source can be downloaded at\n"
		        		            + "https://bitbucket.org/ijabz/jaudiotagger/src/f53ffcf512182d7e1a4e14\n"
		        		            + "261e7a7510687497e3/?at=v2.2.4\n\n"
		        		            + "This software uses SWT library, developed by Eclipse Foundation,\n"
		        		            + "licensed under Eclipse Public License 1.0\n\n"
		        		            + "In order to get encode feature work, you should manually download\n"
		        		            + "FFmpeg static binary from https://ffmpeg.zeranoe.com/builds/ and\n"
		        		            + "put it next to the program; or specify path to existing installation\n\n"
		        		            + "If you still have any questions, contact me at claie@aol.jp");
		        messageBox.open();
			}
		});
		mntmAbout.setText("About");
			    
		// Should be called after all children spawn!
		mainForm.setWeights(new int[] {700, 100, 200});
		
		// Dynamic resizing with fixed element size
		shell.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				int height = shell.getClientArea().height;
				int width = shell.getClientArea().width;
				//Adjust path column according to window width
				if (width > 1279) {
					fileFullPath.setWidth(width-761);
					redraw();
				}
				int[] weights = mainForm.getWeights();
				if (height >= minTableHeight + barHeight) {
					float barPercent = (float)barHeight / height;
					weights [1] = (int) Math.round((10000 * barPercent));
					weights [2] = 2 * weights[1];
					weights [0] = 10000 - weights [1] - weights [2];
				} else {
					weights [0] = 700;
					weights [1] = 100;
					weights [2] = 200;
				}
				mainForm.setWeights(weights);	
			}
		});

	}
}