package ru.its_365.agent365.tools

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter




/**
 *
 * A lightweight version of (and inspired by) https://github.com/xavierlepretre/rx-alert-dialog
 *
 * Usage:
 *  observable
 *      .observeOn(AndroidSchedulers.mainThread())
 *      .flatMap({ event ->
 *          RxAlertDialog.show(context, R.string.title, R.string.message, android.R.string.ok)
 *      })
 *      .subscribe({ button ->
 *          when (button) {
 *              RxAlertDialog.BUTTON_POSITIVE -> Log.v("OK")
 *              RxAlertDialog.BUTTON_NEGATIVE-> Log.v("Cancel")
 *              RxAlertDialog.DISMISS_ALERT-> Log.v("Dismissed")
 *          }
 *     })
 *
 */
class RxAlertDialog {
    companion object {
        const val DISMISS_ALERT = 0
        const val BUTTON_POSITIVE = 1
        const val BUTTON_NEGATIVE = 2

        @JvmStatic @JvmOverloads
        fun show(
                context: Context,
                @StringRes title: Int? = null,
                @StringRes message: Int? = null,
                @StringRes positiveButton: Int? = null,
                @StringRes negativeButton: Int? = null): Observable<Int> {

            val o = Observable.create<Int> { emitter ->

                    val builder = AlertDialog.Builder(context)
                            .setOnDismissListener { emitter.onNext(DISMISS_ALERT) }

                    title?.let { builder.setTitle(it)}
                    message?.let { builder.setMessage(it)}
                    positiveButton?.let { builder.setPositiveButton(positiveButton, { _, _ -> emitter.onNext(BUTTON_POSITIVE)})}
                    negativeButton?.let { builder.setNegativeButton(negativeButton, { _, _ -> emitter.onNext(BUTTON_NEGATIVE)})}

                    val dialog = builder.show()
                    emitter.setCancellable { dialog.dismiss() }
            }

            return o
        }
    }
}