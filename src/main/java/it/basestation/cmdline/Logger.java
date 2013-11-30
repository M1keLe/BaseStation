package it.basestation.cmdline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

	public static void log(String text){
		try {
			Date now = new Date();
			// nome file
			String name = "log/"+new SimpleDateFormat("dd-MMM-yyyy", Locale.ITALY).format(now) + ".log";
			// String name = df.format(startTime).replace('/','-').replace(' ','_').trim()+".log";
			FileOutputStream out = new FileOutputStream(name, true);
			PrintStream p = new PrintStream(out);
			// aggiungo timestamp al pacchetto
			if(text.indexOf("<packet>") != -1){
				// text = text.substring(0, text.indexOf('>')+ 1).trim(); decommentare se si usa il file di log
				//text += " "+ System.currentTimeMillis(); // to add  timestamp to the received packet
				text += " " + new SimpleDateFormat("HH:mm:ss.S", Locale.ITALY).format(now);
			}
			p.println(text);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
