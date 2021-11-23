package Main_Server;

import Controllers.Server_FirstScene_Controller;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static java.lang.Class.forName;
public class Main_application extends Application
{
	private Socket socket		 = null;
	private DataInputStream input = null;
	private DataOutputStream out	 = null;
    public static String[] parser;
	private ServerSocket server = null;
	private DataInputStream in	 = null;

	public static void Start_server(){

		Thread server_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){

					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(Server_FirstScene_Controller.IsUserValidated) {
						System.out.println("request sent");
						try {
							Utility_server.main(parser);

						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Server_FirstScene_Controller.IsUserValidated = false;
						break;
					}

				}
			}
		});
		server_thread.start();
	}

	// constructor to put ip address and port
	/*public Main(String address, String port, String roll, String name) throws IOException {
		String[] str = new String[] {roll,name,"login","login"};
		int j=0;
		// establish a connection
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
		String line = "";

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
//            input.close();
			out.close();
			in.close();
			socket.close();
		}
		catch(IOException i)
		{
			System.out.println(i);
		}
	}*/

	public static Stage stage_storer;
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
                        
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../Fxml_Files/Server_FirstScene.fxml"));
			BorderPane root = (BorderPane) loader.load();
			Scene scene = new Scene(root);
			primaryStage.setTitle("Face Detection and Tracking");
			primaryStage.setScene(scene);
			//primaryStage.setResizable(false);

			primaryStage.show();
			stage_storer= primaryStage;
			Server_FirstScene_Controller controller = loader.getController();
			controller.init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		// load the native OpenCV library
		parser = args;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Start_server();
		launch(args);
	}
}
