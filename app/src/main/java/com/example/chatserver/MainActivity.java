package com.example.chatserver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    TextView info, infoip, chatHistory;
    TextView feed;

    EditText msgText;
    Button msgSendBtn;

    String message = "";

    ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        chatHistory = (TextView) findViewById(R.id.chatHistory);
        feed = (TextView) findViewById(R.id.feed);

        msgText = findViewById(R.id.msgText);
        msgSendBtn = findViewById(R.id.msgSendBtn);

    //handling IPs
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String WifiIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String hotspotIP = "192.168.43.1";

    //checking if hotspot is ON or OFF
        Boolean hostpostStatus = isHostspotOn(this);
        if(!hostpostStatus) //hotspot is OFF
        {
            feed.setText("Mobile Hotspot is OFF. Please start Hotspot and restart the App");
        }
        else // hotspot is ON //all izz well
        {
            infoip.setText("IP: " + hotspotIP);

        //handling socket
            Thread socketServerThread = new Thread(new SocketServerThread());
            socketServerThread.start();

        //on clicking on send msg button
            msgSendBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                }
            });
        }
    }

//check whether wifi hotspot on or off
    public static boolean isHostspotOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

//inner class to send and receive message
    private class SocketServerThread extends Thread
    {
        static final int SocketServerPORT = 3399;
        int count = 0;x

        @Override
        public void run() {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);

                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        info.setText("Port: " + serverSocket.getLocalPort());
                    }
                });

                while (true)
                {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient = "";

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();

                    count++;
                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n"
                            + "Msg from client: " + messageFromClient + "\n";

                    MainActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run() {
                            chatHistory.setText(message);
                        }
                    });

                    String msgReply = "Hello from Android, you are #" + count;
                    dataOutputStream.writeUTF(msgReply);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                final String errMsg = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        chatHistory.setText(errMsg);
                    }
                });

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//function to get IP Address of the server
    private String getIpAddress()
    {
        String ip = "";

        try
        {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();

                while (enumInetAddress.hasMoreElements())
                {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "IP Address: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            ip = "Something went wrong!";
        }

        return ip;
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
