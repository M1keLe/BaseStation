package it.basestation.cmdline;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.Fusiontables.Table.Delete;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;



public class FusionTablesManager {
	
	private static final String APPLICATION_NAME = "VirtualSenseBaseStation";
	private static final java.io.File DATA_STORE_DIR =
		      new java.io.File(System.getProperty("user.home"), ".store/VirtualSenseBaseStation");
	
	private static FileDataStoreFactory dataStoreFactory;
	
	private static HttpTransport httpTransport;
	
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static Fusiontables fusiontables;
	
	//private static Hashtable <Short,Node> nodeList = new Hashtable <Short,Node>();
	  
	private static TableList tableList = null;
	  
	private static Hashtable <Short, String> tablesID = new Hashtable <Short, String>();
	
	private static String globalTableID = new String();
	
	private static Credential authorize() throws Exception {
	    // load client secrets
		
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
	        JSON_FACTORY, new InputStreamReader(
	            FusionTablesManager.class.getResourceAsStream("/client_secrets.json")));
	    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
	        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
	      System.out.println(
	          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=fusiontables "
	          + "into fusiontables-cmdline-sample/src/main/resources/client_secrets.json");
	      System.exit(1);
	    }
	    // set up authorization code flow
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        httpTransport, JSON_FACTORY, clientSecrets,
	        Collections.singleton(FusiontablesScopes.FUSIONTABLES)).setDataStoreFactory(
	        dataStoreFactory).build();
	    // authorize
	    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}
	
	public static void connect() {
	    try {
	      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
	      // authorization
	      Credential credential = authorize();
	      // set up global FusionTables instance
	      fusiontables = new Fusiontables.Builder(
	    	  //httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	          httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	
	      return;
	    } catch (IOException e) {
	      System.err.println(e.getMessage());
	    } catch (Throwable t) {
	      t.printStackTrace();
	    }
	    System.exit(1);
	}
	
	private static TableList listTables() throws IOException {


	    // Fetch the table list
	    Fusiontables.Table.List listTables = fusiontables.table().list();
	    tableList = listTables.execute();

	    if (tableList.getItems() == null || tableList.getItems().isEmpty()) {
	      System.out.println("Nessuna Tabella Trovata");
	      tableList = null;
	    }
	    return tableList;
	  }
	
	private static String createTable(Node n)throws IOException{
	    Table table = new Table();
	    table.setName("Nodo_"+n.getMyID());
	    table.setIsExportable(false);
	    table.setDescription("Recorded data from Node #" +n.getMyID());
	
	    LinkedList <Column> columns = new LinkedList<Column>();
	    LinkedList <String> capability = n.getCapabilitiesSet();
	    for (String c : capability) {
	    	columns.add(new Column().setName(c).setType("NUMBER"));
		}
	   
	    // aggiungo il campo data
	    columns.add(new Column().setName("Date").setType("DATETIME"));
	    
	    
	    table.setColumns(columns);
	    Fusiontables.Table.Insert t = fusiontables.table().insert(table);
	    Table r = t.execute();
	    // salvo l'id della tabella nella mia lista localmente
	    tablesID.put(n.getMyID(), r.getTableId());
	    return r.getTableId();   
	}
	
	private static String createGlobalTable()throws IOException{
	    Table table = new Table();
	    table.setName("Global_Table");
	    table.setIsExportable(false);
	    LinkedList <Column> columns = new LinkedList<Column>();
	    LinkedList <String> capability = Configurator.getGlobalCapabilitiesSet();
	    for (String c : capability) {
	    	columns.add(new Column().setName(c).setType("NUMBER"));
		}
	   
	    // aggiungo il campo data
	    columns.add(new Column().setName("Date").setType("DATETIME"));
	    
	    
	    table.setColumns(columns);
	    Fusiontables.Table.Insert t = fusiontables.table().insert(table);
	    Table r = t.execute();
	    // salvo l'id della tabella globale
	    globalTableID =  r.getTableId();
	    return r.getTableId();   
	}
	
	
	public static void deleteTable(String tableId) throws IOException {
	    // Deletes a table
	    Delete delete = fusiontables.table().delete(tableId);
	    delete.execute();
	}
	
	
	public static void setupTables(Hashtable<Short, Node> nList){
		//nodeList = nList;
		try {
			tableList = listTables();
		} catch (IOException exception) {
			// TODO Auto-generated catch block
	        exception.printStackTrace();
		}
		// creo le tabelle relative ai nodi
		Enumeration<Short> e = nList.keys();
		while(e.hasMoreElements()){
			short id = e.nextElement();
			if(!tableExists(id)){
				try {
					String s = createTable(nList.get(id));          
	    			System.out.println("Nodo_"+id+": Creata tabella con id = " +s);
				} catch (IOException exception) {
					// TODO Auto-generated catch block
	    			exception.printStackTrace();
	    		}
			} else {
				System.out.println("Nodo_"+ id +": La tabella esiste già!");
			}
		}
		// creo la tabella globale se non esiste
		if(!globalTableExists()){
			try {
				String s = createGlobalTable();          
    			System.out.println("Creata \"Global Table\" con id = " +s);
			} catch (IOException exception) {
				// TODO Auto-generated catch block
    			exception.printStackTrace();
    		}
		} else {
			System.out.println("La \"Global Table\" esiste già!");
		}
	}
	
	private static boolean tableExists (short nodeID){
		boolean toRet = false;
	    if(tableList != null){
	    	for (Table table : tableList.getItems()){
	    		if(table.getName().equals("Nodo_"+(int) nodeID)){
	    			//System.out.println("IDtabellaTROVATA: "+ table.getTableId());
	    			toRet = true;
	    			break;
	    		}
	    	}
	    }
	    return toRet;
	}
	
	private static boolean globalTableExists (){
		boolean toRet = false;
	    if(tableList != null){
	    	for (Table table : tableList.getItems()){
	    		if(table.getName().equals("Global_Table")){
	    			//System.out.println("IDtabellaTROVATA: "+ table.getTableId());
	    			globalTableID = table.getTableId();
	    			toRet = true;
	    			break;
	    		}
	    	}
	    }
	    return toRet;
	}
	
	private static String getTableID(short nodeID){
	    String tableID = null;
	    for (Table table : tableList.getItems()) {
	    	if(table.getName().endsWith("_"+nodeID)){
	    		tableID = table.getTableId();
	    		tablesID.put(nodeID, tableID);          
	    		break;
	    	}         
	    }
	    return tableID;
	}
	public static void insertData(LastPeriodNodeRecord nodeRecord) throws IOException {
		short nodeID = nodeRecord.getNodeID();
		String tableID = tablesID.get(nodeID);
		if(tableID == null)
	    {
	      tableID = getTableID(nodeID);
	      tablesID.put(nodeID, tableID);
	    }
		
		LinkedList<CapabilityInstance> capListToStore = nodeRecord.getDataListToStore();
		Sql sql = fusiontables.query().sql(getQueryInsert(tableID, capListToStore));
		System.out.println("Debug: NODE TABLE N° "+ nodeID +" - Sto inserendo i seguenti dati:\nQuery generata: " + getQueryInsert(tableID, capListToStore));
		try {
			sql.execute();
			
		    } catch (IllegalArgumentException e) {
		    	System.out.print("ERROR TABLE NODE NR ="+nodeID + e.toString());
		      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
		      // been thrown.
		      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
		      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
		    }
	}

	public static void insertData(LinkedList<CapabilityInstance> globalValuesToStore) throws IOException {
		Sql sql = fusiontables.query().sql(getQueryInsert(globalTableID, globalValuesToStore));
		System.out.println("Debug: GLOBAL TABLE Sto inserendo i seguenti dati: " + getQueryInsert(globalTableID, globalValuesToStore));
		try {
			sql.execute();
			
			
		    } catch (IllegalArgumentException e) {
		      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
		      // been thrown.
		      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
		      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
		    }
	}
	
	
	private static String getQueryInsert(String tableID, LinkedList<CapabilityInstance> capListToStore){
	    java.text.DecimalFormat format = new java.text.DecimalFormat("0.00");
	    
	    String queryHead = new String();
	    
	    queryHead = queryHead.concat("INSERT INTO "+ tableID +" (");
	    // queryHead = queryHead.concat("INSERT INTO "+ this.myTable.getTableId() + " (");
	    String queryTail = new String();
	    queryTail = queryTail.concat(" VALUES (");
	    
	    for (CapabilityInstance c : capListToStore) {
			queryHead = queryHead.concat(c.getName()+", ");
			queryTail = queryTail.concat(" '" +format.format(c.getValue())+"', ");
		}
	    
	    queryHead = queryHead.concat("Date) ");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    //queryTail.concat(" '" +format.format(new Date(System.currentTimeMillis()))+"') ");
	    queryTail = queryTail.concat(" '" + dateFormat.format(new Date())+"') ");
	    //System.out.println(queryHead.concat(queryTail));    
	    return queryHead.concat(queryTail).replaceAll(" {2,}", " ");
	} 
	  
	
	
	
}
