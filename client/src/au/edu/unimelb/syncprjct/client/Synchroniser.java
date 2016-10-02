/**
 * @author iesiyok
 * @purpose synchroniser class,
 * 			calls Push or Pull Synchronisers according to the direction argument
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONObject;

import filesync.SynchronisedFile;

public class Synchroniser extends Thread {
	
	
	private SynchronisedFile sFile;
	
	private String hostName;
	
	private int portNumber;
	
	private int packSize;
	
	private String direction;

	public Synchroniser(SynchronisedFile sFile, String hostName, int portNumber, int packSize,String direction) {
		this.sFile = sFile;
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.packSize = packSize;
		this.direction = direction;
	}
	//thread run method
	public void run(){
		
		InetAddress address = null;
		int port = this.getPortNumber();
		int packetSize = this.packSize;
		try{
			address = InetAddress.getByName(this.getHostName());
		}catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}
		//client will push its instructions 
		if(direction.equals("push")){
			Thread chListener = new ChangeListener(sFile);
			chListener.start();
			PushSynchroniser pushSync = new PushSynchroniser(sFile, address, port, packetSize);//call push
			pushSync.start();
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
			
		}else{
			//client will pull from server
			DatagramSocket socket = null;
			JSONObject negObj = createNegMessage("pull",packetSize);//what if this packet is lost?TODO test
			byte[] negBuf = negObj.toJSONString().getBytes();
			try {
				socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(negBuf, negBuf.length, address,port);
				socket.send(packet);
				PullSynchroniser pSync = new PullSynchroniser(socket,sFile, address, port, packetSize);//call put
				pSync.start();
				
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
		}
		
	}

	private JSONObject createNegMessage(String direction, int packSize) {
		
		JSONObject jObj = new JSONObject();
		jObj.put("type", "negotiation");
		jObj.put("direction",direction);
		jObj.put("blocksize", packSize);
		
		return jObj;
	}
	public SynchronisedFile getsFile() {
		return sFile;
	}

	public void setsFile(SynchronisedFile sFile) {
		this.sFile = sFile;
	}
	
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	
	public int getPackSize() {
		return packSize;
	}
	public void setPackSize(int packSize) {
		this.packSize = packSize;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}


}

