package it.basestation.cmdline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class FileReader extends Thread {
	private String fileName = ""; 
			
	private BufferedReader bReader = null;
	
	// attributi pacchetto
	private long time;
	private short lastRouter;
	private short sender;
	private short counter;
	private short route;
	private LinkedList<CapabilityInstance> capInstanceList = new LinkedList<CapabilityInstance>();
	private LinkedList<String> capabilitiesSet = new LinkedList<String>();
	private boolean insidePacket = false;
	
	// timestamp arrivo pacchetto simulo tempo tra arrivo di un pacchetto ed un altro
	private long lastTimeStamp = 0;
	private long newTimeStamp = 0;
	
	private Calendar now;

	public FileReader(String fileName) {	
		super ("File Reader");
		this.fileName = fileName;
	}
		
	@Override
	public void run(){
		while(true){		
			
			try {
				bReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName)));
				String line = "";
				while((line = bReader.readLine()) != null){
					
					if(line.contains("<packet>")&& !this.insidePacket){
						
						this.insidePacket = true;
/*						// old log
						this.lastTimeStamp = newTimeStamp;
						this.newTimeStamp = Long.parseLong(line.substring(line.indexOf('>') +1).trim());
*/						// end old log
						
						// new log
						now = Calendar.getInstance();
						String time = line.substring(line.indexOf('>') +1).trim();
						int h; 		// ora
						int m; 		// minuti
						int s; 		// secondi
						int mils; 	// millisecondi
						mils = Integer.parseInt(time.substring(time.indexOf('.')+1));
						time = time.substring(0, time.lastIndexOf('.'));
						StringTokenizer token = new StringTokenizer(time, ":");
						h = Integer.parseInt(token.nextToken());
						m = Integer.parseInt(token.nextToken());
						s = Integer.parseInt(token.nextToken());
						now.set(Calendar.HOUR_OF_DAY, h);
						now.set(Calendar.MINUTE, m);
						now.set(Calendar.SECOND, s);
						now.set(Calendar.MILLISECOND, mils);
						this.lastTimeStamp = this.newTimeStamp;
						this.newTimeStamp = now.getTimeInMillis();
						// end new log
						
						line = line.substring(0, line.indexOf('>')+1);
						
						
						//reset();
					}

					// print file di log
					Logger.log(line);
					TextParser.parseText(line);
					if(line.contains("</packet>")&& this.insidePacket){
						this.insidePacket = false;
						if(this.lastTimeStamp != 0){
							
							// tempo di sleep tra un pacchetto ed un altro
							Thread.sleep((this.newTimeStamp - this.lastTimeStamp)/5);
							// Thread.sleep(000);
						}
					}
					
					
/*					else if(line.contains(">time:")&& this.insidePacket){
						this.time = Long.parseLong(line.substring(line.indexOf(":")+1 ).trim()); 
					}
					else if(line.contains(">router:")&& this.insidePacket){
						this.lastRouter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
					}
					else if(line.contains(">sender:")&& this.insidePacket){
						this.sender = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
						Node n = Configurator.getNode(this.sender);
						if(n == null){
							System.err.println("Sender id: " + this.sender);
							System.err.println("Il pacchetto non proviene da un nodo conosciuto");
							System.err.println("Controllare il file di configurazione");
							reset();
						}else{
							this.capabilitiesSet = n.getCapabilitiesSet();
						}
					}
					else if(line.contains(">Counter:")&& this.insidePacket){
						// da definire meglio!!!
						this.counter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
						CapabilityInstance cI = Configurator.getCapabilityInstance("Counter");
						cI.setValue(counter);
						this.capInstanceList.add(cI);
					}
					else if(line.contains(">route:")&& this.insidePacket){
						this.route = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
					}
					else if (line.contains(">") && line.contains(":") && !line.contains("<")&& this.insidePacket){
						String name = line.substring(line.indexOf('>')+1, line.indexOf(':'));
						CapabilityInstance c = Configurator.getCapabilityInstance(name);
						
						if(c != null && capabilitiesSet.contains(c.getName())){
							c.setValue(Double.parseDouble(line.substring(line.indexOf(':')+1).trim()));
							this.capInstanceList.add(c);		
						}
					}
					else if(line.contains("</packet>")&& this.insidePacket){
						Packet p = new Packet(this.time, this.lastRouter, this.sender, this.counter, this.route, this.capInstanceList);
						LocalStatsManager.addNewPacket(p);
						// debug
						// System.out.println(p);
						// TestWriter.write(p);
						reset();
					}
*/				}
					
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {	
					bReader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			// sleep quando tutto il file viene letto...
			try {
				Thread.sleep(1000*60*10); // 10minuti
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 			
		}		
	}
	
	private void reset(){
		this.time = -1;
		this.lastRouter = -1;
		this.sender = -1;
		this.counter = -1;
		this.route = -1;
		this.capInstanceList = new LinkedList<CapabilityInstance>();
		this.capabilitiesSet = new LinkedList<String>();
		this.insidePacket = false;
	}

}
