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
   return getId();
}

String getLocationString()
{
   return "line @ method";
}


String getSymptomString()
{
   return "SYMPTOM";
}


Color getTabColor()
{
   return null;
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
      case INTERUPTED :
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

