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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.ivy.swing.SwingGridPanel;

class BirdDebugPanel extends SwingGridPanel implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BirdInstance    for_instance;
private JTextField      symptom_text;
private JTextField      state_text;
private JEditorPane     log_pane;
private JTextArea       input_area;
private JButton         symptom_btn;
private JButton         locations_btn;
private JButton         repairs_btn;
private JButton         explain_btn;

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
      case FINAL_LOCATIONS :
         symptom_btn.setEnabled(true);
         locations_btn.setEnabled(true);
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
   JTextField loc = addTextField("Location",for_instance.getLocationString(),
         null,null); 
   loc.setEditable(false);
   
   state_text = addTextField("State",for_instance.getState().toString(),
         null,null); 
   state_text.setEditable(false);
   
   symptom_text = addTextField("Symptom",for_instance.getSymptomString(),
         null,null);
   symptom_text.setEditable(false);
   
   addSeparator();
   
   log_pane = new JEditorPane("text/html","");
   log_pane.setMinimumSize(new Dimension(300,200));
   
   JScrollPane outrgn = new JScrollPane(log_pane,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
// outrgn.setMinimumSize(new Dimension(300,300));
   addLabellessRawComponent("Log",outrgn);
   
   input_area = addTextArea("Query","",1,40,null);
   input_area.setText("");
// input_area.setMinimumSize(new Dimension(300,30));
   
   addSeparator();
   
   symptom_btn = addBottomButton("Edit Symptom","SYMP",true,null);
   locations_btn = addBottomButton("Locations","LOCS",true,null);
   repairs_btn = addBottomButton("Repairs","FIX",true,null);
   explain_btn = addBottomButton("Explain","EXPLAIN",true,null);
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




}       // end of class BirdDebugPanel




/* end of BirdDebugPanel.java */

