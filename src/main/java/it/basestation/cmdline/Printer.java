package it.basestation.cmdline;

import java.util.concurrent.locks.ReentrantLock;

public class Printer {

	private static final ReentrantLock lock = new ReentrantLock();
	public Printer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void println(Object toPrint){
		lock.lock();
		System.out.println(toPrint);
		lock.unlock();
	}
		
	public static void print(Object toPrint){
		lock.lock();
		System.out.println(toPrint);
		lock.unlock();
	}
	

}
