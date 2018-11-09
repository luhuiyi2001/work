package com.runde.cap.tcp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpManager {

    public static final int STATE_FROM_SERVER_OK = 0;
    private static String dsName = "47.106.114.236";
    private static int dstPort = 9510;
    private static Socket socket;

    private static TcpManager instance;

    private TcpManager() {
    };

    public static TcpManager getInstance() {
        if (instance == null) {
            synchronized (TcpManager.class) {
                if (instance == null) {
                    instance = new TcpManager();
                }
            }
        }
        return instance;
    }

    /**
     * 连接
     *
     * @return
     */
    public boolean connect(final Handler handler) {
        Log.e("luhuiyi", "connect");
        if (socket == null || socket.isClosed()) {
            Log.e("luhuiyi", "socket == null || socket.isClosed())");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("luhuiyi", "Runnable - run");
                    try {
                        socket = new Socket(dsName, dstPort);
                    } catch (UnknownHostException e) {
                        Log.e("luhuiyi", "UnknownHostException");
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.obj = e.getMessage();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        Log.e("luhuiyi", "IOException");
                        Message msg2 = Message.obtain();
                        msg2.obj = e.getMessage();
                        msg2.what = 2;
                        handler.sendMessage(msg2);
                        throw new RuntimeException("连接错误: " + e.getMessage());
                    }

                    try {
                        // 输入流，为了获取客户端发送的数据
                        InputStream is = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = is.read(buffer)) != -1) {
                            final String result = new String(buffer, 0, len);
                            Log.e("luhuiyi", "result = " + result);
                            Message msg = Message.obtain();
                            msg.obj = result;
                            msg.what = STATE_FROM_SERVER_OK;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        Log.e("luhuiyi", "IOException");
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.obj = e.getMessage();
                        msg.what = 3;
                        handler.sendMessage(msg);
                    }

                }
            }).start();
        }

        return true;
    }

    /**
     * 发送信息
     *
     * @param content
     */
    public void sendMessage(String content) {
        Log.e("luhuiyi", "sendMessage = " + content);
        OutputStream os = null;
        try {
            if (socket != null) {
                os = socket.getOutputStream();
                os.write(content.getBytes());
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("发送失败:" + e.getMessage());
        }
        //此处不能关闭
//      finally {
//          if (os != null) {
//              try {
//                  os.close();
//              } catch (IOException e) {
//                  throw new RuntimeException("未正常关闭输出流:" + e.getMessage());
//              }
//          }
//      }
    }

    /**
     * 关闭连接
     */
    public void disConnect() {
        Log.e("luhuiyi", "disConnect");
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭异常:" + e.getMessage());
            }
            socket = null;
        }
    }
}
