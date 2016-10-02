/**
 * @author iesiyok
 * @purpose helper methods, not working in this version!!!
 * @date 21.09.2014
 */


package au.edu.unimelb.syncprjct.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Helper {
	
	//manages send-receive timeout for packets
	public static DatagramPacket socketSendReceiveTimer(DatagramPacket packet,
			DatagramSocket socket, int packetSize) {
		try {
			
			//socket.send(packet);
			socket.setSoTimeout(4000);
			//DatagramPacket packet3 = packet
			byte[] b = packet.getData();
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			//DatagramPacket packet2 = packet;
			packet.setData(new byte[packetSize]);
			//int counter = 0;
			while(true){
				//counter++;
				try{									
					socket.receive(packet);
					return packet;
				}catch(SocketTimeoutException e){
					System.out.println("socket timeout.. trying again ");
					//if(counter2 > 0){
					DatagramPacket packet2 = new DatagramPacket(b, b.length, address,port);
					socket.send(packet2);
					socketSendReceiveTimer(packet2, socket, packetSize);//try 2 times to receive packets
					//}
					//socket.close();
					//System.out.println("Program shut down..");
					//System.exit(0);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		
		return packet;
	}
	
	//manages receive timeout for packets
	public static DatagramPacket socketReceiveTimer(DatagramPacket packet,
			DatagramSocket socket, int packetSize) {
		try {
			
			socket.setSoTimeout(4000);
			//byte[] b = packet.getData();
			//InetAddress address = packet.getAddress();
			//int port = packet.getPort();
			packet.setData(new byte[packetSize]);
			//int counter = 0;
			while(true){
				//counter++;
				try{									
					socket.receive(packet);
					return packet;
				}catch(SocketTimeoutException e){
					System.out.println("socket timeout.. trying again");
					//if(counter2 > 0){
					//DatagramPacket packet2 = new DatagramPacket(b, b.length);
					//packet.setData(new byte[packetSize]);
						socketReceiveTimer(packet, socket, packetSize);//try 2 times to receive packets
					//}
					//socket.close();
					//System.out.println("Program shut down..");
					//System.exit(0);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		
		return packet;
	}

}
