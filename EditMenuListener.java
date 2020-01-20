
/*
 *  EditMenuListener.java
 *  (c) Copyright, 2020 - 2021 Krishna Moorthy
 *  akrishnamoorthy007@gmail.com | github.io/KrishnaMoorthy12
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/*
 * (GPL v3) Trident > EditMenuListener
 * @author: Krishna Moorthy
 */

class EditMenuListener implements ActionListener {
  /*
   * Influences the behaviour of Edit Menu
   */
  protected void showClipboard() {
    /*
     * Displays a text editor that shows clipboard contents
     */
    Clipboard clipboard;
    JDialog cbviewer = new JDialog();
    try {
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      cbviewer.setSize(450, 350);
      cbviewer.setTitle("Clipboard Viewer");
      JPanel TextViewer = new JPanel();
      JTextArea cta = new JTextArea(clipboard.getData(DataFlavor.stringFlavor).toString());
      JScrollPane spv = new JScrollPane(cta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      spv.setBorder(new EmptyBorder(-1, 0, -1, 0));
      TextViewer.setLayout(new GridLayout(1, 1, 1, 1));
      cbviewer.setLayout(new BorderLayout());
      TextViewer.add(spv);
      cbviewer.getContentPane().add(TextViewer, BorderLayout.CENTER);
      cbviewer.setLocationRelativeTo(Trident.frame);
      cbviewer.setVisible(true);
    } catch (UnsupportedFlavorException ufe) {
      // Trident.ErrorDialog("FLAVOR_ERR", ufe); // Don't throw unnecessary errors
      Trident.status1.setText("Clipboard has some unsupported content.");
      cbviewer.dispose();
    } catch (IOException ioe) {
      // Trident.ErrorDialog("IOE_CLIPBOARD", ioe); <- Avoid
    }
  }

  public void actionPerformed(ActionEvent e) {
    /*
     * Controls the actions of Edit Menu items
     */
    try {
      switch (e.getActionCommand()) {
      case "Show Contents":
        showClipboard();
        break;

      case "Erase Contents":
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
        break;

      case "Cut":
        Trident.textarea.cut();
        break;

      case "Copy":
        Trident.textarea.copy();
        break;

      case "Paste":
        Trident.textarea.paste();
        break;

      case "Undo":
        Trident.undoManager.undo();
        Trident.status1.setText("Ready.");
        Trident.Redo.setEnabled(true);
        Toolbar.redoButton.setEnabled(true);
        break;

      case "Redo":
        Trident.undoManager.redo();
        Trident.Undo.setEnabled(true);
        Toolbar.undoButton.setEnabled(true);
        Trident.status1.setText("Ready.");
        break;

      case "Find":
        FindReplace.showUI("Find");
        break;

      case "Replace":
        FindReplace.showUI("Replace");
        break;

      case "Go To":
        GoToController.go();
        break;
      }
    } catch (CannotRedoException redoErr) {
      Trident.status1.setText("No more Redos available.");
      Trident.Redo.setEnabled(false);
      Toolbar.redoButton.setEnabled(false);
    } catch (CannotUndoException undoErr) {
      Trident.status1.setText("No more Undos available.");
      Trident.Undo.setEnabled(false);
      Toolbar.undoButton.setEnabled(false);
    } catch (HeadlessException noHead) {
      // Trident.ErrorDialog("HEADLESS_ERR", noHead); <-Avoid
    } catch (Exception oopsErr) {
      Trident.ErrorDialog("EDIT_MENU_CRASH", oopsErr);
    }
  }
}

class GoToController {
  static JSpinner lineSpinner;
  static JDialog Goto;

  static void go() {
    /*
     * Displays and controls the Goto dialog
     */
    Goto = new JDialog(Trident.frame, "Go To");

    lineSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Trident.textarea.getLineCount(), 1));
    JButton go = new JButton("Go");

    JLabel instruction = new JLabel(" Enter the line number to set the insertion point: ");
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          int lineNum = Integer.parseInt(lineSpinner.getValue().toString());
          Trident.textarea.setCaretPosition(Trident.textarea.getLineStartOffset(lineNum - 1));
          Goto.dispose();
        } catch (BadLocationException | NullPointerException ble) {
          Trident.ErrorDialog("GOTO_LOCATION_ERR", ble);
        }
      }
    });
    Goto.setLayout(new BorderLayout());
    Goto.getContentPane().add(instruction, BorderLayout.NORTH);
    JPanel oprPane = new JPanel(new FlowLayout());
    oprPane.add(lineSpinner);
    oprPane.add(go);
    Goto.getContentPane().add(oprPane, BorderLayout.CENTER);
    Goto.pack();
    Goto.setLocationRelativeTo(Trident.frame);
    Goto.setResizable(false);
    Goto.setVisible(true);
  }
}
