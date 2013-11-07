package it.basestation.cmdline;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

public class PacketGenerator extends Thread {
	
	Hashtable<Short, Packet> lastPackets = new Hashtable<Short, Packet>();
	LinkedList<Capability> p2List = new LinkedList<Capability>();
	LinkedList<Capability> p3List = new LinkedList<Capability>();
	LinkedList<Capability> p4List = new LinkedList<Capability>();
	LinkedList<Capability> p5List = new LinkedList<Capability>();
	LinkedList<Capability> p6List = new LinkedList<Capability>();
	LinkedList<Capability> p7List = new LinkedList<Capability>();
	LinkedList<Capability> p8List = new LinkedList<Capability>();
	LinkedList<Capability> p9List = new LinkedList<Capability>();
	LinkedList<Capability> p10List = new LinkedList<Capability>();
	LinkedList<Capability> p11List = new LinkedList<Capability>();
	LinkedList<Capability> p12List = new LinkedList<Capability>();
	//LinkedList<Capability> randomList = new LinkedList<Capability>();
	int counter = 0;
	
	Random rand = new Random();
	public PacketGenerator() {
		//randomList.add(new Capability(""));
		HashSet<String> cSet = Configurator.getNode((short) 2).getCapabilitiesSet();
		for (String string : cSet) {
			p2List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 3).getCapabilitiesSet();
		for (String string : cSet) {
			p3List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 4).getCapabilitiesSet();
		for (String string : cSet) {
			p4List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 5).getCapabilitiesSet();
		for (String string : cSet) {
			p5List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 6).getCapabilitiesSet();
		for (String string : cSet) {
			p6List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 7).getCapabilitiesSet();
		for (String string : cSet) {
			p7List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 8).getCapabilitiesSet();
		for (String string : cSet) {
			p8List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 9).getCapabilitiesSet();
		for (String string : cSet) {
			p9List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 10).getCapabilitiesSet();
		for (String string : cSet) {
			p10List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 11).getCapabilitiesSet();
		for (String string : cSet) {
			p11List.add(Configurator.getCapability(string));
		}
		
		cSet = Configurator.getNode((short) 12).getCapabilitiesSet();
		for (String string : cSet) {
			p12List.add(Configurator.getCapability(string));
		}
		
		//changesValues();
		
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(rand.nextInt(1000*3));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			changesValues();
			Enumeration<Short> e = this.lastPackets.keys();
			while (e.hasMoreElements()) {
				Short s = (Short) e.nextElement();
				Packet p = this.lastPackets.get(s);
				try {
					Thread.sleep(rand.nextInt(1000*2));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				LocalStatsManager.addNewPacket(p);
			}
		}
	}
	
	
	private void changesValues() {
		this.counter++;
		for (Capability c : p2List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p3List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p4List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p5List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p6List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p7List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p8List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p9List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p10List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p11List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		for (Capability c : p12List) {
			Random r = new Random();
			c.setValue(counter);
		}
		
		
		
		
		this.lastPackets.put((short)2, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 2, (short)( 0 +rand.nextInt(100)), (short) 0, p2List));
		
		this.lastPackets.put((short)3, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 3, (short)( 0 +rand.nextInt(100)), (short) 0, p3List));
		
		this.lastPackets.put((short)4, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 4, (short)( 0 +rand.nextInt(100)), (short) 0, p4List));
		
		this.lastPackets.put((short)5, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 5, (short)( 0 +rand.nextInt(100)), (short) 0, p5List));
		
		this.lastPackets.put((short)6, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 6, (short)( 0 +rand.nextInt(100)), (short) 0, p6List));
		
		this.lastPackets.put((short)7, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 7, (short)( 0 +rand.nextInt(100)), (short) 0, p7List));
		
		this.lastPackets.put((short)8, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 8, (short)( 0 +rand.nextInt(100)), (short) 0, p8List));
		
		this.lastPackets.put((short)9, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 9, (short)( 0 +rand.nextInt(100)), (short) 0, p9List));
		
		this.lastPackets.put((short)10, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 10, (short)( 0 +rand.nextInt(100)), (short) 0, p10List));
		
		this.lastPackets.put((short)11, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 11, (short)( 0 +rand.nextInt(100)), (short) 0, p11List));
		
		this.lastPackets.put((short)12, new Packet(rand.nextLong(),
				(short) (2 + rand.nextInt(12)),
				(short) 12, (short)( 0 +rand.nextInt(100)), (short) 0, p12List));
		
	//	this.lastPackets.put((short) rand.nextInt(15), new Packet(rand.nextLong(),
	//			(short) (2 + rand.nextInt(12)),
	//			(short) (15 + rand.nextInt(20)), (short)( 0 +rand.nextInt(100)), (short) 0, randomList));
		
	}

}
