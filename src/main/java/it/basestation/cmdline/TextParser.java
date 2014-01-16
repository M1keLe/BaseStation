package it.basestation.cmdline;

import java.util.LinkedList;

public class TextParser {
	// attributi pacchetto
	private static long time;
	private static short lastRouter;
	private static short sender;
	private static short counter;
	private static short route;
	private static LinkedList<CapabilityInstance> capInstanceList = new LinkedList<CapabilityInstance>();
	private static LinkedList<String> capabilitiesSet = new LinkedList<String>();
	private static boolean insidePacket = false;

	public static void parseText(String line){
		if(line.contains("<packet>")&& !insidePacket){
			insidePacket = true;
		}
		else if(line.contains(">time:")&& insidePacket){
			// time = Long.parseLong(line.substring(line.indexOf(":")+1 ).trim());
			time = System.currentTimeMillis();
		}
		else if(line.contains(">router:")&& insidePacket){
			lastRouter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
		}
		else if(line.contains(">sender:")&& insidePacket){
			sender = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
			Node n = Configurator.getNode(sender);
			if(n == null){
				System.err.println("Sender id: " + sender);
				System.err.println("Il pacchetto non proviene da un nodo conosciuto");
				System.err.println("Controllare il file di configurazione");
				reset();
			}else{
				capabilitiesSet = n.getCapabilitiesSet();
			}
		}
		else if(line.contains(">Counter:")&& insidePacket){
			// da definire meglio!!!
			counter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
			CapabilityInstance cI = Configurator.getCapabilityInstance("Counter");
			cI.setValue(counter);
			capInstanceList.add(cI);
		}
		else if(line.contains(">route:")&& insidePacket){
			route = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
		}
		else if (line.contains(">") && line.contains(":") && !line.contains("<")&& insidePacket){
			String name = line.substring(line.indexOf('>')+1, line.indexOf(':'));
			
			LinkedList<CapabilityInstance> cList = Configurator.getCapabilityInstanceList(name, "local", false);
			
			if(cList != null && capabilitiesSet.contains(name)){
				for (CapabilityInstance cI : cList) {
					if(!cI.localOperator().contains("(") && !cI.localOperator().contains("(")){
						cI.setValue(Double.parseDouble(line.substring(line.indexOf(':')+1).trim()));
						capInstanceList.add(cI);
					}
				}		
			}
		}
		else if(line.contains("</packet>")&& insidePacket){
			Packet p = new Packet(time, lastRouter, sender, counter, route, capInstanceList);
			LocalStatsManager.addNewPacket(p);
			// debug
			// System.out.println(p);
			reset();
		}
	}
	

	private static void reset(){
		time = -1;
		lastRouter = -1;
		sender = -1;
		counter = -1;
		route = -1;
		capInstanceList = new LinkedList<CapabilityInstance>();
		capabilitiesSet = new LinkedList<String>();
		insidePacket = false;
	}
}
