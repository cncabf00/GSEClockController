package edu.njucs.gseclockcontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int START = 0;
	public static final int READY = 1;

	EditText host;
	Button connect;
	Button switchSide;
	Button togglePause;
	Button nextPhase;
	Button disconnect;

	Client client = new Client(this);

	AsyncTask<Void, Void, Boolean> connectTask;

	Handler mHandler=new Handler();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);

		host = (EditText) findViewById(R.id.host);
		connect = (Button) findViewById(R.id.connectBtn);
		switchSide = (Button) findViewById(R.id.switchBtn);
		togglePause = (Button) findViewById(R.id.pauseBtn);
		nextPhase = (Button) findViewById(R.id.nextPhaseBtn);
		disconnect = (Button) findViewById(R.id.disconnectBtn);

		setState(START);

		connect.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (connectTask != null) {
					connectTask.cancel(true);
					connectTask = null;
				}
				connectTask = new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected void onPreExecute() {
						client.setHost(host.getText().toString());
						client.clean();
					}

					@Override
					protected Boolean doInBackground(Void... params) {
						return (client.connect() && client.identify());
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											MainActivity.this.getCurrentFocus()
													.getWindowToken(),
											InputMethodManager.HIDE_NOT_ALWAYS);
							setState(READY);
							Toast.makeText(MainActivity.this,
									R.string.connected, Toast.LENGTH_SHORT)
									.show();
						} else {
							Toast.makeText(MainActivity.this,
									R.string.connectFailed, Toast.LENGTH_SHORT)
									.show();
						}
					}

				}.execute();
			}
		});

		switchSide.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (!client.requestSwitch()) {
					Toast.makeText(MainActivity.this, R.string.connectFailed,
							Toast.LENGTH_SHORT).show();
					setState(START);
				} else {
					Toast.makeText(MainActivity.this, R.string.success,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		togglePause.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (!client.requestTogglePause()) {
					Toast.makeText(MainActivity.this, R.string.connectFailed,
							Toast.LENGTH_SHORT).show();
					setState(START);
				} else {
					Toast.makeText(MainActivity.this, R.string.success,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		nextPhase.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				 new AlertDialog.Builder(MainActivity.this)
                 .setMessage(R.string.nextphaseMessage).setPositiveButton(R.string.ok, new AlertDialog.OnClickListener(){

					public void onClick(DialogInterface dialog, int which) {
						if (!client.requestNextPhase()) {

							Toast.makeText(MainActivity.this, R.string.connectFailed,
									Toast.LENGTH_SHORT).show();
							setState(START);
						} else {
							Toast.makeText(MainActivity.this, R.string.success,
									Toast.LENGTH_SHORT).show();
						}
					}
                	 
                 }).setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener(){

					public void onClick(DialogInterface dialog, int which) {
					}
                	 
                 }).create().show();
			}
		});
		
		disconnect.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				client.clean();
				setState(START);
			}
		});

	}

	public void setState(final int state) {
		mHandler.post(new Runnable() {
			
			public void run() {
				if (state == START) {
					host.setVisibility(View.VISIBLE);
					connect.setVisibility(View.VISIBLE);
					switchSide.setVisibility(View.INVISIBLE);
					togglePause.setVisibility(View.INVISIBLE);
					nextPhase.setVisibility(View.INVISIBLE);
					disconnect.setVisibility(View.INVISIBLE);
				} else if (state == READY) {
					host.setVisibility(View.INVISIBLE);
					connect.setVisibility(View.INVISIBLE);
					switchSide.setVisibility(View.VISIBLE);
					togglePause.setVisibility(View.VISIBLE);
					nextPhase.setVisibility(View.VISIBLE);
					disconnect.setVisibility(View.VISIBLE);
				}
			}
		});
		
	}
	
	public void setPauseText(boolean pause)
	{
		final String text;
		if (pause)
		{
			text=getString(R.string.resume);
		}
		else
		{
			text=getString(R.string.pause);
		}
		mHandler.post(new Runnable() {
			
			public void run() {
				togglePause.setText(text);
			}
		});
	}
	
	public void setNextPhaseText(int phase)
	{
		final String text;
		switch (phase) {
		case Client.STATE_0:
			text=getString(R.string.state0);
			break;
		case Client.STATE_1:
			text=getString(R.string.state1);
			break;
		case Client.STATE_2:
			text=getString(R.string.state2);
			break;
		case Client.STATE_3:
			text=getString(R.string.state3);
			break;
		default:
			text="";
			break;
		}
		mHandler.post(new Runnable() {
			
			public void run() {
				nextPhase.setText(text);
			}
		});
	}
	
	public void end()
	{
		mHandler.post(new Runnable() {
			
			public void run() {
				nextPhase.setVisibility(View.INVISIBLE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
