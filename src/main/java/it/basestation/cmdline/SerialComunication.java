package it.basestation.cmdline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.*;

public class SerialComunication {
	
	private static InputStream in;
	private static OutputStream out;
	private static CommPort commPort;

	public static void createComunication() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		String port = Configurator.getUSBPort();
		int baudRate = Configurator.getSpeedUsbPort();
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
		if(portIdentifier.isCurrentlyOwned()){
			System.out.println("Error: Port is currently in use");
		}else{
			commPort = portIdentifier.open("BaseStation", 2000);
			if(commPort instanceof SerialPort){
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();
			}else{
				System.out.println("Error: port is not a serial port!");
			}
		}
	}
	
	public static InputStream getInputStream(){
		return in;
	}
	
	public static OutputStream getOutputStream(){
		return out;
	}
	
	public static void resetPort(){
		if(commPort != null){
			commPort.close();
			in = null;
			out = null;
		}
	}

}
