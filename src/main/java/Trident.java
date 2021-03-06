/*
 *  Trident.java
 *  (c) Copyright, 2020 - 2021 Krishna Moorthy
 *  akrishnamoorthy007@gmail.com | github.com/KrishnaMoorthy12
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// * Listeners

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;

// * IO ELEMENTS

import java.io.File;
import java.io.FileWriter;

// * Exceptions

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

// * UI Elements

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

// *Misc

import javax.swing.undo.UndoManager;

/*
 * Trident Text Editor v4.0 + 
 * @author: Krishna Moorthy
 */

class Trident {
  /*
   * Main Trident Application
   * 
   * Contains all the basic components and links all the libraries and
   * functionalities with Trident.
   * 
   * Handles all the errors and writes logs
   */

  public static boolean isRunning = true;

  protected static JTextArea textarea;
  protected static JFrame frame;
  public static JLabel status1, status2, status3, status4;
  public static String fileType, path;
  public static Boolean warned;
  public static JMenuBar mb;
  public static JScrollPane editor;
  public static JPanel statusBar, commentPanel, othersPanel;
  public static JMenu fileMenu, editMenu, settingsMenu, toolsMenu, about, ClipMenu, newSource, openRecent;
  public static JMenuItem newFile, newWindow, OpenFile, SaveFile, SaveAs, Exit, Undo, Redo, Copy, Cut, Paste, goTo,
      pCopy, pCut, pPaste, ShowClipboard, EraseClipboard, Find, Replace, StyleEditor, configs, Compile, Run, CRun,
      console, AboutFile, help, AboutTrident, updates, pyFile, javaFile, cFile, cppFile, htmlFile, bstrp, pboil;
  public static JMenuItem[] recentlyOpened;
  public static JCheckBoxMenuItem wordWrap, autoSave;
  public static JToolBar toolBar;
  public static UndoManager undoManager;
  public static JPopupMenu editorMenu;

  // * Listener Variable declarations
  static FileMenuListener fml = new FileMenuListener();
  static EditMenuListener eml = new EditMenuListener();
  static SettingsMenuListener sml = new SettingsMenuListener();
  static ToolsMenuListener tml = new ToolsMenuListener();
  static AboutMenuListener aml = new AboutMenuListener();

  public static void ErrorDialog(String code, Exception e) {
    try {
      /*
       * The Trident Error Handler :- * Handles Errors that occur during the runtime
       * of Trident and writes logs whenever an error occurs. * Shows Error dialog.
       * 
       * @param: Error code, excpection object.
       */
      writeLog(code, e);
      int option = JOptionPane.showConfirmDialog(frame,
          "An Unexpected error occured. \nThis may lead to a crash. Save any changes and continue. \nERROR CODE: "
              + code + "\nERROR NAME: " + e.getClass().getName() + "\nERROR CAUSE: " + e.getCause(),
          "Aw! Snap!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, new ImageIcon("raw/error.png"));
      if (option == JOptionPane.YES_OPTION) {
        try {
          Desktop.getDesktop().browse(java.net.URI.create(
              "https://github.com/KrishnaMoorthy12/trident/issues/new?assignees=&labels=&template=bug_report.md&title="));
        } catch (Exception shit) {
          ErrorDialog("DESKTOP_UNAVAILABLE", shit);
        }
        status1.setText("Thanks for your positive intent.");
      } else {
        status1.setText("Please report errors to help improve Trident.");
      }
    } catch (Exception dialogShowErr) {
      System.err.println("Trident was terminated with an unexpected error.");
      System.exit(-1);
      writeLog("CRASH_ERR_DIALOG", dialogShowErr);
    }
  }

  public static void writeLog(String code, Exception e) {
    /*
     * Writes error logs to %TridentPath%/logs/log.txt
     * 
     * @param: Error Code, Exception object
     */
    try {
      File logFile = new File("logs/error.log");
      logFile.createNewFile();
      FileWriter logWriter = new FileWriter(logFile, true);
      BufferedWriter writer = new BufferedWriter(logWriter);

      writer.write(System.lineSeparator());
      writer.write("TIME: " + LocalDateTime.now().toString());
      writer.write(System.lineSeparator());
      writer.write("ERR CODE: " + code + System.lineSeparator() + "Exception Class: " + e.getClass().getName()
          + System.lineSeparator() + "Cause: " + e.getCause() + System.lineSeparator() + "Error Message: "
          + e.getMessage());
      writer.write(System.lineSeparator());
      writer.write("------------------------------------------");
      writer.write(System.lineSeparator());

      for (StackTraceElement ste : e.getStackTrace()) {
        writer.write("[" + code + "] ");
        writer.write(ste.toString());
        writer.write(System.lineSeparator());
      }
      writer.write("=========================================================");

      writer.close();
      logWriter.close();
    } catch (IOException ioException) {
      // ErrorDialog("LOG_IO_ERR", ioException);
      ioException.printStackTrace();
      // System.out.println(ioException);
    }
  }

  public static final int checkOS() {
    /*
     * Checks the Operating System and returns a OS Code
     * 
     * @returns: Integer corresponding to the OS.
     */
    String operatingSystem = System.getProperty("os.name").toLowerCase();
    if (operatingSystem.contains("windows")) {
      return 1;
    } else if (operatingSystem.contains("linux")) {
      return 2;
    } else {
      return 3;
      // throw new UnsupportedOperatingSystemException();
    }
  }

  public Trident(String file) {
    /*
     * Initializes the Main application Trident and all the objects. Starts all the
     * basic processes and threads for the basic functioning of Trident.
     * 
     * @param: File name as string to open.
     */
    try {
      // * Global variable inits
      warned = false;
      fileType = "Plain File";
      textarea = new JTextArea();
      mb = new JMenuBar();
      path = file;

      // * Themeing
      Configurations.read();
      try {
        UIManager.setLookAndFeel(Configurations.themeName);
      } catch (Exception e) {
        Configurations.themeName = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(Configurations.themeName);
        Configurations.write();
        ErrorDialog("UI_THEME_ERR", e);
      }

      // * Frame Setup
      frame = new JFrame();
      frame.setTitle("Trident Text Editor - " + Paths.get(path).getFileName().toString());
      frame.setSize(800, 550);
      frame.setResizable(true);
      WindowListener WindowCloseListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          status1.setText("Exiting Trident...");
          if (warned) {
            int opt = FileMenuListener.warningDialog();
            if (opt == JOptionPane.NO_OPTION) {
              System.exit(0);
            } else {
              Trident.status1.setText("Ready.");
            }
          } else {
            System.exit(0);
          }
        }
      };

      frame.addWindowListener(WindowCloseListener);
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.setLayout(new BorderLayout());
      ImageIcon ic = new ImageIcon("raw/trident_icon.png");
      frame.setIconImage(ic.getImage());

      // * Menu Bar Setup
      // > File Menu
      fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);

      newFile = new JMenuItem("New");
      newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      fileMenu.add(newFile);
      newFile.addActionListener(fml);

      newWindow = new JMenuItem("New Window");
      newWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
          java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
      fileMenu.add(newWindow);
      newWindow.addActionListener(fml);

      newSource = new JMenu("New Source File");
      newSource.setMnemonic(KeyEvent.VK_O);
      cFile = new JMenuItem("C Source File");
      cFile.addActionListener(fml);
      newSource.add(cFile);
      cppFile = new JMenuItem("C++ Source File");
      cppFile.addActionListener(fml);
      newSource.add(cppFile);
      javaFile = new JMenuItem("Java Source File");
      javaFile.addActionListener(fml);
      newSource.add(javaFile);
      pyFile = new JMenuItem("Python Source File");
      pyFile.addActionListener(fml);
      newSource.add(pyFile);
      htmlFile = new JMenuItem("HTML File");
      htmlFile.addActionListener(fml);
      newSource.add(htmlFile);
      bstrp = new JMenuItem("Bootstrap");
      bstrp.addActionListener(fml);
      newSource.add(bstrp);
      pboil = new JMenuItem("Open PowerBoil");
      pboil.addActionListener(fml);
      newSource.add(pboil);
      fileMenu.add(newSource);

      OpenFile = new JMenuItem("Open");
      OpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      fileMenu.add(OpenFile);
      OpenFile.addActionListener(fml);

      openRecent = new JMenu("Open Recent");
      ArrayList<String> recents = RecentsTracker.getRecords();
      int num = 0;
      recentlyOpened = new JMenuItem[5];
      for (String rec : recents) {
        recentlyOpened[num] = new JMenuItem(rec);
        recentlyOpened[num].addActionListener(fml);
        openRecent.add(recentlyOpened[num]);
        num++;
      }
      fileMenu.add(openRecent);

      SaveFile = new JMenuItem("Save");
      SaveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      fileMenu.add(SaveFile);
      SaveFile.addActionListener(fml);

      SaveAs = new JMenuItem("Save As");
      SaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
          (java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK)));
      fileMenu.add(SaveAs);
      SaveAs.addActionListener(fml);

      Exit = new JMenuItem("Exit");
      Exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
      fileMenu.add(Exit);
      Exit.addActionListener(fml);
      // < File Menu

      // > Edit Menu
      editMenu = new JMenu("Edit");
      editMenu.setMnemonic(KeyEvent.VK_E);

      Undo = new JMenuItem("Undo");
      Undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      Undo.addActionListener(eml);
      editMenu.add(Undo);

      Redo = new JMenuItem("Redo");
      Redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      Redo.addActionListener(eml);
      editMenu.add(Redo);

      Copy = new JMenuItem("Copy");
      Copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      Copy.addActionListener(eml);
      editMenu.add(Copy);

      Cut = new JMenuItem("Cut");
      Cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      Cut.addActionListener(eml);
      editMenu.add(Cut);

      Paste = new JMenuItem("Paste");
      Paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      editMenu.add(Paste);
      Paste.addActionListener(eml);

      Find = new JMenuItem("Find");
      Find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      editMenu.add(Find);
      Find.addActionListener(eml);

      Replace = new JMenuItem("Replace");
      Replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
          java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
      editMenu.add(Replace);
      Replace.addActionListener(eml);

      goTo = new JMenuItem("Go To");
      goTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
      editMenu.add(goTo);
      goTo.addActionListener(eml);

      ClipMenu = new JMenu("Clipboard");
      editMenu.add(ClipMenu);
      ShowClipboard = new JMenuItem("Show Contents");
      ClipMenu.add(ShowClipboard);
      ShowClipboard.addActionListener(eml);
      EraseClipboard = new JMenuItem("Erase Contents");
      ClipMenu.add(EraseClipboard);
      EraseClipboard.addActionListener(eml);

      // < Edit Menu

      // > Settings Menu
      settingsMenu = new JMenu("Settings");
      settingsMenu.setMnemonic(KeyEvent.VK_S);

      wordWrap = new JCheckBoxMenuItem("Word Wrap", false);
      wordWrap.addItemListener(sml);
      settingsMenu.add(wordWrap);

      autoSave = new JCheckBoxMenuItem("Auto Save", true);
      autoSave.addItemListener(sml);
      settingsMenu.add(autoSave);

      configs = new JMenuItem("Configurations");
      configs.addActionListener(sml);
      settingsMenu.add(configs);
      // < Settings Menu

      // > Run Menu
      toolsMenu = new JMenu("Tools");
      toolsMenu.setMnemonic(KeyEvent.VK_T);

      Compile = new JMenuItem("Compile");
      Compile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, java.awt.event.InputEvent.ALT_DOWN_MASK));
      toolsMenu.add(Compile);
      Compile.addActionListener(tml);

      Run = new JMenuItem("Run");
      Run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, java.awt.event.InputEvent.ALT_DOWN_MASK));
      toolsMenu.add(Run);
      Run.addActionListener(tml);

      CRun = new JMenuItem("Compile and Run");
      CRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, java.awt.event.InputEvent.ALT_DOWN_MASK));
      toolsMenu.add(CRun);
      CRun.addActionListener(tml);

      console = new JMenuItem("Open Console");
      toolsMenu.add(console);
      console.addActionListener(tml);
      // < Run Menu

      // > About Menu
      about = new JMenu("About");

      AboutFile = new JMenuItem("File Properties");
      AboutFile.addActionListener(aml);
      about.add(AboutFile);

      help = new JMenuItem("Help");
      help.addActionListener(aml);
      about.add(help);

      AboutTrident = new JMenuItem("About Trident");
      about.add(AboutTrident);
      AboutTrident.addActionListener(aml);

      updates = new JMenuItem("Updates");
      about.add(updates);
      updates.addActionListener(aml);
      // < About Menu

      mb.add(fileMenu);
      mb.add(editMenu);
      mb.add(toolsMenu);
      mb.add(settingsMenu);
      mb.add(about);
      mb.setBorder(new EmptyBorder(0, 0, -1, 0));
      // * Menu bar setup ends here

      // * Pop up menu for text area
      editorMenu = new JPopupMenu();
      pCopy = new JMenuItem("Copy");
      pCopy.addActionListener(eml);
      pCut = new JMenuItem("Cut");
      pCut.addActionListener(eml);
      pPaste = new JMenuItem("Paste");
      pPaste.addActionListener(eml);

      editorMenu.add(pCopy);
      editorMenu.add(pCut);
      editorMenu.add(pPaste);

      // * Tool bar setup
      toolBar = new JToolBar();
      toolBar.setFloatable(false);
      new Toolbar();

      // * Text Area setup
      editor = new JScrollPane(textarea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      editor.setBorder(new EmptyBorder(0, 0, 0, 0));
      textarea.setBorder(new EmptyBorder(2, 2, 0, 0));
      textarea.setComponentPopupMenu(editorMenu);

      // > Listeners for Text Area
      AbstractDocument doc = (AbstractDocument) textarea.getDocument();
      doc.setDocumentFilter(new IndentListener());
      EditActionsListener eal = new EditActionsListener();
      undoManager = new UndoManager();
      eal.start();
      textarea.getDocument().addDocumentListener(new ChangeListener());
      textarea.getDocument().addDocumentListener(new AutoSave());
      textarea.addCaretListener(new LineNumberListener());
      textarea.getDocument().addUndoableEditListener(undoManager);
      Undo.setEnabled(false);
      Toolbar.undoButton.setEnabled(false);
      Redo.setEnabled(false);
      Toolbar.redoButton.setEnabled(false);

      // * Status bar setup
      statusBar = new JPanel();
      status1 = new JLabel("Ready.");
      status2 = new JLabel("Unsaved");
      status3 = new JLabel(fileType);
      status4 = new JLabel("Line: 1 Col: 1");

      CommentPaneListener cpl = new CommentPaneListener();
      cpl.start();

      statusBar.setBorder(new EmptyBorder(2, 3, 2, 2));
      statusBar.setLayout(new GridLayout(1, 2, 2, 2));

      commentPanel = new JPanel();
      othersPanel = new JPanel();
      commentPanel.setLayout(new GridLayout(1, 1, 0, 0));
      othersPanel.setLayout(new GridLayout(1, 3, 0, 0));

      commentPanel.add(status1);
      othersPanel.add(status2);
      othersPanel.add(status3);
      othersPanel.add(status4);

      statusBar.add(commentPanel);
      statusBar.add(othersPanel);
      // * Status bar setup ends here

      // * Apply settings
      Configurations.raw_apply();

      if (!path.equals("New File")) {
        FileMenuListener.openFile();
        FileMenuListener.saveFile();
        status1.setText("File opened using command-line.");
      }

      frame.setJMenuBar(mb);
      frame.getContentPane().add(toolBar, BorderLayout.PAGE_START);
      frame.getContentPane().add(editor, BorderLayout.CENTER);
      frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

      frame.setLocationRelativeTo(null);
      frame.setLocationByPlatform(true);
      frame.setVisible(true);

    } catch (Exception e) {
      ErrorDialog("MAIN_THREAD_CRASH", e);
      System.err.println("Unexpected crash...");
      e.printStackTrace();
      System.exit(0);
    }
  }

  public static void main(String[] args) {
    /**
     * TRIDENT MAIN METHOD:-
     * 
     * Initializes Trident
     * 
     * Controls Command-line instructions
     * 
     * @param: (Optional) File name/ path to open
     */
    if (args.length == 0)
      new Trident("New File");
    else {
      switch (args[0]) {
        case "-version":
        case "--version":
        case "-v":
          System.out.println("Trident Text Editor");
          System.out.print("Version: 5.0");
          System.out.println("\tChannel: Beta");
          System.out.println("(c) 2020 Krishna Moorthy Athinarayan. All rights reserved.");
          break;

        case "-readme":
          new Trident("readme.md");
          break;

        case "-reset":
          System.out.println("Restoring default settings...");
          try {
            Configurations.restoreDefaults();
            System.out.println("Restore successful.");
          } catch (IOException restoreErr) {
            System.err
                .println("Restotation unsuccessful.\nTry manually changing the values in configurations.tcf file.");
          }
          break;
        default:
          new Trident(args[0]);
      }
    }
  }
}
