/**
 * @author iesiyok
 * @purpose main class for the client.
 * 			takes the parameters and sends them to Synchroniser
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.client;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import filesync.SynchronisedFile;

public class Client {

	private static int DEFAULT_PORT_NUMBER = 4144;
	private static int DEFAULT_PACK_SIZE = 2048;
	private static String DEFAULT_DIRECTION = "push";
	public static void main(String[] args) {
		
		CmdValues values = new CmdValues();
		CmdLineParser parser = new CmdLineParser(values);
		parser.setUsageWidth(80);
		
		try{
			parser.parseArgument(args);
		}catch(CmdLineException e){
			System.err.println(e.getMessage());
			System.exit(0);
		}
		//parameters from command line
		String fileName = values.getFileName();
		String hostName = values.getHostName();
		int portNumber = DEFAULT_PORT_NUMBER;
		if(values.getPortNumber()>0){
			portNumber = values.getPortNumber();
		}
		String direction = DEFAULT_DIRECTION;//push default
		if(values.getDirection()!=null){
			direction = values.getDirection();
		}
		int packSize = DEFAULT_PACK_SIZE;
		if(values.getBlockSize()>0){
			packSize = values.getBlockSize();
		}
		try {
			SynchronisedFile sFile = new SynchronisedFile(fileName);
			Thread synchroniser = new Synchroniser(sFile,hostName,portNumber, packSize,direction);
			synchroniser.start();
			System.out.println("Client started");
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

}
