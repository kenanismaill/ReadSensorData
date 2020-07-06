package com.example.readsensordata;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {
    String text1;
    String checkUserID;
    TextView tv;
    Button startbtn,stopbtn;
    EditText userid;
    FileOutputStream fout;
    OutputStreamWriter oswriter;
    BufferedWriter bwriter;
    PrintWriter Pwriter;
    float[] acceleration = new float[3];
    float[] rotationRate = new float[3];
    float[]gravity=new float[3];


    private SensorManager mysensormanger;
    private  long currenttime;
    private long starttime;

    boolean startflag=false;
    boolean stopflag=false;
    boolean isfirstset=true;
    TextView textView;
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startbtn=(Button)findViewById(R.id.button);
        userid=findViewById(R.id.editText);
        textView=findViewById(R.id.tv);


        startbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File myfile=new File(MainActivity.this.getFilesDir(),"new data");
                    if (!myfile.exists())
                    {
                        myfile.mkdir();
                    }
                    try
                    {
                        new CountDownTimer(120000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
                                //here you can have your logic to set text to edittext
                            }
                            public void onFinish() {
                                textView.setText("done!");
                            }
                        }.start();
                        checkUserID=userid.getText().toString();

                        File file=new File(myfile,"Sensors data");
                        fout=new FileOutputStream(file);
                        oswriter=new OutputStreamWriter(fout);

                        bwriter=new BufferedWriter(oswriter);
                        Pwriter= new PrintWriter(bwriter);

                        Toast.makeText(getBaseContext(),"start recording data",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        startflag=true;
                    }
                }
            });

        stopbtn=(Button)findViewById(R.id.button2);
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopflag=true;
                    Toast.makeText(getBaseContext(),"data record finished",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.HAR_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mysensormanger=(SensorManager)getSystemService(SENSOR_SERVICE);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
//        final float alpha = (float) 0.8;
//
//        gravity[0] = (alpha * gravity[0] + (1 - alpha) * event.values[0]);
//        gravity[1] = (alpha * gravity[1] + (1 - alpha) * event.values[1]);
//        gravity[2] = (alpha * gravity[2] + (1 - alpha) * event.values[2]);


        if (startflag)
        {
            if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
                acceleration[0]=event.values[0] ;
                acceleration[1]=event.values[1];
                acceleration[2]=event.values[2];
            }

            if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE)
            {
                rotationRate[0]=event.values[0];
                rotationRate[1]=event.values[1];
                rotationRate[2]=event.values[2];
            }
            if (isfirstset)
            {
                starttime=System.currentTimeMillis();
                isfirstset=false;

            }
            currenttime=System.currentTimeMillis();
            for (int i=0;i<1;i++)
            {
                if (!stopflag)
                {
                    savedata();
                }
                else
                {
                    try {
                        oswriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        fout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private void savedata(){
        long time= System.currentTimeMillis();
        String X=textView.getText().toString();
        if (X=="done!") {
            try {
                Toast.makeText(this,"Please Click on Stop Button To Save Data",Toast.LENGTH_LONG).show();
                oswriter.append(X);
                stopflag=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
//                currenttime - starttime + "," +
                        oswriter.append(checkUserID+","+text1+","+time+","+acceleration[0]+ "," +acceleration[1]+ "," + acceleration[2]
                        + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2]+";"+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mysensormanger.registerListener(this,mysensormanger.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),SensorManager.SENSOR_DELAY_GAME);
        mysensormanger.registerListener(this,mysensormanger.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mysensormanger.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        text1=parent.getItemAtPosition(position).toString();
        Toast.makeText(this,text1,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
