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
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingGridPanel;
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
   location_text.setText(for_instance.getLocationString());
   symptom_text.setText(for_instance.getSymptomString());
   state_text.setText(for_instance.getState().toString());
   
   symptom_btn.setEnabled(false);
   locations_btn.setEnabled(false);
   repairs_btn.setEnabled(false);
   explain_btn.setEnabled(false);
   
   switch (for_instance.getState()) {
      default :
         break; 
      case NO_SYMPTOM :
      case SYMPTOM_FOUND :
         symptom_btn.setEnabled(true);
         break;


      case READY :
         symptom_btn.setEnabled(true);
         locations_btn.setEnabled(true);
         repairs_btn.setEnabled(true);
         explain_btn.setEnabled(true);
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
   menu.add(new SymptomAction());
   menu.add(new ParameterAction());
   menu.add(new StartFrameAction());
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
   
   log_pane = new JEditorPane("text/html","");
   log_pane.setMinimumSize(new Dimension(300,100));
   
   JScrollPane outrgn = new JScrollPane(log_pane,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
// outrgn.setMinimumSize(new Dimension(300,300));
   addLabellessRawComponent("Log",outrgn,true,true);
   
   SwingGridPanel inp = new SwingGridPanel();
   inp.addGBComponent(new JLabel("Enter Query"),0,0,1,1,10,0);
   input_area = new JEditorPane("text/plain","");
   input_area.setEditable(true);
   submit_btn = new JButton("SUBMIT");
   submit_btn.addActionListener(new SubmitAction());
   inp.addGBComponent(submit_btn,1,0,1,1,0,0);
   JScrollPane inregion = new JScrollPane(input_area,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   inp.addGBComponent(inregion,0,1,0,1,10,10);
   inp.setMinimumSize(new Dimension(300,100));
   addLabellessRawComponent("Query",inp,true,false);
   
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
// Graphics2D g2 = (Graphics2D) g.create();
// Dimension d = getSize();
// BoardColors.setColors(this,getBackground());
// g2.setBackground(getBackground());
// g2.clearRect(0,0,d.width,d.height);
   
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
      kit.insertHTML(doc,doc.getLength(),s,
            0,0,null);
    }
   catch (Exception e) { 
      BoardLog.logE("BIRD","Problem appending output",e);
    }
}


private String formatText(String text)
{
   String ntext = text;
   if (ntext == null) ntext = "<No Response>";
   ntext = ntext.replace("<","&lt;");
   ntext = ntext.replace(">","&gt;");
   
   for ( ; ; ) {
      int idx0 = ntext.indexOf("```");
      if (idx0 < 0) break;
      int idx1 = ntext.indexOf("\n",idx0);
      int idx2 = ntext.indexOf("```",idx1);
      int idx3 = ntext.length();
      if (idx2 < 0) {
         idx2 = ntext.length();
       }
      else {
         idx3 = ntext.indexOf("\n",idx2);
       }
      
      String quote = ntext.substring(idx1,idx2);
      String pre = ntext.substring(0,idx0);
      String post = ntext.substring(idx3);
      ntext = pre + "<pre><code>\n" + quote + "\n" + post;
    }
   
   return ntext;
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
      AskLimbaCommand cmd = new AskLimbaCommand("LOCATIONS",text);
      cmd.start(); 
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + text + 
                  "</font></p></div>";
      appendOutput(disp);
      
      input_area.setText("");
    }
   
}       // end of inner class SubmitAction


private final class Responder implements ResponseHandler {
   
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
      
      text = formatText(text);
      
      String disp = "<div align='left'><p><font color='black'>" + text +
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
       BirdFactory.getFactory().issueCommand("ASKLIMBA",args,
             what,query_value,new Responder());
     }
    
}       // end of inner class AskLimbaCommand



}       // end of class BirdDebugPanel




/* end of BirdDebugPanel.java */

