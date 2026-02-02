/********************************************************************************/
/*										*/
/*		BirdConstants.java						*/
/*										*/
/*	Bubbles Intelligent Responsive Debugger constants			*/
/*										*/
/********************************************************************************/
/*	Copyright 2025 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2025, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/


package edu.brown.cs.diadbb.bird;

import org.w3c.dom.Element;

public interface BirdConstants
{

enum DiadCandidateState {
   INITIAL,
   FINDING_SYMPTOM,
   NO_SYMPTOM_FOUND,
   DOING_ANALYSIS,
   FINDING_ALL_LOCATIONS,
   FINDING_STARTING_FRAME,
   DOING_BASE_EXECUTION,
   FINDING_EXECUTED_LOCATIONS,
   PREPARING_DATA,
   READY,
   NO_USER_STACK,
   NO_ANALYSIS,
   NO_START_FRAME,
   NO_LOCATIONS_FOUND,
   NO_BASE_EXECUTION,
   NO_FINAL_LOCATIONS,
   DEAD,
   INTERRUPTED,
}


enum DiadSymptomType {
   NONE,
   EXCEPTION,
   ASSERTION,
   VARIABLE,
   EXPRESSION,
   LOCATION,
   NO_EXCEPTION,
   CAUGHT_EXCEPTION,
}

enum DiadValueOperator {
   NONE,
   EQL, NEQ, GTR, GEQ, LSS, LEQ,
}


interface ResponseHandler {
   void handleResponse(Element xml);
}


}	// end of interface BirdConstants




/* end of BirdConstants.java */


