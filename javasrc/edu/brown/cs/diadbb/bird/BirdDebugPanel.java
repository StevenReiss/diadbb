/********************************************************************************/
/*                                                                              */
/*              BirdDebugPanel.java                                             */
/*                                                                              */
/*      Debug panel for a specific breakpoint instance                          */
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
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaRoot;
import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingWrappingEditorPane;
import edu.brown.cs.ivy.xml.IvyXml;

class BirdDebugPanel extends SwingGridPanel implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BirdInstance    for_instance;
private JTextField      location_text;
private JTextField      symptom_text;
private JTextField      state_text;
private JEditorPane     log_pane;
private JEditorPane     input_area;
private JButton         symptom_btn;
private JButton         locations_btn; 
private JButton         repairs_btn;
private JButton         explain_btn;
private JButton         submit_btn;
private boolean         doing_query;

private static final long serialVersionUID = 1;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdDebugPanel(BirdInstance bi)
{
   for_instance = bi;
   BoardColors.setColors(this,BoardColors.getColor("Bird.panel.background"));
   doing_query = false;
   setupPanel();
}


/********************************************************************************/
/*                                                                              */
/*     Access methods                                                           */
/*                                                                              */
/********************************************************************************/

BirdInstance getInstance()              
{
   return for_instance;
}


/********************************************************************************/
/*                                                                              */
/*      Update methods                                                          */
/*                                                                              */
/********************************************************************************/

void updateInstance()
{
   if (symptom_text == null) return;
   
   location_text.setText(for_instance.getLocationString());
   symptom_text.setText(for_instance.getSymptomString());
   state_text.setText(for_instance.getState().toString());
   state_text.setBackground(for_instance.getTabColor());
   
   if (submit_btn == null) return;
   
   locations_btn.setEnabled(false);
   repairs_btn.setEnabled(false);
   explain_btn.setEnabled(false);
   submit_btn.setEnabled(false);
   if (symptom_btn != null) symptom_btn.setEnabled(false);
   
   BoardLog.logD("BIRD","Update instance " + doing_query + " " + for_instance.getState());
   
   if (doing_query) return;
   
   switch (for_instance.getState()) {
      default :
         break; 
      case NO_SYMPTOM_FOUND :
      case DOING_ANALYSIS :
         if (symptom_btn != null) symptom_btn.setEnabled(true);
         break;

      case READY :
         if (symptom_btn != null) symptom_btn.setEnabled(true);
         locations_btn.setEnabled(true);
         repairs_btn.setEnabled(true);
         explain_btn.setEnabled(true);
         submit_btn.setEnabled(true);
         break;
    }
}
  


void dispose()
{ }



/********************************************************************************/
/*                                                                              */
/*      Handle menu buttons                                                     */
/*                                                                              */
/********************************************************************************/

void addPopupButtons(JPopupMenu menu)
{
   DiadCandidateState state = for_instance.getState();
   if (state != DiadCandidateState.INITIAL) {
      menu.add(new SymptomAction());
    }
   menu.add(new ParameterAction());
   menu.add(new StartFrameAction());
   if (for_instance.shouldRemove() && for_instance.isShouldSave()) {
      menu.add(new RemoveAction());
    }
}



/********************************************************************************/
/*                                                                              */
/*      Initialize the panel                                                    */
/*                                                                              */
/********************************************************************************/

private void setupPanel()
{
   beginLayout();
   location_text = addTextField("Location",for_instance.getLocationString(),
         null,null); 
   location_text.setEditable(false);
   
   state_text = addTextField("State",for_instance.getState().toString(),
         null,null); 
   state_text.setEditable(false);
   
   symptom_text = addTextField("Symptom",for_instance.getSymptomString(),
         null,null);
   symptom_text.setEditable(false);
   
   addSeparator();
   
   log_pane = new SwingWrappingEditorPane("text/html","");
   log_pane.setMinimumSize(new Dimension(300,100));
   
   JScrollPane outrgn = new JScrollPane(log_pane,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   outrgn.setMinimumSize(new Dimension(300,100));
   outrgn.setPreferredSize(new Dimension(300,200));
   addLabellessRawComponent("Log",outrgn,true,true);
   
   SwingGridPanel inp = new SwingGridPanel();
   inp.addGBComponent(new JLabel("Enter Query"),0,0,1,1,10,0);
   submit_btn = new JButton("SUBMIT");
   submit_btn.addActionListener(new SubmitAction());
   inp.addGBComponent(submit_btn,1,0,1,1,0,0);
   input_area = new SwingWrappingEditorPane("text/plain","");
   input_area.setEditable(true);
   JScrollPane inregion = new JScrollPane(input_area,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   inp.addGBComponent(inregion,0,1,0,1,10,10);
   inp.setMinimumSize(new Dimension(300,75));
   inp.setPreferredSize(new Dimension(300,100));
   inp.setMaximumSize(new Dimension(300,150));
   addLabellessRawComponent("Query",inp,true,true);
   
   addSeparator();
   
   explain_btn = addBottomButton("Explain","EXPLAIN",true,
         new ExplainAction());
   locations_btn = addBottomButton("Locations","LOCS",true,
         new LocationsAction());
   repairs_btn = addBottomButton("Repairs","FIX",true,
         new RepairsAction());
  
   addBottomButtons();
   
   setMinimumSize(new Dimension(400,250));
}


/********************************************************************************/
/*                                                                              */
/*      Painting methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public void paintComponent(Graphics g)
{
   super.paintComponent(g);
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

private void appendOutput(String s)
{
   try {
      HTMLEditorKit kit = (HTMLEditorKit) log_pane.getEditorKit(); 
      HTMLDocument doc = (HTMLDocument) log_pane.getDocument();
      log_pane.setCaretPosition(doc.getLength());
      kit.insertHTML(doc,doc.getLength(),s,
            0,0,null);
    }
   catch (Exception e) { 
      BoardLog.logE("BIRD","Problem appending output",e);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Menu actions                                                            */
/*                                                                              */
/********************************************************************************/

private final class ParameterAction extends AbstractAction {
   
   private static final long serialVersionUID = 1;
   
   ParameterAction() {
      super("Set Diad Parameters");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
    }
   
}       // end of inner class ParameterAction


private final class StartFrameAction extends AbstractAction {
   
   private static final long serialVersionUID = 1;
 
   StartFrameAction() {
      super("Choose Execution Frame");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
    }
   
}       // end of inner class StartFrameAction


private final class SymptomAction extends AbstractAction {
   
   private static final long serialVersionUID = 1;
   
   SymptomAction() {
      super("Change Error Symptom");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
    }
   
}       // end of inner class StartFrameAction


private final class RemoveAction extends AbstractAction {

   private static final long serialVersionUID = 1;
   
   RemoveAction() {
      super("Remove Panel");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      BirdDebugBubble bbl = (BirdDebugBubble) BudaRoot.findBudaBubble(BirdDebugPanel.this);
      if (bbl != null) bbl.removeDebugInstance(for_instance);
    }
   
}       // end of inner class RemoveAction


private final class ExplainAction extends AbstractAction {

   private static final long serialVersionUID = 1;
   
   ExplainAction() {
      super("Explain the Problem");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Explain the root cause of the problem";
      AskLimbaCommand cmd = new AskLimbaCommand("EXPLAIN",null);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }

}       // end of inner class RepairsAction



private final class LocationsAction extends AbstractAction {

   private static final long serialVersionUID = 1;
   
   LocationsAction() {
      super("Do Fault Localization");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Find potential fault locations for this symptom";
      AskLimbaCommand cmd = new AskLimbaCommand("LOCATIONS",null);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }
   
}       // end of inner class LocationsAction


private final class RepairsAction extends AbstractAction {

   private static final long serialVersionUID = 1;
   
   RepairsAction() {
      super("Find Repairs");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Find repairs for this symptom";
      AskLimbaCommand cmd = new AskLimbaCommand("LOCATIONS",null);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }

}       // end of inner class RepairsAction



private final class SubmitAction implements ActionListener {

   @Override public void actionPerformed(ActionEvent evt) {
      String text = input_area.getText();
      if (text.isBlank()) return;
      AskLimbaCommand cmd = new AskLimbaCommand("USER",text);
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + 
            IvyFormat.formatText(text) + "</font></p></div>";
      appendOutput(disp);
      input_area.setText("");
      
      cmd.start(); 
     }
   
}       // end of inner class SubmitAction


private final class Responder implements ResponseHandler, Runnable {
   
   private String display_text;
   
   Responder() {
      display_text = null;
    }
   
   @Override public void handleResponse(Element xml) { 
      Element rslt = xml;
      if (!IvyXml.isElement(xml,"RESULT")) {
         rslt = IvyXml.getChild(xml,"RESULT");
       }
      String text = IvyXml.getTextElement(rslt,"RESPONSE");
      if (text == null) {
         BoardLog.logE("BAIT","Problem with response result: " +
               IvyXml.convertXmlToString(rslt));
         text = "???";
       }
      
      display_text = IvyFormat.formatText(text);
      
      SwingUtilities.invokeLater(this);
    }
   
   @Override public void run() {
      doing_query = false;
      updateInstance();
      String disp = "<div align='left'><p><font color='black'>" + display_text +
         "</font></p></div>";
      appendOutput(disp);    
    }

}       // end of inner class ResponseAction


private final class AskLimbaCommand extends Thread {
   
    private String query_type;
    private String query_value;
   
    AskLimbaCommand(String typ,String value) {
       super("AskLimba_" + typ + "_Thread");
       query_type = typ;
       query_value = value;
     }
    
    @Override public void run() {
       CommandArgs args = new CommandArgs("DEBUGID",for_instance.getId(),
             "TYPE",query_type); 
       String what = (query_value == null ? null : "QUESTION");
       doing_query = true;
       updateInstance();
       for_instance.setShouldSave(true);
       BirdFactory.getFactory().issueCommand("ASKLIMBA",args,
             what,query_value,new Responder());
     }
    
}       // end of inner class AskLimbaCommand



}       // end of class BirdDebugPanel




/* end of BirdDebugPanel.java */

