package com.viatom.usbhid

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log


class LittlePuReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        UsbManager.ACTION_USB_DEVICE_ATTACHED
        if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
            val device=intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            if(device!=null){
//                Log.e("TAG","拔出设备的ProductId值为："+device.getProductId());
//                Log.e("TAG","拔出设备的VendorId值为："+device.getVendorId());
                Log.e("TAG","拔出设备的VendorId值为："+device.productName);
            }

        }else if(UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action){
            val device=intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            if(device!=null){
//                Log.e("TAG","插入设备的ProductId值为："+device.getProductId());
//                Log.e("TAG","插入设备的VendorId值为："+device.getVendorId());
                Log.e("TAG","拔出设备的VendorId值为："+device.productName);
            }
        }


    }
}