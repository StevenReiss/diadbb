/********************************************************************************/
/*                                                                              */
/*              BirdSymptomPanel.java                                           */
/*                                                                              */
/*      description of class                                                    */
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.bale.BaleFactory;
import edu.brown.cs.bubbles.bale.BaleConstants.BaleFileOverview;
import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.board.BoardThreadPool;
import edu.brown.cs.bubbles.bump.BumpClient;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpEvaluationHandler;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpProcess;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpRunModel;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpRunValue;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpStackFrame;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpThread;
import edu.brown.cs.bubbles.bump.BumpConstants.BumpThreadStack;
import edu.brown.cs.diadbb.bird.BirdConstants.DiadSymptomType;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingComboBox;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class BirdSymptomPanel extends SwingGridPanel
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BirdInstance for_instance;

private DataPanel assertion_panel;
private DataPanel exception_panel;
private DataPanel location_panel;
private VariablePanel variable_panel;
private ExpressionPanel expression_panel;
private DataPanel active_panel;
private DataPanel none_panel;

private JComboBox<DataPanel> symptom_panel;

private BumpStackFrame for_frame;
private BumpThread for_thread;
private BumpStackFrame start_frame;
private BaleFileOverview bale_file;
private Map<String,Element> expression_data;
private Element base_symptom;
private String exception_type;

private static Set<String> assertion_exceptions;

private static final long serialVersionUID = 1;


static {
   assertion_exceptions = new HashSet<>();
   assertion_exceptions.add("java.lang.AssertionError");
   assertion_exceptions.add("org.junit.ComparisonFailure");
   assertion_exceptions.add("junit.framework.AssertionFailedError");
   assertion_exceptions.add("junit.framework.ComparisonFailure");
   assertion_exceptions.add("org.junit.AssumpptionViolatedException");
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdSymptomPanel(BirdInstance bi)
{
   for_instance = bi;
   Element inxml = bi.getXml();
   
   for_thread = null; 
   for_frame = null;
   Element thrxml = IvyXml.getChild(inxml,"THREAD");
   String xid = IvyXml.getAttrString(thrxml,"ID");
   BumpRunModel mdl = BumpClient.getBump().getRunModel();
   for (BumpProcess bp : mdl.getProcesses()) {
      for (BumpThread bt : bp.getThreads()) {
         if (bt.getId().equals(xid)) { 
            for_thread = bt;
            break;
          }
       }
    }
   if (for_thread != null) {
      Element frxml = IvyXml.getChild(inxml,"FRAME");
      String fidx = IvyXml.getAttrString(frxml,"ID");
      Element stxml = IvyXml.getChild(inxml,"STARTFRAME");
      String stidx = IvyXml.getAttrString(stxml,"ID");
      BumpThreadStack bs = for_thread.getStack();
      for (int i = 0; i < bs.getNumFrames(); ++i) {
         BumpStackFrame bsf = bs.getFrame(i);
         if (bsf == null) continue;
         if (bsf.getId().equals(fidx)) for_frame = bsf;
         if (bsf.getId().equals(stidx)) start_frame = bsf;
       }
      exception_type = for_thread.getExceptionType();
    }
   if (for_frame != null) {
      File fnm = for_frame.getFile();
      bale_file = BaleFactory.getFactory().getFileOverview(null,fnm);
    }
   
   base_symptom = IvyXml.getChild(inxml,"SYMPTOM");
   
   createDisplay();
}


/********************************************************************************/
/*                                                                              */
/*      Panel maintenance and creation                                          */
/*                                                                              */
/********************************************************************************/

private void createDisplay()
{
   DiadSymptomType symptyp = IvyXml.getAttrEnum(base_symptom,
         "TYPE",DiadSymptomType.NONE);
   
   variable_panel = new VariablePanel();
   variable_panel.setVisible(false);
   expression_panel = new ExpressionPanel();
   expression_panel.setVisible(false);
   exception_panel = new ExceptionPanel();
   exception_panel.setVisible(false);
   assertion_panel = new AssertionPanel();
   assertion_panel.setVisible(false);
   location_panel = new LocationPanel();
   location_panel.setVisible(false);
   none_panel = new NonePanel();
   none_panel.setVisible(false);
   active_panel = null;
   
   beginLayout();
   addBannerLabel("Define Problem Symptom");
   addDescription("Location",for_instance.getLocationString());
   addSeparator();
   
   List<DataPanel> choices = new ArrayList<>();
   if (exception_type != null) {
      if (assertion_exceptions.contains(exception_type)) {
         choices.add(assertion_panel);
       }
      else {
         choices.add(exception_panel);
       }
    }
   choices.add(location_panel);
   choices.add(variable_panel);
   choices.add(expression_panel);
   choices.add(none_panel);
   
   switch (symptyp) {
      case ASSERTION :
         active_panel = assertion_panel;
         break;
      case EXCEPTION :
      case CAUGHT_EXCEPTION :
         active_panel = exception_panel;
         break;
      case VARIABLE :
         active_panel = variable_panel;
         break;
      case EXPRESSION :
         active_panel = expression_panel;
         break;
      case LOCATION :
         active_panel = location_panel;
         break;
      case NO_EXCEPTION :
         // handle exception should be thrown
         break;
      case NONE :
         active_panel = none_panel;
         break;
    }
   
   int idx = choices.indexOf(active_panel);
   symptom_panel = addChoice("Symptom",choices,idx,false,new PanelSelector());
   symptom_panel.setRenderer(new SymptomRenderer());
   
   addLabellessRawComponent("VARIABLES",variable_panel);
   addLabellessRawComponent("EXPRESSIONS",expression_panel);
   addLabellessRawComponent("NOPROBLEM",none_panel);
   addLabellessRawComponent("ASSERTIONS",assertion_panel);
   addLabellessRawComponent("EXCEPTIONS",exception_panel);
   addLabellessRawComponent("LOCATIONS",location_panel);
   addLabellessRawComponent("NONE",none_panel);
   
   active_panel.setVisible(true);
}


private void updateSize()
{
   
}



private final class PanelSelector implements ActionListener {
   
   @Override public void actionPerformed(ActionEvent evt) {
      JComboBox<?> cbx = (JComboBox<?>) evt.getSource();
      DataPanel pnl = (DataPanel) cbx.getSelectedItem();
      BoardLog.logD("BIRD","Handle panel choice " + pnl);
      
      if (active_panel != null) active_panel.setVisible(false);
      active_panel = pnl;
      active_panel.setVisible(true);
      updateSize();
    }
   
}	// end of inner class PanelSelector


/********************************************************************************/
/*                                                                              */
/*      Subpanel definitions                                                    */
/*                                                                              */
/********************************************************************************/

private abstract static class DataPanel extends SwingGridPanel {

   private static final long serialVersionUID = 1;
   
   boolean isReady()                    { return true; }
   
   abstract void outputXml(IvyXmlWriter xw);
   
   abstract String getPrompt();
}


private interface ValuePanel {

   void setValue(String base,BumpRunValue value,String error);

}	// end of inner interface ValuePanel



/********************************************************************************/
/*                                                                              */
/*      Empty panels                                                            */
/*                                                                              */
/********************************************************************************/

private final class LocationPanel extends DataPanel {

   private static final long serialVersionUID = 1;
   
   @Override void outputXml(IvyXmlWriter xw) {
      // output XML for SHOULDNT be here
    }
   
   @Override String getPrompt() {
      return "Shouldn't be at this location";
    }

}       // end of inner class LocationPanel



private final class ExceptionPanel extends DataPanel {

   private static final long serialVersionUID = 1;

   @Override void outputXml(IvyXmlWriter xw) {
      // output XML for SHOULDNT be here
    }
   
   @Override String getPrompt() {
      return "Exception " + exception_type + " should not be thrown";
    }
   
}       // end of inner class ExceptionPanel


private final class AssertionPanel extends DataPanel {
   
   private static final long serialVersionUID = 1;
   
   @Override void outputXml(IvyXmlWriter xw) {
      // output XML for SHOULDNT be here
    }
   
   @Override String getPrompt() {
      return "Assertion should be true";
    }
   
}       // end of inner class AssertionPanel


private final class NonePanel extends DataPanel {

   private static final long serialVersionUID = 1;
   
   @Override void outputXml(IvyXmlWriter xw) {
      // output XML for SHOULDNT be here
    }
   
   @Override String getPrompt() {
      return "No symptom -- everything as expected";
    }
   
}       // end of inner class NonePanel


/********************************************************************************/
/*                                                                              */
/*      Variable/Expression panels                                              */
/*                                                                              */
/********************************************************************************/

private abstract class VarExprPanel extends DataPanel implements ActionListener, ValuePanel {
   
   private SwingComboBox<String> variable_selector;
   private JLabel current_value;
   private SwingComboBox<String> should_be;
   private JTextField other_value;
   private SwingGridPanel other_value_panel;
   private boolean is_ready;
   
   private static final long serialVersionUID = 1;
   
   VarExprPanel() {
      is_ready = false;
      setBackground(BoardColors.getColor("Rose.background.color"));
      setOpaque(false);
      beginLayout();
      List<String> vars = new ArrayList<>();
      String what = getHeading();
      vars.add(0,"Select a " + what + " ...");
      variable_selector = addChoice(what,vars,0,false,this);
      ElementsFinder finder = new ElementsFinder(this);
      BoardThreadPool.start(finder);
      current_value = addDescription("Current Value","<No Variable>");
      List<String> shoulds = new ArrayList<>();
      should_be = addChoice("Should Be",shoulds,0,false,this);
      should_be.setVisible(false);
      other_value_panel = new SwingGridPanel();
      other_value_panel.setBackground(BoardColors.getColor("Rose.background.color"));
      other_value_panel.setOpaque(false);
      other_value_panel.beginLayout();
      other_value = other_value_panel.addTextField("Other Value","",32,this,null);
      addLabellessRawComponent("OTHER",other_value_panel);
      other_value_panel.setVisible(false);
    }
   
   protected abstract List<String> getElements();
   protected abstract String getHeading();
   protected BumpRunValue getValue(String what)                 { return null; }
   
   protected void addElements(List<String> elts) {
      if (elts == null) return;
      for (String elt : elts) variable_selector.addItem(elt);
    }
   
   @Override boolean isReady()                           { return is_ready; }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String what = getHeading();
      BoardLog.logD("BUSH",what + " action " + evt.getActionCommand() + " " + evt);
      
      switch (evt.getActionCommand()) {
         case "Expression" :
         case "Variable" :
            setReady(false);
            String var = (String) variable_selector.getSelectedItem();
            BoardLog.logD("BUSH","Check variable " + var + " @ " + variable_selector.getSelectedIndex());
            BoardLog.logD("BUSH","Selections: " + variable_selector.getModel().getSize());
            BoardLog.logD("BUSH",what + " " + var + " selected");
            current_value.setText("");
            if (var != null && !var.startsWith("Select ")) {
               BumpRunValue rval = getValue(var);
               if (rval == null) {
                  var = var.replace("?",".");
                  for_frame.evaluate(var,new EvalHandler(this));
                }
               else setValue(var,rval,null);
             }
            else current_value.setText("<No Variable>");
            break;
         case "Should Be" :
            String s = (String) should_be.getSelectedItem();
            BoardLog.logD("BUSH","Should be " + s + " " + should_be.getSelectedIndex());
            if (s != null && s.startsWith("Other")) {
               other_value_panel.setVisible(true);
               BoardLog.logD("BUSH","Other panel should be visible");
               invalidate();
             }
            else {
               BoardLog.logD("BUSH","Set other invisible");
               other_value_panel.setVisible(false);
             }
            updateSize();
            break;
         case "Other Value" :
            other_value.getText();
            break;
         case "comboBoxEdited" :
            break;
         default :
            BoardLog.logE("BUSH","Unknown " + what + " action " + evt.getActionCommand());
            break;
       }
    }
   
   @Override public void setValue(String expr,BumpRunValue value,String err) {
      BoardLog.logD("BUSH","Set value " + expr + " " + value + " " + err);
      if (err != null) {
         current_value.setForeground(BoardColors.getColor("Rose.value.error.color"));
         current_value.setText("???");
         should_be.setVisible(false);
         other_value_panel.setVisible(false);
       }
      else {
         current_value.setForeground(BoardColors.getColor("Rose.value.color"));
         String val = "(" + value.getType() + ") " + value.getValue();
         if (value.getType().equals("null")) val = "null";
         current_value.setText(val);
         setupShouldBe(value);
       }
    }
   
   protected String getCurrentItem() {
      return (String) variable_selector.getSelectedItem();
    }
   
   protected String getCurrentValue() {
      return current_value.getText();
    }
   
   protected String getShouldBeValue() {
      if (should_be == null) return null;
      
      String shd = (String) should_be.getSelectedItem();
      if (shd == null) return null;
      
      if (shd.startsWith("Other") || shd.startsWith("A different value")) {
         shd = other_value.getText();
         if (shd.equals("")) shd = null;
       }
      return shd;
    }
   
   private void setReady(boolean fg) {
      is_ready = fg;
      updateUI();
    }
   
   private void setupShouldBe(BumpRunValue value) {
      List<String> alternatives = findAlternatives(value);
      other_value_panel.setVisible(false);
      if (alternatives == null || alternatives.isEmpty()) {
         BoardLog.logD("BUSH","Should be contents: NONE");
         should_be.setVisible(false);
       }
      else {
         // alternatives.add(0,"A different value");
         BoardLog.logD("BUSH","Should be contents: " + alternatives.size());
         should_be.setContents(alternatives);
         should_be.setSelectedIndex(0);
         should_be.setVisible(true);
       }
      setReady(true);
    }
   
   
}       // end of inner class VarExprPanel




private class VariablePanel extends VarExprPanel {
   
   private static final long serialVersionUID = 1;
   
   VariablePanel() { }
   
   protected String getHeading()                        { return "Variable"; }
   
   protected List<String> getElements() {
      List<String> vars = findVariables();
      Collections.sort(vars);
      return vars;
    }
   
   protected BumpRunValue getValue(String elt) {
      return getVariableValue(elt,null,null);
    }
   
   @Override void outputXml(IvyXmlWriter xw) {
    }
   
   @Override String getPrompt() {
      return "Variable has the wrong value";
    }
   
   private BumpRunValue getVariableValue(String s,BumpRunValue brv,String pfx) {
      String var = s;
      String sfx = null;
      int idx = s.indexOf("?");
      if (idx > 0) {
         var = s.substring(0,idx);
         sfx = s.substring(idx+1);
       }
      
      if (brv != null) {
         BoardLog.logD("BUSH","Inner variables");
         for (String s1 : brv.getVariables()) {
            BoardLog.logD("BUSH","\t VAR: " + s1);
          }
       }
      BumpRunValue base = null;
      if (pfx != null) var = pfx + "?" +var;
      if (brv == null) base = for_frame.getValue(var);
      else base = brv.getValue(var);
      
      BoardLog.logD("BUSH","GET VALUE " + var + " " + pfx + " = " + base + " " + sfx);
      
      if (base == null) return null;
      if (sfx == null) return base;
      return getVariableValue(sfx,base,var);
    }
   
}	// end of inner class VariablePanel



private class ExpressionPanel extends VarExprPanel {
   
   private static final long serialVersionUID = 1;
   
   ExpressionPanel() { }
   
   protected String getHeading()                        { return "Expression"; }
   
   protected List<String> getElements() {
      return findExpressions();
    }
   
   @Override void outputXml(IvyXmlWriter xw) {
    }
   
   @Override String getPrompt() {
      return "Expression has the wrong value";
    }
   
}       // end of inner class ExpressionPanel


private class ElementsFinder implements Runnable {
   
   private VarExprPanel for_panel;
   
   ElementsFinder(VarExprPanel pnl) {
      for_panel = pnl;
    }
   
   @Override public void run() {
      List<String> exps = for_panel.getElements();
      for_panel.addElements(exps);
    }
   
}       // end of inner class ElementsFinder




/********************************************************************************/
/*										*/
/*	Find alternative values for a value					*/
/*										*/
/********************************************************************************/

private List<String> findAlternatives(BumpRunValue value)
{
   List<String> rslt = null;
   switch (value.getKind()) {
      case PRIMITIVE :
	 String typ = value.getType();
	 switch (typ) {
	    case "int" :
	    case "short" :
	    case "byte" :
	    case "long" :
	    case "char" :
	       rslt = findIntegerAlternatives(value);
	       break;
	    case "float" :
	    case "double" :
	       rslt = findFloatAlternatives(value);
	       break;
	    case "boolean" :
	       rslt = findBooleanAlternatives(value);
	       break;
	    case "void" :
	       break;
	  }
	 break;
      case STRING :
	 rslt = findStringAlterantives(value);
	 break;
      case CLASS :
      case OBJECT :
	 rslt = findObjectAlternatives(value);
	 break;
      case ARRAY :
	 break;
      case UNKNOWN :
	 break;
    }
   
   return rslt;
}



private List<String> findIntegerAlternatives(BumpRunValue rv)
{
   long ival = 0;
   try {
      ival = Long.parseLong(rv.getValue());
    }
   catch (NumberFormatException e) {
      return null;
    }
   
   List<String> rslt = new ArrayList<>();
   rslt.add(Long.toString(ival+1));
   rslt.add(Long.toString(ival-1));
   rslt.add("> " + ival);
   rslt.add("< " + ival);
   if (ival != 0 && Math.abs(ival) != 1) rslt.add("0");
   rslt.add("Other ...");
   
   return rslt;
}


private List<String> findFloatAlternatives(BumpRunValue rv)
{
   double ival = 0;
   try {
      ival = Double.parseDouble(rv.getValue());
    }
   catch (NumberFormatException e) {
      return null;
    }
   
   List<String> rslt = new ArrayList<>();
   rslt.add("> " + ival);
   rslt.add("< " + ival);
   if (ival != 0) rslt.add("0");
   rslt.add("Other ...");
   
   return rslt;
}


private List<String> findBooleanAlternatives(BumpRunValue rv)
{
   boolean bval = Boolean.parseBoolean(rv.getValue());
   
   List<String> rslt = new ArrayList<>();
   rslt.add(Boolean.toString(!bval));
   return rslt;
}


private List<String> findStringAlterantives(BumpRunValue rv)
{
   List<String> rslt = new ArrayList<>();
   rslt.add("Other ...");
   rslt.add("null");
   return rslt;
}


private List<String> findObjectAlternatives(BumpRunValue rv)
{
   List<String> rslt = new ArrayList<>();
   if (rv.getValue().equals("null")) {
      rslt.add("Non-Null");
    }
   else {
      rslt.add("null");
    }
   rslt.add("Other ...");
   return rslt;
}



private List<String> findVariables()
{
   List<String> rslt = new ArrayList<>();
   for (String s : for_frame.getVariables()) {
      if (s.contains(" returned")) continue;
      rslt.add(s);
    }
   for (String s : for_frame.getVariables()) {
      BumpRunValue rv = for_frame.getValue(s);
      switch (rv.getKind()) {
	 case CLASS :
	 case PRIMITIVE :
	 case STRING :
	 case UNKNOWN :
	    break;
	 case OBJECT :
	    if (s.equals("this")) {
	       for (String fld : rv.getVariables()) {
                  String disp = fld.replace("?",".");
		  rslt.add(disp);
		}
	     }
	    break;
	 case ARRAY :
	    break;
       }
    }
   
   return rslt;
}


private List<String> findExpressions()
{
   int off = bale_file.findLineOffset(for_frame.getLineNumber());
   CommandArgs args = new CommandArgs("THREAD",for_thread.getId(),
         "FRAME",for_frame.getId(),
         "FILE",for_frame.getFile(),
         "CLASS",for_frame.getFrameClass(),
         "METHOD",for_frame.getMethod(),
         "PROJECT",for_thread.getLaunch().getConfiguration().getProject(),
         "OFFSET",off,
         "LINE",for_frame.getLineNumber());
   BirdFactory bush = BirdFactory.getFactory();
   Element rslt = bush.sendDiadMessage("EXPRESSIONS",args,null); 
   
   List<String> exps = new ArrayList<>();
   expression_data = new HashMap<>();
   for (Element e : IvyXml.children(rslt,"EXPR")) {
      String exp = IvyXml.getTextElement(e,"TEXT");
      if (exp == null) continue;
      exp = exp.trim();
      if (exp.length() == 0) continue;
      exps.add(exp);
      expression_data.put(exp,e);
    }
   
   return exps;
   
}




/********************************************************************************/
/*										*/
/*	Handle evaluation results asynchronously				*/
/*										*/
/********************************************************************************/

private class EvalHandler implements BumpEvaluationHandler {
   
   private ValuePanel	value_panel;
   
   EvalHandler(ValuePanel pnl) {
      value_panel = pnl;
    }
   
   @Override public void evaluationResult(String eid,String expr,BumpRunValue val) {
      value_panel.setValue(expr,val,null);
    }
   
   @Override public void evaluationError(String eid,String expr,String err) {
      value_panel.setValue(expr,null,err);
    }
   
}	// end of inner class EvalHandler


/********************************************************************************/
/*                                                                              */
/*      SymptomRenderer class                                                   */
/*                                                                              */
/********************************************************************************/

private class SymptomRenderer extends DefaultListCellRenderer {
   
   @Override public Component getListCellRendererComponent(JList<?> list,
         Object value,int idx,boolean isselected,boolean hasfocus) {
      JLabel lbl = (JLabel) super.getListCellRendererComponent(list,value,idx,
            isselected,hasfocus);
      if (value instanceof DataPanel) {
         DataPanel dp = (DataPanel) value;
         lbl.setText(dp.getPrompt());
       }
      return lbl;
    }
   
}       // end of inner class SymptomRenderer


}       // end of class BirdSymptomPanel




/* end of BirdSymptomPanel.java */

