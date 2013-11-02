package it.basestation.cmdline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Configurator {
	private static final String FILE_NAME = "config.txt"; 
	
	private static int freqDataProcessor = 1000*60*10;
	private static String usbPort = "/dev/ttyUSB0";
	private static String speedUsbPort = "57600";
	private static Calendar resetTime = null;
	
	// misure che non sono da mediare
	private static HashSet<String> indirectMeasures = new HashSet<String>();
	
	// capabilities globali
	private static HashSet<String> globalCap = new HashSet<String>();
	
	// capabilities con un range da rispettare
	private static LinkedList<Capability> rangedCapabilities = new LinkedList<Capability>();
	
	// capabilities derivate (il dato non viene preso dal sensore ma calcolato da altre misure)
	private static Hashtable<String,String> derivedMeasures = new Hashtable<String,String>();
	
	// lista di nodi
	private static Hashtable<Short,Node> nodes = new Hashtable<Short,Node>();
	
	public static boolean loadConfigFile(){
		boolean toRet = true;
		
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_NAME)));
			String temp = bReader.readLine();
		
			while(temp != null){
				if(!temp.isEmpty() && !temp.startsWith("##")){					
					StringTokenizer tok = new StringTokenizer(temp, ":");
					String varToSet = tok.nextToken().toLowerCase();
					System.out.println("varToSet = "+varToSet);
					String valueToSet = tok.nextToken().trim();
					
					switch (varToSet) {
					
					case "usbport":
						usbPort = valueToSet;
						System.out.println("USBPort impostata su: " + usbPort);
						break;
						
					case "freqdataprocessor":
						if(tryParseInt(valueToSet)){
							freqDataProcessor = Integer.parseInt(valueToSet) * 1000 * 60;
							System.out.println("frequenza ftupdater impostata a: " + freqDataProcessor/(1000*60) + " Minuti");							
						}else{
							System.out.println("FreqFTUpdater ERROR controllare file di configurazione");
							toRet = false;
						}
						break;
						
					case "usbspeedport":
						speedUsbPort = valueToSet;
						System.out.println("Baud Rate Usb Port: " + speedUsbPort);
						break;
						
					case "resettime":
						StringTokenizer tokTimer = new StringTokenizer(valueToSet, "_");
						String hour = tokTimer.nextToken().trim();
						String minute = tokTimer.nextToken().trim();
						String second = tokTimer.nextToken().trim();
						if (tryParseInt(hour) && tryParseInt(minute) && tryParseInt(second)) {
							resetTime = Calendar.getInstance();
							resetTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
							resetTime.set(Calendar.MINUTE, Integer.parseInt(minute));
							resetTime.set(Calendar.SECOND, Integer.parseInt(second));
							System.out.println("Il prossimo reset verrà effettuato il " + resetTime.getTime());							
						} else {
							System.out.println("Reset Time ERROR: Controllare il file di configurazione");
							toRet = false;
						}
						break;
						
					case "indirectmeasures":
						StringTokenizer tokSumCap = new StringTokenizer(valueToSet, "#");
						while(tokSumCap.hasMoreTokens()){
							String s = tokSumCap.nextToken();
							indirectMeasures.add(s);
							System.out.println("Aggiunta sumCap: " + s);
						}
						break;
						
					case "globalcap":
						StringTokenizer tokGlobalCap = new StringTokenizer(valueToSet, "#");
						while(tokGlobalCap.hasMoreTokens()){
							String s = tokGlobalCap.nextToken();
							globalCap.add(s);
							System.out.println("Aggiunta globalCap: " + s);
						}
						break;

					case "caprange":
						String capName = valueToSet.substring(0, valueToSet.indexOf('['));
						//System.out.println(capName);
						String range = valueToSet.substring(1+valueToSet.indexOf('['), valueToSet.indexOf(']'));
						//System.out.println(range);
						StringTokenizer tokRange = new StringTokenizer(range, "-");
						String sMin = tokRange.nextToken();
						String sMax = tokRange.nextToken();
						if(tryParseInt(sMin) && tryParseInt(sMax)){
							double min = Integer.parseInt(sMin);
							double max = Integer.parseInt(sMax);
							Capability c = new Capability(capName, isGlobalCapability(capName), isIndirectMeasure(capName));
							c.setRangeValues(min, max);
							rangedCapabilities.add(c);
							System.out.println(c);
							
						}
						else{
							System.out.println("Cababilities Range ERROR: controllare il file di configurazione");
							toRet = false;
						}
						break;
					case "derivedmeasure": // non ho effettuato un controllo sul parsing
						StringTokenizer tokMeasure = new StringTokenizer(valueToSet, "=");
						String name = tokMeasure.nextToken();
						String syntax = tokMeasure.nextToken().replaceAll("( )+", " ").trim();
						derivedMeasures.put(name, syntax);
						System.out.println("Add new derived measure named: "+name);
						System.out.println("Syntax: "+syntax);
						break;
						
					case "node":
						StringTokenizer tokNode = new StringTokenizer(valueToSet, ";");
						String sNodeID = tokNode.nextToken();
						String sXValue = tokNode.nextToken();
						String sYValue = tokNode.nextToken();
						if (tryParseShort(sNodeID) && tryParseInt(sXValue) && tryParseInt(sYValue)) {
							short nodeID = Short.parseShort(sNodeID);
							int xValue = Integer.parseInt(sXValue);
							int yValue = Integer.parseInt(sYValue);
							String cap = tokNode.nextToken();
							StringTokenizer tokCap = new StringTokenizer(cap, "#");
							HashSet<String> capability = new HashSet<String>();
							while(tokCap.hasMoreTokens()){
								capability.add(tokCap.nextToken());
							}
							
							Node n = new Node(nodeID, xValue, yValue, capability);
							// ----- controllo se le capability in elenco sono derivate o meno (es: controllare il numero di persone all'interno di una stanza)
							for (String capab : capability) {
								if(derivedMeasures.containsKey(capab)){
									n.addDerivedMeasure(capab, derivedMeasures.get(capab));
								}
							}
							
							nodes.put(nodeID, n);
							
							System.out.println("\n" + nodes.get(nodeID) + "\n");
						}else{
							System.out.println("Node ERROR controllare file di configurazione");
							toRet = false;
						}
						break;
						
					default:
						System.out.println("Default .....");
						break;
					}
				}
				temp = bReader.readLine();
			}
			
			bReader.close();
			
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			toRet = false;
		}catch (NoSuchElementException e) {
			e.printStackTrace();
			toRet = false;
		
		} catch (IOException e) {
			
			e.printStackTrace();
			toRet = false;
		}
		
		if(nodes.isEmpty()){
			toRet = false;
			System.out.println("Errore: nessun nodo configurato");
		}
		return toRet;
		
	}
	
	public static boolean isGlobalCapability(String capability){
		return globalCap.contains(capability);
	}
	
	public static boolean isIndirectMeasure(String capability){
		return indirectMeasures.contains(capability);
	}
	
	public static Capability getRangedCapability(String name){
		Capability toRet = null;
		for (Capability c : rangedCapabilities) {
			if(c.getName() == name){
				toRet = c;
				break;
			}
		}				
		return toRet;
	}
	
	public static String getUSBPort(){
		return usbPort;
	}
	
	public static int getFreqDataProcessor(){
		return freqDataProcessor;
	}
	
	public static Hashtable<Short,Node> getNodeList(){
		return nodes;
	}
	public static Node getNode(short nodeID){		
		return nodes.get(nodeID);
	}
	
	public static Date getResetTime(){
		Date toRet = null;
		if(resetTime != null)
			toRet = resetTime.getTime();
		
		return toRet;
	}
	
	public static String getDerivedMeasureSyntax(String name){
		return derivedMeasures.get(name);
	}
	
	// controllo sul parsing di dati
	
	private static boolean tryParseInt(String value){
		boolean toRet = false;
		 try {
			Integer.parseInt(value);
			toRet = true;
		} catch (NumberFormatException e) {
			toRet = false;
		}
		return toRet;
	}
	
	private static boolean tryParseShort(String value){
		boolean toRet = false;
		try {
			Short.parseShort(value);
			toRet = true;
		} catch (NumberFormatException e) {
			toRet = false;
		}
		return toRet;
	}
}
