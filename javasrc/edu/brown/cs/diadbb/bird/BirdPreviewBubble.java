/********************************************************************************/
/*                                                                              */
/*              BirdPreviewBubble.java                                          */
/*                                                                              */
/*      Bubble to show a preview of a proposed patch                            */
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import edu.brown.cs.bubbles.bale.BaleConstants.BaleFileOverview;
import edu.brown.cs.bubbles.bass.BassFactory;
import edu.brown.cs.bubbles.bass.BassName;
import edu.brown.cs.bubbles.board.BoardAttributes;
import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaBubble;
import edu.brown.cs.bubbles.bump.BumpLocation;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingLineScrollPane;

class BirdPreviewBubble extends BudaBubble implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BirdDebugPanel  for_panel;
private Collection<BirdFileEdit> repair_edits;
private List<PreviewPanel> preview_panels;

private final short MARGIN_WIDTH_PX = 35;

private static final long serialVersionUID = 1;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdPreviewBubble(BirdDebugPanel pnl,Collection<BirdFileEdit> edits)
{
   for_panel = pnl;
   repair_edits = edits;
   
   preview_panels = new ArrayList<>();
   
   setupPreviewPanels();
   
   // create a SwingGridPane holding the preview panels and
   //    bottom buttons for make repair, goto sources
   
   // handle popup menu based on preview panel
}




/********************************************************************************/
/*                                                                              */
/*      Setup preview panels                                                    */
/*                                                                              */
/********************************************************************************/

private void setupPreviewPanels()
{
   BassFactory bsf = BassFactory.getFactory();
   Map<BassName,List<BirdFileEdit>> nameedits = new HashMap<>();
   
   for (BirdFileEdit bfe : repair_edits) {
      BaleFileOverview bfo = bfe.getFile();
      int line = bfe.getStartLine();
      int loff = bfo.findLineOffset(line);
      int eoff = bfo.mapOffsetToEclipse(loff);
      BassName bn = bsf.findBubbleName(bfo.getFile(),eoff);
      if (bn == null) continue;
      List<BirdFileEdit> eds = nameedits.get(bn);
      if (eds == null) {
         eds = new ArrayList<>();
         nameedits.put(bn,eds);
       }
      eds.add(bfe);
    }
   
   for (Map.Entry<BassName,List<BirdFileEdit>> ent : nameedits.entrySet()) {
      List<BirdFileEdit> edits = ent.getValue();
      BaleFileOverview bfo = edits.get(0).getFile();
      BassName bn = ent.getKey();
      BumpLocation loc = bn.getLocation();
      int m0 = loc.getDefinitionOffset();
      int m1 = loc.getDefinitionEndOffset();
      if (m0 == 0 || m1 == 0) continue;
      BoardAttributes atts = new BoardAttributes("Bird");
      AttributeSet oldedit = atts.getAttributes("Original");
      AttributeSet newedit = atts.getAttributes("Edited");
      
      List<Integer> origoffsets = new ArrayList<>();
      List<Position> editoffsets = new ArrayList<>();
      
      DefaultStyledDocument d1 = new DefaultStyledDocument();
      DefaultStyledDocument d2 = new DefaultStyledDocument();
      try {
         String text = bfo.getText(m0,m1-m0);
         d1.insertString(0,text,null);
         d2.insertString(0,text,null);
         for (BirdFileEdit be : edits) {
            int s0 = be.getStartOffset() - m0;
            int s1 = be.getEndOffset() - m0;
            if (s0 < 0) s0 = 0;
            if (s1 > m1-m0) s1 = m1-1;
            Position p0 = d2.createPosition(s0 - 1);
            Position p1 = d2.createPosition(s1 + 1);
            editoffsets.add(p0);
            editoffsets.add(p1);
          }
         for (BirdFileEdit be : edits) {
            be.applyEdit(d2,m0); 
          }
         PreviewPanel pnl = new PreviewPanel(bn.getDisplayName(),d1,d2,0);
         JTextPane otp = pnl.getOriginalEditor();
         JTextPane etp = pnl.getEditedEditor();
         for (int i = 0; i < origoffsets.size(); i += 2) {
            int s0 = origoffsets.get(i);
            int s1 = origoffsets.get(i+1);
            int e0 = editoffsets.get(i).getOffset()+1;
            int e1 = editoffsets.get(i+1).getOffset()-1;
            otp.select(s0,s1);
            otp.setCharacterAttributes(oldedit,true);
            etp.select(e0,e1);
            etp.setCharacterAttributes(newedit,true);
          }
         preview_panels.add(pnl);
       }
      catch (BadLocationException e) {
         BoardLog.logE("BIRD","Problem getting text for preview",e);
         continue;
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Preview Panel                                                           */
/*                                                                              */
/********************************************************************************/

private class PreviewPanel extends SwingGridPanel {
   
   private JTextPane before_editor;
   private JTextPane after_editor;
   
   private static final long serialVersionUID = 1;
   
   PreviewPanel(String ttl,StyledDocument d1,StyledDocument d2,int start) {
      String txt = "Preview of " + ttl;
      beginLayout();
      addBannerLabel(txt);
      addSeparator();
      before_editor = new PreviewEditor(d1);
      after_editor = new PreviewEditor(d2);
      JScrollPane bsp = new SwingLineScrollPane(before_editor,start);
      JScrollPane asp = new SwingLineScrollPane(after_editor,start);
      
      SwingGridPanel codepanel = new SwingGridPanel();
      codepanel.addGBComponent(bsp,0,0,1,1,10,10);
      codepanel.addGBComponent(new JSeparator(JSeparator.VERTICAL),1,0,1,1,0,10);
      codepanel.addGBComponent(asp,2,0,1,1,10,10);
      addLabellessRawComponent("EDITS",codepanel);
      setBackground(BoardColors.getColor("Rose.background.color"));
      setOpaque(true);
      if (start > 0) {
         Dimension d = codepanel.getPreferredSize();
         d.width += 2*MARGIN_WIDTH_PX + 6;
         codepanel.setPreferredSize(d);
       }
      
    }
   
   JTextPane getOriginalEditor()                        { return before_editor; }
   JTextPane getEditedEditor()                          { return after_editor; }
   
}       // end of inner class PreviewPanel



private class PreviewEditor extends JTextPane {
   
   private static final long serialVersionUID = 1;
   
   PreviewEditor(StyledDocument d) {
      super(d);
      setEditable(false);
    }

}       // end of inner class PreviewEditor



}       // end of class BirdPreviewBubble




/* end of BirdPreviewBubble.java */

