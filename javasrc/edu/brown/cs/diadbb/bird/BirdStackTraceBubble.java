/********************************************************************************/
/*                                                                              */
/*              BirdStackTraceBubble.java                                       */
/*                                                                              */
/*      Bubble for stack trace debugging                                        */
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.board.BoardProperties;
import edu.brown.cs.bubbles.buda.BudaBubble;
import edu.brown.cs.bubbles.buda.BudaBubbleArea;
import edu.brown.cs.bubbles.buda.BudaConstants;
import edu.brown.cs.bubbles.buda.BudaErrorBubble;
import edu.brown.cs.bubbles.buda.BudaRoot;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingText;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class BirdStackTraceBubble extends BudaBubble implements BirdConstants,
      BirdConstants.BirdDebugSet
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private DebugTabs   debug_tabs;
private StackTracePanel stack_panel;
private BirdDebugPanel debug_panel;
private Dimension   preferred_size;
private String debug_id;

private static final long serialVersionUID = 1;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdStackTraceBubble()
{
   BoardProperties birdprops = BoardProperties.getProperties("Bird");
   int w = birdprops.getInt("Bird.panel.width",400);
   int h = birdprops.getInt("Bird.panel.height",300);
   preferred_size = new Dimension(w,h);
   debug_tabs = new DebugTabs();
   
   debug_panel = null;
   debug_id = null;
   stack_panel = new StackTracePanel();
   debug_tabs.addTab("Stack Trace",stack_panel);
   
   setContentPane(debug_tabs);
}



/********************************************************************************/
/*                                                                              */
/*      Debug set methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public void addDebugInstance(BirdInstance bi)
{
   debug_panel = new BirdDebugPanel(bi);
   debug_tabs.addTab(bi.getTitle(),debug_panel);
   updateDebugInstance(bi);
   debug_tabs.repaint();
}


@Override public void updateDebugInstance(BirdInstance bi)
{ 
   if (bi == debug_panel.getInstance()) {
      debug_panel.updateInstance();
      int idx = findPanelIndex();
      if (idx >= 0) {
         Color c = bi.getTabColor(); 
         if (c !=  null) {
            debug_tabs.setBackgroundAt(idx,c);
          }
       }
    }
}


private int findPanelIndex()
{
   for (int i = 1; i < debug_tabs.getTabCount(); ++i) {
      BirdDebugPanel c = (BirdDebugPanel) debug_tabs.getComponentAt(i);
      if (c == debug_panel) {
         return i;
       }
    }
   
   BoardLog.logD("BIRD","Can't find debug panel for " +
         debug_panel.getInstance().getId());
   
   return -1;
}


@Override public void removeDebugInstance(BirdInstance bi)
{ 
   if (bi == debug_panel.getInstance()) {
      int idx = findPanelIndex();
      if (idx >= 0) {
         debug_tabs.removeTabAt(idx);
         debug_panel.dispose();
         debug_panel = null;
         // reenable DEBUG button?
       }
    }
}



@Override public boolean isIdRelevant(String id)
{
   if (id != null && id.equals(debug_id)) return true;
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

private void setDebugId(String id)
{
   debug_id = id;
   BirdFactory fac = BirdFactory.getFactory();
   CommandArgs args = new CommandArgs("DEBUGID",debug_id);
   fac.sendDiadMessage("STARTSTACK",args,null);
}



/********************************************************************************/
/*                                                                              */
/*      Main panel                                                              */
/*                                                                              */
/********************************************************************************/

private class DebugTabs extends JTabbedPane {
   
   private static final long serialVersionUID = 1;
   
   DebugTabs() {
      super(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
      setMinimumSize(new Dimension(400,300));
      setPreferredSize(preferred_size);
      BoardColors.setColors(this,BoardColors.getColor("Bird.panel.background"));
      setOpaque(true);
    }
   
   @Override public void paint(Graphics g) {
      if (getTabCount() == 0) {
         Graphics2D g2 = (Graphics2D) g;
         Rectangle bnds = getBounds();
         g2.fillRect(0,0,bnds.width,bnds.height);
         g2.clearRect(0,0,bnds.width,bnds.height);
         SwingText.drawText("Smart Debugger Assistant",g2,bnds);
       }
      else {
         super.paint(g);
       }
    }
   
}       // end of inner class DebugTabs



/********************************************************************************/
/*                                                                              */
/*      Stack Trace Panel                                                       */
/*                                                                              */
/********************************************************************************/

private class StackTracePanel extends SwingGridPanel implements UndoableEditListener,
      ActionListener {
   
   private JTextArea text_area;
   private JButton debug_button;
   
   private static final long serialVersionUID = 1;
   
   StackTracePanel() {
      beginLayout();
      addBannerLabel("Provide Stack Trace to Debug");
      text_area = addTextArea("Stack Trace",null, 80,30,this);
      addSeparator();
      debug_button = addBottomButton("Debug","DEBUG",this);
      debug_button.setEnabled(false);
      addBottomButtons();
    }
   
   @Override public void undoableEditHappened(UndoableEditEvent evt) {
      boolean valid = false;
      String txt = text_area.getText();
      if (txt != null && !txt.isBlank() && txt.contains("\n")) {
         valid = true;
       }
      debug_button.setEnabled(valid);
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String st = text_area.getText().trim();
      if (st == null || st.isBlank()) return;
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.cdataElement("STACK",st);
      String cnts = xw.toString();
      xw.close();
      BirdFactory bf = BirdFactory.getFactory();
      Element rslt = bf.sendDiadMessage("STACKDEBUG",null,cnts);
      if (IvyXml.isElement(rslt,"RESULT")) {
         String id = IvyXml.getAttrString(rslt,"ID");
         if (id == null) {
            BudaBubbleArea bba = BudaRoot.findBudaBubbleArea(this);
            BudaErrorBubble bub = new BudaErrorBubble("Bad stack trace");
            if (bba != null) {
               bba.addBubble(bub, BirdStackTraceBubble.this, null,
                     BudaConstants.PLACEMENT_LEFT);
             }
            return;
          }
         setDebugId(id);
         debug_button.setEnabled(false);
       } 
    }
   
}   // end of inner class StackTracePanel



}       // end of class BirdStackTraceBubble




/* end of BirdStackTraceBubble.java */

