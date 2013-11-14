package it.basestation.cmdline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class FileReader extends Thread {
	private final String FILE_NAME = "6-nov-2013.log"; 
	private BufferedReader bReader = null;
	
	// attributi pacchetto
	private long time;
	private short lastRouter;
	private short sender;
	private short counter;
	private short route;
	private LinkedList<DataContainer> dataContainerList = new LinkedList<DataContainer>();
	private LinkedList<String> capabilitiesSet = new LinkedList<String>();
	
	// timestamp arrivo pacchetto simulo tempo tra arrivo di un pacchetto ed un altro
	private long lastTimeStamp = 0;
	private long newTimeStamp = 0;

	public FileReader() {	
		super ("File Reader");
	}
	
	
	@Override
	public void run(){
		while(true){		
			
			try {
				bReader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_NAME)));
				String line = "";
				while((line = bReader.readLine()) != null){
					if(line.contains("<packet>")){
						this.lastTimeStamp = newTimeStamp;
						this.newTimeStamp = Long.parseLong(line.substring(line.indexOf('>') +1).trim());
						if(this.lastTimeStamp != 0){
							Thread.sleep((this.newTimeStamp - this.lastTimeStamp)/100);
							// Thread.sleep(1000*5);
							// Thread.sleep(0);
						}
						//reset();
					}
					else if(line.contains(">time:")){
						this.time = Long.parseLong(line.substring(line.indexOf(":")+1 ).trim()); 
					}
					else if(line.contains(">router:")){
						this.lastRouter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
					}
					else if(line.contains(">sender:")){
						this.sender = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
						this.capabilitiesSet = Configurator.getNode(this.sender).getCapabilitiesSet();
					}
					else if(line.contains(">counter:")){
						// da definire meglio!!!
						this.counter = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
						DataContainer dc = Configurator.getDataContainerBySensorID("counter");
						dc.setValue(counter);
						this.dataContainerList.add(dc);
					}
					else if(line.contains(">route:")){
						this.route = Short.parseShort(line.substring(line.indexOf(":")+1 ).trim());
					}
					else if (line.contains(">") && line.contains(":") && !line.contains("<")){
						String sensorID = line.substring(line.indexOf('>')+1, line.indexOf(':'));
						DataContainer c = Configurator.getDataContainerBySensorID(sensorID);
						
						//System.out.println("SENSOR ID PARSATO: " +sensorID);
						
						if(c != null && capabilitiesSet.contains(c.getName())){
							c.setValue(Double.parseDouble(line.substring(line.indexOf(':')+1).trim()));
							this.dataContainerList.add(c);		
						}
					}
					if(line.contains("</packet>")){
						Packet p = new Packet(this.time, this.lastRouter, this.sender, this.counter, this.route, this.dataContainerList);
						LocalStatsManager.addNewPacket(p);
						//System.out.println(p);
						reset();
					}
				}
					
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
					if (bReader != null)
						bReader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(1000*60*15); // 15minuti
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
		this.dataContainerList = new LinkedList<DataContainer>();
		this.capabilitiesSet = new LinkedList<String>();
	}

}
