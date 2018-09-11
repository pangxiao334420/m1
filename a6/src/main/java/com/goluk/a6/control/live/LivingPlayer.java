package com.goluk.a6.control.live;

import android.content.Context;

import com.ksyun.media.player.KSYTextureView;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public class LivingPlayer {
    private KSYTextureView mKsyTextureView;
    private static LivingPlayer INSTANCE;
    private LivingPlayer(){}

    public static LivingPlayer getInstance(){
        if (INSTANCE==null){
            synchronized (LivingPlayer.class){
                if (INSTANCE==null)
                    INSTANCE = new LivingPlayer();
            }
        }
        return INSTANCE;
    }

    public void init(final Context context){
        if (mKsyTextureView!=null){
            mKsyTextureView.release();
            mKsyTextureView=null;
        }
        mKsyTextureView = new KSYTextureView(context);
    }

    public KSYTextureView getKsyTextureView() {
        return mKsyTextureView;
    }

    public void destroy(){
        if (mKsyTextureView!=null){
            mKsyTextureView.release();
        }
        mKsyTextureView=null;
    }
}
