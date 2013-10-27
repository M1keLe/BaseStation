package it.basestation.cmdline;

import java.util.Hashtable;
import java.util.LinkedList;

public interface IStats {
	
	void elabNodeLists(Hashtable <Short, LinkedList<Packet>> packetsOfNodes);

}
