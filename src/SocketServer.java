import com.sun.xml.internal.bind.v2.TODO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * This class implements java Socket server
 * @author pankaj
 *
 */
public class SocketServer {

    //static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int port = 9876;

    public static void main(String args[]) throws IOException, ClassNotFoundException{
        try {
            //create the socket server object
            server = new ServerSocket(port);
            //keep listens indefinitely until receives 'exit' call or program terminates
            while (true) {
                System.out.println("Waiting for the client request");
                //creating socket and waiting for client connection
                Socket socket = server.accept();
                //read from socket to ObjectInputStream object
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //convert ObjectInputStream object to String
                String message = (String) ois.readObject();
                System.out.println("Message Received: " + message);

                //create ObjectOutputStream object
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //write object to Socket

                //Todo Create Response
                oos.writeObject(Response(message));
                //close resources
                ois.close();
                oos.close();
                socket.close();
                //terminate the server if client sends exit request
                if (message.equalsIgnoreCase("exit")) break;
            }
            System.out.println("Shutting down Socket server!!");
            //close the ServerSocket object
            server.close();
        } catch (Exception e){

        }
    }

    private static String Response(String request){
        String ResponseMessage = "";

        //splited by space
        String[] splited = request.split("\\s+");
        for (int i = 0; i < splited.length; i++) {
            System.out.println("splited[" + i + "]: " + splited[i]);
        }

        //GET
        if (splited[0].equalsIgnoreCase("get")){
            // url (tested)
            if (splited[1].charAt(0) == '\''){
                try {
                    String ourl = splited[1].substring(1, splited[1].length()-1);
                    URL url = new URL(ourl);

                    String[] argsSplited = url.getQuery().split("&");
                    String args = "";
                    for (int i = 0; i < argsSplited.length; i++) {
                        String name = "";
                        String para = "";
                        String[] nameParaSplited = argsSplited[i].split("=");
                        name = nameParaSplited[0];
                        para = nameParaSplited[1];
                        args = args + "\t\t\"" + name + "\"" + " : " + "\"" + para + "\",\n\r";

                        System.out.println(args);
                    }
                    ResponseMessage = "{\n\r" +
                            "\t\"args\": {\n\r" +
                            args + "\t},\n\r" +
                            "\t\"headers\": {\n\r" +
                            "\t\t\"Host\": \"" + url.getHost() + "\",\n\r" +
                            "\t\t\"User-Agent\": \"" + url.getUserInfo() + "\",\n\r" +
                            "\t},\n\r" +
                            "\t\"url\": " + "\"" + ourl + "\"\n\r" +
                            "}";


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
            // -v (tested)
            else if (splited[1].equalsIgnoreCase("-v")){

            }
            // / (tested)
            else if (splited[1].equalsIgnoreCase("/")){

            }
            // /foo (tested)
            else if (splited[1].charAt(0) == '/'){

            }

            else {

            }
        }
        //POST
        else if (splited[0].equalsIgnoreCase("post")){

        }

        else {

        }

        return ResponseMessage;
    }

}
