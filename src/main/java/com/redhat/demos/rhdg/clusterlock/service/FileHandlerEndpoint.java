package com.redhat.demos.rhdg.clusterlock.service;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.redhat.demos.rhdg.clusterlock.DataGridEmbeddedCacheManager;
import com.redhat.demos.rhdg.clusterlock.FileHandler;


@Path("/filehandler")
@Component
public class FileHandlerEndpoint {
	
	private static Logger log = Logger.getLogger(FileHandlerEndpoint.class.getName());
	
	@Autowired
	FileHandler fileHandle;
	
	@Autowired
	DataGridEmbeddedCacheManager cacheMgr;
	
	@Value("${CLUSTER_LOCK:false}")
	Boolean useClusterLock;
	
	@Value("${DEMO_WRITE_DELAY:1000}")
	Integer delay;
	
	@GET
	@Produces("application/text")
	public String hello()
	{
		return "Hello World!";
	}
	
	
	@GET
	@Path("/{filename}")
	@Produces("application/text")
	public String readFile(@PathParam("filename") String filename) {
		String content = fileHandle.readFile(filename);
		return "Filename: " +filename +"\nContent\n" + content;
	}
	
	@POST
	@Path("/{filename}")
	@Produces("application/text")
	public String appendToFile(@PathParam("filename") String filename, String message) {
		// To demonstrate synchronization, perform three writes with a configurable delay in between.
		
		
		

		boolean lockAcquired = false;
		try {
			if(useClusterLock)
			{
				int count = 0;
				while(!lockAcquired && count++<5) {
					lockAcquired = cacheMgr.acquireLock(filename);
				}
				log.info("Using Cluster Lock, lock acquired: " + lockAcquired);
//				System.out.println("Using Cluster Lock, lock acquired: " + lockAcquired);
				if(lockAcquired) {
					performFileWrite(filename, message);
					
					cacheMgr.releaseLock(filename);
				}
				else {
					throw new ServiceUnavailableException("Unable to update file: " + filename);
				}
			}
			else {
				log.info("Perfomring unsafe write to file: " + filename);
				performFileWrite(filename, message);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.severe(e.getMessage());
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			log.severe(e.getMessage());
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
		
		String content = fileHandle.readFile(filename);
		return "Filename: " +filename +"\nContent\n" + content;
	}


	private void performFileWrite(String filename, String message)
			throws InterruptedException {
		String id = "UNDEFINED";
		try {
			id = Inet4Address.getLocalHost().getHostName() +":"+ Thread.currentThread().getName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String openBlock= id + ": START MESSAGE BLOCK";
		String closeBlock = id + ": END MESSAGE BLOCK";
		
		//FIXME skip if using Clusterlock and lock is not acquired
		fileHandle.writeFile(filename, openBlock);
		Thread.sleep(delay);

		fileHandle.writeFile(filename, message);			
		Thread.sleep(delay);

		fileHandle.writeFile(filename, closeBlock);
		Thread.sleep(delay);
	}
	
	@DELETE
	@Path("/{filename}")
	public void deleteFile(@PathParam("filename") String filename) {
		fileHandle.deleteFile(filename);
	}


}
