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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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
import edu.brown.cs.bubbles.board.BoardProperties;
import edu.brown.cs.bubbles.buda.BudaBubble;
import edu.brown.cs.bubbles.buda.BudaBubbleArea;
import edu.brown.cs.bubbles.buda.BudaRoot;
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
private Map<PreviewPanel,BassName> preview_panels;
private Map<BassName,List<BirdFileEdit>> panel_edits;
private JTextField      validate_field;

private static final short MARGIN_WIDTH_PX = 35;

private static final long serialVersionUID = 1;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdPreviewBubble(BirdDebugPanel pnl,Collection<BirdFileEdit> edits)
{
   for_panel = pnl;
   repair_edits = new ArrayList<>(edits);
   
   preview_panels = new LinkedHashMap<>();
   panel_edits = new HashMap<>();
   
   setupPreviewPanels();
   
   JComponent comp = null;
   for (PreviewPanel pp : preview_panels.keySet()) {
      if (comp == null) comp = pp;
      else {
         comp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
               comp,pp);
       }
    }
   
   SwingGridPanel sgp = new SwingGridPanel();
   sgp.beginLayout();
   sgp.addBannerLabel("FIX: " + for_panel.getInstance().getSymptomString());
   validate_field = sgp.addTextField("VALIDATE","Validation Status",null,null);
   validate_field.setEditable(false);
   setValidationStatus("UNKNOWN");
   sgp.addLabellessRawComponent("EDITS",comp);
   sgp.addSeparator();
   sgp.addBottomButton("Make Repairs","REPAIRS",new MakeRepairsAction());
   sgp.addBottomButtons();
   
   setContentPane(sgp);
}



/********************************************************************************/
/*                                                                              */
/*      Handle popup menu                                                       */
/*                                                                              */
/********************************************************************************/

@Override public void handlePopupMenu(MouseEvent e) 
{
   JPopupMenu menu = new JPopupMenu();
   
   Point pt = new Point(e.getXOnScreen(),e.getYOnScreen());
   SwingUtilities.convertPointFromScreen(pt,this);
   Component c = SwingUtilities.getDeepestComponentAt(this,pt.x,pt.y);
   while (c != null) {
      BassName bn = preview_panels.get(c);
      if (bn != null) {
         menu.add(new RemovePanelAction((PreviewPanel) c));
         menu.add(new ShowSourceAction(bn));
         break;
       }
      else if (c == this) break;
      c = c.getParent();
    }
   
   menu.add(getFloatBubbleAction());
   
   menu.show(this,e.getX(),e.getY());
}


/********************************************************************************/
/*                                                                              */
/*      Handle validation status                                                */
/*                                                                              */
/********************************************************************************/

void setValidationStatus(String sts)
{
   validate_field.setText(sts);
   
   Color bkg = null;
   if (sts != null) {
      if (sts.contains("UNKNOWN") || sts.contains("MAYBE")) {
         bkg = BoardColors.getColor("Bird.valid.unknown");
       }
      else if (sts.equals("VALID")) {
         bkg = BoardColors.getColor("Bird.valid.valid");
       }
      else if (sts.contains("INVALID")) {
         bkg = BoardColors.getColor("Bird.valid.invalid");
       }
      else if (sts.startsWith("VALID_")) {
         bkg = BoardColors.getColor("Bird.valid.likely");
       }
      else {
         bkg = BoardColors.getColor("Bird.valid.unknown");
       }
    }
   if (bkg != null) validate_field.setBackground(bkg);
}


/********************************************************************************/
/*                                                                              */
/*      Setup preview panels                                                    */
/*                                                                              */
/********************************************************************************/

private void setupPreviewPanels()
{
   BassFactory bsf = BassFactory.getFactory();
   
   for (BirdFileEdit bfe : repair_edits) {
      BaleFileOverview bfo = bfe.getFile();
      int line = bfe.getStartLine();
      int loff = bfo.findLineOffset(line);
      int eoff = bfo.mapOffsetToEclipse(loff);
      BassName bn = bsf.findBubbleName(bfo.getFile(),eoff);
      if (bn == null) continue;
      List<BirdFileEdit> eds = panel_edits.get(bn);
      if (eds == null) {
         eds = new ArrayList<>();
         panel_edits.put(bn,eds);
       }
      eds.add(bfe);
    }
   
   BoardAttributes atts = new BoardAttributes("Bird");
   AttributeSet oldedit = atts.getAttributes("Original");
   AttributeSet newedit = atts.getAttributes("Edited");
   
   for (Map.Entry<BassName,List<BirdFileEdit>> ent : panel_edits.entrySet()) {
      List<BirdFileEdit> edits = ent.getValue();
      BaleFileOverview bfo = edits.get(0).getFile();
      BassName bn = ent.getKey();
      BumpLocation loc = bn.getLocation();
      int m0 = loc.getDefinitionOffset();
      int m1 = loc.getDefinitionEndOffset();
      if (m0 == 0 || m1 == 0) continue;
      
      int startline = bfo.findLineNumber(m0);
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
            BoardLog.logD("BIRD","Find edit " + s0 + " " + s1 + " " + m0 + " " + m1);
            Position p0 = d2.createPosition(s0 - 1);
            Position p1 = d2.createPosition(s1 + 1);
            origoffsets.add(s0);
            origoffsets.add(s1);
            editoffsets.add(p0);
            editoffsets.add(p1);
          }
         for (BirdFileEdit be : edits) {
            be.applyEdit(d2,m0); 
          }
         PreviewPanel pnl = new PreviewPanel(bn.getDisplayName(),d1,d2,startline);
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
         preview_panels.put(pnl,bn);
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
      setBackground(BoardColors.getColor("Bird.panel.background"));
      setOpaque(true);
      
      Dimension d = codepanel.getPreferredSize();
      d.width += 2*MARGIN_WIDTH_PX + 6;
      d.height = Math.min(d.height,600);
      codepanel.setPreferredSize(d);
    }
   
   JTextPane getOriginalEditor()                        { return before_editor; }
   JTextPane getEditedEditor()                          { return after_editor; }
   
}       // end of inner class PreviewPanel



private class PreviewEditor extends JTextPane {
   
   private static final long serialVersionUID = 1;
   
   PreviewEditor(StyledDocument d) {
      super(d);
      setEditable(false);
      setPreferredSize(new Dimension(300,400));
      setMargin(new Insets(2,5,2,5));
      BoardProperties bp = BoardProperties.getProperties("Bird");
      setFont(bp.getFont("Bird.preview.font"));
    }
   
   

}       // end of inner class PreviewEditor



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/
  
private class RemovePanelAction extends AbstractAction implements Runnable {
  
   private PreviewPanel for_panel;
   private static final long serialVersionUID = 1;
   
   RemovePanelAction(PreviewPanel pnl) {
      super("Remove these Edits");
      for_panel = pnl;
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      BassName bn = preview_panels.get(for_panel);
      List<BirdFileEdit> edits = panel_edits.get(bn);
      if (edits != null) {
         repair_edits.removeAll(edits);
         SwingUtilities.invokeLater(this);
       }
    }
   
   @Override public void run() {
      // might want to more sophisticated in setting up SplitPane
      for_panel.setVisible(false);
    }
   
}       // end of inner class RemovePanelAction



private class ShowSourceAction extends AbstractAction implements Runnable {
   
   private BassName for_name;
   private static final long serialVersionUID = 1; 
   
   ShowSourceAction(BassName bn) {
      super("Show Source Editor");
      for_name = bn;
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      SwingUtilities.invokeLater(this);
    }
   
   @Override public void run() {
      BudaBubble bb = for_name.createBubble();
      if (bb == null) return;
      BudaBubbleArea bba = BudaRoot.findBudaBubbleArea(BirdPreviewBubble.this);
      if (bba != null) {
         bba.addBubble(bb,BirdPreviewBubble.this,null,
               PLACEMENT_LOGICAL|PLACEMENT_MOVETO);
       }
    }

}       // end of inner class ShowSourceAction


private class MakeRepairsAction extends AbstractAction implements Runnable {
   
   private static final long serialVersionUID = 1; 
   
   MakeRepairsAction() {
      super("Make the Repairs");
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      SwingUtilities.invokeLater(this);
    }
   
   @Override public void run() {
      for (BirdFileEdit bfe : repair_edits) {
         bfe.doEdit();
       }
    }
   
}       // end of inner class MakeRepairsAction



}       // end of class BirdPreviewBubble




/* end of BirdPreviewBubble.java */

