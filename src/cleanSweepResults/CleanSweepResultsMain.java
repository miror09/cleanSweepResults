package cleanSweepResults;

import java.util.*;
import java.lang.System;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.UpdatingBatch;
import com.filenet.api.sweep.CmJobSweepResult;
import com.filenet.api.collection.CmJobSweepResultSet;

import com.filenet.api.query.SearchSQL;
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
		long startBatchTime = 0;
		long endBatchTime = 0;

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

		System.out.print("\n\n\tWould you like to delete Sweep Result objects? (Y/N) ... ");

		String input = System.console().readLine();

		if(!"Y".equalsIgnoreCase(input))
		{
			System.out.println("\n\tExiting ...\n");
			System.exit(0);
		}
		
		System.out.print("\n\n\tEnter batch size: ... ");

		String batchSizeStr = System.console().readLine();

		if(batchSizeStr.isEmpty() || !batchSizeStr.matches("-?\\d+(\\.\\d+)?"))
		{
			System.out.println("\n\tExiting, wrong number ...\n");
			System.exit(0);
		}
		
		System.out.println("\n\tDeleting ...");		
		
		int rowCount = 0, sweepResultCount = 0;
		int  batchSize = Integer.parseInt(batchSizeStr);
		System.out.println("\n\tBatch size ... " + batchSize);
		
		if(!(sweepResultSet.isEmpty())){
			Iterator iter = sweepResultSet.iterator();

			CmJobSweepResult sweepResult;
			//PropertyFilter pf = new PropertyFilter();
			//pf.addIncludeType(0, null, Boolean.TRUE, FilteredPropertyType.ANY, null);
			//com.filenet.api.property.Properties props;
			//String ID;
			
			// 	Iterate sweep result items // and delete Sweep Reults objects.
			String anim= "|/-\\";
			while (iter.hasNext())
			{
				rowCount++;

				// Work with batches
				startBatchTime = System.currentTimeMillis();
				UpdatingBatch ub = UpdatingBatch.createUpdatingBatchInstance(os1.get_Domain(), RefreshMode.NO_REFRESH);

				for (int i = 0; i < batchSize && iter.hasNext(); i++)
				{
				System.out.printf("\r " + rowCount + " Batch " + anim.charAt(i % anim.length()));

				sweepResultCount++;
				sweepResult = (CmJobSweepResult) iter.next();

				//sweepResult.fetchProperties(pf);
				//props = sweepResult.getProperties();
				//ID = props.getIdValue("ID").toString();
				//System.out.println(ID);
/*
				// No batches processing
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
				ub.updateBatch();
				endBatchTime = System.currentTimeMillis();
				System.out.printf("\r " + rowCount + " Batch   ..... done!\t Total deleted sweepResults: " + sweepResultCount + "\t Time: " + (endBatchTime - startBatchTime) + " ms\n");
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
