package cleanSweepResults;

import java.util.*;
import java.lang.System;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.UpdatingBatch;
import com.filenet.api.replication.ReplicationJournalEntry;
import com.filenet.api.sweep.CmJobSweepResult;
import com.filenet.api.collection.CmJobSweepResultSet;
import com.filenet.api.collection.IndependentObjectSet;

import com.filenet.api.query.SearchSQL;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.FilteredPropertyType;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.query.SearchScope;
import com.filenet.api.constants.RefreshMode;
import java.text.SimpleDateFormat;

/**
 * @author Miroslav Richter - 4.B
 *
 */

public class CleanSweepResultsMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Start - main.");
		
		//String sqlString = "select this from CmSweepResult OPTIONS (COUNT_LIMIT 10000000)";	
		String sqlString = "select this from CmSweepResult";

		/* parsing main arguments */
		if (args.length != 0)
			sqlString=args[0];

//		if(!(sqlString.toLowerCase()).startsWith("select this from CmSweepResult"))
//		{
//			System.out.println("\n\tNot a valid argument: '" + sqlString + "'\n");
//			System.out.println("\tOnly one argument is alowed and it must start with string 'Select This from CmSweepResult'. Exiting ...");
//			System.exit(1);	
//		}


		CEConnection ce = null;	
		ObjectStore os1 = null;

		long startTime = 0;
		long endTime = 0;
		long testTime = 0;
		RepositoryRowSet rowSet = null;

		try
        {
		startTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");

		ce = new CEConnection();
		System.out.println("\n\tGetting connection to FileNet CE -> OS1 ...");
		ce.establishConnection("Administrator","filenet","FileNetP8","iiop://cep1.codisdom:2809/FileNet/Engine");
		os1 = ce.fetchOS("OS1");
		System.out.println("\n\tConnection to FileNet CE -> ObjectStore: " + os1.get_DisplayName() + " successful!");			
		
		System.out.println("\n\tSearching ...\n");
		SearchSQL sqlObject = new SearchSQL(sqlString);
		SearchScope searchScope = new SearchScope(os1);
		
		// Get all items in the sweep result.
		CmJobSweepResultSet sweepResultSet = (CmJobSweepResultSet)searchScope.fetchObjects(sqlObject, new Integer(1), null, Boolean.TRUE);
		//System.out.println(sweepResultSet.pageIterator().getTotalCount());
		

		System.out.print("\n\n\tWould you like to delete Sweep Result objects? (Y/N) ... ");

		String input = System.console().readLine();

		if(!"Y".equalsIgnoreCase(input))
		{
			System.out.println("\n\tExiting ...\n");
			System.exit(0);
		}
		
		System.out.println("\n\tDeleting ...\n");		
		
		int rowCount = 0, docCount = 0;
		if(!(sweepResultSet.isEmpty())){
			Iterator iter = sweepResultSet.iterator();

			CmJobSweepResult sweepResult;
			//PropertyFilter pf = new PropertyFilter();
			//pf.addIncludeType(0, null, Boolean.TRUE, FilteredPropertyType.ANY, null);
			//com.filenet.api.property.Properties props;
			//String ID;
			
			// 	Iterate sweep result items // and delete Sweep Reults objects.
			while (iter.hasNext())
			{
				rowCount++;
				// Work with batches
				UpdatingBatch ub = UpdatingBatch.createUpdatingBatchInstance(os1.get_Domain(), RefreshMode.NO_REFRESH);
				for (int batchSize = 0; batchSize < 1000 && iter.hasNext(); batchSize++)
				{
				docCount++;
				sweepResult = (CmJobSweepResult) iter.next();
				// Return document properties.
				//sweepResult.fetchProperties(pf);
				//props = sweepResult.getProperties();
				//ID = props.getIdValue("ID").toString();
				//System.out.println(ID);
/*
				//if (sweepResult.get_ClassDescription().get_SymbolicName().equals(ClassNames.CM_JOB_SWEEP_RESULT))
				//{
					sweepResult.delete();
					rowCount++;
					//System.out.print(rowCount + " : " + ID);
					System.out.print(rowCount + " : ");
					sweepResult.save(RefreshMode.NO_REFRESH);
					System.out.printf(" ..... deleted!\n");
				//}
*/
					sweepResult.setUpdateSequenceNumber(null);
					sweepResult.delete();
					ub.add(sweepResult, null);
				}
				System.out.print("Batch " + rowCount);
				ub.updateBatch();
				System.out.printf(" ..... done!\t Total deleted docs: " + docCount + "\n");
			}
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("\t\t\tTotal number of deleted records in Sweep result table: " + rowCount);
		System.out.println("\t\t\tSearching time: " + (endTime - startTime) + " ms");

        }
        catch (EngineRuntimeException e)
        {
        	if(e.getExceptionCode() == ExceptionCode.E_NOT_AUTHENTICATED)
        	{
        		System.out.println("Invalid login credentials supplied - please try again " + e.getMessage());
        	}
        	else if(e.getExceptionCode() == ExceptionCode.API_UNABLE_TO_USE_CONNECTION)
        	{
        		System.out.println("Unable to connect to server.  Please check to see that URL is correct and server is running" + e.getMessage());
        	}
        	else
        	{
        		System.out.println("ERR: " + e.getMessage());
        	}
            	e.printStackTrace();
        }
	}
}
