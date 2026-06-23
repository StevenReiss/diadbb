/********************************************************************************/
/*                                                                              */
/*              BirdTestCaseBubble.java                                         */
/*                                                                              */
/*      Let the user view, edit, and insert a test case from DIAD               */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.diadbb.bird;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import jsyntaxpane.DefaultSyntaxKit;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.bale.BaleFactory;
import edu.brown.cs.bubbles.bass.BassFactory;
import edu.brown.cs.bubbles.batt.BattFactory;
import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaBubble;
import edu.brown.cs.bubbles.buda.BudaBubbleArea;
import edu.brown.cs.bubbles.buda.BudaConstants;
import edu.brown.cs.bubbles.buda.BudaRoot;
import edu.brown.cs.bubbles.bueno.BuenoFactory;
import edu.brown.cs.bubbles.bueno.BuenoLocation;
import edu.brown.cs.bubbles.bueno.BuenoProperties;
import edu.brown.cs.bubbles.bueno.BuenoConstants.BuenoKey;
import edu.brown.cs.bubbles.bueno.BuenoConstants.BuenoType;
import edu.brown.cs.bubbles.bump.BumpClient;
import edu.brown.cs.bubbles.bump.BumpLocation;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingComboBox;
import edu.brown.cs.ivy.swing.SwingEditorPane;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.xml.IvyXml;

class BirdTestCaseBubble extends BudaBubble implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          test_name;
private String          test_item;
private String          start_frame;
private BirdInstance    for_instance;
private String          test_body;
private String          test_assertion;
private String          test_project;
private boolean         user_frame;
private TestPanel       test_panel;
      
private static final int SPACES_PER_INDENT = 3;


private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdTestCaseBubble(BirdInstance inst,Element xml)
{
   for_instance = inst;
   user_frame = false;
   test_panel = null;
   test_project = null;
   
   BoardColors.setColors(this,BoardColors.getColor("Bird.panel.background"));
   
   DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);  // initialize if not done so
   
   setMessage("Working on test case generation");
}



/********************************************************************************/
/*                                                                              */
/*      Update methods                                                          */
/*                                                                              */
/********************************************************************************/

void setMessage(String msg)
{
   test_name = null;
   test_item = null;
   start_frame = null;
   test_body = null;
   test_assertion = null;
   test_panel = null;
   test_project = null;
   
   JLabel lbl = new JLabel(msg);
   lbl.setOpaque(true);
   BoardColors.setColors(lbl,BoardColors.getColor("Bird.panel.background"));
   setContentPane(lbl);
   setVisible(true);
}


void update(Element xml) 
{ 
   test_name = IvyXml.getAttrString(xml,"NAME");
   test_item = IvyXml.getAttrString(xml,"TESTCLASS") + "." +
         IvyXml.getAttrString(xml,"TESTMETHOD");
   start_frame = IvyXml.getAttrString(xml,"STARTFRAME");
   test_body = IvyXml.getTextElement(xml,"BODY");
   test_assertion = IvyXml.getTextElement(xml,"ASSERTION");
   user_frame = false;
   
   String fnm = IvyXml.getAttrString(xml,"STARTFILE");
   File ff = new File(fnm);
   test_project = BassFactory.getFactory().findProjectForFile(ff);
   
   test_panel = new TestPanel();
   setContentPane(test_panel);
   setVisible(true);
}



/********************************************************************************/
/*                                                                              */
/*      Retry option                                                            */
/*                                                                              */
/********************************************************************************/

private void retry()
{
   if (test_panel != null) {
      if (user_frame) {
         start_frame = test_panel.getStartFrame();
       }
      test_name = test_panel.getTestName();
    }
   CommandArgs args = new CommandArgs("DEBUGID",for_instance.getId(),
         "NAME",test_name);
   if (user_frame) args.put("STARTFRAME",start_frame);
   BirdFactory.getFactory().issueXmlCommand("CREATETEST",args,null,
         new RetryResponse());
   
   setMessage("Retrying test generation");     
}


private class RetryResponse implements ResponseHandler, Runnable {
   
   private Element test_response;
   
   RetryResponse() {
      test_response = null;
    }
   
   @Override public void handleResponse(Element xml) {
      test_response = xml;
      SwingUtilities.invokeLater(this);
    }
   
   @Override public void run() {
      Element test = IvyXml.getChild(test_response,"TESTCASE");
      if (test == null) {
         setMessage("Test Case GenerationFailed");
       }
      else {
         update(test);
       }
    }
   
}       // end of inner class RetryResponse


/********************************************************************************/
/*                                                                              */
/*      Popup menu handling                                                     */
/*                                                                              */
/********************************************************************************/

@Override public void handlePopupMenu(MouseEvent e) 
{
   JPopupMenu menu = new JPopupMenu();
   
   menu.add(getFloatBubbleAction());
   menu.show(this,e.getX(),e.getY());
}

/********************************************************************************/
/*                                                                              */
/*      Insert the resultant test                                               */
/*                                                                              */
/********************************************************************************/

private void insertTest(String incls,String code)
{
   BumpLocation target = null;
   String proj = test_project;
   String loc = null;
   
   BumpClient bc = BumpClient.getBump();
   List<BumpLocation> clocs = bc.findTypes(proj,incls);
   if (clocs != null) {
      for (BumpLocation bl : clocs) {
         String nm = bl.getSymbolName();
         if (nm.equals(incls)) {
            target = bl;
            break;
          }
       }
    }
   if (target == null) {
      BuenoFactory bf = BuenoFactory.getFactory();
      BuenoLocation bloc = bf.createLocation(proj,incls,null,false);
      BuenoProperties bp = loadFileProperties(incls);
      bf.createNew(BuenoType.NEW_CLASS,bloc,bp);
      loc = "New class " + incls;
    }
   else {
      loc = "Class " + incls;
    }
   
   boolean telluser = true;
   BaleFactory bf = BaleFactory.getFactory();
   BudaBubble bb = bf.createClassBubble(proj,incls);
   BudaBubbleArea bba = BudaRoot.findBudaBubbleArea(this);
   if (bb != null && bba != null) {
      bba.addBubble(bb,this,null,
            BudaConstants.PLACEMENT_MOVETO | BudaConstants.PLACEMENT_NEW |
            BudaConstants.PLACEMENT_PREFER);
      telluser = false;
    }
   
   if (telluser) {
      JOptionPane.showMessageDialog(this,
            "Test case inserted into " + loc,
            "Bird Test Generation Response",
            JOptionPane.INFORMATION_MESSAGE);
    }
}



private BuenoProperties loadFileProperties(String incls)
{
   BuenoProperties bp = new BuenoProperties();
   
   int idx = incls.lastIndexOf(".");
   String pnm = incls.substring(0,idx);
   String cnm = incls.substring(idx+1);
   bp.put(BuenoKey.KEY_PACKAGE,pnm);
   bp.put(BuenoKey.KEY_NAME,cnm);
   
   return bp;
}


/********************************************************************************/
/*                                                                              */
/*      Format the code                                                         */
/*                                                                              */
/********************************************************************************/

private static String formatMethod(String code)
{
   if (code == null || code.trim().isEmpty()) {
      return code;
    }
   
   // Split by lines and trim excess whitespace
   String[] lines = code.split("\\r?\\n");
   StringBuffer formatted = new StringBuffer();
   int indentlevel = 0;
   
   for (String rawline : lines) {
      String line = rawline.trim();
      if (line.isEmpty()) continue;
      
      // Reduce indent if line starts with a closing brace
      if (line.startsWith("}")) {
         indentlevel = Math.max(0, indentlevel - 1);
       }
      
      // Build current indent string
      StringBuffer indent = new StringBuffer();
      for (int i = 0; i < (indentlevel * SPACES_PER_INDENT); i++) {
         indent.append(" ");
       }
      
      // Append the indented line
      formatted.append(indent).append(line).append("\n");
      
      // Increase indent for the next lines if an opening brace is present
      // (Ignoring braces that open and close on the same line)
      if (line.contains("{") && !line.contains("}")) {
         indentlevel++;
       }
    }
   
   return formatted.toString();
}



/********************************************************************************/
/*                                                                              */
/*      Bubble contents                                                         */
/*                                                                              */
/********************************************************************************/

private class TestPanel extends SwingGridPanel implements ActionListener {
   
   private JTextField name_field;
   private JTextField totest_field;
   private SwingComboBox<FrameElement> frame_field;
   private JTextComponent code_area;
   private SwingComboBox<String> insert_field;
   private String default_class;
   private String code_name;
   
   private static final long serialVersionUID = 1L;
   
   TestPanel() {
      BoardColors.setColors(this,BoardColors.getColor("Bird.panel.background"));
      beginLayout();
      addBannerLabel("Create Test Case for Symptom");
      name_field = addTextField("Name",test_name,this,null);
      code_name = test_name;
      totest_field = addTextField("Test",test_item,null,null);
      totest_field.setEditable(false);
      
      CommandArgs args = new CommandArgs("DEBUGID",for_instance.getId());
      Element xml = BirdFactory.getFactory().sendDiadMessage("STARTFRAME",
            args,null);
      List<FrameElement> choices = new ArrayList<>();
      FrameElement sel = null;
      for (Element frm : IvyXml.children(IvyXml.getChild(xml,"FRAMES"))) {
         FrameElement fe = new FrameElement(frm);
         if (fe.isValid()) {
            choices.add(fe);
            if (fe.getId().equals(start_frame)) sel = fe;
          }
       }
      frame_field = addChoice("Start Frame",choices,sel,false,this);
      
      addSeparator();
      
      StringBuffer testcode = new StringBuffer();
      testcode.append("@Test\n");
      testcode.append("public void " + test_name + "()\n");
      testcode.append("{\n");
      testcode.append(test_body);
      testcode.append("\n");
      if (test_assertion != null) {
         testcode.append(test_assertion);
         testcode.append("\n");
       }
      testcode.append("}");
      String code = formatMethod(testcode.toString());
      
      SwingEditorPane coded = new SwingEditorPane("text/java",code);
      Font ft = coded.getFont();
      ft = ft.deriveFont(Font.BOLD);
      ft = ft.deriveFont(13.0f);
      coded.setFont(ft);
      Dimension d = coded.getPreferredSize();
      d.height = Math.min(d.height + 10,400);
      d.width = Math.min(d.width + 25,500);
      JScrollPane sp = new JScrollPane(coded);
      addRawComponent("Test Code",sp);
      code_area = coded;
//    code_area = addTextArea("Test Code",code,30,80,null);
      
      addSeparator();
      
      Set<String> classes = new TreeSet<>();
      int idx = test_item.lastIndexOf(".");
      String tcl = test_item.substring(0,idx);
      String tmthd = test_item.substring(idx+1);
      String tcnm = BattFactory.getFactory().findTestClasses(test_project,
            tcl,tmthd,classes);
      default_class = tcnm;
      if (default_class != null && !classes.contains(default_class)) {
         tcnm += " (NEW)";
         classes.add(tcnm);
       }
      insert_field = addChoice("Insert Into",classes,tcnm,null);
      insert_field.setEditable(true);
      
      addSeparator();
      
      addBottomButton("Cancel","CANCEL",this);
      addBottomButton("Retry","RETRY",this);
      addBottomButton("Insert","INSERT",this);
      addBottomButtons();      
    }
   
   String getTestName()                         { return name_field.getText(); }
   String getStartFrame() {
      FrameElement fe = (FrameElement) frame_field.getSelectedItem();
      return fe.getId();
    }
   String getTextCode()                         { return code_area.getText(); }
   
   String getInsertClass() {
      return insert_field.getSelectedItem().toString();
    }  
   
   void updateCode() {
      String nm = getTestName();
      if (!nm.equals(code_name)) {
         String txt = getTextCode();
         txt =  txt.replace(code_name,nm);
         code_area.setText(txt);
         code_name = nm;
       }
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String what = evt.getActionCommand().toUpperCase();
      switch (what) {
         case "START FRAME" :
            String sf = getStartFrame();
            if (sf != null && !sf.equals(start_frame)) user_frame = true;
            break;
         case "NAME" :
            String nm = getTestName();
            if (nm != null && !nm.equals(test_name)) {
               updateCode();
             }
            break;
         case "CANCEL" :
            BudaBubble bbl = BudaRoot.findBudaBubble(this);
            bbl.setVisible(false);
            break;
         case "RETRY" : 
            retry();
            break;
         case "INSERT" :
            BoardLog.logD("BIRD","Insert test case into " + getInsertClass() + 
                  " start at " + getStartFrame());
            insertTest(getInsertClass(),getTextCode());
            // this should use BATT
            break;
       }
    }
   
}       // end of inner class TestPanel



private final class FrameElement {

   private Element frame_xml;
   
   FrameElement(Element xml) {
      frame_xml = xml;
    }
   
   boolean isValid() {
      String cls = IvyXml.getAttrString(frame_xml,"CLASS");
      if (cls.contains("java.lang.invoke")) return false;
      if (cls.contains("jdk.internal.reflect")) return false;
      if (cls.contains("java.lang.reflect.Method")) return false;
      if (cls.matches(".*\\$[0-9]+")) return false;
      
      return true;
    }
   
   String getId() {
      return IvyXml.getAttrString(frame_xml,"ID");
    }
   
   @Override public String toString() {
      String cls = IvyXml.getAttrString(frame_xml,"CLASS");
      String mthd = IvyXml.getAttrString(frame_xml,"METHOD");
      String line = IvyXml.getAttrString(frame_xml,"LINE");
      
      String rslt = cls + "." + mthd;
      if (line != null && IvyXml.getAttrBool(frame_xml,"USER")) {
         rslt += " @ " + line;
       }
      
      return rslt;
    }

}

}       // end of class BirdTestCaseBubble




/* end of BirdTestCaseBubble.java */

