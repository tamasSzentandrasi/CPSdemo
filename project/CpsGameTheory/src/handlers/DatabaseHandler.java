package handlers;

import java.util.concurrent.TimeUnit;

//import org.influxdb.BatchOptions; TODO
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class DatabaseHandler {
	private static String InfluxURL = "http://localhost:8086"; //"http://grafana.wlap.eu:8086";
	private static String InfluxUser = "cps";
	private static String InfluxPass = "LaborImage";
	private static String InfluxDBname = "cps";
	protected InfluxDB databaseConnection;
	
	public DatabaseHandler() {
		databaseConnection = InfluxDBFactory.connect(InfluxURL, InfluxUser, InfluxPass);
		databaseConnection.setDatabase(InfluxDBname);
		//databaseConnection.enableBatch(BatchOptions.DEFAULTS); TODO
	}
	
	public void addData(String id, double value){
		databaseConnection.write(Point.measurement("cps")
        	    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)	//TODO 
        	    .tag("name", id)
        	    .addField("value", value)
        	    .build());
	}
}
