package com.goluk.a6.cpstomp;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.goluk.a6.http.UrlHostManager;

import org.java_websocket.WebSocket;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class CPStompClient {

    private StompClient mStompClient;

    public boolean isOpend() {
        return mIsOpend;
    }

    private boolean mIsOpend;
    private String mDeviceId;
    private StompConnListener stompConnListener;

    private Handler mUiHandler;

    public void connect(StompConnListener stompConnListener) {
        mUiHandler = new Handler(Looper.getMainLooper());

        this.stompConnListener = stompConnListener;
        mStompClient = Stomp.over(WebSocket.class, UrlHostManager.getLiveWebSocketAddress());
        mStompClient.connect();
        mStompClient.lifecycle()
                .subscribe(new Action1<LifecycleEvent>() {
                    @Override
                    public void call(LifecycleEvent lifecycleEvent) {

                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                mIsOpend = true;

                                if (CPStompClient.this.stompConnListener != null) {
                                    CPStompClient.this.stompConnListener.stompOpened();
                                }
                                break;
                            case ERROR:
                                if (CPStompClient.this.stompConnListener != null) {
                                    CPStompClient.this.stompConnListener.stompError(lifecycleEvent.getException());
                                }
                                mIsOpend = false;
                                break;

                            case CLOSED:
                                if (CPStompClient.this.stompConnListener != null) {
                                    CPStompClient.this.stompConnListener.stompClosed();
                                }
                                mIsOpend = false;
                                break;
                        }
                    }
                });
    }

    public void topic(String deviceId) {
        mDeviceId = deviceId;
        // 监听
        mStompClient.topic("/message/topic/device/" + mDeviceId)
                .subscribe(new Action1<StompMessage>() {
                               @Override
                               public void call(StompMessage stompMessage) {
                                   if (CPStompClient.this.stompConnListener != null) {
                                       String msgData = stompMessage.getPayload();
                                       Log.e("STOMP MESSAGE ", msgData);
                                       if (TextUtils.isEmpty(msgData)) {
                                           return;
                                       }
                                       if (msgData.contains("\n")) {
                                           msgData = msgData.replace("\n", "");
                                       }
                                       final String data = msgData;
                                       mUiHandler.post(new Runnable() {
                                           @Override
                                           public void run() {
                                               if ( CPStompClient.this.stompConnListener != null)
                                                   CPStompClient.this.stompConnListener.onRcvMsg(data);
                                           }
                                       });
                                   }
                               }
                           }
                );
    }

    public void send(String msg) {
        mStompClient.send("/message/queue/command", msg).subscribe();
    }

    public void disconnect() {
        mIsOpend = false;
        mStompClient.disconnect();
        stompConnListener = null;
        mUiHandler = null;
    }

    public interface StompConnListener {
        void stompOpened();

        void stompClosed();

        void stompError(Exception e);

        void onRcvMsg(String bean);

        void onReturn(Void aVoid);
    }

}
