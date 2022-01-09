package com.example.sendreceivehost;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity2 extends AppCompatActivity {

    EditText e1;
    TextView myIp , disMsg;
    Button back_btn;

    String distributed_msg;

    public static String MY_IP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        e1 = findViewById(R.id.txtIp);
        myIp = findViewById(R.id.myIp);
        disMsg = findViewById(R.id.disMsg);
        back_btn = (Button)findViewById(R.id.back_button);

        //Reading file from the app-specific storage for distribution
        read();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intents are objects of the android.content.Intent type. Your code can send them
                // to the Android system defining the components you are targeting.
                // Intent to start an activity called SecondActivity with the following code:
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);

                // start the activity connect to the specified class
                startActivity(intent);
                finish();

            }
        });


//        try {
//            MY_IP = getLocalIpAddress2();
//            myIp.setText("My IP: " +MY_IP);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }



//    //Receiving from the main activity the file to be distributed.
//        Intent intent = getIntent();
//
//        try {
//            distributed_msg = intent.getStringExtra("Distribution_content");
//        }catch(Exception e){
//            System.out.println(e.toString());
//        }

        Thread myThread = new Thread(new MyServer());
        myThread.start();

//        myThread.interrupt();
    }

    private String getLocalIpAddress2() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }


//    Receiving message from peers
    class MyServer implements Runnable{

        ServerSocket ss;
        Socket mysocket;
        DataInputStream dis;
        String message;
        Handler handler = new Handler();

        @Override
        public void run() {

            try {
                ss = new ServerSocket(9700);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Waiting for peers", Toast.LENGTH_SHORT).show();
                    }
                });
                while (true)
                {
                    mysocket = ss.accept();
                    dis = new DataInputStream(mysocket.getInputStream());
                    //This message stores the information from the peer -- to be stored in app-specific storage.
                    message = dis.readUTF();

                    //The received is displayed for testing purpose
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            disMsg.setText(message);

                        }
                    });
                    //This received msg will be stored in app-specific storage.
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    public void button_click(View v){
        BackgroundTask b = new BackgroundTask();
        b.execute(e1.getText().toString(), distributed_msg);

    }

    //Sending Data to peers --- the peers ip addresses would be fetched from the database
    class BackgroundTask extends AsyncTask<String, Void, String> {
        Socket s;
        DataOutputStream dos;
        String ip, message, error;

        @Override
        protected String doInBackground(String... params) {
            ip = params[0];
            message = params[1];
            try {
                s =new Socket(ip, 9700);
                dos = new DataOutputStream(s.getOutputStream());
                dos.writeUTF(message); //distributed_msg is data to be shared by the primary distributor.
                dos.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
                error = e.toString();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            disMsg.setText("Error: " + error +"Message is : "+message);
        }


    }

    public void read(){
        try {
            FileInputStream fileInputStream = openFileInput("index.html");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            String lines;
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines + "\n");
            }
            StringBuffer response = stringBuffer;
            distributed_msg = response.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}