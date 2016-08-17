package edu.umass.cs.gnsserver.activecode.prototype;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;

import edu.umass.cs.gnsserver.activecode.prototype.blocking.ActiveBlockingClient;
import edu.umass.cs.gnsserver.activecode.prototype.interfaces.Client;
import edu.umass.cs.gnsserver.activecode.prototype.unblocking.ActiveNonBlockingClient;
import edu.umass.cs.gnsserver.interfaces.ActiveDBInterface;
import edu.umass.cs.gnsserver.interfaces.InternalRequestHeader;
import edu.umass.cs.gnsserver.utils.ValuesMap;

/**
 * @author gaozy
 *
 */
public class ActiveHandler {
	
	private static Client[] clientPool;
	
	private final static String cfilePrefix = "/tmp/client_";
	private final static String sfilePrefix = "/tmp/server_";
	private final static String suffix = "_pipe";
	private final static int clientPort = 50000;
	private final static int workerPort = 60000;
	
	/**
	 * Test then initialize this variable
	 */
	public boolean pipeEnable = true;
	
	private final int numProcess;
	final AtomicInteger counter = new AtomicInteger();
	
	
	/**
	 * Initialize handler with clients and workers.
	 * @param app 
	 * @param numProcess
	 * @param numThread 
	 * @param blocking blocking client or not
	 */
	public ActiveHandler(ActiveDBInterface app, int numProcess, int numThread, boolean blocking){
		final String fileTestForPipe = "/tmp/test";
		try {
			Runtime.getRuntime().exec("mkfifo "+fileTestForPipe);			
		} catch (IOException e) {
			pipeEnable = false;
			e.printStackTrace();			
		} finally{
			new File(fileTestForPipe).delete();
		}
		
		this.numProcess = numProcess;
		
		// initialize single clients and workers
		clientPool = new Client[numProcess];
		for (int i=0; i<numProcess; i++){
			if(blocking){
				if(pipeEnable){
					clientPool[i] = new ActiveBlockingClient(app, cfilePrefix+i+suffix, sfilePrefix+i+suffix, i, numThread);
				}else{
					clientPool[i] = new ActiveBlockingClient(app, clientPort+i, workerPort+i, i, numThread);
				}
			}else{
				if(pipeEnable){
					clientPool[i] = new ActiveNonBlockingClient(app, cfilePrefix+i+suffix, sfilePrefix+i+suffix, i, numThread);
				} else {
					clientPool[i] = new ActiveNonBlockingClient(app, clientPort+i, workerPort+i, i, numThread);
				}
				new Thread((ActiveNonBlockingClient) clientPool[i]).start();
			}
		}
		
	}
	
	/**
	 * Initialize a handler with multi-process single-threaded workers.
	 * @param app
	 * @param numProcess
	 */
	public ActiveHandler(ActiveDBInterface app, int numProcess){
		this(app, numProcess, 1, false);
	}
	
	/**
	 * Shutdown all the client and its corresponding workers
	 */
	private void shutdown(){
		for(int i=0; i<numProcess; i++){
			if(clientPool[i] != null){
				clientPool[i].shutdown();
			}
		}
	}
	
	/**
	 * @param header 
	 * @param guid
	 * @param field
	 * @param code
	 * @param value
	 * @param ttl
	 * @return executed result
	 * @throws ActiveException 
	 */
	public ValuesMap runCode(InternalRequestHeader header, String guid, String field, String code, ValuesMap value, int ttl) throws ActiveException{
		return clientPool[counter.getAndIncrement()%numProcess].runCode(header, guid, field, code, value, ttl, 2000);
	}
	
	/***************** Test methods ****************/	
	/**
	 * @param args
	 * @throws JSONException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws JSONException, InterruptedException, ExecutionException{
		int numProcess = Integer.parseInt(args[0]);
		int numThread = Integer.parseInt(args[1]);
		boolean blocking = Boolean.parseBoolean(args[2]);
		if(numProcess <= 0){
			System.out.println("Number of clients must be larger than 0.");
			System.exit(0);
		}
				
		final ThreadPoolExecutor executor;		
		
		executor = new ThreadPoolExecutor(numProcess*numThread, numProcess*numThread, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());		
		executor.prestartAllCoreThreads();
		
		String guid = "zhaoyu";
		String field = "gao";
		String noop_code = "";
		try {
			noop_code = new String(Files.readAllBytes(Paths.get("./scripts/activeCode/noop.js")));
		} catch (IOException e) {
			e.printStackTrace();
		} 
		ValuesMap value = new ValuesMap();
		value.put("string", "hello world");
		
		// initialize a handler
		ActiveHandler handler = new ActiveHandler(null, numProcess, numThread, blocking);
		ArrayList<Future<ValuesMap>> tasks = new ArrayList<Future<ValuesMap>>();
		
		int n = 1000000;
		
		long t1 = System.currentTimeMillis();
		
		for(int i=0; i<n; i++){
			tasks.add(executor.submit(new ActiveTask(clientPool[i%numProcess], guid, field, noop_code, value, 0)));
		}
		for(Future<ValuesMap> task:tasks){
			task.get();
		}
		
		long elapsed = System.currentTimeMillis() - t1;
		System.out.println("It takes "+elapsed+"ms, and the average latency for each operation is "+(elapsed*1000.0/n)+"us");
		System.out.println("The throughput is "+n*1000.0/elapsed);
		handler.shutdown();
		
	}
	
}
