import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This class implements java socket client
 * @author pankaj
 *
 */
public class SocketClient {

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        InetAddress host = InetAddress.getLocalHost();
        System.out.println(InetAddress.getLocalHost());
        System.out.println(host.getHostName() + " " + host.getAddress());
        Socket socket = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;


        while (true) {
            String inputMessage = "";
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the command line here: ");
            inputMessage = scanner.nextLine();

            socket = new Socket(host.getHostName(), 9876);
            System.out.println(host.getHostName());
            output = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("sending request to Socket Server");
            if (inputMessage.equalsIgnoreCase("quit")){
                output.writeObject("exit");
                output.close();
                break;
            }
            output.writeObject(inputMessage);

            //read the server response message
            input = new ObjectInputStream(socket.getInputStream());
            String message = (String) input.readObject();
            System.out.println(message);

            //close resources
            input.close();
            output.close();
            Thread.sleep(100);
        }
    }
}
