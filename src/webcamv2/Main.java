/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamv2;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.video.*;
import org.opencv.videoio.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

class FacePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage image;
    int count = 0;
    ArrayList <BufferedImage> image_list = new ArrayList(); //arrayList para ir almacenando los frames de la cam
    // Create a constructor method  

    public FacePanel() {
        super();
    }

    /*  
      * Converts/writes a Mat into a BufferedImage.  
      *   
      * @param matrix Mat of type CV_8UC3 or CV_8UC1  
      * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
     */
    public boolean matToBufferedImage(Mat matrix) {
        MatOfByte mb = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mb);

        try {
            File outputfile = new File("records/image"+count+".jpg");
            count++;
            this.image = ImageIO.read(new ByteArrayInputStream(mb.toArray()));
            image_list.add(this.image);
            ImageIO.write(this.image, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error  
        }
        return true; // Successful  
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image == null) {
            return;
        }
        g.drawImage(this.image, 10, 10, this.image.getWidth(), this.image.getHeight(), null);
    }

}

class FaceDetector {

    private CascadeClassifier face_cascade;
    // Create a constructor method  

    public FaceDetector() {
        // face_cascade=new CascadeClassifier("./cascades/lbpcascade_frontalface_alt.xml");  
        //..didn't have not much luck with the lbp

        face_cascade = new CascadeClassifier("C:\\Users\\plata\\OneDrive\\Documentos\\NetBeansProjects\\FaceDetection\\haarcascades\\haarcascade_frontalface_alt2.xml");
        if (face_cascade.empty()) {
            System.out.println("--(!)Error loading A\n");
            return;
        } else {
            System.out.println("Face classifier loooaaaaaded up");
        }
    }

    public Mat detect(Mat inputframe) {
        Mat mRgba = new Mat();
        Mat mGrey = new Mat();
        MatOfRect faces = new MatOfRect();
        inputframe.copyTo(mRgba);
        inputframe.copyTo(mGrey);
        Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(mGrey, mGrey);
        face_cascade.detectMultiScale(mGrey, faces);
        System.out.println(String.format("Detected %s faces", faces.toArray().length));
        for (Rect rect : faces.toArray()) {
            Point center = new Point(rect.x + rect.width * 0.5, rect.y + rect.height * 0.5);
            //draw a blue eclipse around face
            Scalar scal = new Scalar(0, 0, 255);
            Size s = new Size(rect.width * 0.5, rect.height * 0.5);

            Imgproc.ellipse(mRgba,
                    center,
                    s,
                    0,
                    0,
                    360,
                    scal);
        }
        return mRgba;
    }
}

public class Main {

    public static void main(String arg[]) throws InterruptedException {
        // Load the native library.  
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        JFrame frame = new JFrame("WebCam Capture - Face detection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FaceDetector faceDetector = new FaceDetector();
        FacePanel facePanel = new FacePanel();
        frame.setSize(400, 400); //give the frame some arbitrary size 
        frame.setBackground(Color.BLUE);
        frame.add(facePanel, BorderLayout.CENTER);
        frame.setVisible(true);

        //Open and Read from the video stream  
        Mat webcam_image = new Mat();
        VideoCapture webCam = new VideoCapture(0);

        if (webCam.isOpened()) {
            Thread.sleep(500); /// This one-time delay allows the Webcam to initialize itself  
            while (true) {
                webCam.read(webcam_image);
                if (!webcam_image.empty()) {
                    Thread.sleep(200); /// This delay eases the computational load .. with little performance leakage
                    frame.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
                    //Apply the classifier to the captured image  
                    webcam_image = faceDetector.detect(webcam_image);
                    //Display the image  
                    facePanel.matToBufferedImage(webcam_image);
                    facePanel.repaint();
                    //ImageIO.write(this.image, "jpg", outputfile);
                } else {
                    System.out.println(" --(!) No captured frame from webcam !");
                    break;
                }
            }
        }
        webCam.release(); //release the webcam

    } //end main 

}
