package ru.its_365.agent365.tools

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import ru.its_365.agent365.activitys.OnSelectDialog
import ru.its_365.agent365.activitys.SelectDialog
import java.util.*

class DialogHelper {
    companion object {
        public fun AlertDialog(ctx:Context,title:String,message:String){
            val builder = AlertDialog.Builder(ctx)
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setNegativeButton("OK",object :DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            dialog?.cancel()
                        }
                    })
            val alertDialog = builder.create()
            alertDialog.show()
        }

        public fun getSelectDialog(ctx:Context, title: String, onSelectDialog: OnSelectDialog) : SelectDialog{
            return SelectDialog(ctx,onSelectDialog, false, title)
        }

        public fun getSearchableSelectDialog(ctx:Context, title: String, onSelectDialog: OnSelectDialog) : SelectDialog{

            return SelectDialog(ctx,onSelectDialog, true, title)
        }

        public fun getDatePickerDialog(ctx:Context, listener : DatePickerDialog.OnDateSetListener, date: Date?) : DatePickerDialog{

            val clndr = Calendar.getInstance()
            if (date != null){
                clndr.time = date
            }

            val datePickerDialog =  DatePickerDialog(ctx,listener,clndr.get(Calendar.YEAR),clndr.get(Calendar.MONTH),clndr.get(Calendar.DAY_OF_MONTH))
            return datePickerDialog
        }
    }
}