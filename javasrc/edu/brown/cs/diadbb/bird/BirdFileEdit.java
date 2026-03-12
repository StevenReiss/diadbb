/********************************************************************************/
/*                                                                              */
/*              BirdFileEdit.java                                               */
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

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import edu.brown.cs.bubbles.bale.BaleConstants.BaleFileOverview;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class BirdFileEdit implements BirdConstants, Comparable<BirdFileEdit>
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BaleFileOverview for_file;
private int     start_offset;
private int     end_offset;
private String  edit_replace;
private int     edit_number;
private int     start_line;
private int     add_lines;
private int     delete_lines;

private static AtomicInteger edit_counter = new AtomicInteger(0);



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdFileEdit(BaleFileOverview bf,int offset,int end,String replace,
      int startline,int addline,int delline)
{
   for_file = bf;
   start_offset = offset;;
   end_offset = end;
   edit_replace = replace;
   edit_number = edit_counter.incrementAndGet();
   start_line = startline;
   add_lines = addline;
   delete_lines = delline;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

BaleFileOverview getFile()              { return for_file; }

int getStartLine()                      { return start_line; }

int getStartOffset()                    { return start_offset; }
int getEndOffset()                      { return end_offset; }


/********************************************************************************/
/*                                                                              */
/*      Apply the edit                                                          */
/*                                                                              */
/********************************************************************************/

boolean doEdit()
{
   BoardLog.logD("BIRD","Do edit " + start_offset + " " +
         end_offset + " " + edit_replace);
   
   // don't try indenting or formatting without a real editor
   return for_file.replace(start_offset,
         end_offset-start_offset,edit_replace,
         false,false);
}


void applyEdit(DefaultStyledDocument doc,int delta) throws BadLocationException
{
   int off = start_offset - delta;
   int len = end_offset - start_offset;
   String text  = edit_replace;
   if (text.isEmpty()) text = null;
   if (text != null && len == 0) {
      doc.insertString(off-delta,text,null);
    }
   else if (text == null) {
      doc.remove(off,len);
    }
   else {
      doc.replace(off,len,text,null);
    }
}




/********************************************************************************/
/*                                                                              */
/*      Comparator for sorting                                                  */
/*                                                                              */
/********************************************************************************/

@Override public int compareTo(BirdFileEdit  e1) 
{
   int v = for_file.getFile().getPath().compareTo(e1.for_file.getFile().getPath());
   if (v != 0) return v;
   v = e1.start_offset - start_offset;
   if (v != 0) return v;
   v = e1.edit_number - edit_number;
   return v;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw)
{
   xw.begin("TEXTEDIT");
   String fnm = IvyFile.getCanonicalPath(for_file.getFile());
   xw.field("FILE",fnm);
   xw.field("OFFSET",start_offset);
   xw.field("ENDOFFSET",end_offset);
   xw.field("LENGTH",end_offset - start_offset);
   xw.field("NUMBER",edit_number);
   xw.field("STARTLINE",start_line);
   xw.field("ADDLINES",add_lines);
   xw.field("DELLINES",delete_lines);
   if (edit_replace != null && !edit_replace.isEmpty()) {
      xw.cdataElement("REPLACE",edit_replace);
    }
   xw.end("TEXTEDIT");
}



}       // end of class BirdFileEdit




/* end of BirdFileEdit.java */

