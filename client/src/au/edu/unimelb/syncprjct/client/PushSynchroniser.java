/**
 * @author iesiyok
 * @purpose Synchroniser thread for PUSH type of operation
 * 			gets the instructions from messageQueue and 
 * 			sends them to the other source, 
 * 			gets the response and prepares the next packet
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import filesync.CopyBlockInstruction;
import filesync.InstructionFactory;
import filesync.NewBlockInstruction;
import filesync.SynchronisedFile;

public class PushSynchroniser extends Thread{
	
	private SynchronisedFile sFile;
	
	private InetAddress address;
	
	private int portNumber;
	
	private int packetSize;
	
	private static final JSONParser parser = new JSONParser();
	
	public PushSynchroniser(SynchronisedFile sFile, InetAddress host, int port, int packetSize) {
		
		this.setAddress(host);
		this.portNumber = port;
		this.setPacketSize(packetSize);
		this.sFile = sFile;
	}
	//thread run method
	public void run(){
		InetAddress address = this.address;
		int port = this.portNumber;
		int packetSize= this.packetSize;
		DatagramSocket socket = null;
		
		HashMap<Integer,JSONObject> messageMap=null;
		int expected = 0;
		try{
			socket = new DatagramSocket();
			while(true){
				//checks ChangeListener.msgQueue indefinetely to get the instructions
				while (ChangeListener.msgQueue.size()>0 && socket != null) {
					messageMap = (HashMap<Integer, JSONObject>) ChangeListener.msgQueue.take();
					expected = 1;
					/**
					 * Messagequeue creates a hashmap including an instruction's whole packages
					 * it is typically marshaled as:
					 * Starts from StartUpdate,
					 * continues with blocks(copy,new),
					 * ends with EndUpdate 
					 * 
					 */
					while(true){
						JSONObject instMsg = messageMap.get(expected);
						
						/**
						 * This loop gets the instruction and sends them to the other source
						 * as the same order in the hashmap
						 */
						
						byte[] buffer = toJSON("inst", instMsg, expected).toJSONString().getBytes();
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address,port);
						try{
							socket.send(packet);
							packet = Helper.socketSendReceiveTimer(packet,socket, packetSize);//check timeout
							/*socket.send(packet);
							packet.setData(new byte[packetSize]);
							socket.receive(packet);*/
							String pack = new String(packet.getData(), 0,
									packet.getLength());
							JSONObject json = (JSONObject)parser.parse(pack);
							if(json.get("type").equals("ack")){
								if(!instMsg.get("Type").equals("EndUpdate")){
									expected = ((Long)json.get("counter")).intValue() + 1;
								}else{
									break;
								}
							}else if(json.get("type").equals("expecting")){
								expected = ((Long)json.get("counter")).intValue();
							}else if(json.get("type").equals("exception")){
								expected = ((Long)json.get("counter")).intValue();
								//convert the Copyblock to NewInstruction
								messageMap = convert2NewInstruction(messageMap, expected);
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if (socket!=null){
				socket.close();
			}
		}
		
		
	}
	
	
	private JSONObject toJSON(String type, JSONObject o, int count){
		JSONObject jObj = new JSONObject();
		jObj.put("type", type);
		jObj.put("inst", o);
		jObj.put("counter", count);
		return jObj;
		
	}
	
	private HashMap<Integer, JSONObject> convert2NewInstruction(HashMap<Integer, JSONObject> instMap, int index){
		
		JSONObject jObj = instMap.get(index);
		InstructionFactory fac = new InstructionFactory();
		CopyBlockInstruction copInst = (CopyBlockInstruction)fac.FromJSON(jObj.toString());
		NewBlockInstruction nb = new NewBlockInstruction(copInst);
		
		nb.getBlock().setBytes(Base64.decodeBase64((String) jObj.get("bytes")));//have to!!!!
		
		try {
			instMap.remove(index);
			instMap.put(index,((JSONObject)parser.parse(nb.ToJSON())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return instMap;
	}
	

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public SynchronisedFile getsFile() {
		return sFile;
	}

	public void setsFile(SynchronisedFile sFile) {
		this.sFile = sFile;
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

}
