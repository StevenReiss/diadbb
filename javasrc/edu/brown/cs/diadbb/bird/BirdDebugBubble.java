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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import edu.brown.cs.bubbles.bddt.BddtConstants;
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
   int i = debug_tabs.indexOfComponent(pnl);
   debug_tabs.setTabComponentAt(i,new ButtonTabComponent(pnl));
   
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
      if (c !=  null) {
         debug_tabs.setBackgroundAt(idx,c);
         BoardLog.logD("BIRD","Change background " + idx + " " + c);
         ButtonTabComponent tab = (ButtonTabComponent) debug_tabs.getTabComponentAt(idx);
         if (tab != null) {
            tab.setBackground(c);
            Container cont = tab.getParent();
            if (cont instanceof JComponent) {
               JComponent jc = (JComponent) cont;
               jc.setBackground(c);
//             jc.setOpaque(true);
             }
            BoardLog.logD("BIRD","Change tab color " + c);
          }
       }
    }
   
}


void removeDebugInstance(BirdInstance bi)
{ 
   BirdDebugPanel pn = active_panels.get(bi.getId());
   
   int i = findPanelIndex(pn);
   if (i >= 0) {
      BoardLog.logD("BIRD","Removing tab " + i);
      debug_tabs.removeTabAt(i);
      pn.dispose(); 
    }
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

Object getLaunchId()                            { return launch_id; }

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
      BoardLog.logD("BIRD","Paint debug tabs " + active_panels.size());
      if (active_panels.isEmpty()) {
         Graphics2D g2 = (Graphics2D) g;
         Rectangle bnds = getBounds();
         BoardLog.logD("BIRD","Draw empty panel " + bnds + " " + isOpaque());
         g2.setColor(Color.LIGHT_GRAY);
         g2.setBackground(Color.LIGHT_GRAY);
         g2.drawRect(0,0,bnds.width,bnds.height);
         g2.setColor(Color.BLACK);
         SwingText.drawText("Smart Debugger Assistant",g2,bnds);
       }
      else {
         super.paint(g);
       }
    }
   
}       // end of inner class DebugTabs



/********************************************************************************/
/*                                                                              */
/*      Tab component (from Oracle tutorial)                                    */
/*                                                                              */
/********************************************************************************/


private final class ButtonTabComponent extends JPanel {
   
   private static final long serialVersionUID = 1;
   
   ButtonTabComponent(BirdDebugPanel pnl) {
      //unset default FlowLayout' gaps
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));
      
      setOpaque(true);
      
      JLabel label = new ButtonTabLabel(pnl);
      //make JLabel read titles from JTabbedPane
      add(label);
      //add more space between the label and the button
      label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
      //tab button
      JButton button = new ButtonTabButton(pnl);
      add(button);
      //add more space to the top of the component
      setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
      
      BirdInstance bi = pnl.getInstance();
      setBackground(bi.getTabColor());
    }
    
}

private class ButtonTabLabel extends JLabel {
   
   private BirdDebugPanel tab_comp;
   private static final long serialVersionUID = 1;
   
   ButtonTabLabel(BirdDebugPanel tab) {
      tab_comp = tab;
      BirdInstance bi = tab_comp.getInstance();
      setText(bi.getTitle());
      setBackground(bi.getTabColor());
    }
   
   @Override public void paint(Graphics g) {
      Container comp = getParent();
      setBackground(comp.getBackground());
      BoardLog.logD("BIRD","Button tab paint " + comp.getBackground());
      super.paint(g);
    }
   
}       // end of inner class ButtonTabLabel


private class ButtonTabButton extends JButton implements ActionListener {
   
   private BirdDebugPanel tab_comp;
   private static final long serialVersionUID = 1;
   
   ButtonTabButton(BirdDebugPanel tab) {
      tab_comp = tab;
      int size = 17;
      setPreferredSize(new Dimension(size, size));
      setToolTipText("close this tab");
      //Make the button looks the same for all Laf's
      setUI(new BasicButtonUI());
      //Make it transparent
      setContentAreaFilled(false);
      //No need to be focusable
      setFocusable(false);
      setBorder(BorderFactory.createEtchedBorder());
      setBorderPainted(false);
      //Making nice rollover effect
      //we use the same listener for all buttons
      addMouseListener(new ButtonMouser());
      setRolloverEnabled(true);
      //Close the proper tab by clicking the button
      addActionListener(this);
    }
   
   public void actionPerformed(ActionEvent e) {
      int i = debug_tabs.indexOfTabComponent(tab_comp);
      if (i != -1) {
         BirdDebugPanel pnl = (BirdDebugPanel) debug_tabs.getTabComponentAt(i);
         BirdFactory bf = BirdFactory.getFactory();
         BirdInstance bi = pnl.getInstance();
         bf.removeInstance(bi);
         debug_tabs.removeTabAt(i);
         pnl.dispose();
       } 
    }
   
   //we don't want to update UI for this button
   public void updateUI() {
    }
   
   //paint the cross
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      //shift the image for pressed buttons
      if (getModel().isPressed()) {
         g2.translate(1, 1);
       }
      g2.setStroke(new BasicStroke(2));
      g2.setColor(Color.BLACK);
      if (getModel().isRollover()) {
         g2.setColor(Color.MAGENTA);
       }
      int delta = 6;
      g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
      g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
      g2.dispose();
    }
   
}       // end of inner class ButtonTabButtone



private final class ButtonMouser extends MouseAdapter {
   
   public void mouseEntered(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
         AbstractButton button = (AbstractButton) component;
         button.setBorderPainted(true);
       }
    }
   
   public void mouseExited(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
         AbstractButton button = (AbstractButton) component;
         button.setBorderPainted(false);
      
       }
    }
   
}       // end of inner class ButtonMouser


}       // end of class BirdDebugBubble




/* end of BirdDebugBubble.java */

