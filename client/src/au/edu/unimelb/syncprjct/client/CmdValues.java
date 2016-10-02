/**
 * @author iesiyok
 * @purpose command line argument parser
 * @date 21.09.2014
 */
package au.edu.unimelb.syncprjct.client;

import org.kohsuke.args4j.*;

public class CmdValues {

	@Option(name = "-file", usage = "file name", required = true)
	private String fileName;

	@Option(name = "-host", usage = "host name")
	private String hostName;

	@Option(name = "-p", usage = "server port")
	private int portNumber;

	@Option(name = "-b", usage = "block size")
	private int blockSize;

	@Option(name = "-d", usage = "direction")
	private String direction;


	public CmdValues() {

	}

	public CmdValues(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}


	public String getDirection() {
		return direction;
	}

}
