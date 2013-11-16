package it.basestation.cmdline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Configurator {
	private static final String FILE_NAME = "config.txt"; 
	private static final int MINUTE =1000*60; 
	
	private static int freqDataProcessor = MINUTE * 10;
	private static String usbPort = "/dev/ttyUSB0";
	private static String speedUsbPort = "57600";
	private static Calendar resetTime = null;
	
	// Stringa che contiene eventuali errori nel file di configurazione
	private static String log = "";
	private static int lineCounter = 0;
	
	// elenco capabilities
	private static LinkedList<Capability> capabilities = new LinkedList<Capability>();
	
	// lista di nodi
	private static Hashtable<Short,Node> nodes = new Hashtable<Short,Node>();
	
	// set capabilities globali
	private static LinkedList <String> globalCapabilitiesSet = new LinkedList<String>();
	
	public static boolean loadConfigFile(){
		boolean toRet = true;
		
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_NAME)));
			String line;
			String name = "";
			String localOperator = "";
			String globalOperator = "";
			String min = "";
			String max = "";
			Double minValue = Double.NEGATIVE_INFINITY;
			Double maxValue = Double.POSITIVE_INFINITY;
		
			while((line = bReader.readLine()) != null){
				++lineCounter;
				line = line.trim();
				
				// commenti su config.txt
				if(!line.startsWith("#")){									
					if(line.indexOf("FreqDataProcessor") != -1){
						
						String freq = line.substring(line.indexOf(':')+1).trim();
						System.out.println("La freq è: ->"+freq+ "<- minuti");
						if(tryParseInt(freq)){
							freqDataProcessor = Integer.parseInt(freq) * MINUTE;
						}else{
							log += "[Line: "+lineCounter+"] La freq non è stata impostata correttamente: ->"+freq +"\n";
							toRet = false;
						}
					}
					else if(line.indexOf("USBPort") != -1){
						usbPort = line.substring(line.indexOf(':')+1).trim();
						System.out.println("La usbport è: ->"+usbPort);						
					}
					else if(line.indexOf("USBSpeedPort") != -1){
						speedUsbPort = line.substring(line.indexOf(':')+1).trim();
						System.out.println("Speed usbport ->"+speedUsbPort);
					}
					else if(line.indexOf("ResetTime") != -1){
						StringTokenizer tokTimer = new StringTokenizer(line.substring(line.indexOf(':')+1).trim(), ":");
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
	                            log += "[Line: "+lineCounter+"] Reset Time ERROR: Controllare il file di configurazione\n";
	                            toRet = false;
	                    }					
					}
					
					else if(line.contains("<") && line.contains("[")){
						name = line.substring(line.indexOf('<')+1, line.indexOf('[')).trim();
						min = line.substring(line.indexOf('[')+1, line.indexOf(',')).trim();
						max = line.substring(line.indexOf(',')+1, line.indexOf(']')).trim();
					}
					else if(line.contains("local")){
						localOperator = line.substring(line.indexOf(':')+1).replaceAll(" {2,}", " ").trim();
					}
					else if(line.contains("global")){
						globalOperator = line.substring(line.indexOf(':')+1).replaceAll(" {2,}", " ").trim();
					}
					else if(line.contains("</"+name+">")){
		
						if(!min.equals("*")){
							if(tryParseDouble(min)){
								minValue = Double.parseDouble(min);
							}else{
								toRet = false;
								log += "[Line: "+lineCounter+"] Errore impostazione minValue Capability chiamata: " +name+"\n";
							}
							
						}
						if(!max.equals("*")){
							if(tryParseDouble(max)){
								maxValue = Double.parseDouble(max);
							}else{
								toRet = false;
								log += "[Line: "+lineCounter+"] Errore impostazione maxValue Capability chiamata: " +name +"\n";
							}
						}
						capabilities.add(new Capability(name, localOperator, globalOperator, minValue, maxValue));
						System.out.println("*********** New Capability ***********");
						System.out.println("Name: " +name);
						System.out.println("Local Operator: " +localOperator);
						System.out.println("Global Operator: " +globalOperator);
						System.out.println("Min Value: " +minValue);
						System.out.println("Max Value: " +maxValue);
						System.out.println("********* End New Capability *********\n");
						if(!globalOperator.isEmpty()){
							globalCapabilitiesSet.add(name);
							System.out.println("Aggiunta Capability set globale ->"+globalOperator+"<-");
						}
						
						name = "";
						localOperator = "";
						globalOperator = "";
						min = "";
						max = "";
						minValue = Double.NEGATIVE_INFINITY;
						maxValue = Double.POSITIVE_INFINITY;
					
					}
					else if(line.indexOf("Node") != -1){
						String nodeLine = line.substring(line.indexOf(':')+1).trim();
						StringTokenizer tokNode = new StringTokenizer(nodeLine, ";");
	                    String sNodeID = tokNode.nextToken();
	                    String sXValue = tokNode.nextToken();
	                    String sYValue = tokNode.nextToken();
	                    if (tryParseShort(sNodeID) && tryParseInt(sXValue) && tryParseInt(sYValue)) {
	                            short nodeID = Short.parseShort(sNodeID);
	                            int xValue = Integer.parseInt(sXValue);
	                            int yValue = Integer.parseInt(sYValue);
	                            String cap = tokNode.nextToken();
	                            StringTokenizer tokCap = new StringTokenizer(cap, "#");
	                            LinkedList<String> capability = new LinkedList<String>();
	                            while(tokCap.hasMoreTokens()){
	                            	// controllo se le capabilities sono state tutte dichiarate
	                            	String s = tokCap.nextToken();
	                            	if(getCapabilityInstance(s) == null){
	                            		log += "[Line: "+lineCounter+"] La capability \""+s+"\" non è stata dichiarata! controllare il file di configurazione\n";
	                            		toRet = false;
	                            	}else{
	                            		capability.add(s);
	                            	}
                                    
	                            }

	                            Node n = new Node(nodeID, xValue, yValue, capability);
	                            
	                            nodes.put(nodeID, n);
	                            
	                            System.out.println("\n" + nodes.get(nodeID) + "\n");
	                    }else{
	                            log += "[Line: "+lineCounter+"] Node ERROR controllare file di configurazione\n";
	                            toRet = false;
	                    }
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			log += e + "\n";
			e.printStackTrace();
			toRet = false;
		}catch (NoSuchElementException e) {
			log += e + "\n";
			e.printStackTrace();
			toRet = false;
		
		} catch (IOException e) {
			log += e + "\n";
			e.printStackTrace();
			toRet = false;
		} finally {
			try {
				if (bReader != null)
					bReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		if(nodes.isEmpty()){
			toRet = false;
			log += "Errore: nessun nodo configurato\n";
		}
		
		if(capabilities.isEmpty()){
			toRet = false;
			log += "Errore: nessuna capability configurata\n";
		}
		
		if(toRet)
			System.out.println("Il file di configurazione è stato caricato correttamente.");
		else
			System.out.println(log);
		
		lineCounter = 0;
		
		return toRet;
		
	}	
	
	public static String getUSBPort(){
		return usbPort;
	}
	
	public static int getFreqDataProcessor(){
		return freqDataProcessor;
	}
	
	public static CapabilityInstance getCapabilityInstance(String name){
		CapabilityInstance toRet = null;
		for (Capability c : capabilities) {
			if(name.equals(c.getName())){
				toRet = new CapabilityInstance(c.getName(),  c.localOperator(), c.globalOperator(), c.getMinValue(), c.getMaxValue());
				break;
			}
		}
		return toRet;
	}
		
	public static LinkedList<String> getGlobalCapabilitiesSet(){
		return globalCapabilitiesSet;
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
	

	
	// controllo sul parsing di dati
	
	private static boolean tryParseDouble(String value){
		boolean toRet = false;
		 try {
			Double.parseDouble(value);
			toRet = true;
		} catch (NumberFormatException e) {
			toRet = false;
		}
		return toRet;
	}
	
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
