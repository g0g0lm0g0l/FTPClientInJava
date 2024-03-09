package clienteFTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * @author Ivan Kovalenko
 * 
 */
public class ClientFTPManager {

	private Scanner scanner;

	private FTPClient clienteFTP;

	public ClientFTPManager(Scanner scanner, FTPClient clienteFTP) {
		this.scanner = scanner;
		this.clienteFTP = clienteFTP;
	}

	public void getConnectionToServer() {
		try {
			System.out.format("Introduce 'connect NombreServidor Usuario/Contraseña'\n");
			System.out.format("Ejemplo: \t 'connect ftp.rediris.es anonymous/anonymous' \n");

			String commandInit = scanner.nextLine();

			String[] splitCommandInit = commandInit.split(" ");

			if (splitCommandInit.length != 3 || !splitCommandInit[0].trim().equals("connect")) {
				throw new IllegalArgumentException(
						"Formato incorrecto!!! El comando debe ser 'connect NombreServidor Usuario/Contraseña'");
			}
			clienteFTP.connect(splitCommandInit[1]);
			clienteFTP.enterLocalPassiveMode();

			String[] splitUserPassword = splitCommandInit[2].split("/");

			System.out.println("splitUserPassword: " + splitUserPassword);

			boolean login = clienteFTP.login(splitUserPassword[0], splitUserPassword[1]);

			if (!login) {
				throw new IOException("Error al iniciar sesión en el servidor FTP. Verifiquat tus credenciales!!!");
			}
			System.out.format("Login correcto...\n");
		} catch (IOException e) {
			System.err.format("Error al conectar al servidor FTP: %s\n", e.getMessage());
			handleException(e);
		}
	}

	public void getCommand() {
		try {

			while (clienteFTP.isConnected()) {

				System.out.format("Introduce: 'command'\n");
				System.out.format("'command' permitido:" + "\t 'list' " + "\t 'changePath NuevoPath' "
						+ "\t 'down NombreFicheroRemoto' " + "\t 'up NombreFicheroLocal' " + "\t 'disconnect'\n");

				String commandUse = scanner.nextLine();

				String[] splitCommandUse = commandUse.split(" ");

				if (splitCommandUse.length == 1) {

					switch (splitCommandUse[0].trim()) {

					case "list":
						getWorkingDirectoryContent();
						break;
					case "disconnect":
						disconnectNormal();
						break;
					default:
						disconnectERROR();
					}

				} else if (splitCommandUse.length == 2) {

					switch (splitCommandUse[0].trim()) {

					case "changePath":
						changeWorkingDirectory(splitCommandUse[1].trim());
						break;
					case "down":
						downloadFile(splitCommandUse[1].trim());
						break;
					case "up":
						uploadFile(splitCommandUse[1].trim());
						break;
					default:
						disconnectERROR();
					}

				} else {
					System.err.println("COMANDO NO EXISTE");
					disconnectERROR();
				}
			}
		} catch (IOException e) {
			System.err.format("Error al obtener el comando: %s\n", e.getMessage());
			handleException(e);
		}
	}

	public void getWorkingDirectoryContent() {
		try {

			System.out.format("Directorio actual: %s\n", clienteFTP.printWorkingDirectory());

			FTPFile[] files = clienteFTP.listFiles();

			System.out.format("Ficheros en el directorio actual: %d", files.length);

			String tipos[] = { "Fichero", "Directorio", "Enlace simb." };

			for (int i = 0; i < files.length; i++) {
				System.out.format("\t %s \t => tipo:%s \n", files[i].getName(), tipos[files[i].getType()]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void changeWorkingDirectory(String newDirectory) {
		try {

			boolean success = clienteFTP.changeWorkingDirectory(newDirectory);
			if (success) {
				System.out.format("Directorio de trabajo cambiado a: %s\n", newDirectory);
			} else {
				System.err.format("No se pudo cambiar al directorio especificado\n");
			}
		} catch (IOException e) {
			System.err.format("Error al camabiar el directorio: %s\n", e.getMessage());
			handleException(e);
		}

	}

	public void downloadFile(String remoteFileName) {
		try {

			boolean success = clienteFTP.retrieveFile(remoteFileName, new FileOutputStream(remoteFileName));
			if (success) {
				System.out.format("Fichero '%s' descargado exitosamente!!!\n", remoteFileName);
				System.out.format("Refresca el proyecto en Eclipse y aparecerá!!!\n");
			} else {
				System.err.format("No se pudo descargar el fichero :( '%s'\n", remoteFileName);
			}
		} catch (IOException e) {
			System.err.format("Error al descargar el fichero: %s\n", e.getMessage());
			handleException(e);
		}

	}

	public void uploadFile(String localFileName) throws IOException {

		File localFile = new File(localFileName);

		try (FileInputStream inputStream = new FileInputStream(localFile)) {

			boolean success = clienteFTP.storeFile(localFileName, inputStream);

			if (success) {
				System.out.format("Fichero '%s' subido exitosamente\n", localFileName);
			} else {
				System.err.format("No se pudo subir el fichero %s\n", localFileName);
			}

		} catch (IOException e) {
			System.err.format("Error al subir el fichero: %s\n", e.getMessage());
			handleException(e);
		}

	}

	public void disconnectNormal() {
		try {

			boolean logout = clienteFTP.logout();

			if (logout) {
				System.out.println("Logout del servidor FTP...");
			} else {
				System.out.println("Error al hacer Logout...");
			}

			clienteFTP.disconnect();

			System.out.println("Desconectado...");

		} catch (IOException e) {
			System.err.format("Error al desconectarse: %s\n", e.getMessage());
			handleException(e);
		}
	}

	public void disconnectERROR() {

		System.err.format("ERROR...\n");

		try {

			clienteFTP.disconnect();
			System.out.format("¡Desconexión del servidor FTP exitosa!!!\n");

		} catch (IOException e) {
			System.err.format("Error al desconectar del servidor FTP: %s\n", e.getMessage());
			e.printStackTrace();
		}
		System.exit(1);
	}

	private void handleException(IOException e) {
		System.err.format("Error: %s\n", e.getMessage());
		disconnectERROR();
	}
}
