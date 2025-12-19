/********************************************************************************/
/*										*/
/*		BirdFactory.java						*/
/*										*/
/*	Factory for setting up and interfacing with DIAD for debugging		*/
/*										*/
/********************************************************************************/
/*	Copyright 2025 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2015, Brown University, Providence, RI.				 *
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


import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JPopupMenu;

import org.w3c.dom.Element;

import edu.brown.cs.bubbles.bale.BaleConstants;
import edu.brown.cs.bubbles.bale.BaleFactory;
import edu.brown.cs.bubbles.bale.BaleConstants.BaleContextConfig;
import edu.brown.cs.bubbles.board.BoardConstants;
import edu.brown.cs.bubbles.board.BoardPluginManager;
import edu.brown.cs.bubbles.board.BoardProperties;
import edu.brown.cs.bubbles.board.BoardSetup;
import edu.brown.cs.bubbles.board.BoardConstants.BoardPluginFilter;
import edu.brown.cs.bubbles.board.BoardLog;
import edu.brown.cs.bubbles.buda.BudaRoot;


public final class BirdFactory implements BirdConstants, MintConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private boolean server_running;
private boolean server_started;
private Map<String,ResponseHandler> hdlr_map;

private static BirdFactory the_factory = new BirdFactory();



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

public static void setup()
{
   BoardPluginManager.installResources(BirdFactory.class,"diad",new ResourceFilter());
}
 


private static final class ResourceFilter implements BoardPluginFilter {

   @Override public boolean accept(String nm) {
      return false;
    }

}	// end of inner class ResSurceFilter




public static void initialize(BudaRoot br)
{
// if (!BumpClient.getBump().getOptionBool("bubbles.useDiad")) return;

   BoardLog.logD("BIRD","USING LIMBA");

   switch (BoardSetup.getSetup().getRunMode()) {
      case NORMAL :
      case CLIENT :
	 the_factory.setupCallbacks();
	 break;
      case SERVER :
	 break;
    }

   BirdStarter bs = new BirdStarter(br);
   bs.start();
}


private void setupCallbacks()
{
   BaleFactory.getFactory().addContextListener(new BirdContexter());
}



public static BirdFactory getFactory()
{
   return the_factory;
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private BirdFactory()
{
   server_running = false;
   server_started = false;
   hdlr_map = new HashMap<>();

   BoardSetup bs = BoardSetup.getSetup();
   MintControl mc = bs.getMintControl();
   mc.register("<DIADREPLY RID='_VAR_0' />",new DiadReplyHandler());
   mc.register("<DIADREPLY DO='_VAR_0' />",new DiadMessageHandler());

   switch (BoardSetup.getSetup().getRunMode()) {
      case NORMAL :
      case CLIENT :
	 break;
      case SERVER :
	 mc.register("<BIRD TYPE='START' />",new StartHandler());
	 break;
    }
}



/********************************************************************************/
/*										*/
/*	Starting methods							*/
/*										*/
/********************************************************************************/

private void start()
{
   startDiad();
   if (!server_running) return;
// BoardProperties bp = BoardProperties.getProperties("Bird");
}



//CHECKSTYLE:OFF
private boolean startDiad()
// CHECKSTYLE:ON
{
   BoardSetup bs = BoardSetup.getSetup();
   MintControl mc = bs.getMintControl();
   BoardProperties bp = BoardProperties.getProperties("Bird");

   if (BoardSetup.getSetup().getRunMode() == BoardConstants.RunMode.CLIENT) {
      MintDefaultReply rply = new MintDefaultReply();
      mc.send("<LIMBA DO='PING' />");
      String rslt = rply.waitForString();
      if (rslt != null) {
	 server_running = true;
	 server_started = true;
	 return true;
       }
    }

   if (server_running || server_started) return false;

   BoardLog.logD("BIRD","Starting diad server");

   IvyExec exec = null;
   File wd =  new File(bs.getDefaultWorkspace());
   File logf = new File(wd,"diad.log");

   List<String> args = new ArrayList<>();

   args.add(IvyExecQuery.getJavaPath());

   String dbgargs = bp.getProperty("Bird.jvm.args");
   if (dbgargs != null && dbgargs.contains("###")) {
      int port = (int) Math.random() * 1000 + 3000;
      dbgargs = dbgargs.replace("###",Integer.toString(port));
      BoardLog.logI("BIRD","Diad debug port " + port);
    }
   if (dbgargs != null) {
      StringTokenizer tok = new StringTokenizer(dbgargs);
      while (tok.hasMoreTokens()) {
	 args.add(tok.nextToken());
       }
    }
   File jarfile = IvyFile.getJarFile(BirdFactory.class);

   String xcp = bp.getProperty("Bird.diad.class.path");
   if (xcp == null) {
      xcp = System.getProperty("java.class.path");
      String ycp = bp.getProperty("Bird.diad.add.path");
      if (ycp != null) xcp = ycp + File.pathSeparator + xcp;
    }
   else {
      BoardSetup setup = BoardSetup.getSetup();
      StringBuffer buf = new StringBuffer();
      StringTokenizer tok = new StringTokenizer(xcp,":;");
      while (tok.hasMoreTokens()) {
	 String elt = tok.nextToken();
	 if (!elt.startsWith("/") &&  !elt.startsWith("\\")) {
	    if (elt.equals("eclipsejar")) {
	       elt = setup.getEclipsePath();
	     }
	    else if (elt.equals("diad.jar") && jarfile != null) {
	       elt = jarfile.getPath();
	     }
	    else {
	       String oelt = elt;
	       elt = setup.getLibraryPath(elt);
	       File f1 = new File(elt);
	       if (!f1.exists()) {
		  f1 = setup.getLibraryDirectory().getParentFile();
		  File f2 = new File(f1,"dropins");
		  File f3 = new File(f2,oelt);
		  if (f3.exists()) elt = f3.getPath();
		}
	       BoardLog.logD("BIRD","Use class path diad element " + elt);
	     }
	  }
	 if (buf.length() > 0) buf.append(File.pathSeparator);
	 buf.append(elt);
       }
      xcp = buf.toString();
    }
   args.add("-cp");
   args.add(xcp);

   args.add("edu.brown.cs.diad.diad.DiadMain");
   args.add("-m");
   args.add(bs.getMintName());
   args.add("-L");
   args.add(logf.getPath());
   if (bp.getBoolean("Bird.diad.debug")) {
      args.add("-D");
    }

   synchronized (this) {
      if (server_started || server_running) return false;
      server_started = true;
    }

   for (int i = 0; i < 500; ++i) {
      MintDefaultReply rply = new MintDefaultReply();
      mc.send("<DIAD DO='PING' />",rply,MINT_MSG_FIRST_NON_NULL);
      String rslt = rply.waitForString(1000);
      BoardLog.logD("BIRD","Diad ping response " + rslt);
      if (rslt != null) {
	 server_running = true;
	 break;
       }
      if (i == 0) {
	 try {
	    // make IGNORE_OUTPUT to clean up otutput
	    exec = new IvyExec(args,null,IvyExec.ERROR_OUTPUT);
	    BoardLog.logD("BIRD","Run " + exec.getCommand());
	  }
	 catch (IOException e) {
	    break;
	  }
       }
      else {
	 try {
	    if (exec != null) {
	       int sts = exec.exitValue();
	       BoardLog.logD("BIRD","Diad server disappeared with status " + sts);
	       break;
	     }
	  }
	 catch (IllegalThreadStateException e) { }
       }

      try {
	 Thread.sleep(2000);
       }
      catch (InterruptedException e) { }
    }
   if (!server_running) {
      BoardLog.logE("BIRD","Unable to start diad server: " + args);
      return false;
    }

   return true;
}


private static class BirdStarter extends Thread {

   BirdStarter(BudaRoot br) {
      super("Diad Starter");
    }

   @Override public void run() {
      the_factory.start();
    }

}	// end of inner class BirdStarter


private final class StartHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      boolean sts = startDiad();
      if (sts) msg.replyTo("<RESULT VALUE='true'/>");
      else msg.replyTo("<RESULT VALUE='false' />");
    }

}	 // end of inner class StartHandler




/********************************************************************************/
/*										*/
/*	Command methods 							*/
/*										*/
/********************************************************************************/

void issueCommand(String cmd,CommandArgs args,String elt,String body,ResponseHandler hdlr)
{
   String cnts = body;
   if (elt != null) {
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.cdataElement(elt,body);
      cnts = xw.toString();
      xw.close();
    }

   issueXmlCommand(cmd,args,cnts,hdlr);
}


void issueXmlCommand(String cmd,CommandArgs args,String body,ResponseHandler hdlr)
{
   if (hdlr == null) hdlr = new DummyResponder();

   String rid = "DIAD_" + (int) (Math.random()*1000000);
   hdlr_map.put(rid,hdlr);
   if (args == null) args = new CommandArgs("RID",rid);
   else args.put("RID",rid);

   Element xml = sendDiadMessage(cmd,args,body);
   String nrid = IvyXml.getAttrString(xml,"RID");
   if (!rid.equals(nrid)) {
      BoardLog.logE("BIRD","Reply ids don't match " + rid + " " + nrid);
    }
}


private static final class DummyResponder implements ResponseHandler {
   @Override public void handleResponse(Element xml) { }
}	// end of inner class DummyResponder



/********************************************************************************/
/*										*/
/*	Diad Server communication						*/
/*										*/
/********************************************************************************/

Element sendDiadMessage(String cmd,CommandArgs args,String cnts)
{
   if (!server_running) return null;

   BoardSetup bs = BoardSetup.getSetup();
   MintControl mc = bs.getMintControl();

   MintDefaultReply rply = new MintDefaultReply();
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("DIAD");
   xw.field("DO",cmd);
   if (args != null) {
      for (Map.Entry<String,Object> ent : args.entrySet()) {
	 xw.field(ent.getKey(),ent.getValue());
       }
    }
   if (cnts != null) {
      xw.xmlText(cnts);
    }
   xw.end("DIAD");
   String msg = xw.toString();
   xw.close();

   BoardLog.logD("BIRD","Send to DIAD: " + msg);

   mc.send(msg,rply,MINT_MSG_FIRST_NON_NULL);

   Element rslt = rply.waitForXml(60000);

   BoardLog.logD("BSEAN","Reply from DIAD: " + IvyXml.convertXmlToString(rslt));

   return rslt;
}


/********************************************************************************/
/*										*/
/*	Message handling							*/
/*										*/
/********************************************************************************/

private final class DiadReplyHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      Element xml = msg.getXml();
      String rid = args.getArgument(0);
      String rslt = null;
      try {
	 BoardLog.logD("BIRD","Handle deferred reply " + rid + " " +
	       IvyXml.convertXmlToString(xml));
	 ResponseHandler hdlr = hdlr_map.remove(rid);
	 if (hdlr != null) {
	    Element xmlrslt = IvyXml.getChild(xml,"RESULT");
	    hdlr.handleResponse(xmlrslt);
	  }
       }
      catch (Throwable e) {
	 BoardLog.logE("BIRD","Error processing command",e);
       }
      msg.replyTo(rslt);
   }

}	// end of inner class UpdateHandler



private final class DiadMessageHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      Element xml = msg.getXml();
      String cmd = args.getArgument(0);
      try {
         BoardLog.logD("BIRD","Handle DIAD message " + cmd + " " +
               IvyXml.convertXmlToString(xml));
         switch (cmd) {
            case "PING" :
               msg.replyTo("<PONG/>");
               break;      
            default :
               BoardLog.logE("BRID","Unknown DIAD message " + cmd);
               msg.replyTo();
               break;
          }
       }
      catch (Throwable e) {
         BoardLog.logE("BIRD","Error processing diad message",e);
         msg.replyTo();
       }
    }

}	// end of inner class UpdateHandler



/********************************************************************************/
/*										*/
/*	Editor context actions							*/
/*										*/
/********************************************************************************/

private final class BirdContexter implements BaleConstants.BaleContextListener {

   @Override public void addPopupMenuItems(BaleContextConfig cfg,JPopupMenu menu) {
    }

}	// end of inner class BucsContexter


}      // end of class BirdFactory




/* end of BirdFactory.java */


