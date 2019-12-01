//	Two Phase Commit protocol: Server Application
import java.io.*;
import java.net.*;

public class Server {

    public static ServerSocket ss;

    public Server() {}
    public static void main(String args[]) throws Exception {
        ss = new ServerSocket(8088);
        System.out.println("Two Phase Commit Protocol: Server");
        new Clients(2);
        while (true) {
            System.out.println("Server waiting: ");
            Socket s = ss.accept();
            new Coordinator(s);
        }
    }
}

class Clients {
    static int n;
    static String[] status;

    Clients(int num) {
        n = num;
        status = new String[n];
        for (int j = 0; j < n; j++) {
            status[j] = new String("");
        }
    }
}

class Coordinator implements Runnable {

    public static int i = -1;
    int flag = 1;
    Socket s;
    Thread t;
    MulticastSocket ms = null;
    InetAddress group;
    DataInputStream input;
    DataOutputStream output;
    int port = 8890;
    String groupIP = "228.5.6.200";
    Coordinator(Socket c) {
        s = c;
        try {
            input = new DataInputStream(s.getInputStream());
            output = new DataOutputStream(s.getOutputStream());
            ms = new MulticastSocket(port);
            group = InetAddress.getByName(groupIP);
            ms.joinGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
        t = new Thread(this);
        t.start();
        i++;
    }

    public void run() {
        int index = i;
        String clientSattus;
        try {
            while (true) {
                clientSattus = input.readUTF();
                if (clientSattus.equalsIgnoreCase("Prepared")) {
                    output.writeUTF("Wait for others to prepare");
                }
                Clients.status[index] = new String(clientSattus);
                for (int k = 0; k < Clients.n; k++) {
                    System.out.println("Client " + (k + 1) + " " + Clients.status[k]);
                    if (Clients.status[k].equalsIgnoreCase("Prepared")) {
                        continue;
                    } else {
                        flag = 0;
                    }
                }
                if (flag == 1) {
                    byte[] msg = new String("commit").getBytes();
                    DatagramPacket msgpack = new DatagramPacket(msg, msg.length, group, port);
                    ms.send(msgpack);
                    System.out.println("Commit message sent to clients: " + new String(msg));
                }
                flag = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}