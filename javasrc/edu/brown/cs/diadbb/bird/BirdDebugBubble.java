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
private Object          launch_id;     

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdDebugBubble(BirdFactory fac,Object lid)
{
   launch_id = lid;
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
}


void updateDebugInstance(BirdInstance bi)
{ 
   BirdDebugPanel pnl = active_panels.get(bi.getId());
   if (pnl == null) return;
   
   pnl.updateInstance(); 
   int idx = findPanelIndex(pnl);
  if (idx >= 0) {
     Color c = bi.getTabColor(); 
     if (c !=  null) debug_tabs.setBackgroundAt(idx,c);
   }
   
}


void removeDebugInstance(BirdInstance bi)
{ 
   BirdDebugPanel pn = active_panels.get(bi.getId());
   
   int i = findPanelIndex(pn);
   if (i >= 0) {
      debug_tabs.removeTabAt(i);
      pn.dispose(); 
    }
}



private int findPanelIndex(BirdDebugPanel pnl)
{
   for (int i = 0; i < debug_tabs.getTabCount(); ++i) {
      BirdDebugPanel c = (BirdDebugPanel) debug_tabs.getTabComponentAt(i);
      if (c == pnl) {
         return i;
       }
    }
   
   return -1;
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                           */
/*                                                                              */
/********************************************************************************/

@Override public String getAuxType()            { return "DebuggerAssistant"; }

Object getLaunchId()                            { return launch_id; }



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
      pnl.addPopupButtons(menu);
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
    }
   
   @Override public void paint(Graphics g) {
      if (active_panels.isEmpty()) {
         Graphics2D g2 = (Graphics2D) g;
         Rectangle bnds = getBounds();
         g.setColor(Color.LIGHT_GRAY);
         g.drawRect(0,0,bnds.width,bnds.height);
         g.setColor(Color.BLACK);
         SwingText.drawText("Smart Debugger Assistant",g2,bnds);
       }
      else {
         super.paint(g);
       }
    }
   
}       // end of inner class DebugTabs




}       // end of class BirdDebugBubble




/* end of BirdDebugBubble.java */

