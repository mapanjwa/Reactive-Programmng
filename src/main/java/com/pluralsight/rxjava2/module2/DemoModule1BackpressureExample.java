package com.pluralsight.rxjava2.module2;

import com.pluralsight.rxjava2.utility.subscribers.DemoSubscriber;
import com.pluralsight.rxjava2.utility.GateBasedSynchronization;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/*In run configuration: change the VM option to -Xmx64m*/
public class DemoModule1BackpressureExample {

    private static Logger log = LoggerFactory.getLogger(DemoModule1BackpressureExample.class);

    public static void main(String[] args) {

        // Synchronization helper
        GateBasedSynchronization gate = new GateBasedSynchronization();

        // Create an ever-repeating number counter that counts from 1 to 1 billion.
        Observable<Integer> rangeOfNumbers = Observable.range(1 , 1_000_000_000)
                .repeat()
                //This is not onNext() method of observer.  
                //This is observable method called every time when observable emits events
                .doOnNext( nextInt -> log.info("emitting int {}", nextInt)) 
                .subscribeOn(Schedulers.newThread()) //every event subscribe on new thread
                .observeOn(Schedulers.newThread()); //every event observe on new thread

        // Create a DemoSubscriber with a slight delay of 10ms.
        // This should make the rangeOfNumber's emission far outpace
        // the subscriber.
        //when the event comes in, the subscriber (i.e. observer will have delay of 10ms)
        DemoSubscriber<Integer> demoSubscriber = new DemoSubscriber<>(
                10L, TimeUnit.MILLISECONDS,
                gate, "onError", "onComplete"
        );

        // Subscribe to start the numbers flowing.
        rangeOfNumbers.subscribe(demoSubscriber);

        // Wait for things to finish
        gate.waitForAny("onError", "onComplete");

        System.exit(0);
    }
}

/**
 when observable is emitting faster and observer consumes slower then the remaining events
 get stored in the memory which may lead to out of memory exception. Hence the need of backpressure like
 flowable
 
 Refer DemoModule1FlowableExample.java for the fix:
 
 
Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.util.Arrays.copyOfRange(Arrays.java:3664)
	at java.lang.String.<init>(String.java:207)
	at java.lang.StringBuilder.toString(StringBuilder.java:407)
	at ch.qos.logback.core.pattern.PatternLayoutBase.writeLoopOnConverters(PatternLayoutBase.java:118)
	at ch.qos.logback.classic.PatternLayout.doLayout(PatternLayout.java:141)
	at ch.qos.logback.classic.PatternLayout.doLayout(PatternLayout.java:39)
	at ch.qos.logback.core.encoder.LayoutWrappingEncoder.encode(LayoutWrappingEncoder.java:115)
	at ch.qos.logback.core.OutputStreamAppender.subAppend(OutputStreamAppender.java:230)
	at ch.qos.logback.core.OutputStreamAppender.append(OutputStreamAppender.java:102)
	at ch.qos.logback.core.UnsynchronizedAppenderBase.doAppend(UnsynchronizedAppenderBase.java:84)
	at ch.qos.logback.core.spi.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:51)
	at ch.qos.logback.classic.Logger.appendLoopOnAppenders(Logger.java:270)
	at ch.qos.logback.classic.Logger.callAppenders(Logger.java:257)
	at ch.qos.logback.classic.Logger.buildLoggingEventAndAppend(Logger.java:421)
	at ch.qos.logback.classic.Logger.filterAndLog_1(Logger.java:398)
	at ch.qos.logback.classic.Logger.info(Logger.java:583)
	at com.pluralsight.rxjava2.module2.DemoModule1BackpressureExample.lambda$0(DemoModule1BackpressureExample.java:27)
	at com.pluralsight.rxjava2.module2.DemoModule1BackpressureExample$$Lambda$1/1282473384.accept(Unknown Source)
	at io.reactivex.internal.operators.observable.ObservableDoOnEach$DoOnEachObserver.onNext(ObservableDoOnEach.java:93)
	at io.reactivex.internal.operators.observable.ObservableRepeat$RepeatObserver.onNext(ObservableRepeat.java:60)
	at io.reactivex.internal.operators.observable.ObservableRange$RangeDisposable.run(ObservableRange.java:64)
	at io.reactivex.internal.operators.observable.ObservableRange.subscribeActual(ObservableRange.java:35)
	at io.reactivex.Observable.subscribe(Observable.java:12267)
	at io.reactivex.internal.operators.observable.ObservableRepeat$RepeatObserver.subscribeNext(ObservableRepeat.java:91)
	at io.reactivex.internal.operators.observable.ObservableRepeat.subscribeActual(ObservableRepeat.java:35)
	at io.reactivex.Observable.subscribe(Observable.java:12267)
	at io.reactivex.internal.operators.observable.ObservableDoOnEach.subscribeActual(ObservableDoOnEach.java:42)
	at io.reactivex.Observable.subscribe(Observable.java:12267)
	at io.reactivex.internal.operators.observable.ObservableSubscribeOn$SubscribeTask.run(ObservableSubscribeOn.java:96)
	at io.reactivex.Scheduler$DisposeTask.run(Scheduler.java:578)
	at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:66)
	at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:57)
Exception in thread "RxNewThreadScheduler-1" io.reactivex.exceptions.UndeliverableException: The exception could not be delivered to the consumer because it has already canceled/disposed the flow or the exception has nowhere to go to begin with. Further reading: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling | java.lang.OutOfMemoryError: GC overhead limit exceeded
	at io.reactivex.plugins.RxJavaPlugins.onError(RxJavaPlugins.java:367)
	at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:69)
	at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:57)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.util.Arrays.copyOfRange(Arrays.java:3664)
	at java.lang.String.<init>(String.java:207)
	at java.lang.StringBuilder.toString(StringBuilder.java:407)346434
 
 * */
