import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.stream.Stream;

public class Server {

	public static void main(String[] args) {

		// get input from user when server start
		String dir = "/Users/liuhe/Library/Mobile Documents/3L68KQB4HG~com~readdle~CommonDocuments/Documents/COMP 445/Assignment/2/src/data";

		boolean v = true;
		int port = 8080;
		boolean ReturnFile = false;

		System.out.println("httpfs is a simple file server.");
		System.out.println("usage: httpfs [-v] [-p port] [-d PATH-TO-DIR]\n");
		System.out.println("\t-v Prints debugging messages.");
		System.out.println("\t-p Specifies the port number that server will listen and serve at. Default is 8080.");
		System.out.println("\t-d Specifies the directory that the server will use to read/write requested files. Default is the current directory when launching the application.");

		boolean loop = true;
		while (loop){
			String input = "";
			Scanner scanner = new Scanner (System.in);
			System.out.println("Enter the command line here: ");
			input = scanner.nextLine();
			String[] inputSplited = input.split(" ");


			loop = false;
			for (int i = 0; i < inputSplited.length; i++) {
				if (inputSplited[i].equalsIgnoreCase("httpfs")) {
					continue;
				} else if (inputSplited[i].equalsIgnoreCase("-v")) {
					v = true;
					continue;
				} else if (inputSplited[i].matches("\\d+")) {
					port =Integer.parseInt(inputSplited[i]);
					continue;
				} else if (!inputSplited[i].isEmpty() && inputSplited[i].charAt(0) == '/') {
					dir = inputSplited[i];
					continue;
				} else {
					loop = true;
					dir = "/Users/liuhe/Library/Mobile\\ Documents/3L68KQB4HG~com~readdle~CommonDocuments/Documents/COMP\\ 445/Assignment/2/src/data";
					v = false;
					port = 8080;
					break;
				}
			}

		}

		// get connection
		while (true){
			try {
				ServerSocket server = new ServerSocket(port);

				// debug message
				if (v){
					System.out.println("Server is listening at port " + port);
				}

				Socket clientSocket  = server.accept();

				// debug message
				if (v){
					System.out.println("Server accepted a connection");
				}

				// get Date
				final Date currentTime = new Date();
				final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

				// IO
				InputStream inputStream = clientSocket.getInputStream();
				OutputStream outputStream = clientSocket.getOutputStream();

				// variable
				StringBuilder request = new StringBuilder();
				String postData = "";
				int indexOfContentLength = 0;
				int indexOfUserAgent = 0;
				String body = "";
				String response = "";

				//read GET
				int data = inputStream.read();
				int counter = 0;
				while(data != -1) {
					if(((char) data) == '\r' || ((char) data) == '\n') {
						counter++;
						if (counter == 4)
							break;
					} else {
						counter = 0;
					}
					request.append((char) data);
					data = inputStream.read();
				}
				String[] splitedRequest = request.toString().split("\\s+");

				// read POST data
				if (request.substring(0,4).equalsIgnoreCase("post")){

					for (int i = 0; i < splitedRequest.length; i++){
						if (splitedRequest[i].equalsIgnoreCase("Content-Length:")){
							indexOfContentLength = i+1;
						} else if (splitedRequest[i].equalsIgnoreCase("User-Agent:")){
							indexOfUserAgent = i+1;
						}
					}
					for (int i = 0; i < Integer.parseInt(splitedRequest[indexOfContentLength]); i++){

						data = inputStream.read();
						postData = postData + (char)data;
					}
					request.append("\r\n");
					request.append(postData);
				}

				// debug message
				if (v){
					System.out.println("\nRequest From client");
					System.out.println(request);
// Show request split
//					for (int i = 0; i < splitedRequest.length; i++){
//						System.out.println(i);
//						System.out.println(splitedRequest[i]);
//					}
				}




				// create GET body
				// GET /
				if (splitedRequest[1].length()<5){
					String tdir = dir + splitedRequest[1];
					System.out.println(tdir);
					if (splitedRequest[1].contains(".")){
						StringBuilder contentBuilder = new StringBuilder();
						// NEED change path before running
						try (Stream<String> stream = Files.lines(Paths.get(tdir), StandardCharsets.UTF_8)){
							stream.forEach(s -> contentBuilder.append(s).append("\n"));
							body = contentBuilder.toString();
							response = "HTTP/1.0 200 ok\r\n" +
									"Date: " + sdf.format(currentTime) + "\r\n" +
									"Content-Disposition: inline \r\n" +
									"Content-Disposition: attachment; filename=\"" + splitedRequest[1] + "\"\r\n" +
									"Content-Type: application/json\r\n" +
									"Content-Length: " + body.length() + "\r\n" +
									"Connection: close\r\n" +
									"Access-Control-Allow-Origin: *\r\n" +
									"Access-Control-Allow-Credentials: true\r\n\r\n" +
									body;
						} catch (NoSuchFileException e) {
							response = "HTTP/1.1 404 Not Found\r\n" +
									"Server: \r\n" +
									"Date: " + sdf.format(currentTime) + "\r\n" +
									"Content-Type: application/json\r\n" +
									"Content-Length: " + body.length() + "\r\n" +
									"Connection: close\r\n" +
									"Access-Control-Allow-Origin: *\r\n" +
									"Access-Control-Allow-Credentials: true\r\n\r\n" +
									body;
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						try {
							File f = new File(tdir);
							String[] pathnames = f.list();
							if (v){
								System.out.println("PathList: ");
							}
							for (String pathname : pathnames) {
								if (v){
									System.out.println(pathname);
								}
								body = body + pathname + "\r\n";
							}

							response = 	"HTTP/1.0 200 ok\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Type: application/json\r\n\r\n"
										+ body;

						} catch (NullPointerException e) {
							response = 	"HTTP/1.0 404 Not Found\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Type: application/json\r\n\r\n";
						}
					}
				}
				// /get?
				else if (splitedRequest[0].equalsIgnoreCase("GET") && splitedRequest[1].substring(0, 5).equalsIgnoreCase("/get?")){
					String arg = splitedRequest[1].substring(5);

					String[] Sarg = arg.split("&");
					String[][] key = new String[Sarg.length][2];
					for (int i = 0; i < Sarg.length; i++){
						String[] tem = Sarg[i].split("=");
						key[i][0] = tem[0];
						key[i][1] = tem[1];
					}
					body = "{\r\n" +
							"\t\"args\": {\r\n";
					for (int i = 0; i < key.length; i++){

						if (i == key.length-1){
							body = body + "\t\t\"" + key[i][0] + "\": \"" + key[i][1] + "\"\r\n" +
									"\t},\r\n";
						} else {
							body = body + "\t\t\"" + key[i][0] + "\": \"" + key[i][1] + "\",\r\n";
						}
					}
					body = body + "\t\"headers\": {\r\n" +
							"\t\t\"Host\": \"" + splitedRequest[4] + "\",\r\n" +
							"\t\t\"User-Agent\": \"" + splitedRequest[6] + "\", \r\n" +
							"\t},\r\n" +
							"\t\"url\": \"http://" + splitedRequest[4] + splitedRequest[1] + "\"\r\n" +
							"}\r\n";

					if (ReturnFile){
						response = 	"HTTP/1.0 200 ok\r\n"
									+ "Data: " + sdf.format(currentTime) + "\r\n"
									+ "Content-Length: " + body.length() + "\r\n"
									+ "Content-Disposition: inline \r\n"
									+ "Content-Disposition: attachment; filename=\"This is working.json\"" + "\r\n"
									+ "Content-Type: application/json\r\n\r\n"
									+ body;
					} else {
						response = 	"HTTP/1.0 200 ok\r\n"
									+ "Data: " + sdf.format(currentTime) + "\r\n"
									+ "Content-Length: " + body.length() + "\r\n"
									+ "Content-Type: application/json\r\n\r\n"
									+ body;
					}
				}
				// GET /
				else {
					String tdir = dir + splitedRequest[1];
					System.out.println(tdir);
					if (splitedRequest[1].contains(".")){
						StringBuilder contentBuilder = new StringBuilder();
						// NEED change path before running
						try (Stream<String> stream = Files.lines(Paths.get(tdir), StandardCharsets.UTF_8)){
							stream.forEach(s -> contentBuilder.append(s).append("\n"));
							body = contentBuilder.toString();
							response = "HTTP/1.0 200 ok\r\n" +
									"Date: " + sdf.format(currentTime) + "\r\n" +
									"Content-Disposition: inline \r\n" +
									"Content-Disposition: attachment; filename=\"" + splitedRequest[1] + "\"\r\n" +
									"Content-Type: application/json\r\n" +
									"Content-Length: " + body.length() + "\r\n" +
									"Connection: close\r\n" +
									"Access-Control-Allow-Origin: *\r\n" +
									"Access-Control-Allow-Credentials: true\r\n\r\n" +
									body;
						} catch (NoSuchFileException e) {
							response = "HTTP/1.1 404 Not Found\r\n" +
									"Server: \r\n" +
									"Date: " + sdf.format(currentTime) + "\r\n" +
									"Content-Type: application/json\r\n" +
									"Content-Length: " + body.length() + "\r\n" +
									"Connection: close\r\n" +
									"Access-Control-Allow-Origin: *\r\n" +
									"Access-Control-Allow-Credentials: true\r\n\r\n" +
									body;
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						try {
							File f = new File(tdir);
							String[] pathnames = f.list();
							if (v){
								System.out.println("PathList: ");
							}
							for (String pathname : pathnames) {
								if (v){
									System.out.println(pathname);
								}
								body = body + pathname + "\r\n";
							}

							response = 	"HTTP/1.0 200 ok\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Type: application/json\r\n\r\n"
										+ body;

						} catch (NullPointerException e) {
							response = 	"HTTP/1.0 404 Not Found\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Type: application/json\r\n\r\n";
						}
					}
				}

				//create POST body
				String dataPart = "\t\"data\": \"{\"";
				body = "{\r\n" +
						"\t\"args\": {},\r\n";
				if (splitedRequest[0].equalsIgnoreCase("POST")){
					String[] Sarg = postData.split("&");
					String[][] key = new String[Sarg.length][2];
					for (int i = 0; i < Sarg.length; i++){
						String[] tem = Sarg[i].split("=");
						key[i][0] = tem[0];
						key[i][1] = tem[1];
					}

					for (int i = 0; i < key.length; i++){

						if (i != key.length-1){
							dataPart = dataPart + "\"" + key[i][0] + "\": " + key[i][1] + ", ";
						} else {
							dataPart = dataPart + "\"" + key[i][0] + "\": " + key[i][1] + "}\",\r\n";
						}
					}
					String filesPart = "\t\"files\": {},\r\n";
					String formPart = "\t\"form\": {},\r\n";
					String headersPart = "\t\"headers\": {\r\n" +
							"\t\t\"Content-Length\": \"" + Integer.parseInt(splitedRequest[indexOfContentLength]) + ", \r\n" +
							"\t\t\"Content-Type\": \"application/json\",\r\n" +
							"\t\t\"Host\": \"localhost\",\r\n" +
							"\t\t\"User-Agent\": " + splitedRequest[indexOfUserAgent] + "\"\r\n" +
							"\t},\r\n";
					String jsonPart = "\t\"json\": {\r\n" + "\t},\r\n";
					String urlPart = "\t\t\"url\": \"http://localhost/post\"\r\n" +
							"}";
					body = body + dataPart + filesPart + formPart + headersPart + jsonPart + urlPart;

					if (ReturnFile){
						response = 	"HTTP/1.0 200 ok\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Disposition: inline \r\n"
										+ "Content-Disposition: attachment; filename=\"This is working.json\"" + "\r\n"
										+ "Content-Type: application/json\r\n\r\n"
										+ body;
					} else {
						response = 	"HTTP/1.0 200 ok\r\n"
										+ "Data: " + sdf.format(currentTime) + "\r\n"
										+ "Content-Length: " + body.length() + "\r\n"
										+ "Content-Type: application/json\r\n\r\n"
										+ body;
					}

				}




				if (v){
					System.out.println("Response sent to client\n" + response);
				}


				outputStream.write(response.getBytes());
				outputStream.flush();
				clientSocket.close();
				server.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

//Supporint Multiple Clients
//One thread per each client connection
//While(true){
	//accept a connection;
	//create a thread to deal with the client;
//}
