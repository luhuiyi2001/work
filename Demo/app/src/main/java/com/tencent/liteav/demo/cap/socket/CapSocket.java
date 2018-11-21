package com.tencent.liteav.demo.cap.socket;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.listener.OnReceiveMsgListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CapSocket {

	private static final String TAG = CapSocket.class.getSimpleName();

	private OnReceiveMsgListener mListener;
	
	private Socket mSocket;
	private BufferedWriter mSendBW;
	private InputStream mReadIS;
	
	public void setOnReceiveMsgListener(OnReceiveMsgListener listener) {
		mListener = listener;
	}

	public synchronized boolean connect() {
		CLog.d(TAG, "connect");
		if (mSocket != null) {
			return false;
		}
		try {
			/* * * * * * * * * * 客户端 Socket 通过构造方法连接服务器 * * * * * * * * * */
			// 客户端 Socket 可以通过指定 IP 地址或域名两种方式来连接服务器端,实际最终都是通过 IP 地址来连接服务器
			// 新建一个Socket，指定其IP地址及端口号
			mSocket = new Socket(CapConfig.SERVER_IP, CapConfig.SERVER_PORT);
			// 客户端socket在接收数据时，有两种超时：1. 连接服务器超时，即连接超时；2. 连接服务器成功后，接收服务器数据超时，即接收超时
			// 设置 socket 读取数据流的超时时间
			mSocket.setSoTimeout(0);
			// 发送数据包，默认为 false，即客户端发送数据采用 Eagle 算法；
			// 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
			mSocket.setTcpNoDelay(true);
			// 设置客户端 socket 关闭时，close() 方法起作用时延迟 30 秒关闭，如果 30 秒内尽量将未发送的数据包发送出去
			mSocket.setSoLinger(true, 30);
			// 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
			mSocket.setSendBufferSize(4096);
			// 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
			mSocket.setReceiveBufferSize(4096);
			// 作用：每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
			// 防止服务器端无效时，客户端长时间处于连接状态
			mSocket.setKeepAlive(true);
			// 代表可以立即向服务器端发送单字节数据
			mSocket.setOOBInline(true);
			// 数据不经过输出缓冲区，立即发送
			mSocket.sendUrgentData(0x44);// "D"
			// 客户端向服务器端发送数据，获取客户端向服务器端输出流
			OutputStream osSend = mSocket.getOutputStream();
			OutputStreamWriter osWrite = new OutputStreamWriter(osSend);
			mSendBW = new BufferedWriter(osWrite);
			
			// 客户端接收服务器端的响应，读取服务器端向客户端的输入流
			mReadIS = mSocket.getInputStream();
			
			if (mSocket.isConnected()) {
				return true;
			} else {
				close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			CLog.e(TAG, "connect = " + e.getMessage());
			close();
		}
		return false;
	}
	
	public void send(String msg) {
		if (mSocket == null) {
			CLog.d(TAG, "send - mSocket == null");
			return;
		}
		CLog.d(TAG, "send - isConnected = " + mSocket.isConnected() + ", isClosed = " + mSocket.isClosed());
		try {
			if (mSocket.isConnected() && !mSocket.isClosed()) {
				// 向服务器端写数据，写入一个缓冲区
				// 注：此处字符串最后必须包含“\r\n\r\n”，告诉服务器HTTP头已经结束，可以处理数据，否则会造成下面的读取数据出现阻塞
				// 在write() 方法中可以定义规则，与后台匹配来识别相应的功能，例如登录Login()
				// 方法，可以写为write("Login|LeoZheng,0603 \r\n\r\n"),供后台识别;
				mSendBW.write(msg + "\r\n");
				// 发送缓冲区中数据 - 前面说调用 flush() 无效，可能是调用的方法不对吧！
				mSendBW.flush();
				CLog.i(TAG, "send = " + msg);
			} else {
				// 关闭网络
				close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			CLog.e(TAG, "send = " + e.getMessage());
			close();
		}
	}

	public void receive() {
		if (mSocket == null) {
			CLog.d(TAG, "receive - mSocket == null");
			return;
		}
		CLog.d(TAG, "receive - isConnected = " + mSocket.isConnected() + ", isClosed = " + mSocket.isClosed());
		/* * * * * * * * * * Socket 客户端读取服务器端响应数据 * * * * * * * * * */
		try {
			// serverSocket.isConnected 代表是否连接成功过
			// 判断 Socket 是否处于连接状态
			if (mSocket.isConnected() && !mSocket.isClosed()) {
				// 缓冲区
//				byte[] buffer = new byte[mReadIS.available()];
				// 读取缓冲区
//				mReadIS.read(buffer);
				//读取服务器返回的消息
				// 转换为字符串
//				String responseInfo = new String(buffer);
				
	            BufferedReader br = new BufferedReader(new InputStreamReader(mReadIS));
	            String msg = br.readLine();
				// 日志中输出
				CLog.i(TAG, "receive = " + msg);
				if (mListener != null) {
					mListener.onReceived(msg);
				}
			} else {
				// 关闭网络
				close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			CLog.e(TAG, "receive = " + e.getMessage());
			close();
		}
	}
	
	public void close() {
		CLog.d(TAG, "close");
		try {
			if (mReadIS != null) {
				mReadIS.close();
			}
			if (mSendBW != null) {
				mSendBW.close();
			}
			if (mSocket != null) {
				mSocket.close();
				mSocket = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CLog.e(TAG, "close = " + e.getMessage());
		}
	}
	
	public boolean isConnected() {
		if (mSocket != null && mSocket.isConnected()) {
			return true;
		}
		return false;
	}
	
	public boolean isClosed() {
		if (mSocket == null || mSocket.isClosed()) {
			return true;
		}
		return false;
	}
	
	public boolean isDisconnected() {
		if (mSocket != null && (!mSocket.isConnected() || mSocket.isClosed())) {
			return true;
		}
		return false;
	}

}
