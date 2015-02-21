package hellostorm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Driver;
import org.apache.hadoop.hive.ql.processors.CommandProcessorResponse;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.log4j.Logger;
import org.apache.storm.hdfs.common.rotation.RotationAction;

/**
 * When an HDFS File Rotation policy executes, this action will create hive partition based on the timestamp of the file and load the hdfs file into the partition
 * @author gvetticaden
 *
 */
public class HiveTablePartitionAction implements RotationAction {


	private static final long serialVersionUID = 2725320320183384402L;
	
	private static final Logger LOG = Logger.getLogger(HiveTablePartitionAction.class);
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private String sourceMetastoreUrl;
	private String databaseName;
	private String tableName;
	private String sourceFSrl;
	
	
	
	public HiveTablePartitionAction(String sourceMetastoreUrl,String tableName, String databaseName, String sourceFSUrl) {
		super();
		this.sourceMetastoreUrl = sourceMetastoreUrl;
		this.tableName = tableName;
		this.databaseName = databaseName;
		this.sourceFSrl = sourceFSUrl;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void execute(FileSystem fileSystem, Path filePath)
			throws IOException {
		
		long timeStampFromFile= getTimestamp(filePath.getName());
		Date date = new Date(timeStampFromFile);
		
		
		String datePartitionName = constructDatePartitionName(date);
		String hourPartitionName = constructHourPartitionName(date);
		
		String fileNameWithSchema = sourceFSrl + filePath.toString();		
		
		addFileToPartition(fileNameWithSchema, datePartitionName, hourPartitionName);

	}
	
    private String constructHourPartitionName(Date date) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(date);
		int hour =  calendar.get(Calendar.HOUR_OF_DAY);
		if(hour< 10) {
			return "0"+hour;
		} else {
			return String.valueOf(hour);
		}
		
	}

	private String constructDatePartitionName(Date date) {
		return dateFormat.format(date);
	}

	private long getTimestamp(String fileName) {
		int startIndex = fileName.lastIndexOf("-");
		int endIndex = fileName.lastIndexOf(".");
		String timeStamp = fileName.substring(startIndex + 1, endIndex);
		return Long.valueOf(timeStamp);
	}

	private void addFileToPartition(String fileNameWithSchema, String datePartitionName, String hourPartitionName ) {
         
        loadData(fileNameWithSchema, datePartitionName, hourPartitionName);
        
    }	
    
    public void loadData(String path, String datePartitionName, String hourPartitionName ) {
    	
    	String partitionValue = datePartitionName + "-" + hourPartitionName;
    	
    	LOG.info("About to add file["+ path + "] to a partitions["+partitionValue + "]");
    	
    	StringBuilder ddl = new StringBuilder();
    	ddl.append(" load data inpath ")
			.append(" '").append(path).append("' ")
			.append(" into table ")
			.append(tableName)
			.append(" partition ").append(" (date='").append(partitionValue).append("')");

    	startSessionState(sourceMetastoreUrl);

    	try {
			execHiveDDL("use " + databaseName);
			execHiveDDL(ddl.toString());
		} catch (Exception e) {
			String errorMessage = "Error exexcuting query["+ddl.toString() + "]";
			LOG.error(errorMessage, e);
			//throw new RuntimeException(errorMessage, e);
		}
	}    
    
    public void startSessionState(String metaStoreUrl) {
        HiveConf hcatConf = new HiveConf();
        hcatConf.setVar(HiveConf.ConfVars.METASTOREURIS, metaStoreUrl);
        hcatConf.set("hive.metastore.local", "false");
        hcatConf.set(HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.varname, "false");
        hcatConf.set("hive.root.logger", "DEBUG,console");
        SessionState.start(hcatConf);
    }   
    
    public void execHiveDDL(String ddl) throws Exception {
        LOG.info("Executing ddl = " + ddl);

        Driver hiveDriver = new Driver();
        CommandProcessorResponse response = hiveDriver.run(ddl);

        if (response.getResponseCode() > 0) {
            throw new Exception(response.getErrorMessage());
        }
    }    

}

