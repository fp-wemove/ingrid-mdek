package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.IMdekCaller;
import de.ingrid.mdek.MdekCaller;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.IMdekCaller.Quantity;
import de.ingrid.utils.IngridDocument;

public class MdekExample {

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties> [--threads 1]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() < 1) {
			printUsage();
		}

		// read passed Parameters
		System.out.println("\n###### PARAMS ######");
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}
		System.out.println("THREADS: " + numThreads);

		// INITIALIZE MDEK INTERFACE ONCE !
		MdekCaller.initialize(new File((String) map.get("--descriptor")));

		// start threads calling job
		System.out.println("\n###### OUTPUT THREADS ######\n");
		MdekThread[] threads = new MdekThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekThread(i+1);
		}
		// fire
		for (int i=0; i<numThreads; i++) {
			threads[i].start();
		}

		// wait till all threads are finished
		boolean threadsFinished = false;
		while (!threadsFinished) {
			threadsFinished = true;
			for (int i=0; i<numThreads; i++) {
				if (threads[i].isRunning()) {
					threadsFinished = false;
					Thread.sleep(500);
					break;
				}
			}
		}

		// shutdown mdek
		MdekCaller.shutdown();
	}
}

class MdekThread extends Thread {

	private int threadNumber;
	
	private boolean isRunning = false;

	public MdekThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
	}

	public void run() {
		isRunning = true;

		IMdekCaller mdekCaller = MdekCaller.getInstance();
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		//System.out.println("\n###### INVOKE testMdekEntity ######");
		//mdekCaller.testMdekEntity(threadNumber);

		System.out.println("\n###### INVOKE fetchTopObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchTopObjects();
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}

		System.out.println("\n###### INVOKE fetchSubObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchSubObjects("3866463B-B449-11D2-9A86-080000507261");
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchObject("E118D248-0705-11D5-87B3-00600852CACF", Quantity.DETAIL_ENTITY);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> objs = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + objs.size() + " Entities");
			for (IngridDocument o : objs) {
				System.out.println("Object: " + o.get(MdekKeys.UUID) + ", " + o.get(MdekKeys.TITLE));			
				System.out.println(o);
				List<IngridDocument> adrs = (List<IngridDocument>) o.get(MdekKeys.ADR_ENTITIES);
				System.out.println("  Addresses: " + adrs.size() + " Entities");
				for (IngridDocument a : adrs) {
					System.out.println("  " + a);								
					List<IngridDocument> coms = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
					System.out.println("    Communication: " + coms.size() + " Entities");
					for (IngridDocument c : coms) {
						System.out.println("    " + c);
					}
				}
			}
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}

/*
		System.out.println("\n###### INVOKE fetchTopAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchTopAddresses();
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCaller.fetchSubAddresses("0DAE03C6-373D-45FE-AF45-4D8359750A08");
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			System.out.println("ERROR: " + mdekCaller.getErrorMsgFromResponse(response));			
		}
*/

		isRunning = false;
	}
	
	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
