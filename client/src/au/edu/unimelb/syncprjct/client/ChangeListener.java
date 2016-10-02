/**
 * @author iesiyok
 * @purpose thread checks the differences fromsynchronised file
 * 			and puts new instructions into msgqueue 
 * @date 21.09.2014
 */

package au.edu.unimelb.syncprjct.client;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import filesync.CopyBlockInstruction;
import filesync.Instruction;
import filesync.NewBlockInstruction;
import filesync.SynchronisedFile;

public class ChangeListener extends Thread {
	
	//instruction queue
	public static BlockingQueue<HashMap<Integer, JSONObject>> 
			msgQueue = new ArrayBlockingQueue<HashMap<Integer, JSONObject>>(1024*1024);
	
	private static final JSONParser parser = new JSONParser();
	
	private SynchronisedFile sFile;

	public ChangeListener(SynchronisedFile sFile) {
		this.sFile = sFile;
	}
	//thread run method
	public void run(){
		
		Instruction inst = null;
		int counter = 1;
		SynchronisedFile sFile;
		try {
			sFile = this.getsFile();
			HashMap<Integer, JSONObject> m = null;
				while((inst = sFile.NextInstruction())!=null){
					JSONObject instMsg;
					try{
						instMsg = (JSONObject)parser.parse(inst.ToJSON());
						if(instMsg.get("Type").equals("StartUpdate")){
							
							m = new HashMap<Integer, JSONObject>();
							counter = 1;
							m.put(counter, instMsg);
						}else if(instMsg.get("Type").equals("EndUpdate")){
							counter++;
							m.put(counter, instMsg);
							msgQueue.put(m);
							m = null;
						}else{
							counter++;
							if(instMsg.get("Type").equals("CopyBlock")){
								CopyBlockInstruction cb = (CopyBlockInstruction) inst;
								/**
								 * We have to put bytes into the message, because
								 * when the NewBlockInstruction attempts to 
								 * convert CopyBlockInstruction -> NewBlockInstruction
								 * the conversion method uses (bytes) parameter.
								 * Another solution would be changing the NewBlockInstruction code,
								 * But, in this version, first solution is preferred.
								 * However, in this version byte size under 2048 may result in END OF FILE exceptions 
								 * 
								 */
								instMsg.put("bytes", new String(Base64.encodeBase64(cb.getBlock().getBytes()),"US-ASCII"));
							}else if(instMsg.get("Type").equals("NewBlock")){
								NewBlockInstruction nb = (NewBlockInstruction) inst;
								instMsg.put("bytes", new String(Base64.encodeBase64(nb.getBlock().getBytes()),"US-ASCII"));
							}
							m.put(counter, instMsg);
						}
						
					}catch (ParseException e) {
						e.printStackTrace();
					}catch (InterruptedException e) {
						e.printStackTrace();
					}catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}	
	

	public SynchronisedFile getsFile() {
		return sFile;
	}

	public void setsFile(SynchronisedFile sFile) {
		this.sFile = sFile;
	}

}
