/********************************************************************************/
/*                                                                              */
/*              BirdDebugBubble.java                                            */
/*                                                                              */
/*      Main debug panel with tabs for active sessions                          */
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
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import edu.brown.cs.bubbles.bddt.BddtConstants;
import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaBubble;
import edu.brown.cs.ivy.swing.SwingText;

class BirdDebugBubble extends BudaBubble implements BddtConstants.BddtAuxBubble, BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private DebugTabs       debug_tabs;
private Map<String,BirdDebugPanel> active_panels;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdDebugBubble(BirdFactory fac,Object lid)
{
   debug_tabs = new DebugTabs();
   active_panels = new HashMap<>();
   setContentPane(debug_tabs);
}

@Override public void localDispose() 
{ }



/********************************************************************************/
/*                                                                              */
/*      Handle Debug Instances                                                  */
/*                                                                              */
/********************************************************************************/

void addDebugInstance(BirdInstance bi) 
{
   BirdDebugPanel pnl = new BirdDebugPanel(bi);
   active_panels.put(bi.getId(),pnl);
    
   debug_tabs.addTab(bi.getTitle(),pnl);
   
   updateDebugInstance(bi);
   
   repaint();
}


void updateDebugInstance(BirdInstance bi)
{ 
   BirdDebugPanel pnl = active_panels.get(bi.getId());
   if (pnl == null) return;
   
   pnl.updateInstance(); 
   int idx = findPanelIndex(pnl);
   if (idx >= 0) {
      Color c = bi.getTabColor(); 
      if (c !=  null) {
         debug_tabs.setBackgroundAt(idx,c);
       }
    }
   
}


void removeDebugInstance(BirdInstance bi)
{ 
   BirdDebugPanel pn = active_panels.remove(bi.getId());
   if (pn == null) return;
   
   int i = findPanelIndex(pn);
   if (i >= 0) {
      BoardLog.logD("BIRD","Removing tab " + i);
      debug_tabs.removeTabAt(i);
      pn.dispose(); 
    }
   
   repaint();
}



private int findPanelIndex(BirdDebugPanel pnl)
{
   for (int i = 0; i < debug_tabs.getTabCount(); ++i) {
      BirdDebugPanel c = (BirdDebugPanel) debug_tabs.getComponentAt(i);
      if (c == pnl) {
         return i;
       }
    }
   
   BoardLog.logD("BIRD","Can't find debug panel for " +
         pnl.getInstance().getId());
   
   return -1;
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                           */
/*                                                                              */
/********************************************************************************/

@Override public String getAuxType()            { return "DebuggerAssistant"; }

boolean isIdRelevant(String id) 
{
   if (active_panels.get(id) != null) return true;
   
   return false;
}


/********************************************************************************/
/*                                                                              */
/*      Menu methods                                                             */
/*                                                                              */
/********************************************************************************/

@Override public void handlePopupMenu(MouseEvent e)
{
   JPopupMenu menu = new JPopupMenu();
   if (!active_panels.isEmpty()) {
      BirdDebugPanel pnl = (BirdDebugPanel) debug_tabs.getSelectedComponent();
      if (pnl != null) pnl.addPopupButtons(menu);
    }
    
   menu.add(getFloatBubbleAction());
   menu.show(this,e.getX(),e.getY());
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
      setPreferredSize(new Dimension(400,300));
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


}       // end of class BirdDebugBubble




/* end of BirdDebugBubble.java */

