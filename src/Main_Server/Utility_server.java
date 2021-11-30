package Main_Server;

import Controllers.Server_FirstScene_Controller;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static java.lang.Class.forName;

public class Utility_server {

    //public static final Scanner sc = new Scanner(System.in);

    // initialize socket and input output streams
    private Socket socket		 = null;
    private DataInputStream input = null;
    private DataOutputStream out	 = null;

    public static ServerSocket server = null;
    private DataInputStream in	 = null;
    public static String SysAllocated_parser;
    // constructor to put ip address and port
    public Utility_server(String address, String port, String roll, String name) throws IOException {
        String[] str = new String[] {"1602-18-735-031","rohit","shutdown","login"};
        int j=0;
        try
        {
            socket = new Socket(address, Integer.parseInt(port));
            System.out.println("Connected");
            // takes input from terminal
            input = new DataInputStream(System.in);
//
//            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }

        // string to read message from input
        String line = "null";

        // keep reading until "Over" is input

        while (!line.equals("ok"))
        {
            try
            {
                line = str[j];
                j++;
                //line = input.readLine();
                System.out.println("Sent : "+line);
                out.writeUTF(line);
                line = in.readUTF();
                System.out.println("Received : "+line);

            }
            catch(IOException i)            {

                System.out.println(i);
            }
        }
        //out.writeUTF(line);
        // close the connection
        try
        {
            input.close();
            out.close();
            in.close();
            socket.close();
//            TimeUnit.SECONDS.sleep(6);
//            server.close();
//            System.out.println(Thread.activeCount());
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException
    {

            System.out.println("Server Initialized.............");

            try{
                //System.in is a standard input stream
                //System.out.print("Enter Your Roll Number : ");
                //long then = System.currentTimeMillis();
                String roll= Server_FirstScene_Controller.newRoll;
                //  long now = System.currentTimeMillis();//reads string
                //System.out.print("Enter Your Name : ");
                String name= "vishnu";              //reads string
                //sc.close();
                //System.out.print("You have entered: "+roll);
                forName("com.mysql.cj.jdbc.Driver");
                Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/lab","root","Lab@Authentication123");
                Statement stmt = con.createStatement();
                ResultSet rs=stmt.executeQuery("select * from clients where status=1");
                Vector<String> vec = new Vector<String>();
                Vector<String> ids = new Vector<String>();
                Vector<String> ports = new Vector<String>();
                System.out.println(" connection established");

                if (rs.next())
                {

                    do {
                        vec.add(rs.getString(2));
                        ids.add(rs.getString(1));
                        ports.add(rs.getString(3));
                        // System.out.println(rs.getInt(1)+"  "+rs.getString(2));
                    }while(rs.next());
                    System.out.println("Available Systems : "+vec);
                    SysAllocated_parser = vec.get(0);
                    System.out.println("Allocating System : "+SysAllocated_parser);
                    System.out.println(ids.get(0));
                    System.out.println(ports.get(0));
                    Utility_server client = new Utility_server(vec.get(0),ports.get(0),roll,name);

                    TimeUnit.SECONDS.sleep(2);
                    int rs1=stmt.executeUpdate("INSERT INTO `logs`(roll_no,name,sys_allocated) VALUES ('"+roll+"','"+name+"','"+vec.get(0)+"')");
                    System.out.println("Successfully allocated System "+ids.get(0)+" to user "+roll);

                }
                else{
                    System.out.println("Systems Not available....Please come after sometime....");
                }
                con.close();
            }catch(Exception e){ System.out.println(e);
                System.exit(0);}

    }
}
