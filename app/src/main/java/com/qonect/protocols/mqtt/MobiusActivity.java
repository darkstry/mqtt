package com.qonect.protocols.mqtt;

/**
 * Created by 박종현 on 2016-09-11.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MobiusActivity extends Activity implements Button.OnClickListener {

    private Button btnRetrieve;
    private ToggleButton btnControl;

    private TextView textViewData;
    private Button back;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobius);

        btnRetrieve = (Button)findViewById(R.id.btnRetrieve);
        btnControl = (ToggleButton)findViewById(R.id.btnControl);
        back = (Button)findViewById(R.id.back);
        textViewData = (TextView)findViewById(R.id.textViewData);

        btnRetrieve.setOnClickListener(this);
        btnControl.setOnClickListener(this);
        back.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRetrieve : {

                RetrieveRequest req = new RetrieveRequest();
                textViewData.setText("");

                req.setReceiver(new IReceived() {
                    @Override
                    public void getResponseBody(final String msg) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewData.setText("************** Data retrieve *************\r\n\r\n"+msg);
                            }
                        });
                    }
                });

                req.start();

                break;
            }
            case R.id.btnControl : {

                if (((ToggleButton) v).isChecked())
                {
                    ControlRequest req = new ControlRequest("1");
                    req.setReceiver(new IReceived() {
                        @Override
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewData.setText("************** Control(on) *************\r\n\r\n"+msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                else
                {
                    ControlRequest req = new ControlRequest("0");
                    req.setReceiver(new IReceived() {
                        @Override
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewData.setText("************** Control(off) **************\r\n\r\n"+msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                break;
            }
            case R.id.back : {
                try {
                    String changingString = "";
                    String changedString="";
                    int start;

                    //changingString = textViewData.getText().toString();
                    changingString = textViewData.getText().toString();

                    String splitedString[] = changingString.split("</content>");

                    int i = -1;
                    while(true){
                        i++;
                        try{
                            start = splitedString[i].indexOf("<content>");
                            start = start+9;

                            if(!splitedString[i].substring(splitedString[i].length()-9,splitedString[i].length()-1).equals("Instance")){
                            changedString += splitedString[i].substring(start, splitedString[i].length());
                        //    Toast.makeText(getApplicationContext(), splitedString[i].substring(splitedString[i].length()-9,splitedString[i].length()-1),Toast.LENGTH_LONG).show();
                             }
                        }
                        catch(Exception e){break;}

                    }
/*
                   start = changingString.indexOf("<content>");
                   start = start+9;

                   end = changingString.indexOf("</content>");
                    changedString = changingString.substring(start, end);*/
                    Intent message = new Intent();
                    message.putExtra("beacon", changedString);
                    setResult(RESULT_OK, message);
                    finish();
                }catch(Exception e){}
            }
        }
    }

    //mobius root url setting
    public class MobiusConfig{
        //public final static String MOBIUS_ROOT_URL = "http://52.78.68.226:7579/mobius-yt";
        public final static String MOBIUS_ROOT_URL = "http://52.78.68.226:7579/mobius-yt";
    }

    //response callback
    public interface IReceived{
        void getResponseBody(String msg);
    }

    //get sensing data from container of the mobius
    class RetrieveRequest extends Thread {

        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());

        private IReceived receiver;

        //private String ae_name = "AE_araha"; //change to your ae name
        //private String container_name = "container_araha"; //change to your sensing data container name

        private String ae_name = "Sajouiot01"; //change to your ae name
        private String container_name = "beacon01"; //change to your sensing data container name

        public RetrieveRequest(String aeName, String containerName){
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        public RetrieveRequest(){

        }

        public void setReceiver(IReceived hanlder){
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try{
                StringBuilder sb = new StringBuilder();
                sb.append(MobiusConfig.MOBIUS_ROOT_URL).append("/")
                        .append(ae_name).append("/")
                        .append(container_name).append("/")
                        .append("latest");

                URL mUrl = new URL(sb.toString());

                HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "SOrigin");
                conn.setRequestProperty("nmtype", "long");

                conn.connect();

                String strResp = "";

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String strLine;
                while((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if(receiver != null){
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            }catch(Exception exp){
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

    //send command to control container of the mobius
    class ControlRequest extends Thread{
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());

        private IReceived receiver;

        //private String ae_name = "AE_araha"; //change to your ae name
        //private String container_name = "container_araha"; //change to control container name

        private String ae_name = "Sajouiot01"; //change to your ae nS
        private String container_name = "beacon01"; //change to control container name

        private ContentInstanceObject instance;

        public ControlRequest(String comm){
            instance = new ContentInstanceObject();
            instance.setAeName(ae_name);
            instance.setContainerName(container_name);
            instance.setContent(comm);
        }

        public ControlRequest(String aeName, String containerName, String comm){

            this.ae_name = aeName;
            this.container_name = containerName;

            instance = new ContentInstanceObject();
            instance.setAeName(ae_name);
            instance.setContainerName(container_name);
            instance.setContent(comm);
        }

        public void setReceiver(IReceived hanlder){
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try{
                StringBuilder sb = new StringBuilder();
                sb.append(MobiusConfig.MOBIUS_ROOT_URL).append("/");
                sb.append(ae_name).append("/");
                sb.append(container_name);

                URL mUrl = new URL(sb.toString());

                HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "SOrigin");

                String reqContent = instance.makeBodyXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine;
                while((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if(receiver != null){
                    receiver.getResponseBody(resp);
                }

                conn.disconnect();

            }catch(Exception exp){
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    //content instance data object(for sending command data)
    class ContentInstanceObject {
        private String aeName = "";
        private String containerName = "";
        private String content = "";

        public void setAeName(String value){
            this.aeName = value;
        }

        public void setContainerName(String value){
            this.containerName = value;
        }

        public String getAeName(){
            return this.aeName;
        }

        public String getContainerName(){
            return this.containerName;
        }

        public void setContent(String value){
            this.content = value;
        }

        public String getContent(){
            return this.content;
        }

        public String makeBodyXML(){
            String xml = "";

            xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            xml += "<m2m:cin ";
            xml += "xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" ";
            xml += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
            xml += "<cnf>text</cnf>";
            xml += "<con>" + content + "</con>";
            xml += "</m2m:cin>";

            return xml;
        }
    }
}