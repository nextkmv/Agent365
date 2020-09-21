package ru.its_365.agent365.activitys

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.activity_progress_dialog.*
import ru.its_365.agent365.R
import ru.its_365.agent365.R.id.toolbar

class ProgressDialog(context: Context) : Dialog(context), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_progress_dialog)
        setCanceledOnTouchOutside(false)
        buttonCancel.setOnClickListener(this)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showCancelButton() {

        buttonCancel?.visibility = View.VISIBLE
    }

    fun hideCancelButton() {
        buttonCancel?.visibility = View.INVISIBLE
    }

    fun setMessage(message:String) {
        dialogTitle?.setText(message)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.buttonCancel -> dismiss()
        }
    }

}
