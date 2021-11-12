package com.viatom.usbhid;

import static java.lang.Thread.sleep;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;


/**
 * Created by Administrator on 2016/9/27 0027.
 */
public class Service_usb extends Service {
    private int ProductID = 0;
    private int Permission = 0;
    private boolean STATE = true;
    private final int VendorID = 1155;
    private final int ProductID_Remote = 40976;       //数传PID
    private final int ProductID_Airplane = 40964;     //飞控PID
    /**
     * ===========USB HID===============
     **/
    private UsbManager usbManager;                      //usb管理类
    private UsbDevice usbDevice;                        //usb设备
    private UsbInterface usbInterface;                  //usb接口
    private UsbEndpoint usbEndpoint_IN, usbEndpoint_OUT; //节点
    private UsbDeviceConnection usbDeviceConnection;    //连接
    private PendingIntent intent;                       //意图
    private static final String ACTION_USB_PERMISSION =
            "com..years.USB_PERMISSION";//定义常量
    IntentFilter intentFilter;
    private byte[] buffer = new byte[64];             //接收缓冲

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("=======Service_usb OnCreate==========" + STATE);
        intent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        intentFilter = new IntentFilter(ACTION_USB_PERMISSION);


    }

    private void addData(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            data_to_send[i] = bytes[i];
        }
    }


    public void scanUSBDevice() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Toast.makeText(this, "手机不支持OTG", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (!(deviceList.isEmpty())) //获取的device列表
        {
            for (UsbDevice device : deviceList.values()) {
                usbDevice = device;                       //获取到设备  一般只有一个
                System.out.println("========DeviceName======" + usbDevice.getDeviceName());
                if (usbDevice.getProductId() == ProductID_Remote && usbDevice.getVendorId() == VendorID)           //判断usb设备PID VID 此处用来和匿名数传和飞控对比
                    ProductID = 1;                   //与数传pid vid吻合  判断usb设备为数传
                else if (usbDevice.getProductId() == ProductID_Airplane && usbDevice.getVendorId() == VendorID)
                    ProductID = 2;                   //与飞控pid vid吻合   判断usb设备为飞控
                else ProductID = 3;                  //均不匹配  判断usb设备为第三方设备

                System.out.println("======拥有权限=======");
                Permission = 1;
                int i = usbDevice.getInterfaceCount();//获取接口数量
                System.out.println("fic=======" + i);
                //特殊判断   匿名新版数传增加了虚拟串口  多了两个接口  一般都是一个
                if (i < 2) {
                    usbInterface = usbDevice.getInterface(0);
                    System.out.println("=======UsbInterfce0=======" + usbInterface.getName());
                }

                //定义两个节点 1发送 0 接收
                usbEndpoint_IN = usbInterface.getEndpoint(0);
                usbEndpoint_OUT = usbInterface.getEndpoint(0);
                //连接的类 得到实例
                usbDeviceConnection = usbManager.openDevice(usbDevice);
                usbDeviceConnection.claimInterface(usbInterface, true);
                usb_receiveData();
                System.out.println("=======进入接收======" + STATE);

            }
        } else {
            ProductID = 4;
            Toast.makeText(this, "未找到设备", Toast.LENGTH_SHORT).show();
            System.out.println("===============未找到设备===============" + ProductID);
        }
    }

    public byte[] data_to_send = new byte[64];      //发送数据缓存

    public void usb_sendData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    byte[] bytes = {(byte) 0x06, (byte) 0xAA, (byte) 0x55, (byte) 0x40, (byte) 0x02, (byte) 0x01, (byte) 0x29};
                    sendCommand(usbDeviceConnection, bytes);
//                    usbDeviceConnection.bulkTransfer( usbEndpoint_IN,bytes,bytes.length,10);

                }

            }
        }).start();
    }

    private void sendCommand(UsbDeviceConnection connection, byte[] buf) {
        connection.controlTransfer(0x21, 0x09, 0x200, 1, buf, buf.length, 0);
    }

    private void usb_receiveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (STATE) {
                    try {
                        sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int maxPacketSize = usbEndpoint_IN.getMaxPacketSize();
                    ByteBuffer bBuffer = ByteBuffer.allocate(maxPacketSize);
                    UsbRequest request = new UsbRequest();
                    request.initialize(usbDeviceConnection, usbEndpoint_IN);
                    request.queue(bBuffer, maxPacketSize);
                    if (usbDeviceConnection.requestWait() == request) {
                        Log.e("sfda","sdfsdf");
                        byte[] b = bBuffer.array();
                        MainActivity.Companion.getMm().postValue(byte2hex(b));
                    }

                }
            }
        }).start();

        usb_sendData();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        scanUSBDevice();
        System.out.println("=======Service_usb OnStart==========");
        super.onStart(intent, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("===========Service OnBind=============" + STATE);
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        STATE = false;
        System.out.println("===========UNBIND ============" + STATE);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        STATE = false;

        System.out.println("=======Service_usb Destory==========" + STATE);
    }

    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
}
