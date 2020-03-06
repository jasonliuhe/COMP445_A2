import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class serverEx {

	public static void main(String[] args) {

		int port = 8080;

		try {
			ServerSocket server = new ServerSocket(port);

			System.out.println("Server is listening at port " + port);
			Socket clientSocket  = server.accept();
			System.out.println("Server accepted a connection");

			InputStream inputStream = clientSocket.getInputStream();
			OutputStream outputStream = clientSocket.getOutputStream();

			StringBuilder request = new StringBuilder();

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

			System.out.println("\nRequest From client");
			System.out.println(request);

			String body = "{\"Assignment2\" : \"this is working\"}";

			String response = 	"HTTP/1.0 200 ok\r\n"
							 	+ "Content-Length: " + body.length() + "\r\n"
							 	+ "Content-Disposition: inline \r\n"
							 	+ "Content-Disposition: attachment; filename=\"This is working.json\"" + "\r\n"
								+ "Content-Type: application/json\r\n\r\n"
			 					+ body;

			System.out.println("Response sent to client\n" + response);

			outputStream.write(response.getBytes());
			outputStream.flush();
			clientSocket.close();
			server.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//Supporint Multiple Clients
//One thread per each client connection
//While(true){
	//accept a connection;
	//create a thread to deal with the client;
//}
