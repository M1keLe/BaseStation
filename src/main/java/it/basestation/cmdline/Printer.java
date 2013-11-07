package it.basestation.cmdline;

import java.util.concurrent.locks.ReentrantLock;

public class Printer {

	private static final ReentrantLock lock = new ReentrantLock();
	public Printer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void println(String toPrint){
		lock.lock();
		System.out.println(toPrint);
		lock.unlock();
	}
	
	public static void print(String toPrint){
		lock.lock();
		System.out.print(toPrint);
		lock.unlock();
	}

}
