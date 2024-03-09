package clienteFTP;

import java.util.Scanner;

import org.apache.commons.net.ftp.FTPClient;

/**
 * @author Ivan Kovalenko
 * 
 */
public class ClienteFTP {

	private static final Scanner SCANNER = new Scanner(System.in);

	private static FTPClient FTP_CLIENT = new FTPClient();

	public static void main(String[] args) {

		System.out.println("FTPClient is ON");

		ClientFTPManager clientFTPManager = new ClientFTPManager(SCANNER, FTP_CLIENT);

		try {

			if (!FTP_CLIENT.isConnected()) {
				
				clientFTPManager.getConnectionToServer();
				clientFTPManager.getCommand();
				
			} else {
				
				clientFTPManager.getCommand();
				
			}

		} finally {
			
			SCANNER.close();
			
			if (FTP_CLIENT.isConnected()) {
				
				clientFTPManager.disconnectNormal();
				System.out.println("FTPClient is OFF");
				
			}
		}
	}

}
