package com.ece420.myapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity
{
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String camFile;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = this.findViewById(R.id.imageView1);
        Button photoButton = this.findViewById(R.id.button1);
        Button analyze = this.findViewById(R.id.button2);

        //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        //StrictMode.setVmPolicy(builder.build());

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        if (!OpenCVLoader.initDebug()) {
        Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
    } else {
        Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
    }
        analyze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(camFile != null)
                {
                    Log.d("Success", "Found image in external storage.\n" + camFile);
                    Mat img = Imgcodecs.imread(camFile);
                    Mat tmp = new Mat(img.width(), img.height(), CvType.CV_8UC1);
                    //Utils.bitmapToMat(img, tmp);
                    Bitmap b = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.RGB_565);


                    Imgproc.cvtColor(img, tmp, Imgproc.COLOR_RGB2GRAY);
                     //Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGBA2GRAY);
                    Utils.matToBitmap(tmp, b);
                    imageView.setImageBitmap(b);


                }
                else
                {
                    Log.d("Fail", "No image saved\n");

                    Resources res = getResources();
                    Bitmap resH = BitmapFactory.decodeResource(res, R.drawable.res_h);
                    //Bitmap resV = BitmapFactory.decodeResource(res, R.drawable.res_v);

                    Bitmap src = BitmapFactory.decodeResource(res, R.drawable.src);
                    Bitmap circuit = BitmapFactory.decodeResource(res, R.drawable.circuit4);

                    Mat img = new Mat(circuit.getWidth(), circuit.getHeight(), 0);
                    Mat rH = new Mat(resH.getWidth(), resH.getHeight(), 0);
                    Mat rV = new Mat(resH.getHeight(), resH.getWidth(),0);
                    Mat source = new Mat(src.getWidth(), src.getHeight(), 0);

                    Utils.bitmapToMat(circuit, img);
                    Utils.bitmapToMat(resH, rH);

                    Core.rotate(rH, rV, Core.ROTATE_90_CLOCKWISE);

                    Utils.bitmapToMat(src, source);


                    Mat gray = new Mat();
                    Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

                    Imgproc.cvtColor(rH, rH, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.cvtColor(rV, rV, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY);

                    Bitmap b = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.RGB_565);


                    int result_H_cols = img.cols() - rH.cols() + 1;
                    int result_H_rows = img.rows() - rH.rows() + 1;

                    int result_V_cols = img.cols() - rV.cols() + 1;
                    int result_V_rows = img.rows() - rV.rows() + 1;

                    int result_src_cols = img.cols() - source.cols() + 1;
                    int result_src_rows = img.rows() - source.rows() + 1;

                    Mat resultH = new Mat(result_H_rows, result_H_cols, CvType.CV_32FC1);
                    Mat resultV = new Mat(result_V_rows, result_V_cols, CvType.CV_32FC1);
                    Mat result_src = new Mat(result_src_rows, result_src_cols, CvType.CV_32FC1);

                    Imgproc.matchTemplate(gray, rH, resultH, Imgproc.TM_CCOEFF_NORMED);
                    Imgproc.matchTemplate(gray, rV, resultV, Imgproc.TM_CCOEFF_NORMED);
                    Imgproc.matchTemplate(gray, source, result_src, Imgproc.TM_CCOEFF_NORMED);


                    Imgproc.threshold(resultH, resultH, 0.1,1, Imgproc.THRESH_TOZERO);
                    Imgproc.threshold(resultV, resultV, 0.1,1, Imgproc.THRESH_TOZERO);
                    Imgproc.threshold(result_src, result_src, 0.1,1, Imgproc.THRESH_TOZERO);



                    MinMaxLocResult mmrV = Core.minMaxLoc(resultV);
                    MinMaxLocResult mmrH = Core.minMaxLoc(resultH);
                    MinMaxLocResult mmrS = Core.minMaxLoc(result_src);

                    double maxH = mmrH.maxVal;
                    double maxV = mmrV.maxVal;
                    double maxS = mmrS.maxVal;

                    /**
                    Point m = mmrV.minLoc;
                    Imgproc.rectangle(img, m, new Point(m.x + rV.cols(), m.y +rV.rows()), new Scalar(0,0,255));
                    Imgproc.rectangle(resultV, m, new Point(m.x + rV.cols(), m.y +rV.rows()), new Scalar(0,0,255),-1);
                    Log.d("Info", ""+ rV.height() + "\n");
                    Log.d("Info", ""+ rH.width() + "\n");
                    Log.d("Info", ""+ mmrH.maxVal + "\n");**/

                    while(true)
                    {
                        mmrV = Core.minMaxLoc(resultV);
                        mmrH = Core.minMaxLoc(resultH);
                        mmrS = Core.minMaxLoc(result_src);

                        double pH = mmrH.maxVal/maxH;
                        double pV = mmrV.maxVal/maxV;
                        double pS = mmrS.maxVal/maxS;


                        if(pH >= 0.995)
                        {
                            Log.d("Info", "H Current Max: " + maxH + "\n");
                            Log.d("Info", "Horizontal Absolute Max: " + mmrH.maxVal + "\n");
                            Log.d("Info", "H Percentage: " + pH + "\n");
                            Point match = mmrH.maxLoc;
                            Imgproc.rectangle(img, match, new Point(match.x + rH.cols(), match.y +rH.rows()), new Scalar(0,255,0));
                            Imgproc.rectangle(resultH, match, new Point(match.x + rH.cols(), match.y +rH.rows()), new Scalar(0,255,0),-1);

                        }

                        if(pV >= 0.9953)
                        {
                            Log.d("Info", "Current Max: " + maxV + "\n");
                            Log.d("Info", "Vertical Absolute Max: " + mmrV.maxVal + "\n");
                            Log.d("Info", "Percentage: " + pV + "\n");


                            Point match = mmrV.maxLoc;
                            Imgproc.rectangle(img, match, new Point(match.x + rV.cols(), match.y +rV.rows()), new Scalar(0,255,0));
                            Imgproc.rectangle(resultV, match, new Point(match.x + rV.cols(), match.y +rV.rows()), new Scalar(0,255,0),-1);

                        }
                        if(pS >= 0.9953)
                        {
                            Log.d("Info", "Source Current Max: " + maxS + "\n");
                            Log.d("Info", "Source Absolute Max: " + mmrS.maxVal + "\n");
                            Log.d("Info", "Source Percentage: " + pS + "\n");


                            Point match = mmrS.maxLoc;
                            Imgproc.rectangle(img, match, new Point(match.x + source.cols(), match.y +source.rows()), new Scalar(0,255,0));
                            Imgproc.rectangle(result_src, match, new Point(match.x + source.cols(), match.y +source.rows()), new Scalar(0,255,0),-1);

                        }
                        if((pV < 0.9953) && (pH < 0.995) && (pS < 0.9953))
                        {
                            break;
                        }
                    }


                    Utils.matToBitmap(img, b);
                    imageView.setImageBitmap(b);

                }

            }
        });
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Date date = new Date();
                //DateFormat df = new SimpleDateFormat("-mm-ss");

                //String newpic = df.format(date) + ".jpg";
                //String outpath = Environment.getExternalStorageDirectory().getPath() + newpic;
                //File outFile = new File(outpath);

                //camFile = outFile.toString();
               // Uri outuri = Uri.fromFile(outFile);

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                   // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                //File outfile = new File(camFile);
                //Uri outuri = Uri.fromFile(outfile);

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

    }
    protected void analyzeCircuit()
    {
        Log.d("Success", "Analyzing " + camFile);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        //Log.d("Success",""+ resultCode);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(photo);

            File file;
            String path = Environment.getExternalStorageDirectory().toString();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("-mm-ss");

            String newpic = df.format(date) + ".jpg";

            file = new File(path, newpic);



            OutputStream stream = null;
            try {
                stream = new FileOutputStream(file);
                photo.compress(Bitmap.CompressFormat.JPEG,100,stream);
                stream.flush();
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            camFile = file.getAbsolutePath();
            Uri savedImageURI = Uri.parse(file.getAbsolutePath());
            imageView.setImageURI(savedImageURI);
            Log.d("Success", "Image saved in external storage.\n" + savedImageURI);
            /**
            if (data != null) {
                image = data.getData();
                imageView.setImageURI(image);
                imageView.setVisibility(View.VISIBLE);
            }
            if (image == null && camFile!= null) {
                image = Uri.fromFile(new File(camFile));
                imageView.setImageURI(image);
                imageView.setVisibility(View.VISIBLE);
            }
            //File file = new File(camFile);
            //if (!file.exists()) {
              //  file.mkdir();
            //} **/
        }
    }
}
