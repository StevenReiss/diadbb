/********************************************************************************/
/*                                                                              */
/*              BirdInstance.java                                               */
/*                                                                              */
/*      Information about a DIAD debugging session                              */
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

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.board.BoardColors;
import edu.brown.cs.ivy.xml.IvyXml;

class BirdInstance implements BirdConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Element        instance_xml;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdInstance(Element xml)
{
   instance_xml = xml;
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getId()                 
{ 
   return IvyXml.getAttrString(instance_xml,"ID"); 
}

DiadCandidateState getState() 
{
   return IvyXml.getAttrEnum(instance_xml,"STATE",DiadCandidateState.DEAD);
}

String getTitle()
{
   Element thrd = IvyXml.getChild(instance_xml,"THREAD");
   String name = IvyXml.getAttrString(thrd,"NAME");
   if (name != null && !name.isEmpty()) return name;
   
   return getId();
}


String getLocationString()
{
   Element frm = IvyXml.getChild(instance_xml,"FRAME");
   if (frm == null) {
      return "<TBD>";
    }
   
   String cnm = IvyXml.getAttrString(frm,"CLASS");
   String mnm = IvyXml.getAttrString(frm,"METHOD");
   String line = IvyXml.getAttrString(frm,"LINE");
   
   if (cnm != null && !cnm.isEmpty()) {
      cnm = getShortName(cnm) + ".";
    }
   else cnm = "";
   if (line != null && !line.isEmpty()) {
      line = line + " @ ";
    }
   else line = "";
   
   if (mnm == null) mnm = "<TBD>";
   
   return line + cnm + mnm;
}


String getSymptomString()
{
   Element symp = IvyXml.getChild(instance_xml,"SYMPTOM");
   DiadSymptomType typ = IvyXml.getAttrEnum(symp,"TYPE",DiadSymptomType.NONE);
   String itm = IvyXml.getTextElement(symp,"ITEM");
   String orig = IvyXml.getTextElement(symp,"ORIGINAL");
   String tgt = IvyXml.getTextElement(symp,"TARGET");
   DiadValueOperator op = IvyXml.getAttrEnum(symp,"OPERATOR",DiadValueOperator.NONE);
   double prec = IvyXml.getAttrDouble(symp,"PRECISION",0);
   
   switch (typ) {
      case NONE :
         return "No Symptom Found";
      case CAUGHT_EXCEPTION : 
      case EXCEPTION :
         return "Exception " + getShortName(itm) + " was thrown";
      case ASSERTION :
         String cnts = null;
         if (op != DiadValueOperator.NONE && orig != null && tgt != null) {
            String ops = op.toString();
            if (prec != 0) ops = "~" + ops;
            cnts = orig + " " + ops + " " + tgt;
          }
         else if (orig != null) {
            cnts = orig;
          }
         if (cnts == null) return "Assertion failed";
         else return "Assertion failed: " + cnts;
      case VARIABLE :
         return "Variable " + itm + " = " + orig + ", should be " + tgt;
      case EXPRESSION :
         return "Expression " + itm + " = " + orig + ", should be " + tgt;
      case LOCATION :
         return "Execution should not be here";
      case NO_EXCEPTION :
         return "Exception " + getShortName(itm) + " should have been thrown";
    }
   return "SYMPTOM";
}


Color getTabColor()
{
   Color c = Color.WHITE;
   switch (getState()) {
      default :
      case INITIAL :
      case NO_SYMPTOM :
      case SYMPTOM_FOUND :
      case INITIAL_LOCATIONS :
      case ANALYSIS_DONE :
      case STARTING_FRAME_FOUND :
      case BASE_EXECUTION_DONE :
      case FINAL_LOCATIONS :
      case READY :
         String vl = getState().toString().toLowerCase();
         c = BoardColors.getColor("Bird.tab." + vl);
         break;
      case NO_STACK :
      case NO_ANALYSIS :
      case NO_START_FRAME :
      case NO_LOCATIONS :
      case NO_BASE_EXECUTION :
      case NO_FINAL_LOCATIONS :
      case DEAD :
      case INTERRUPTED :
         c = Color.RED;
         break;
    }
   c = BoardColors.getPaleColor(c);
   
   return c;
}



private String getShortName(String nm)
{
   if (nm == null) return null;
   int idx = nm.lastIndexOf(".");
   if (idx >= 0) {
      nm = nm.substring(idx+1);
    }
   return nm;
         
   
}
/********************************************************************************/
/*                                                                              */
/*      Update methods                                                          */
/*                                                                              */
/********************************************************************************/

boolean shouldRemove()
{
   switch (getState()) {
      case DEAD :
      case INTERRUPTED :
         return true;
    }
   
   return false;
}



void update(Element xml)
{
   instance_xml = xml;
}


}       // end of class BirdInstance




/* end of BirdInstance.java */

