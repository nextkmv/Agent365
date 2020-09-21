package ru.its_365.agent365.tools

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.Preference
import android.preference.PreferenceManager


class PreferenceStore {
    companion object {

        public fun getString(ctx:Context,key:String, default: String? = ""):String{
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            return prs.getString(key,default)
        }

        public fun setString(ctx:Context,key:String, value: String){
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            val editor = prs.edit()
            editor.putString(key,value)
            editor.commit()
        }

        public fun getInt(ctx:Context,key:String, default:Int = 0):Int{
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            return prs.getInt(key,default)
        }

        public fun setInt(ctx:Context,key:String, value: Int){
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            val editor = prs.edit()
            editor.putInt(key,value)
            editor.commit()
        }

        public fun getBoolean(ctx:Context,key: String, default: Boolean = false):Boolean{
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            return prs.getBoolean(key,default)
        }

        public fun setBoolean(ctx:Context,key:String, value: Boolean){
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            val editor = prs.edit()
            editor.putBoolean(key,value)
            editor.commit()
        }

        public fun getFloat(ctx:Context,key: String, default: Float = 0f):Float{
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            return prs.getFloat(key,default)
        }

        public fun setFloat(ctx:Context,key:String, value: Float){
            val prs = ctx.getSharedPreferences(Const.APPLICATION_ID,Context.MODE_PRIVATE)
            val editor = prs.edit()
            editor.putFloat(key,value)
            editor.commit()
        }
    }
}
