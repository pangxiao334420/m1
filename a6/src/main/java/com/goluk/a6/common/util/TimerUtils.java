package com.goluk.a6.common.util;

/**
 * Created by goluk_lium on 2018/4/13.
 */

public class TimerUtils {
//        private static Disposable mDisposable;
//
//        /** milliseconds毫秒后执行next操作
//         *
//         * @param milliseconds
//         * @param next
//         */
//        public static void timer(long milliseconds,final IRxNext next) {
//            Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Long>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable disposable) {
//                            mDisposable=disposable;
//                        }
//
//                        @Override
//                        public void onNext(@NonNull Long number) {
//                            if(next!=null){
//                                next.doNext(number);
//                            }
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//                            //取消订阅
//                            cancel();
//                        }
//
//                        @Override
//                        public void onComplete() {
//                            //取消订阅
//                            cancel();
//                        }
//                    });
//        }
//
//
//        /** 每隔milliseconds毫秒后执行next操作
//         *
//         * @param milliseconds
//         * @param next
//         */
//        public static void interval(long milliseconds,final IRxNext next){
//            Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Long>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable disposable) {
//                            mDisposable=disposable;
//                        }
//
//                        @Override
//                        public void onNext(@NonNull Long number) {
//                            if(next!=null){
//                                next.doNext(number);
//                            }
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        }
//
//
//        /**
//         * 取消订阅
//         */
//        public static void cancel(){
//            if(mDisposable!=null&&!mDisposable.isDisposed()){
//                mDisposable.dispose();
//            }
//        }
//
//        public interface IRxNext{
//            void doNext(long number);
//        }
}
