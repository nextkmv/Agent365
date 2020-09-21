package ru.its_365.agent365.tools

import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.versionName
import android.R.attr.versionCode
import android.content.Context
import android.content.pm.PackageInfo
import ru.its_365.agent365.BuildConfig


class ValueHelper {
    companion object {
        public fun DateToString(date : Date?) : String{
            val sdf = SimpleDateFormat("dd.MM.yyyy")
            if (date == null){
                return sdf.format(Calendar.getInstance().time)
            }else{
                return sdf.format(date)
            }
        }

        public fun getVersionName(ctx: Context) : String{
            val pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0)
            val versionNumber = pinfo.versionCode
            val versionName = pinfo.versionName
            return "Версия ${Const.APPLICATION_VERSION}. Сборка ${BuildConfig.VERSION_NAME} от ${Const.APPLICATION_BUILD}"
        }

    }
}