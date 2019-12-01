//	Two Phase Commit protocol: Client Application
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

class DBConnector {
    public static Connection getDBConnection(String dsn) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver"); /*sun.jdbc.odbc.JdbcOdbcDriver");*/
        return DriverManager.getConnection("jdbc:oracle:thin:@135.135.4.22:1526", "exam01", "exam01@rdbms");
    }
}
public class Client extends JFrame implements ActionListener {
    JButton b1, b2, b4, b5;
    JPanel p1, p2;
    JTextField t1;
    JLabel l1;
    ServerSocket ss;
    Socket s;
    DataOutputStream output;
    DataInputStream input;
    Connection con;
    Statement stmt;
    String serverMessage = "Prepared";
    int port = 8890;
    String groupIP = "228.5.6.200";
    Client() {
        b1 = new JButton("Prepared");
        b2 = new JButton("NotPrepared");

        b4 = new JButton("Execute");
        b5 = new JButton("Exit");
        t1 = new JTextField("", 35);
        l1 = new JLabel("SQL");
        p1 = new JPanel();
        p2 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(l1);
        p1.add(t1);
        p2.add(b1);
        p2.add(b2);
        p2.add(b4);
        p2.add(b5);
        add(p1);
        add(p2, "South");
        setSize(600, 300);
        setTitle("Two Phase Commit Protocol: Client");

        b1.addActionListener(this);
        b2.addActionListener(this);
        b4.addActionListener(this);
        b5.addActionListener(this);

        setVisible(true);


        setDefaultCloseOperation(EXIT_ON_CLOSE);

        MulticastSocket ms = null;
        InetAddress group;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            s = new Socket("localhost", 8088);
            System.out.println("Client Connected");
            output = new DataOutputStream(s.getOutputStream());
            input = new DataInputStream(s.getInputStream());
            con = DriverManager.getConnection("jdbc:oracle:thin:@135.135.4.22:1521:rdbms", "exam01", "exam01");
            stmt = con.createStatement();
            con.setAutoCommit(false);
            ms = new MulticastSocket(port);
            group = InetAddress.getByName(groupIP);
            ms.joinGroup(group);
            byte[] buffer = new byte[1024];
            output.writeUTF("NotPrepared");
            while (true) {
                DatagramPacket serMsg = new DatagramPacket(buffer, buffer.length);
                ms.receive(serMsg);
                String commitMsg = new String(serMsg.getData()).trim();
                if (commitMsg.equals("commit")) {
                    System.out.println("Received " + commitMsg);
                    con.commit();
                    t1.setText("Transactions Committed");
                    System.out.println("Transactions Committed");
                }
            }
        } catch (ConnectException ce) {
            ce.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            String str = ae.getActionCommand();

            if (str.equals("Execute")) {
                String query = t1.getText();
                stmt.executeUpdate(query);
                t1.setText("Query Executed (NotPrepared)");
                output.writeUTF("NotPrepared");

            }

            if (str.equals("Prepared")) {
                output.writeUTF("Prepared");
                t1.setText(input.readUTF());
            }

            if (str.equals("NotPrepared")) {
                output.writeUTF("NotPrepared");
                t1.setText("NotPrepared");
            }

            if (str.equals("Exit")) {
                output.writeUTF("Prepared");
                stmt.close();
                con.close();
                System.exit(0);
            }
        } catch (Exception e) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = BLUE>" + e.getMessage() + "</FONT></HTML>");
            JOptionPane.showMessageDialog(null, errorFields);
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        Client c = new Client();
    }
}