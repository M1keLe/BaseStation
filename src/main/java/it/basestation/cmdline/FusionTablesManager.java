package it.basestation.cmdline;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
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
	
	private static Hashtable <Short,Node> nodeList = new Hashtable <Short,Node>();
	  
	private static TableList tableList = null;
	  
	private static Hashtable <Short, String> tablesID = new Hashtable <Short, String>();
	
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

	    TableList tableList = null;

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
	    LinkedList <Column> columns = new LinkedList<Column>();
	    HashSet <String> capability = n.getCapabilities();
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
	
	public static void deleteTable(String tableId) throws IOException {
	    // Deletes a table
	    Delete delete = fusiontables.table().delete(tableId);
	    delete.execute();
	}
	
	
	public static void setupTables(Hashtable<Short, Node> nList){
		nodeList = nList;
		try {
			tableList = listTables();
		} catch (IOException exception) {
			// TODO Auto-generated catch block
	        exception.printStackTrace();
		}
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
	}
	
	private static boolean tableExists (short nodeID){
		boolean toRet = false;
	    if(tableList == null){
	    	toRet = false;
	    } else {
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
	
/*	private static String getQueryInsert(Node n, FusionTableNodeRecord nr, String tableID){
	    java.text.DecimalFormat format = new java.text.DecimalFormat("0.00");
	    
	    String queryHead = new String();
	    
	    queryHead = queryHead.concat("INSERT INTO "+ tableID +" (");
	    // queryHead = queryHead.concat("INSERT INTO "+ this.myTable.getTableId() + " (");
	    String queryTail = new String();
	    queryTail = queryTail.concat(" VALUES (");
	    
	    if(n.hasCapability("Noise")){
	      queryHead = queryHead.concat("Noise,");
	      queryTail = queryTail.concat(" '" +format.format(nr.noise)+"', ");
	    }
	    if(n.hasCapability("Co2")){
	      queryHead = queryHead.concat("Co2,");
	      queryTail = queryTail.concat(" '" +format.format(nr.co2)+"', ");
	    }
	    if(n.hasCapability("Temp")){
	      queryHead = queryHead.concat("Temperature,");
	      queryTail = queryTail.concat(" '" +format.format(nr.temperature)+"', ");
	    }      
	    if(n.hasCapability("Pressure")){
	      queryHead = queryHead.concat("Pressure,");
	      queryTail = queryTail.concat(" '" +format.format(nr.pressure)+"', ");
	    }
	    if(n.hasCapability("Light"))
	    {
	      queryHead = queryHead.concat("Light,");
	      queryTail = queryTail.concat(" '" +format.format(nr.luminosity)+"', ");
	    }
	    if(n.hasCapability("People")){
	      queryHead = queryHead.concat("PeopleOut,PeopleIn,");
	      queryTail = queryTail.concat(" '" +format.format(nr.out)+"', '"+format.format(nr.in)+"', ");
	    }
	    if(n.hasCapability("Counter")){
	      queryHead = queryHead.concat("Counter,");
	      queryTail = queryTail.concat(" '" +format.format(nr.counter)+"', ");
	    }
	    
	    queryHead = queryHead.concat("Date) ");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    //queryTail.concat(" '" +format.format(new Date(System.currentTimeMillis()))+"') ");
	    queryTail = queryTail.concat(" '" + dateFormat.format(new Date())+"') ");
	    //System.out.println(queryHead.concat(queryTail));    
	    return queryHead.concat(queryTail);
	} */
	  
	
	
	
}
