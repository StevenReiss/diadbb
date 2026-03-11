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

import edu.brown.cs.bubbles.bale.BaleConstants.BaleFileOverview;
import edu.brown.cs.bubbles.board.BoardLog;

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

private static AtomicInteger edit_counter = new AtomicInteger(0);



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdFileEdit(BaleFileOverview bf,int offset,int end,String replace)
{
   for_file = bf;
   start_offset = offset;;
   end_offset = end;
   edit_replace = replace;
   edit_number = edit_counter.incrementAndGet();
}


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




}       // end of class BirdFileEdit




/* end of BirdFileEdit.java */

