package ru.its_365.agent365.tools

import android.app.Activity
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.hardware.usb.UsbDevice.getDeviceId
import android.content.Context.TELEPHONY_SERVICE
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

class HardWare{
    companion object {
        fun getHardWareId(ctx:Activity) : String{
            var id : String = ""
            var IMEINumber = ""
            try {
                if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val telephonyMgr = ctx.getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        IMEINumber = telephonyMgr!!.imei
                    } else {
                        IMEINumber = telephonyMgr!!.deviceId
                    }
                    if (telephonyMgr == null) IMEINumber = ""
                }
            }catch (t : Throwable){
                IMEINumber = ""
            }



            id = IMEINumber
            return id
        }
    }
}