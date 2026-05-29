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
import java.util.HashMap;
import java.util.Map;

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

private Element         instance_xml;
private boolean         should_save;

private static final Map<DiadValueOperator,String> OP_NAMES;

static {
   OP_NAMES = new HashMap<>();
   OP_NAMES.put(DiadValueOperator.EQL,"==");
   OP_NAMES.put(DiadValueOperator.GEQ,">=");
   OP_NAMES.put(DiadValueOperator.GTR,">");
   OP_NAMES.put(DiadValueOperator.LEQ,"<=");
   OP_NAMES.put(DiadValueOperator.LSS,"<");
   OP_NAMES.put(DiadValueOperator.NEQ,"!=");
   OP_NAMES.put(DiadValueOperator.CONTAINS,"CONTAINS");
   OP_NAMES.put(DiadValueOperator.NOTCONTAINS,"NOT CONTAINS"); 
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BirdInstance(Element xml)
{
   instance_xml = xml;
   should_save = false;
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


String getResponse()
{
   return IvyXml.getTextElement(instance_xml,"RESPONSE");
}


boolean getAutoQuery()
{
   return IvyXml.getAttrBool(instance_xml,"AUTO_QUERY");
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
   
   String ops = OP_NAMES.get(op);
   if (ops == null) {
      ops = op.toString();
    }
   
   switch (typ) {
      case NONE :
         return "No Symptom Found";
      case EXCEPTION :
         return "Exception " + getShortName(itm) + " was thrown";
      case LIBRARY_EXCEPTION :
         String rtn = IvyXml.getTextElement(symp,"ORIGINAL");
         if (rtn != null && !rtn.isEmpty()) {
            int idx = rtn.lastIndexOf(";");
            if (rtn.equals("<init>")) rtn = "Constructor";
            else if (idx > 0) {
               rtn = "``" + rtn.substring(0,idx) + "''";
             }
            else rtn = "Method " + rtn;
            return rtn + " throws " + getShortName(itm);
          }
         else {
            return "Exception " + getShortName(itm) + " was thrown in library";
          }
      case ASSERTION :
         String cnts = null;
         if (op != DiadValueOperator.NONE && orig != null && tgt != null) {
            if (prec != 0) ops = "~" + ops;
            cnts = orig + " " + ops + " " + tgt;
          }
         else if (itm != null) {
            cnts = itm;
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
      case OTHER :
         String desc = IvyXml.getTextElement(symp,"USER");
         if (desc != null && !desc.isEmpty()) return desc;
         return "User Defined Problem";
    }
   return "SYMPTOM";
}


Color getTabColor()
{
   Color c = Color.WHITE;
   String vl = getState().toString().toLowerCase();
   
   switch (getState()) {
      default :
      case INITIAL :
      case FINDING_SYMPTOM :
      case NO_SYMPTOM_FOUND :
      case DOING_ANALYSIS :
      case FINDING_ALL_LOCATIONS:
      case FINDING_STARTING_FRAME :
      case DOING_BASE_EXECUTION :
      case FINDING_EXECUTED_LOCATIONS :
      case PREPARING_DATA :
      case READY :
         c = BoardColors.getColor("Bird.tab." + vl);
         break;
      case NO_ANALYSIS :
      case NO_LOCATIONS_FOUND :
      case NO_BASE_EXECUTION :
      case NO_FINAL_LOCATIONS :
         c = BoardColors.getColor("Bird.tab.simple");
         break;
      case NO_USER_STACK :
      case NO_START_FRAME :
      case DEAD :
      case INTERRUPTED :
         c = Color.RED;
         break;
    }

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


boolean isShouldSave()                          { return should_save; }
void setShouldSave(boolean fg)                  { should_save = fg; }

Element getXml()                                { return instance_xml; }

String getStartFrameId() {
   Element xml = IvyXml.getChild(instance_xml,"STARTFRAME");
   Element xml1 = IvyXml.getChild(xml,"FRAME");
   String id = IvyXml.getAttrString(xml1,"ID");
   return id;
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

