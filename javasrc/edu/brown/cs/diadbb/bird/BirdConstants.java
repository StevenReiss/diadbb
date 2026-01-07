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
   NO_SYMPTOM,
   NO_STACK,
   NO_ANALYSIS,
   NO_START_FRAME,
   NO_LOCATIONS,
   NO_BASE_EXECUTION,
   NO_FINAL_LOCATIONS,
   SYMPTOM_FOUND,
   INITIAL_LOCATIONS,
   ANALYSIS_DONE,
   STARTING_FRAME_FOUND,
   BASE_EXECUTION_DONE,
   FINAL_LOCATIONS,
   READY,
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


