package edu.njucs.gseclockcontroller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static final int START=5678;
	public static final int SWITCH=1;
	public static final int PAUSE=2;
	public static final int NEXT_PHASE=3;
	public static final int ACK=8765;
	
	
	public static final int SET_TO_PAUSE=10000;
	public static final int SET_TO_RESUME=10001;
	public static final int EXIT=10003;
	public static final int STATE_0=10004;
	public static final int STATE_1=10005;
	public static final int STATE_2=10006;
	public static final int STATE_3=10007;
	public static final int END=10008;
	
	protected String host="192.168.1.4"; // 服务器地址
	private Socket socket; // 用于向服务器发送请求和接收传回信息
	private Socket listenerSocket;
	private DataOutputStream toServer;
	private DataInputStream fromServer;
	MainActivity activity;
	
	public Client(MainActivity activity)
	{
		this.activity=activity;
	}
	
	public void setHost(String host)
	{
		this.host=host;
	}
	
	public void clean()
	{
		if (socket!=null && !socket.isClosed())
		{
			try {
				socket.close();
				socket=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (listenerSocket!=null && !listenerSocket.isClosed())
		{
			try {
				listenerSocket.close();
				listenerSocket=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//尝试与服务器进行连接
    public boolean connect()
    {
        //获得服务器地址
        try {
            //尝试连接
        	socket = new Socket(host, 8000);
        	socket.setSoTimeout(1000);
            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(socket.getInputStream());
            listenerSocket=new Socket(host,8001);
            Listener listener=new Listener(listenerSocket);
            new Thread(listener).start();
        }
        catch (Exception e)
        {
        	return false;
        }
        return true;
    }
    
    public boolean identify()
    {
    	try {
			toServer.writeInt(START);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int message;
		try {
			message = fromServer.readInt();
			if (message==ACK)
	    		return true;
	    	else
	    		return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    public boolean requestSwitch()
    {
    	try {
			toServer.writeInt(SWITCH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int message;
		try {
			message = fromServer.readInt();
			if (message==ACK)
	    		return true;
	    	else
	    		return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    public boolean requestTogglePause()
    {
    	try {
			toServer.writeInt(PAUSE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int message;
		try {
			message = fromServer.readInt();
			if (message==ACK)
	    		return true;
	    	else
	    		return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    public boolean requestNextPhase()
    {
    	try {
			toServer.writeInt(NEXT_PHASE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	int message;
		try {
			message = fromServer.readInt();
			if (message==ACK)
	    		return true;
	    	else
	    		return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    class Listener implements Runnable
    {
    	Socket listenerSocket;
    	DataInputStream listenFromServer;
    	
    	public Listener(Socket listenerSocket)
    	{
    		this.listenerSocket=listenerSocket;
    	}
    	
		public void run() {
			try {
				listenFromServer = new DataInputStream(listenerSocket.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (!listenerSocket.isClosed())
			{
				try {
					int message=listenFromServer.readInt();
					switch (message) {
					case SET_TO_PAUSE:
						activity.setPauseText(true);
						break;
					case SET_TO_RESUME:
						activity.setPauseText(false);
						break;
					case EXIT:
						activity.setState(MainActivity.START);
						break;
					case STATE_0:
					case STATE_1:
					case STATE_2:
					case STATE_3:
						activity.setNextPhaseText(message);
						break;
					case END:
						activity.end();
						break;
					default:
						break;
					}
				} catch (IOException e) {
				}
			}
		}
    	
    }
    
}
