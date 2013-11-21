package it.basestation.cmdline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

public class TestWriter {

	private static ReentrantLock lock = new ReentrantLock();

	public static void write(Object toWrite){
		lock.lock();
		try {
			FileOutputStream out = new FileOutputStream("test.txt", true);
			PrintStream pS = new PrintStream(out);
			pS.println(toWrite);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
