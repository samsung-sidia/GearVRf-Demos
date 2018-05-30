package org.gearvrf.videoplayer.component.video;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableComponent;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.FocusableViewSceneObject;
import org.gearvrf.videoplayer.util.TimeUtils;

public class VideoControllerComponent extends FadeableComponent implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private View mMainView;
    private SeekBar mSeekBar;
    private View mPlayPauseButton;
    private TextView mElapsedTime, mDurationTime, mTitle;
    private OnVideoControllerListener mOnVideoControllerListener;
    @DrawableRes
    private int mStateResource;
    private FocusableViewSceneObject mFocusableViewSceneObject;

    @SuppressLint("InflateParams")
    VideoControllerComponent(GVRContext gvrContext, float width, float height) {
        super(gvrContext);

        mMainView = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.layout_player_widget, null);
        mFocusableViewSceneObject = new FocusableViewSceneObject(gvrContext, mMainView, width, height);
        mFocusableViewSceneObject.setName("videoControlWidget");
        addChildObject(mFocusableViewSceneObject);
        initView();
    }

    private void initView() {

        mSeekBar = mMainView.findViewById(R.id.progressBar);
        mPlayPauseButton = mMainView.findViewById(R.id.playPauseButton);
        mElapsedTime = mMainView.findViewById(R.id.elapsedTimeText);
        mDurationTime = mMainView.findViewById(R.id.durationTimeText);
        mTitle = mMainView.findViewById(R.id.titleText);

        mPlayPauseButton.setOnClickListener(this);
        mMainView.findViewById(R.id.backButton).setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        showPlay();
    }

    public void showPause() {
        mStateResource = R.drawable.selector_button_pause;
        mPlayPauseButton.setBackgroundResource(mStateResource);
    }

    public void showPlay() {
        mStateResource = R.drawable.selector_button_play;
        mPlayPauseButton.setBackgroundResource(mStateResource);
    }

    public void setTitle(final CharSequence title) {
        mTitle.post(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(title);
            }
        });
    }

    private void updateElapsedTimeText(final int progress) {
        mElapsedTime.post(new Runnable() {
            @Override
            public void run() {
                mElapsedTime.setText(TimeUtils.formatDuration(progress));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateDurationTimeText(final int maxProgress) {
        mDurationTime.post(new Runnable() {
            @Override
            public void run() {
                mDurationTime.setText(TimeUtils.formatDuration(maxProgress));
            }
        });
    }

    public void setProgress(final int progress) {
        mSeekBar.post(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(progress);
                updateElapsedTimeText(progress);
            }
        });
    }

    public void setMaxProgress(final int maxProgress) {
        mSeekBar.post(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setMax(maxProgress);
                updateDurationTimeText(maxProgress);
            }
        });
    }

    public void setPlayPauseButtonEnabled(final boolean enabled) {
        mPlayPauseButton.post(new Runnable() {
            @Override
            public void run() {
                mPlayPauseButton.setEnabled(enabled);
            }
        });
    }

    public void setOnVideoControllerListener(OnVideoControllerListener listener) {
        this.mOnVideoControllerListener = listener;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.playPauseButton) {
            if (mOnVideoControllerListener != null) {
                if (mStateResource == R.drawable.selector_button_play) {
                    showPause();
                    mOnVideoControllerListener.onPlay();
                } else {
                    showPlay();
                    mOnVideoControllerListener.onPause();
                }
            }
        } else if (v.getId() == R.id.backButton) {
            if (mOnVideoControllerListener != null) {
                mOnVideoControllerListener.onBack();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mOnVideoControllerListener != null && fromUser) {
            mOnVideoControllerListener.onSeek(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void setFocusListener(@NonNull FocusListener<FocusableViewSceneObject> listener) {
        mFocusableViewSceneObject.setFocusListener(listener);
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mFocusableViewSceneObject;
    }
}
