package ru.its_365.agent365

import io.reactivex.Observable
import org.junit.Test

import org.junit.Assert.*
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


public class RxBus private constructor() {
    init {

    }
    private object Holder { val INSTANCE = RxBus() }
    companion object {
        val instance: RxBus by lazy { Holder.INSTANCE }
    }

    private val bus = PublishSubject.create<String>() // the actual publisher handling all of the events

    fun send(message: String) {
        bus.onNext(message) // the message being sent to all subscribers
    }

    fun toObserverable(): Observable<String> {
        return bus // return the publisher itself as an observable to subscribe to
    }
}



class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        RxBus.instance.toObserverable().subscribe(){
            print(it);
        }

        RxBus.instance.send("OK")

        assertEquals(4, 2 + 2)
    }
}
