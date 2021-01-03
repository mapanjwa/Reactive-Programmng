package com.pluralsight.rxjava2.module4;

import com.pluralsight.rxjava2.utility.GateBasedSynchronization;
import com.pluralsight.rxjava2.utility.datasets.FibonacciSequence;
import com.pluralsight.rxjava2.utility.subscribers.DemoSubscriber;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeOnObserveOnExample1 {

    private final static Logger log = LoggerFactory.getLogger(SubscribeOnObserveOnExample1.class);

    public static void main(String[] args) {

        GateBasedSynchronization gate = new GateBasedSynchronization();

        // Our base observable for this example will be a FibonacciSequence with 10 numbers.
        Observable<Long> fibonacciObservable = FibonacciSequence.create(10)
                .doOnSubscribe( disposable -> {
                    log.info("fibonacciObservable::onSubscribe");
                });

        // -----------------------------------------------------------------------------------------

        // First, let's look at subscription with no threading modification.
        fibonacciObservable.subscribe(new DemoSubscriber<>(gate));

        // No threading, but do our synchronization pattern anyway.
        gate.waitForAny("onError", "onComplete");
        log.info("--------------------------------------------------------");

        // -----------------------------------------------------------------------------------------
        // Scan the numbers on the computation thread pool
        // -----------------------------------------------------------------------------------------

        gate.resetAll();

        // SubscribeOn example illustrating how first SubscribeOn wins.
        fibonacciObservable
                .subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.io()) // This will be ignored.  subscribeOn is always first come, first served.
                .subscribe(new DemoSubscriber<>(gate));

        // No threading, but do our synchronization pattern anyway.
        gate.waitForAny("onError", "onComplete");
        log.info("--------------------------------------------------------");

        // -----------------------------------------------------------------------------------------
        // Illustrate how observeOn's position effects which scheduler is used.
        // -----------------------------------------------------------------------------------------

        gate.resetAll();

        // Illustrate how observeOn's position alters the scheduler that is
        // used for the observation portion of the code.
        fibonacciObservable
                // First observeOn...will be altered by the
                // observeOn further downstream.
                .observeOn(Schedulers.computation())

                // The location of subscribeOn doesn't matter.
                // First subscribeOn always wins.
                .subscribeOn(Schedulers.newThread())

                // the last observeOn takes precedence.
                .observeOn(Schedulers.io())

                .subscribe(new DemoSubscriber<>(gate));

        // No threading, but do our synchronization pattern anyway.
        gate.waitForAny("onError", "onComplete");
        log.info("--------------------------------------------------------");

        System.exit(0);
    }
}

/*

See logs: different threads are formed

706    [main                      ] [INFO ] [bscribeOnObserveOnExample1] - fibonacciObservable::onSubscribe
711    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 0
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 1
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 2
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 3
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 5
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 8
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 13
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 21
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 34
713    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 55
714    [main                      ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onComplete
721    [main                      ] [INFO ] [bscribeOnObserveOnExample1] - --------------------------------------------------------
759    [RxComputationThreadPool-1 ] [INFO ] [bscribeOnObserveOnExample1] - fibonacciObservable::onSubscribe
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 0
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 1
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 2
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 3
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 5
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 8
759    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 13
760    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 21
760    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 34
760    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 55
760    [RxComputationThreadPool-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onComplete
760    [main                      ] [INFO ] [bscribeOnObserveOnExample1] - --------------------------------------------------------
816    [RxNewThreadScheduler-1    ] [INFO ] [bscribeOnObserveOnExample1] - fibonacciObservable::onSubscribe
817    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 0
817    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 1
817    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 2
817    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 3
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 5
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 8
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 13
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 21
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 34
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onNext - 55
818    [RxCachedThreadScheduler-1 ] [INFO ] [c.p.r.u.s.DemoSubscriber  ] - onComplete
818    [main                      ] [INFO ] [bscribeOnObserveOnExample1] - --------------------------------------------------------


 * */
