package com.qonect.protocols.mqtt;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qonect.protocols.mqtt.MqttServiceDelegate.MessageHandler;
import com.qonect.protocols.mqtt.MqttServiceDelegate.MessageReceiver;
import com.qonect.protocols.mqtt.MqttServiceDelegate.StatusHandler;
import com.qonect.protocols.mqtt.MqttServiceDelegate.StatusReceiver;
import com.qonect.protocols.mqtt.service.MqttService;
import com.qonect.protocols.mqtt.service.MqttService.ConnectionStatus;

public class MqttTestActivity extends Activity implements MessageHandler, StatusHandler
{	
	private static final Logger LOG = Logger.getLogger(MqttTestActivity.class);

	private MessageReceiver msgReceiver;
	private StatusReceiver statusReceiver;
	
	private TextView timestampView, topicView, messageView, statusView;
	
	private EditText publishEditView;
	private Button publishButton;
	private Button beaconButton;
	String beacon = null;
	private static String chatMessage = new String();

	@Override  
	public void onCreate(Bundle savedInstanceState)   
	{  
		LOG.debug("onCreate");
		
		super.onCreate(savedInstanceState);		
		
		//Init UI
		setContentView(R.layout.main_test);	
		
		timestampView = (TextView)findViewById(R.id.timestampView);
		topicView = (TextView)findViewById(R.id.topicView);
		messageView = (TextView)findViewById(R.id.messageView);	
		statusView = (TextView)findViewById(R.id.statusView);
		
		publishEditView = (EditText)findViewById(R.id.publishEditView);
		publishButton = (Button)findViewById(R.id.publishButton);
		beaconButton = (Button)findViewById(R.id.beacon);
		publishButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				MqttServiceDelegate.publish(
					MqttTestActivity.this, 
					"the-topic-that-is-now-unused-in-service!", 
					publishEditView.getText().toString().getBytes());
			}
		});
		beaconButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			Intent intent = new Intent(MqttTestActivity.this, MobiusActivity.class);
				startActivityForResult(intent, 1);
			}
		});
		
		//Init Receivers
		bindStatusReceiver();
		bindMessageReceiver();

		//Start service if not started
		MqttServiceDelegate.startService(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {
			// Toast.makeText(getApplicationContext(), "모든게임이끝났습니다.", Toast.LENGTH_LONG).show();
		beacon = data.getStringExtra("beacon");
			Toast.makeText(getApplicationContext(), beacon, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
		}
	}
	@Override  
	protected void onDestroy()   
	{ 
		LOG.debug("onDestroy");
		
		MqttServiceDelegate.stopService(this);
		
		unbindMessageReceiver();
		unbindStatusReceiver();
		
	    super.onDestroy(); 
	}
	
	private void bindMessageReceiver(){
		msgReceiver = new MessageReceiver();
		msgReceiver.registerHandler(this);
		registerReceiver(msgReceiver, 
			new IntentFilter(MqttService.MQTT_MSG_RECEIVED_INTENT));
	}
	
	private void unbindMessageReceiver(){
		if(msgReceiver != null){
			msgReceiver.unregisterHandler(this);
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		}
	}
	
	private void bindStatusReceiver(){
		statusReceiver = new StatusReceiver();
		statusReceiver.registerHandler(this);
		registerReceiver(statusReceiver, 
			new IntentFilter(MqttService.MQTT_STATUS_INTENT));
	}
	
	private void unbindStatusReceiver(){
		if(statusReceiver != null){
			statusReceiver.unregisterHandler(this);
			unregisterReceiver(statusReceiver);
			statusReceiver = null;
		}
	}
	
	private String getCurrentTimestamp(){
		return new Timestamp(new Date().getTime()).toString();  
	}

	@Override
	public void handleMessage(String topic, byte[] payload) {
		String message = new String(payload);
		chatMessage += "\n" + new String(payload);

		LOG.debug("handleMessage: topic="+topic+", message="+message);
				
		//if(timestampView != null)timestampView.setText("When: "+getCurrentTimestamp());
		if(topicView != null)topicView.setText("Topic: "+topic);
		//if(messageView != null)messageView.setText("Message: "+message);
		if(chatMessage != null)messageView.setText("Chat: " + chatMessage);
	}	

	@Override
	public void handleStatus(ConnectionStatus status, String reason) {
		LOG.debug("handleStatus: status="+status+", reason="+reason);
		if(statusView != null)statusView.setText("Status: "+status.toString()+" ("+reason+")");
	}
}
