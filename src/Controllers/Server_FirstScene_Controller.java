package Controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import Main_Server.Main_application;
import Main_Server.Utility_server;
import Utilities.Findfile;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.Face;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;


import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.face.FaceRecognizer;


public class Server_FirstScene_Controller
{
	@FXML
	private Button cameraButton;
	@FXML
	private ImageView originalFrame;
	@FXML
	private CheckBox newUser;
	@FXML
	private TextField newUserName;
	@FXML
	private Button newUserNameSubmit;
	@FXML Label Validation_Message = new Label();


	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture;
	// a flag to change the button behavior
	private boolean cameraActive;
	
	// face cascade classifier
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;
	public static boolean IsnewUser = false;
	public int index = 0;
	public int ind = 0;
	public static String newRoll;
	public static boolean IsUserValidated= false;
	public HashMap<Integer, String> names = new HashMap<Integer, String>();
	public int random = (int )(Math.random() * 20 + 3);
	public static boolean TrainFace_Prompt= false;
	public void init() throws IOException {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		
		// disable 'new user' functionality
		this.newUserNameSubmit.setDisable(true);
		this.newUserName.setDisable(true);
		this.cameraButton.setDisable(false);
		// Takes some time thus use only when training set
		// was updated 
		trainModel();
	}
	@FXML
	protected void startCamera()
	{

		this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");

		//originalFrame.setFitWidth(500);

		originalFrame.setPreserveRatio(true);
		
		if (!this.cameraActive)
		{
			// disable 'New user' checkbox
			this.newUser.setDisable(true);
			
			// start the video capture
			this.capture.open(0);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						while(index< 30){
							Image imageToShow = grabFrame();
							originalFrame.setImage(imageToShow);
						}
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.cameraButton.setText("Restart Process"); // equivalent to stopping the camera
			}
			else
			{
				System.err.println("Failed to open the camera connection...");
			}
		}
		else
		{
			Alert EnterRollNo_Error = new Alert(Alert.AlertType.ERROR);
			EnterRollNo_Error.setTitle("Roll Number Missing");
			EnterRollNo_Error.setContentText("YOU ARE DETECTED AS NEW USER");
			EnterRollNo_Error.setContentText("Please Enter Your Roll Number By selecting NewUser Checkbox and proceed");
			EnterRollNo_Error.show();
			this.cameraActive = false;
			this.cameraButton.setText("Train Your face"); // equivalent to starting the camera.
	        TrainFace_Prompt = true;
	        IsnewUser = true;
			this.newUser.setDisable(false);
			try
			{
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{

				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			this.capture.release();
			this.originalFrame.setImage(null);
			index = 0;
			newRoll = "";
		}
	}

	public void setText() throws InterruptedException {
		TimeUnit.SECONDS.sleep(1);
		Validation_Message.setText("                          Your are Allocated with system having IP: "+ Utility_server.SysAllocated_parser);
	}

	@FXML
	public void Reinitiate() throws IOException {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("../Fxml_Files/Server_FirstScene.fxml"));
		BorderPane root = null;
		try {
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		root.setStyle("-fx-background-color: whitesmoke;");
		Scene scene = new Scene(root, 800, 600);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Main_application.stage_storer.setTitle("Face Detection and Tracking");
		Main_application.stage_storer.setScene(scene);
		Main_application.stage_storer.show();

		Main_application.Start_server();
		Server_FirstScene_Controller controller = loader.getController();
		controller.init();
	}

	public void ValidatingUser() throws IOException {

		IsUserValidated = true;
		IsnewUser= false;

		Platform.runLater(()->{

			try
			{
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{

				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			this.capture.release();
			this.originalFrame.setImage(null);
			index = 0;
			newRoll = "";
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("../Fxml_Files/PostVerification_Interface.fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Server_FirstScene_Controller text_controller = loader.getController();
			try {
				text_controller.setText();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("loading new scene");
			Scene scene = new Scene(root);
			Main_application.stage_storer.setScene(scene);
			Main_application.stage_storer.show();
		});

	}

	private Image grabFrame()
	{
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					// face detection
					this.detectAndDisplay(frame);
					
					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image(frame);
				}
				
			}
			catch (Exception e)
			{
				// log the (full) error
				System.err.println("ERROR: " + e);
			}
		}
		
		return imageToShow;
	}
	
	
	private void trainModel () throws IOException {
		// Read the data from the training set
				File root = new File("resources/trainingset/combined/");
									
				
				FilenameFilter imgFilter = new FilenameFilter() {
		            public boolean accept(File dir, String name) {
		                name = name.toLowerCase();
		                return name.endsWith(".png");
		            }
		        };
		        
		        File[] imageFiles = root.listFiles(imgFilter);
		        
		        List<Mat> images = new ArrayList<Mat>();
		        List<Integer> trainingLabels = new ArrayList<>();
		        
		        Mat labels = new Mat(imageFiles.length,1,CvType.CV_32SC1);
		        
		        int counter = 0;
		        
		        for (File image : imageFiles) {
		        	// Parse the training set folder files
		        	Mat img = Imgcodecs.imread(image.getAbsolutePath());
		        	// Change to Grayscale and equalize the histogram
		        	Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
		        	Imgproc.equalizeHist(img, img);
		        	// Extract label from the file name
		        	int label = Integer.parseInt(image.getName().split("\\@")[0]);
		        	// Extract name from the file name and add it to names HashMap
		        	String labnname = image.getName().split("\\_")[0];
		        	String name = labnname.split("\\@")[1];
		        	names.put(label, name);
		        	// Add training set images to images Mat
		        	images.add(img);

		        	labels.put(counter, 0, label);
		        	counter++;
		        }
                                //FaceRecognizer faceRecognizer = Face.createFisherFaceRecognizer(0,1500);
                                FaceRecognizer faceRecognizer = Face.createLBPHFaceRecognizer();
                                //FaceRecognizer faceRecognizer = Face.createEigenFaceRecognizer(0,50);
		        	faceRecognizer.train(images, labels);
		        	faceRecognizer.save("traineddata");

	}

	
	private double[] faceRecognition(Mat currentFace) {
        	int[] predLabel = new int[1];
            double[] confidence = new double[1];
            int result = -1;
            
            FaceRecognizer faceRecognizer = Face.createLBPHFaceRecognizer();
            faceRecognizer.load("traineddata");
        	faceRecognizer.predict(currentFace,predLabel,confidence);
//        	result = faceRecognizer.predict_label(currentFace);
        	result = predLabel[0];
        	return new double[] {result,confidence[0]};
	}
	private void detectAndDisplay(Mat frame) throws IOException, InterruptedException {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);
		if (this.absoluteFaceSize == 0)
		{
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0)
			{
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
				
		// each rectangle in faces is a face: draw them!
		Rect[] facesArray = faces.toArray(); 
		for (int i = 0; i < facesArray.length; i++) {
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

			// Crop the detected faces
			Rect rectCrop = new Rect(facesArray[i].tl(), facesArray[i].br());
			Mat croppedImage = new Mat(frame, rectCrop);
			// Change to gray scale
			Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2GRAY);
			// Equalize histogram
			Imgproc.equalizeHist(croppedImage, croppedImage);
			// Resize the image to a default size
			Mat resizeImage = new Mat();
			Size size = new Size(250,250);
			Imgproc.resize(croppedImage, resizeImage, size);
			if ((newUser.isSelected()&& !newRoll.isEmpty()) || TrainFace_Prompt) {
				if (index<30) {
					Imgcodecs.imwrite("resources/trainingset/combined/"+ random+"@"+newRoll + "_" + (index++) + ".png", resizeImage);
				}
			}
			double[] returnedResults = faceRecognition(resizeImage);
			double prediction = returnedResults[0];
			double confidence = returnedResults[1];
			int label = (int) prediction;
			String name = "";
			if (names.containsKey(label)) {
				name = names.get(label);
			} else {
				name = "Unknown";
			}
			String box_text;
			if(IsnewUser){
				box_text = "Taking Picture "+ index;
				if(index == 30){
					ValidatingUser();
				}
			}
			else{
				System.out.println(confidence);
				if(confidence > 50.00 || name=="Unknown"){
					box_text= "Unknown User.... Train your face";
//					TrainFace_Prompt = true;
				}else{
					if(name.equals("null")){
						box_text = "Unknown User.... Train your face";
					}else{
						box_text="name"+name;
						newRoll = name;
						System.out.println("detected name:"+name);
						TimeUnit.SECONDS.sleep(1);
						ValidatingUser();
					}
				}

			}
			double pos_x = Math.max(facesArray[i].tl().x - 10, 0);
            double pos_y = Math.max(facesArray[i].tl().y - 10, 0);
            Imgproc.putText(frame, box_text, new Point(pos_x, pos_y), 
            		Core.FONT_HERSHEY_DUPLEX, 1.0, new Scalar(0, 255, 0, 2.0));
		}
	}

	@FXML
	protected void newUserNameSubmitted() throws IOException {
		if ((newUserName.getText() != null && !newUserName.getText().isEmpty())) {
			newRoll = newUserName.getText();
			//collectTrainingData(name);
			IsnewUser= true;
			this.cameraButton.setText("TRAIN MY FACE");
			Findfile ff= new Findfile();
			String directory = "resources/trainingset/combined";
			Pattern pattern = Pattern.compile(newRoll+"_1"+".png"+"$");
			boolean file_found = ff.findFile(pattern,new File(directory));
			if(!newRoll.matches(".*-.*-.*-.*")){
				Alert a = new Alert(Alert.AlertType.ERROR);
				a.setTitle("InValid format");
				a.setContentText("Enter the valid format For Roll NO. : Eg:- 1602-XX-XXX-XXX");
				this.cameraButton.setDisable(true);
				a.show();
			}
			else{

				if(file_found){
					Alert a = new Alert(Alert.AlertType.ERROR);
					a.setTitle("RollNumber already Exists");
					a.setContentText("The Roll Number which you entered already exists in the database");
					a.show();
					cameraButton.setText("VERIFY IDENTITY");
					newUserName.clear();
					newUserName.setDisable(true);
					IsnewUser = false;
				}else{
					newUserName.clear();
					Alert a = new Alert(Alert.AlertType.INFORMATION);
					a.setTitle("Train Your Face For Identification");
					a.setContentText("Please Train your face by closing this window and clicking TRAIN MY FACE BUTTON");
					a.show();
					this.cameraButton.setDisable(false);
				}
			}


		}
	}
	
	@FXML
	protected void newUserSelected(Event event) {
		if (this.newUser.isSelected()){
			this.newUserNameSubmit.setDisable(false);
			this.newUserName.setDisable(false);
		} else {
			this.newUserNameSubmit.setDisable(true);
			this.newUserName.setDisable(true);
		}
	}
	

	private Image mat2Image(Mat frame)
	{
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
}
