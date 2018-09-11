package com.goluk.a6.control;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro;
import com.goluk.a6.internation.SharedPrefUtil;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        MyFragment firstFragment = MyFragment.newInstance(R.string.welcome_1, R.drawable.welcome_1, false);
        MyFragment secondFragment = MyFragment.newInstance(R.string.welcome_2, R.drawable.welcome_2, false);
        MyFragment thirdFragment = MyFragment.newInstance(R.string.welcome_3, R.drawable.welcome_3, false);
        MyFragment fourthFragment = MyFragment.newInstance(R.string.welcome_4, R.drawable.welcome_4, true);

        addSlide(firstFragment);
        addSlide(secondFragment);
        addSlide(thirdFragment);
        addSlide(fourthFragment);

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(false);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
        showSeparator(false);

        setCustomTransformer(new ParallaxPagerTransformer(R.id.tv_Title));
        setIndicatorColor(Color.parseColor("#000000"), Color.parseColor("#CCCCCC"));
    }


    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }


    public static class MyFragment extends Fragment {
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";
        private static final String ARG_PARAM3 = "param3";
        private int mSrc;
        private int mTitle;
        private boolean mOk;

        private ImageView mBg;
        private TextView mTvTitle;
        private TextView mTvOK;

        public static MyFragment newInstance(int param1, int param2, boolean ok) {
            MyFragment fragment = new MyFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_PARAM1, param1);
            args.putInt(ARG_PARAM2, param2);
            args.putBoolean(ARG_PARAM3, ok);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mTitle = getArguments().getInt(ARG_PARAM1);
                mSrc = getArguments().getInt(ARG_PARAM2);
                mOk = getArguments().getBoolean(ARG_PARAM3);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_welcom, container, false);
            initData(view);
            return view;
        }

        private void initData(View view) {
            mTvTitle = (TextView) view.findViewById(R.id.tv_Title);
            mBg = (ImageView) view.findViewById(R.id.iv_welcome);
            mTvOK = (TextView) view.findViewById(R.id.tv_ok);
            mTvTitle.setText(mTitle);
            mBg.setImageResource(mSrc);
            mTvOK.setVisibility(mOk ? View.VISIBLE : View.GONE);
            mTvOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.getContext().startActivity(new Intent(view.getContext(), CarControlActivity.class));
                    MyFragment.this.getActivity().finish();
                    SharedPrefUtil.saveWelcome(true);
                }
            });
        }
    }


    public class ParallaxPagerTransformer implements ViewPager.PageTransformer {

        private int id;
        private int border = 1;
        private float speed = 0.8f;

        public ParallaxPagerTransformer(int id) {
            this.id = id;
        }

        @Override
        public void transformPage(View view, float position) {

            View parallaxView = view.findViewById(id);

            if (view == null ) {
                Log.w("ParallaxPager", "There is no view");
            }

            if (parallaxView != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB ) {
                if (position > -1 && position < 1) {
                    float width = parallaxView.getWidth();
                    parallaxView.setTranslationX(-(position * width * speed));
                    float sc = ((float)view.getWidth() - border)/ view.getWidth();
                    if (position == 0) {
                        view.setScaleX(1);
                        view.setScaleY(1);
                    } else {
                        view.setScaleX(sc);
                        view.setScaleY(sc);
                    }
                }
            }
        }

        public void setBorder(int px) {
            border = px;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }
    }

}