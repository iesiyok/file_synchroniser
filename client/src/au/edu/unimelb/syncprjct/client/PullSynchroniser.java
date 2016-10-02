/**
 * @author iesiyok
 * @purpose Synchroniser thread for PULL type of operation
 * 			gets the instructions and apply them to the file,
 * 			and sends acknowledgement,expectancy or exceptions to the sender
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import filesync.BlockUnavailableException;
import filesync.Instruction;
import filesync.InstructionFactory;
import filesync.SynchronisedFile;

public class PullSynchroniser extends Thread{
	
private SynchronisedFile sFile;
	
	private InetAddress address;
	
	private int portNumber;
	
	private int packetSize;
	
	DatagramSocket socket;
	
	private static final JSONParser parser = new JSONParser();
	
	public PullSynchroniser(DatagramSocket socket, SynchronisedFile sFile, InetAddress host, int port, int packetSize) {
		this.socket = socket;
		this.setAddress(host);
		this.portNumber = port;
		this.setPacketSize(packetSize);
		this.sFile = sFile;
	}
	//thread run method
	public void run(){
		
		int packetSize= this.packetSize;
		DatagramSocket socket = this.socket;
		int counter = 1;
		int expected = 1;
		JSONObject resp = new JSONObject();
		
		
		try {
			DatagramPacket packet; 
			while(true){
				byte[] buf = new byte[packetSize];
				packet = new DatagramPacket(buf, buf.length);
				//socket.receive(packet);
				packet = Helper.socketReceiveTimer(packet,socket, packetSize);//socket timeout
				//
				String pack = new String(packet.getData(), 0,
						packet.getLength());
				String msg = pack;
				JSONObject json = (JSONObject)parser.parse(msg);
				int count = ((Long)json.get("counter")).intValue();
				if(count == expected || count == 1){
					InstructionFactory instFact = new InstructionFactory();
					Instruction receivedInst = instFact.FromJSON(json.get("inst").toString());
					
					if(receivedInst.Type().equals("StartUpdate")){
						counter = 1;expected=1;//if it is startupdate expected start from 1
					}else{
						counter++;
					}
					
					try{
						sFile.ProcessInstruction(receivedInst);
						expected++;
						resp.put("type", "ack");//creates acknowledgement
						resp.put("counter", counter);
						byte[] rbuf = msg.getBytes();
					}catch(BlockUnavailableException e){
						resp.put("type", "exception");//creates exception
						resp.put("counter", expected);
					}
				}else{
					resp.put("type", "expecting");//creates expecting response
					resp.put("counter", expected);
				}
				byte[] rbuf = new byte[packetSize];
				rbuf = resp.toJSONString().getBytes();
				DatagramPacket reply = new DatagramPacket(rbuf, rbuf.length,packet.getAddress(),packet.getPort());
				socket.send(reply);
				//sending the reply to the sender
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}finally {
			if (socket!=null){
				socket.close();
			}
		}
		
		
	}
	


	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public int getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

}
