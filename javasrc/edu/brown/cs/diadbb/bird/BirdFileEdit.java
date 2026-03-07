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



class BirdFileEdit implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private int     edit_line;
private int     line_count;
private String  edit_replace;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdFileEdit(int line,int length,String replace)
{
   edit_line = line;
   line_count = length;
   edit_replace = replace;
}



}       // end of class BirdFileEdit




/* end of BirdFileEdit.java */

