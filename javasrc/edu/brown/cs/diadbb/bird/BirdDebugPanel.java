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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.bale.BaleConstants;
import edu.brown.cs.bubbles.bale.BaleConstants.BaleFileOverview;
import edu.brown.cs.bubbles.bale.BaleFactory;
import edu.brown.cs.bubbles.bass.BassFactory;
import edu.brown.cs.bubbles.bass.BassName;
import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaBubbleArea;
import edu.brown.cs.bubbles.buda.BudaConstants;
import edu.brown.cs.bubbles.buda.BudaRoot;
import edu.brown.cs.bubbles.buda.BudaConstants.BudaLinkStyle;
import edu.brown.cs.bubbles.bump.BumpClient;
import edu.brown.cs.bubbles.bump.BumpLocation;
import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingWrappingEditorPane;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

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
private JButton         retry_btn;
private boolean         doing_query;
private Boolean         initial_response;
private boolean         have_explanation;

private static final Pattern HUNK_HEADER_PATTERN = 
   Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@.*");

private static final Pattern SOURCE_PATTERN = 
   Pattern.compile("^--- ([^ ]+)( .*)?$");

private static final Pattern LOCATION_PATTERN =
   Pattern.compile("LOC\\!\\!([^!]+)\\!\\!([0-9]+)");

// private static final Pattern LOCATION_PATTERN1 =
// Pattern.compile("LOC::([^:]+)::([0-9]+)");


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
   initial_response = null;
   have_explanation = false;
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
   if (initial_response == Boolean.FALSE) {
      String resp = for_instance.getResponse();
      if (resp != null && !resp.isEmpty()) {
         String query = "Explain the problem";
         resp = IvyFormat.formatText(resp);
         DisplayResponse dr = new DisplayResponse(query,resp);
         SwingUtilities.invokeLater(dr);
         initial_response = true;
         doing_query = false;
         have_explanation = true;
       }
    }
   
   if (submit_btn == null) return;
   
   locations_btn.setEnabled(false);
   repairs_btn.setEnabled(false);
   if (explain_btn != null) {
      explain_btn.setEnabled(false);
    }
   if (retry_btn != null) {
      retry_btn.setEnabled(false);
    }
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
         
      case DOING_QUERY :
         initial_response = false;
         doing_query = true;
         break;

      case READY :
         if (initial_response == Boolean.FALSE) {
            doing_query = false;
            initial_response = null;
          }
         if (symptom_btn != null) symptom_btn.setEnabled(true);
         if (have_explanation) {
            locations_btn.setEnabled(true);
            repairs_btn.setEnabled(true);
          }
         if (explain_btn != null) {
            explain_btn.setEnabled(true);
          }
         if (retry_btn != null && initial_response == Boolean.TRUE) {
            retry_btn.setEnabled(true);
          }
         submit_btn.setEnabled(true);
         break;
    }
}
  


void dispose()
{ }


private class DisplayResponse implements Runnable {

   private String query_string;
   private String result_string;
   
   DisplayResponse(String q,String r) {
      query_string = q;
      result_string = r;
    } 
   
   @Override public void run() {
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" +
         query_string + "</font></p></div>";
      appendOutput(disp);
      disp = "<div align='left'><p><font color='black'>" + result_string +
            "</font></p></div>";
      appendOutput(disp);    
    }
   
}       // end of inner class DisplayResponse



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
   
   if (!for_instance.getAutoQuery()) {
      explain_btn = addBottomButton("Explain","EXPLAIN",true,
            new ExplainAction());
      retry_btn = null;
    }
   else {
      explain_btn = null;
      retry_btn = addBottomButton("Try Again","RETRY",true,
            new RetryAction());
    }
   locations_btn = addBottomButton("Sources","LOCS",true,
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
      BirdSymptomPanel pnl = new BirdSymptomPanel(for_instance);
      int sts = JOptionPane.showOptionDialog(BirdDebugPanel.this,pnl,
            "Specify Problem Symptom",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,null,null,null);
      if (sts != JOptionPane.OK_OPTION) return;
      // define new symptom and pass to diad
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


private final class ExplainAction extends AbstractAction implements ResponseHandler {

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
   
   @Override public void handleResponse(Element xml) {
      have_explanation = true;
      Responder resp = new Responder();
      resp.handleResponse(xml);
      updateInstance();
    }
   
}       // end of inner class ExplainAction


private final class RetryAction extends AbstractAction implements ResponseHandler {

   private static final long serialVersionUID = 1;
   
   RetryAction() {
      super("Retry the explanation");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Try that explanation again.";
      AskLimbaCommand cmd = new AskLimbaCommand("RETRY",null);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }
   
   @Override public void handleResponse(Element xml) {
      have_explanation = true;
      Responder resp = new Responder();
      resp.handleResponse(xml);
    }
   
}       // end of inner class RetryAction



private final class LocationsAction extends AbstractAction implements ResponseHandler, Runnable {

   private List<BumpLocation> show_locations;
   
   private static final long serialVersionUID = 1;
   
   LocationsAction() {
      super("Do Fault Localization");
      show_locations = null;
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Show the relevant sources";
      AskLimbaCommand cmd = new AskLimbaCommand("LOCATIONS",null,this);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }
   
   @Override public void handleResponse(Element xml0) {
      Element xml = xml0;
      if (!IvyXml.isElement(xml,"RESULT")) {
         xml = IvyXml.getChild(xml,"RESULT");
       }
      
      List<BumpLocation> locs = new ArrayList<>();
      BaleFactory bf = BaleFactory.getFactory();
      BassFactory bsf = BassFactory.getFactory();
      BumpClient bc = BumpClient.getBump();
      String text = IvyXml.getText(xml);
      Matcher m = LOCATION_PATTERN.matcher(text);
      Set<BassName> names = new HashSet<>();
      Set<String> mnames = new HashSet<>();
      while (m.find()) {
         BoardLog.logD("BIRD","Found location " + m.group(1) + " " + 
               m.group(2));
         String fnm = m.group(1);
         if (!fnm.endsWith(".java")) {
            if (mnames.add(fnm)){
               List<BumpLocation> mlocs = bc.findMethod(null,fnm,false);
               // check the locs to ensure they contain the line number?
               locs.addAll(mlocs);
             }
            continue;
          }
         File f = new File(fnm);
         int line = Integer.parseInt(m.group(2));
         BaleConstants.BaleFileOverview bfo = bf.getFileOverview(null,f);
         if (bfo == null) continue;
         int loff = bfo.findLineOffset(line);
         int eoff = bfo.mapOffsetToEclipse(loff);
         BassName bn = bsf.findBubbleName(f,eoff);
         if (bn == null) continue;
         if (names.add(bn)) {
            locs.add(bn.getLocation());
          }
       }
      if (!locs.isEmpty()) {
         show_locations = new ArrayList<>(locs);
         SwingUtilities.invokeLater(this);
       }
      else {
         Responder resp = new Responder();
         resp.handleResponse(xml);
       }
    }
   
   @Override public void run() {
      BaleFactory bf = BaleFactory.getFactory();
      bf.createBubbleStack(BirdDebugPanel.this,null,null,true,show_locations,
            BudaLinkStyle.NONE); 
      BoardLog.logD("BIRD","Done creating location information");
      doing_query = false;
      updateInstance();
    }
   
}       // end of inner class LocationsAction


private final class RepairsAction extends AbstractAction implements ResponseHandler, Runnable {

   private Collection<BirdFileEdit> repair_edits;
   
   private static final long serialVersionUID = 1;
   
   RepairsAction() {
      super("Find Repairs");
      repair_edits = null;
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String query = "Show the repairs for this symptom";
      AskLimbaCommand cmd = new AskLimbaCommand("REPAIRS",null,this);
      cmd.start();
      String disp = "<div align='right'><p style='text-indent: 50px;'><font color='blue'>" + query + 
            "</font></p></div>";
      appendOutput(disp);
    }
   
   @Override public void handleResponse(Element xml0) {
      // should go through response and find patches for each repair
      // then should use SEEDE to validate the repair and then make the patch
      // Actually, the validation should be done inside DIAD
      Element xml = xml0;
      if (!IvyXml.isElement(xml,"RESULT")) {
         xml = IvyXml.getChild(xml,"RESULT");
       }
      Collection<BirdFileEdit> edits = new TreeSet<>();
      for (Element patch : IvyXml.children(xml,"PATCH")) {
         Collection<BirdFileEdit> nedit = convertPatchToEdits(IvyXml.getText(patch));
         if (nedit != null) edits.addAll(nedit);
       }
      
      if (edits.isEmpty()) {
         Responder resp = new Responder();
         resp.handleResponse(xml);
       }
      else {
         repair_edits = edits;
         SwingUtilities.invokeLater(this);
       }
    }
   
   @Override public void run() {
      BirdPreviewBubble prev = new BirdPreviewBubble(BirdDebugPanel.this,repair_edits);
      BudaBubbleArea bba = BudaRoot.findBudaBubbleArea(BirdDebugPanel.this);
      bba.addBubble(prev,BirdDebugPanel.this,null,
            BudaConstants.PLACEMENT_LOGICAL|BudaConstants.PLACEMENT_RIGHT);
      prev.setVisible(true);
      
      CommandArgs args = new CommandArgs("DEBUGID",for_instance.getId());
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("EDITS");
      for (BirdFileEdit bfe : repair_edits) {
         bfe.outputXml(xw);
       }
      xw.end("EDITS");
      String cnts = xw.closeResult();
      BirdFactory bf = BirdFactory.getFactory();
      bf.issueXmlCommand("VALIDATE",args,cnts,new ValidateHandler(prev));
      
      repair_edits = null;
      
      doing_query = false;
      updateInstance();
   }
   
   private Collection<BirdFileEdit> convertPatchToEdits(String patch)
   {
      IvyLog.logD("LIMBA","DO patch:  " + patch);
      
      BaleFactory bf = BaleFactory.getFactory();
      Collection<BirdFileEdit> edits = new TreeSet<>();
      int startpos = -1;
      int endpos = -1;
      int srcline = 0;
      int startline = 0;
      int delline = 0;
      int addline = 0;
      String insert = null;
      BaleFileOverview file = null;
      
      try (BufferedReader br = new BufferedReader(new StringReader(patch))) {
         for ( ; ; ) {
            String line = br.readLine();
            if (line == null) {
               if (startpos > 0) {
                  if (endpos < 0) {
                     endpos = file.findLineOffset(srcline);
                   }
                  BirdFileEdit bfe = new BirdFileEdit(file,startpos,
                        endpos,insert,
                        startline,addline,delline);
                  edits.add(bfe);
                }
               break;
             }
            if (line.startsWith("--- ")) {
               Matcher m1 = SOURCE_PATTERN.matcher(line);
               if (m1.matches()) {
                  String fnm = m1.group(1);
                  File ff = findActualFile(fnm);
                  // need to get actual file here
                  file = bf.getFileOverview(null,ff);
                }
             }
            else if (line.startsWith("+++ ")) ;
            else if (line.startsWith("@@ ")) {
               Matcher m2 = HUNK_HEADER_PATTERN.matcher(line);
               if (m2.matches()) {
                  srcline = Integer.parseInt(m2.group(1))+1;
                  insert = null;
                  startpos = -1;
                  endpos = -1;
                  addline = 0;
                  delline = 0;
                  startline = 0;
                }
             }
            else if (line.startsWith(" ")) {
               if (startpos > 0) {
                  if (endpos < 0) {
                     endpos = file.findLineOffset(srcline);
                   }
                  BirdFileEdit bfe = new BirdFileEdit(file,startpos,
                        endpos,insert,
                        startline,addline,delline);
                  edits.add(bfe);
                  insert = null; 
                  startpos = -1;
                  endpos = -1;
                  addline = 0;
                  delline = 0;
                  startline = 0;
                }
               ++srcline;
             }
            else if (line.startsWith("-")) {
               if (startpos <= 0) {
                  startline = srcline;
                  startpos = file.findLineOffset(srcline);
                }
               ++delline;
               ++srcline;
             }
            else if (line.startsWith("+")) {
               if (startpos <= 0) {
                  startline = srcline;
                  startpos = file.findLineOffset(srcline);
                }
               if (endpos < 0) {
                  endpos = file.findLineOffset(srcline);
                }
               String cnts = line.substring(1);
               if (insert == null) insert = "";
               insert += cnts + "\n";
               ++addline;
             }
          }
       }
      catch (IOException e) { }
      
      return edits;
   }
   
   private File findActualFile(String name)
   {
      if (name.startsWith("a/")) name = name.substring(1);
      else if (name.startsWith("b/")) name = name.substring(1);
      
      File f1 = new File(name);
      if (f1.exists()) return f1;
      
      if (name.endsWith(".java")) {
         BumpClient bc = BumpClient.getBump();
         int start = 0;
         if (name.startsWith("/")) start = 1;
         String n1 = name.substring(start,name.length()-5);
         n1 = n1.replace("/",".");
         n1 = n1.replace("$",".");
         List<BumpLocation> bl = bc.findClassDefinition(null,n1);
         if (bl != null && !bl.isEmpty()) {
            BumpLocation loc = bl.get(0);
            return loc.getFile();
          }
       }
      
      return f1;
   }
   
   
}       // end of inner class RepairsAction


private final class ValidateHandler implements ResponseHandler, Runnable {

   private BirdPreviewBubble preview_panel;
   private String validation_status;
   
   ValidateHandler(BirdPreviewBubble pnl) {
      preview_panel = pnl;
      validation_status = "UNKNOWN";
    }
   
   @Override public void handleResponse(Element xml) {
      Element velt = IvyXml.getChild(xml,"VALIDATION");
      validation_status = IvyXml.getAttrString(velt,"STATUS");
      SwingUtilities.invokeLater(this);
    }
   
   @Override public void run() {
      preview_panel.setValidationStatus(validation_status); 
    }
   
}


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
    private ResponseHandler response_handler;
   
    AskLimbaCommand(String typ,String value) {
       this(typ,value,null);
     }
    
    AskLimbaCommand(String typ,String value,ResponseHandler resp) {
       super("AskLimba_" + typ + "_Thread");
       query_type = typ;
       query_value = value;
       if (resp == null) {
          resp = new Responder();
        }
       response_handler = resp;
     }
    
    @Override public void run() {
       CommandArgs args = new CommandArgs("DEBUGID",for_instance.getId(),
             "TYPE",query_type); 
       String what = (query_value == null ? null : "QUESTION");
       doing_query = true;
       updateInstance();
       for_instance.setShouldSave(true);
       BirdFactory.getFactory().issueCommand("ASKLIMBA",args,
             what,query_value,response_handler);
     }
    
}       // end of inner class AskLimbaCommand



}       // end of class BirdDebugPanel




/* end of BirdDebugPanel.java */

