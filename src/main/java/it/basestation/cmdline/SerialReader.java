package it.basestation.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SerialReader extends Thread{
	
	private InputStream in;
	private boolean running = true;

	public SerialReader(InputStream in){
		super("SerialReader");
		this.in = in;		
	}
	
	public void run(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.in));
		String readed = "";
		System.out.println("Debug: SerialReader started");
		try {
			
			while(running){
				readed = reader.readLine();
				TextParser.parseText(readed);
				Logger.log(readed);
			}
			System.out.println("Debug: serialReader shut down");	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("For input string: " +readed);
		}
		
	}
	
	public void stopReading(){
		this.running = false;
		this.interrupt();
	}

}
