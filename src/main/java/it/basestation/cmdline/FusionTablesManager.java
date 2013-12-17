package it.basestation.cmdline;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import com.google.api.services.fusiontables.Fusiontables.Table.Insert;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Sqlresponse;
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
	  
	private static TableList tableList = null;
	  
	private static Hashtable <Short, String> tablesID = new Hashtable <Short, String>();
	
	private static Hashtable <String, String> globalTablesID = new Hashtable <String, String>();
	
	private static Credential authorize() throws Exception {
	    // load client secrets
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
	        JSON_FACTORY, new InputStreamReader(
	            FusionTablesManager.class.getResourceAsStream("/client_secrets.json")));
	    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
	        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
	      System.out.println(
	          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=fusiontables "
	          + "into VirtualSenseBaseStation/src/main/resources/client_secrets.json");
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
	    // Fetch the table list
	    Fusiontables.Table.List listTables = fusiontables.table().list();
	    tableList = listTables.execute();

	    /*// debug	    
	    for (Table t : tableList.getItems()) {
	    	if(t.getName().startsWith("Table_")){
	    		//System.out.println(t);
	    		System.out.println(t.toPrettyString());
	    		System.out.println("FACTORY: "+ t.getFactory().toPrettyString(t));
	    		for (Column c : t.getColumns()) {
	    			System.out.println("KIND: "+ c.getKind());
				}
	    		
	    	}
		}	    
	    */// endDebug
	    

	    if (tableList.getItems() == null || tableList.getItems().isEmpty()) {
	      System.out.println("Nessuna Tabella Trovata");
	      tableList = null;
	    }
	    return tableList;
	  }
	
	// debug	
	public static String createT() throws IOException {
	    // Create a new table
	    Table table = new Table();
	    table.setName("TestTable2");
	    table.setIsExportable(true);
	    table.setDescription("Sample Table");

	    // Set columns for new table
	    table.setColumns(Arrays.asList(new Column().setName("Counter_last").setType("NUMBER"),
	        new Column().setName("PeopleIn_last").setType("NUMBER"),
	        new Column().setName("PeopleOut_last").setType("NUMBER"),
	        new Column().setName("Date").setType("DATETIME")));

	    // Adds a new column to the table.
	    Insert t = fusiontables.table().insert(table);
	    Table r = t.execute();

	    return r.getTableId();
	  }
	// endDebug
	
	
	// metodo che crea la tabella di un nodo
	private static String createTable(Node n)throws IOException{
	    Table table = new Table();
	    table.setName("Nodo_"+n.getMyID());
	    table.setIsExportable(true);
	    table.setDescription("Table of Node number " +n.getMyID());
	    
	    LinkedList <Column> columns = new LinkedList<Column>();
	    LinkedList <String> capability = n.getCapabilitiesSet();
	    for (String name : capability) {
	    	for (Capability c : Configurator.getCapabilitiesList(name, "local", true)) {
	    		columns.add(new Column().setName(c.getColumnName()).setType("NUMBER"));
			}
		}	   
	    // aggiungo il campo data
	    //columns.add(new Column().setName("Date").setType("DATETIME"));
	    columns.add(new Column().setName("Date").set("type", "DATETIME").setKind("fusiontables#columnn"));
	    
	    table.setColumns(columns);
	    Insert t = fusiontables.table().insert(table);
	    Table r = t.execute();
	    // salvo l'id della tabella nella mia lista localmente
	    tablesID.put(n.getMyID(), r.getTableId());
	    return r.getTableId();   
	}
	
	// metodo che crea una tabella globale
	private static String createGlobalTable(String name)throws IOException{
	    Table table = new Table();
	    table.setName(name);
	    table.setIsExportable(true);
	    LinkedList <Column> columns = new LinkedList<Column>();
	    // aggiungo le colonne
	    for (Capability c : Configurator.getCapabilitiesList(name, "global", true)) {
    		columns.add(new Column().setName(c.getColumnName()).setType("NUMBER"));
		}
	    
	    // aggiungo il campo data
	    columns.add(new Column().setName("Date").setType("DATETIME"));
	    table.setColumns(columns);
	    Fusiontables.Table.Insert t = fusiontables.table().insert(table);
	    Table r = t.execute();
	    // salvo l'id della tabella globale
	    globalTablesID.put(name, r.getTableId());
	    return r.getTableId();   
	}
		
	// cancella una tabella
	private static void deleteTable(String tableId) throws IOException {
	    // Deletes a table
	    Delete delete = fusiontables.table().delete(tableId);
	    delete.execute();
	}
	
	// effettua il controllo sulle fusion tables crea le tabelle se non sono già presenti
	public static void setupTables(){
		
		Hashtable<Short, Node> nList = Configurator.getNodeList();
		
		// effetto il dl della lista di tabelle presenti
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
			// se non esiste creo la tabella
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
		// creo le tabelle globali se non esistono
		
		LinkedList <String> globalTablesSet = Configurator.getGlobalTablesSet();
		for (String tableName : globalTablesSet) {
			if(!globalTableExists(tableName)){
				try {
					String globalTableID = createGlobalTable(tableName);          
	    			System.out.println("Creata \"Global Table\" "+tableName+" con id = " +globalTableID);
				} catch (IOException exception) {
					// TODO Auto-generated catch block
	    			exception.printStackTrace();
	    		}
			} else {
				System.out.println("La \"Global Table\" "+tableName+" esiste già!");
			}
		}
		
/*		// metodo con lista
		LinkedList <Capability> globalCapabilitiesList = Configurator.getGlobalCapabilitiesList(false);
		for (Capability c : globalCapabilitiesList) {
			if(!globalTableExists(c.getName())){
				try {
					String globalTableID = createGlobalTable(c.getName());          
	    			System.out.println("Creata \"Global Table\" "+c.getName()+" con id = " +globalTableID);
				} catch (IOException exception) {
					// TODO Auto-generated catch block
	    			exception.printStackTrace();
	    		}
			} else {
				System.out.println("La \"Global Table\" "+c.getName()+" esiste già!");
			}
		}
*/		// end metodo con lista
	}
	
	private static boolean tableExists(short nodeID){
		boolean toRet = false;
	    if(tableList != null){
	    	for (Table table : tableList.getItems()){
	    		if(table.getName().equals("Nodo_"+ nodeID)){
	    			//System.out.println("IDtabellaTROVATA: "+ table.getTableId());
	    			tablesID.put(nodeID, table.getTableId());
	    			toRet = true;
	    			break;
	    		}
	    	}
	    }
	    return toRet;
	}
	
	private static boolean globalTableExists(String name){
		boolean toRet = false;
	    if(tableList != null){
	    	for (Table table : tableList.getItems()){
	    		if(table.getName().equals(name)){
	    			//System.out.println("IDtabellaTROVATA: "+ table.getTableId());
	    			globalTablesID.put(name, table.getTableId());
	    			toRet = true;
	    			break;
	    		}
	    	}
	    }
	    return toRet;
	}
	
	// get id tabella nodo
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
	
	// get id tabella globale
	private static String getTableID(String name){
	    String tableID = null;
	    for (Table table : tableList.getItems()) {
	    	if(table.getName().equals(name)){
	    		tableID = table.getTableId();
	    		globalTablesID.put(name, tableID);          
	    		break;
	    	}         
	    }
	    return tableID;
	}
	
	
	// insert su tabella di un nodo
	public static void insertData(LastPeriodNodeRecord nodeRecord, Date d) throws IOException {
		short nodeID = nodeRecord.getNodeID();
		String tableID = tablesID.get(nodeID);
		if(tableID == null){
			tableID = getTableID(nodeID);
			tablesID.put(nodeID, tableID);
	    }
		
		LinkedList<CapabilityInstance> capListToStore = nodeRecord.getDataListToStore();
		capListToStore.addAll(nodeRecord.getMMListToStore());
		Sql sql = fusiontables.query().sql(buildInsertQuery(tableID, capListToStore, d));
		System.out.println("Debug: NODE TABLE N° "+ nodeID +" - Sto inserendo i seguenti dati:\nQuery generata: " + buildInsertQuery(tableID, capListToStore, d));
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

	// insert su tutte le tabelle globali
	public static void insertData(LastPeriodGlobalRecord globalRecord, Date d) throws IOException {
		Hashtable<String, LinkedList<CapabilityInstance>> globalValuesToStore = globalRecord.getHashTableToStore();
		Enumeration<String> e = globalValuesToStore.keys();
		while (e.hasMoreElements()) {
			String gTableName = (String) e.nextElement();
			String gTableID = globalTablesID.get(gTableName);
			if(gTableID == null){	
				gTableID = getTableID(gTableName);
				globalTablesID.put(gTableName, gTableID);
		    }
			Sql sql = fusiontables.query().sql(buildInsertQuery(gTableID, globalValuesToStore.get(gTableName), d)); 
			System.out.println("Global Table: "+ gTableName + "\nQuery: " + sql.toString());
			
			try {
				sql.execute();
			}catch (IllegalArgumentException ex) {
				// For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
				// been thrown.
				// Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
				// http://code.google.com/p/google-api-java-client/issues/detail?id=545
			}
		}
	}

/*	public static void insertDataOld(LastPeriodGlobalRecord globalRecord, Date d) throws IOException {
		LinkedList<CapabilityInstance> globalValuesToStore = globalRecord.getDataListToStore(); 
		for (CapabilityInstance cI : globalValuesToStore) {
			String gTableID = globalTablesID.get(cI.getName());
			if(gTableID == null){	
				gTableID = getTableID(cI.getName());
				globalTablesID.put(cI.getName(), gTableID);
		    }
			
			Sql sql;
			// controllo su Deviazione Standard
			if(cI.globalOperator().equals("avg")){
				sql = fusiontables.query().sql(buildInsertQuery(gTableID, cI, globalRecord.getStandardDeviation(cI.getName()), d));
				//Debug
				System.out.println("Debug: GLOBAL TABLE Sto inserendo i seguenti dati: " + buildInsertQuery(gTableID, cI, globalRecord.getStandardDeviation(cI.getName()), d));
			}else{
				sql = fusiontables.query().sql(buildInsertQuery(gTableID, cI, d));
				//Debug
				System.out.println("Debug: GLOBAL TABLE Sto inserendo i seguenti dati: " + buildInsertQuery(gTableID, cI, d));
			}
			
			try {
				sql.execute();
			}catch (IllegalArgumentException e) {
				// For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
				// been thrown.
				// Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
				// http://code.google.com/p/google-api-java-client/issues/detail?id=545
			}
		}
	}
*/	
	// genera la query relativa ad un nodo
	private static String buildInsertQuery(String tableID, LinkedList<CapabilityInstance> capListToStore, Date d){
	    java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("0.00");
	    
	    String queryHead = new String();
	    
	    queryHead = queryHead.concat("INSERT INTO "+ tableID +" (");
	    // queryHead = queryHead.concat("INSERT INTO "+ this.myTable.getTableId() + " (");
	    String queryTail = new String();
	    queryTail = queryTail.concat(" VALUES (");
	    
	    for (CapabilityInstance c : capListToStore) {
			//queryHead = queryHead.concat(c.getName()+", ");
	    	queryHead = queryHead.concat(c.getColumnName()+", ");
			// controllo range se non è la devstd
	    	if(!c.globalOperator().contains("stddev")){
				if(c.getValue() < c.getMinValue() || c.getValue() > c.getMaxValue()){
					c.setValue(0);
				}
	    	}
			queryTail = queryTail.concat(" '" +decimalFormat.format(c.getValue())+"', ");
		}
	    
	    queryHead = queryHead.concat("Date) ");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    //queryTail.concat(" '" +format.format(new Date(System.currentTimeMillis()))+"') ");
	    //queryTail = queryTail.concat(" '" + new DateTime(d)+"') ");
	    queryTail = queryTail.concat(" '" + dateFormat.format(d)+"') ");
	    //System.out.println(queryHead.concat(queryTail));    
	    return queryHead.concat(queryTail).replaceAll(" {2,}", " ");
	} 
	
/*	// genera la query relativa ad una tabella globale senza Deviazione Standard
	private static String buildInsertQuery(String tableID, CapabilityInstance gCI, Date d){
	    java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("0.00");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    // controllo range
		if(gCI.getValue() < gCI.getMinValue() || gCI.getValue() > gCI.getMaxValue()){
			gCI.setValue(0);
		}
		//return "INSERT INTO "+ tableID +" (" + gCI.getName()+", Date) VALUES ('"+ decimalFormat.format(gCI.getValue())+"', '"+dateFormat.format(d)+"')";
		return "INSERT INTO "+ tableID +" (" + gCI.getColumnName()+", Date) VALUES ('"+ decimalFormat.format(gCI.getValue())+"', '"+dateFormat.format(d)+"')";
		//return "INSERT INTO "+ tableID +" (" + gCI.getName()+", Date) VALUES ('"+ decimalFormat.format(gCI.getValue())+"', '"+new DateTime(d)+"')";
	}
	
	// genera la query relativa ad una tabella globale con Deviazione Standard
	private static String buildInsertQuery(String tableID, CapabilityInstance gCI, double standardDeviation , Date d){
	    java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("0.00");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    // controllo range
	 	if(gCI.getValue() < gCI.getMinValue() || gCI.getValue() > gCI.getMaxValue()){
			gCI.setValue(0);
 		}
	 	return "INSERT INTO "+ tableID +" ("+ gCI.getColumnName()+", StandardDeviation, Date) VALUES ('"+decimalFormat.format(gCI.getValue())+"', '"+decimalFormat.format(standardDeviation)+"', '"+ dateFormat.format(d)+"')";
	 	//return "INSERT INTO "+ tableID +" ("+ gCI.getName()+", StandardDeviation, Date) VALUES ('"+decimalFormat.format(gCI.getValue())+"', '"+decimalFormat.format(standardDeviation)+"', '"+ dateFormat.format(d)+"')";
	    //return "INSERT INTO "+ tableID +" ("+ gCI.getName()+", StandardDeviation, Date) VALUES ('"+decimalFormat.format(gCI.getValue())+"', '"+decimalFormat.format(standardDeviation)+"', '"+ new DateTime(d)+"')";
	}
*/
	// init global record
	public static LastPeriodGlobalRecord initGlobalRecord() throws IOException {
		LastPeriodGlobalRecord toRet = new LastPeriodGlobalRecord();
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		//now.set(Calendar.SECOND, 0);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Sql sql = fusiontables.query().sql("SELECT PeopleIn_sum, Date FROM "+ globalTablesID.get("PeopleIn") + " WHERE Date > '" + dateFormat.format(now.getTime())+"' ORDER BY Date DESC" );
		Sqlresponse r = sql.execute();
		
		// people in
		if(r != null){
			// Debug 
			System.out.println("Query people in not null");
			List<List<Object>> list = r.getRows();
			if(list != null){
				System.out.println("List people in not null");
				Iterator<Object> i = list.get(0).iterator();
				double peopleIn = Double.parseDouble(i.next().toString());
				CapabilityInstance pIn = new CapabilityInstance("PeopleIn","PeopleIn_sum","last","","sum",0.00,Double.POSITIVE_INFINITY,0);
				System.out.println("Last people in value found: " + peopleIn);
				pIn.setValue(peopleIn);
				toRet.addCapabilityInstance(pIn);
			}
		}
		
		// people out
		sql = fusiontables.query().sql("SELECT PeopleOut_sum, Date FROM "+ globalTablesID.get("PeopleOut") + " WHERE Date > '" + dateFormat.format(now.getTime())+"' ORDER BY Date DESC" );
		r = sql.execute();
		if(r != null){
			System.out.println("Query people out not null");
			List<List<Object>> list = r.getRows();
			if(list != null){
				System.out.println("List people out not null");
				Iterator<Object> i = list.get(0).iterator();
				double peopleOut = Double.parseDouble(i.next().toString());
				CapabilityInstance pOut = new CapabilityInstance("PeopleOut","PeopleOut_sum","last","","sum",0.00,Double.POSITIVE_INFINITY,0);
				System.out.println("Last people out value found: " + peopleOut);
				pOut.setValue(peopleOut);
				toRet.addCapabilityInstance(pOut);
			}
		}		
		return toRet;
	} 	
}