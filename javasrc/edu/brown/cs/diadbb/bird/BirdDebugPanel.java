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

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
private JEditorPane     input_area;
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
   setupPanel();
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
   JScrollPane outrgn = new JScrollPane(log_pane,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   addLabellessRawComponent("Log",outrgn);
   
   input_area = new JEditorPane("text/plain","");
   input_area.setEditable(true);
   JScrollPane inregion = new JScrollPane(input_area,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   addRawComponent("Query",inregion);
   
   addSeparator();
   
   symptom_btn = addBottomButton("Edit Symptom","SYMP",true,null);
   locations_btn = addBottomButton("Show Locations","LOCS",true,null);
   repairs_btn = addBottomButton("Find Repairs","FIX",true,null);
   explain_btn = addBottomButton("Explain","EXPLAIN",true,null);
   addBottomButtons();
}

}       // end of class BirdDebugPanel




/* end of BirdDebugPanel.java */

