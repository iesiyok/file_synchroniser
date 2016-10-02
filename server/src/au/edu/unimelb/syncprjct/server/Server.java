/**
 * @author iesiyok
 * @purpose Main class for Server.
 * 			calls push or pull synchronisers according to the 
 * 			direction argument retrieved from  the client
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import au.edu.unimelb.syncprjct.client.ChangeListener;
import au.edu.unimelb.syncprjct.client.CmdValues;
import au.edu.unimelb.syncprjct.client.PullSynchroniser;
import au.edu.unimelb.syncprjct.client.PushSynchroniser;
import filesync.SynchronisedFile;

public class Server {

	private static int DEFAULT_PORT_NUMBER = 4144;
	
	private static final JSONParser parser = new JSONParser();
	
	private static int DEFAULT_PACKET_SIZE = 2048;
	
	public static void main(String[] args) {
		
		CmdValues values = new CmdValues();
		CmdLineParser cmdParser = new CmdLineParser(values);
		cmdParser.setUsageWidth(80);
		
		try{
			cmdParser.parseArgument(args);
		}catch(CmdLineException e){
			System.err.println(e.getMessage());
			System.exit(0);
		}
		//command line parameters
		String fileName = values.getFileName();
		int portNumber = DEFAULT_PORT_NUMBER;
		if(values.getPortNumber()>0){
			portNumber = values.getPortNumber();
		}
		
		DatagramSocket socket = null;
		try{
			
			socket = new DatagramSocket(portNumber);
			SynchronisedFile sFile = new SynchronisedFile(fileName);
			int packetSize = DEFAULT_PACKET_SIZE;
			System.out.println("server started");
			byte[] buf = new byte[packetSize];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			String pack = new String(packet.getData(), 0,
					packet.getLength());
			String msg = pack;
			JSONObject json = (JSONObject)parser.parse(msg);
			String direction="push";
			if(json.get("type")!=null && json.get("type").equals("negotiation")){
				direction = (String) json.get("direction");
				packetSize = ((Long)json.get("blocksize")).intValue();
			}
			//if direction is push, server is pull	
			if(direction == "push"){
				byte[] rbuf = new byte[packetSize];
				JSONObject resp= new JSONObject();
				resp.put("type", "ack");
				resp.put("counter", 0);
				rbuf = resp.toJSONString().getBytes();
				DatagramPacket reply = new DatagramPacket(rbuf, rbuf.length,packet.getAddress(),packet.getPort());
				socket.send(reply);
				PullSynchroniser pullSync = new PullSynchroniser(socket,sFile,
						packet.getAddress(), packet.getPort(), packetSize);
				pullSync.start();
				
			}else{
				//if direction is pull, server is push
				Thread chListener = new ChangeListener(sFile);
				chListener.start();
				PushSynchroniser pSync = new PushSynchroniser(sFile,
						packet.getAddress(), packet.getPort(), packetSize);
				pSync.start();
				while(true){
					try {
						sFile.CheckFileState();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(-1);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}catch(ParseException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

		
	}


}
