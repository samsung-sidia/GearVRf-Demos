package org.gearvrf.videoplayer.component.video;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.component.video.backbutton.BackButton;
import org.gearvrf.videoplayer.component.video.control.ControlWidget;
import org.gearvrf.videoplayer.component.video.control.ControlWidgetListener;
import org.gearvrf.videoplayer.component.video.dialog.OnPlayNextListener;
import org.gearvrf.videoplayer.component.video.dialog.PlayNextDialog;
import org.gearvrf.videoplayer.component.video.loading.LoadingAsset;
import org.gearvrf.videoplayer.component.video.player.OnPlayerListener;
import org.gearvrf.videoplayer.component.video.player.Player;
import org.gearvrf.videoplayer.component.video.player.PlayerListenerDispatcher;
import org.gearvrf.videoplayer.component.video.title.OverlayTitle;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.Focusable;
import org.gearvrf.videoplayer.focus.PickEventHandler;
import org.gearvrf.videoplayer.model.Video;

import java.util.List;

import static org.gearvrf.videoplayer.component.video.VideoPlayer.ConfigureVideoPlayer.SCALE_FACTOR;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();
    private static final float CONTROLLER_HEIGHT_FACTOR = .25f;
    private static final float BACK_BUTTON_SIZE_FACTOR = .1f;
    private static final float OVERLAY_TITLE_HEIGHT_FACTOR = .06f;

    private Player mPlayer;
    private ControlWidget mControl;
    private BackButton mBackButton;
    private PlayNextDialog mPlayNextDialog;
    private OverlayTitle mOverlayTitle;
    private LoadingAsset mLoadingAsset;

    private GVRSceneObject mWidgetsContainer;

    private boolean mVideoPlayerActive = true;
    private boolean mIsControlActive = true;
    private boolean mBackButtonActive = true;
    private boolean mIsPlayNextDialogActive = true;
    private boolean mHideControlWidgetTimerEnabled;
    private HideControlWidgetTimer mHideControlTimer;
    private List<Video> mVideos;


    public VideoPlayer(GVRContext gvrContext, float playerWidth, float playerHeight) {
        super(gvrContext);

        mHideControlTimer = new HideControlWidgetTimer(this);

        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {

            PickEventHandler mPickEventHandler = new PickEventHandler();

            @Override
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mPickEventHandler);
                }
                newController.addPickEventListener(mPickEventHandler);
            }
        });

        mWidgetsContainer = new GVRSceneObject(gvrContext);
        mWidgetsContainer.getTransform().setPositionZ(-8.1f);
        addChildObject(mWidgetsContainer);
        addPlayer(playerWidth, playerHeight);
        addControlWidget(CONTROLLER_HEIGHT_FACTOR * playerHeight);
        addBackButton(BACK_BUTTON_SIZE_FACTOR * playerHeight);
        addPlayNextDialog();
        addTitleOverlay(OVERLAY_TITLE_HEIGHT_FACTOR * playerHeight);
        addLoadingAsset();

    }

    public boolean is360VideoPlaying() {
        return mPlayer.isPlaying() && mPlayer.is360PlayerActive();

    }

    public void prepare(@NonNull List<Video> videos) {
        mVideos = videos;
        if (videos.size() > 0) {
            mPlayer.prepare(videos);
        }
    }

    private void showControlWidget() {
        showControlWidget(mHideControlWidgetTimerEnabled);
    }

    private void showControlWidget(boolean autoHide) {
        Log.d(TAG, "showControlWidget: ");
        if (!mIsControlActive) {
            mWidgetsContainer.addChildObject(mControl);
            mControl.fadeIn(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mIsControlActive = true;
                }
            });
        }
        if (autoHide) {
            mHideControlTimer.start();
        }
    }

    private void hideControlWidget() {
        Log.d(TAG, "hideControlWidget: ");
        mHideControlTimer.cancel();
        if (mIsControlActive) {
            mIsControlActive = false;
            mControl.fadeOut(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mWidgetsContainer.removeChildObject(mControl);
                }
            });
        }
    }

    private void showBackButton() {
        if (!mBackButtonActive) {
            mWidgetsContainer.addChildObject(mBackButton);
            mBackButton.fadeIn(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    Log.d(TAG, "showBackButton");
                    mBackButtonActive = true;
                }
            });
        }
    }

    private void hideBackButton() {
        if (mBackButtonActive) {
            mBackButton.fadeOut(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mBackButtonActive = false;
                    mWidgetsContainer.removeChildObject(mBackButton);
                }
            });
        }
    }

    private void showPlayNextDialog() {
        if (mVideoPlayerActive && !mIsPlayNextDialogActive) {
            mHideControlTimer.cancel();
            mPlayNextDialog.setVideoData(mVideos.get(mPlayer.getNextIndexToPlay()));
            mWidgetsContainer.addChildObject(mPlayNextDialog);
            mPlayNextDialog.fadeIn(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mIsPlayNextDialogActive = true;
                    showBackButton();
                    mPlayNextDialog.startTimer();
                }
            });
        }
    }

    private void hidePlayNextDialog() {
        if (mIsPlayNextDialogActive) {
            mIsPlayNextDialogActive = false;
            mPlayNextDialog.cancelTimer();
            mPlayNextDialog.fadeOut(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mWidgetsContainer.removeChildObject(mPlayNextDialog);
                }
            });
        }
    }

    public void showAllControls() {
        if (mVideoPlayerActive) {
            if (!mIsPlayNextDialogActive) {
                showControlWidget();
            }
            showBackButton();
        }
    }

    private void hideAllControls() {
        if (!mIsPlayNextDialogActive) {
            hideBackButton();
        }
        hideControlWidget();
    }

    public void setControlWidgetAutoHide(boolean autoHide) {
        mHideControlWidgetTimerEnabled = autoHide;
        if (autoHide) {
            mHideControlTimer.start();
        }
    }

    private void addPlayer(float width, float height) {
        mPlayer = new Player(getGVRContext(), width, height);
        mPlayer.setOnVideoPlayerListener(mInternalVideoPlayerListener);
        addChildObject(mPlayer);
    }

    private void addControlWidget(float height) {
        mControl = new ControlWidget(getGVRContext());
        mControl.getTransform().setScale(6, 6, 1);
        mControl.setOnVideoControllerListener(mOnVideoControllerListener);
        mControl.setFocusListener(mFocusListener);
        // Put video control widget below the video screen
        float positionY = -(height / CONTROLLER_HEIGHT_FACTOR / 2f);
        mControl.getTransform().setPositionY(positionY * 1.02f);
        mControl.getTransform().setPositionZ(.05f);
        mWidgetsContainer.addChildObject(mControl);
    }

    private void addBackButton(float height) {
        mBackButton = new BackButton(getGVRContext());
        mBackButton.setFocusListener(mFocusListener);
        mBackButton.getTransform().setScale(1.f * SCALE_FACTOR, 1.f * SCALE_FACTOR, 1f);
        // Put back button above the video screen
        float positionY = (height / BACK_BUTTON_SIZE_FACTOR / 2f);
        mBackButton.getTransform().setPositionY(positionY - (positionY * .08f));
        mBackButton.getTransform().setPositionZ(.05f);
        mWidgetsContainer.addChildObject(mBackButton);

    }

    private void addPlayNextDialog() {
        mPlayNextDialog = new PlayNextDialog(getGVRContext(), mOnPlayNextListener);
        mPlayNextDialog.getTransform().setScale(2.0f, 2.0f, 1.0f);
        mPlayNextDialog.getTransform().setPositionZ(.5f);
        mWidgetsContainer.addChildObject(mPlayNextDialog);
    }

    private void addTitleOverlay(float height) {
        mOverlayTitle = new OverlayTitle(getGVRContext());
        mOverlayTitle.getTransform().setScale(3, 3, 1);
        float positionY = (height / OVERLAY_TITLE_HEIGHT_FACTOR / 2f);
        mOverlayTitle.getTransform().setPositionY(positionY + .5f);
        mWidgetsContainer.addChildObject(mOverlayTitle);
    }

    private void addLoadingAsset() {
        mLoadingAsset = new LoadingAsset(getGVRContext());

        mLoadingAsset.getTransform().setScale(1.f * SCALE_FACTOR, 1.f * SCALE_FACTOR, 1.f);
        mLoadingAsset.getTransform().setPositionZ(mPlayer.getTransform().getPositionZ() + 2f);
        addChildObject(mLoadingAsset);

    }

    public void setPlayerListener(OnPlayerListener listener) {
        mInternalVideoPlayerListener.setOnVideoPlayerListener(listener);
    }

    public void setBackButtonClickListener(@NonNull View.OnClickListener listener) {
        mBackButton.setOnClickListener(listener);
    }

    public void play() {
        mHideControlTimer.start();
        mPlayer.playVideo();
    }

    public void pause() {
        mHideControlTimer.cancel();
        mPlayer.pauseVideo();
    }

    private FocusListener mFocusListener = new FocusListener() {
        @Override
        public void onFocusGained(Focusable focusable) {
            Log.d(TAG, "onFocusGained: " + focusable.getClass().getSimpleName());
            if (focusable instanceof ControlWidget || focusable instanceof BackButton) {
                mHideControlTimer.cancel();
            }
        }

        @Override
        public void onFocusLost(Focusable focusable) {
            if (focusable instanceof ControlWidget || focusable instanceof BackButton) {
                Log.d(TAG, "onFocusLost: " + focusable.getClass().getSimpleName());
                mHideControlTimer.start();
            }
        }
    };

    private PlayerListenerDispatcher mInternalVideoPlayerListener = new PlayerListenerDispatcher() {
        @Override
        public void onProgress(long progress) {
            mControl.setProgress((int) progress);
            super.onProgress(progress);
        }

        @Override
        public void onPrepareFile(String title, long duration) {
            Log.d(TAG, "Video prepared: {title: " + title + ", duration: " + duration + "}");

            mControl.setTitle(title);
            mControl.setMaxProgress((int) duration);
            mControl.setProgress((int) mPlayer.getProgress());
            mControl.setButtonState(ControlWidget.ButtonState.PLAYING);

            if (mVideoPlayerActive) {
                mPlayer.fadeIn(new FadeableObject.FadeInCallback() {
                    @Override
                    public void onFadeIn() {
                        showAllControls();
                        mPlayer.playVideo();
                    }
                });
            }

            super.onPrepareFile(title, duration);
        }

        @Override
        public void onStart() {
            Log.d(TAG, "Video started");
            mControl.setButtonState(ControlWidget.ButtonState.PAUSED);
            showAllControls();
            mWidgetsContainer.removeChildObject(mLoadingAsset);
            super.onStart();
        }

        @Override
        public void onLoading() {
            Log.d(TAG, "Video loading");

            mWidgetsContainer.addChildObject(mLoadingAsset);
            super.onLoading();
        }

        @Override
        public void onFileEnd() {
            Log.d(TAG, "Video ended");
            if (mPlayer.hasNextToPlay()) {
                mPlayer.fadeOut();
                hideControlWidget();
                showPlayNextDialog();
            }
            mWidgetsContainer.removeChildObject(mLoadingAsset);
            super.onFileEnd();
        }

        @Override
        public void onAllFilesEnd() {
            super.onAllFilesEnd();
            Log.d(TAG, "All videos ended");
            // Force back to gallery
            mBackButton.performClick();
        }
    };

    private ControlWidgetListener mOnVideoControllerListener = new ControlWidgetListener() {
        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: ");
            mPlayer.playVideo();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: ");
            mPlayer.pauseVideo();
        }

        @Override
        public void onBack() {
            Log.d(TAG, "onBack: ");
        }

        @Override
        public void onSeek(long progress) {
            Log.d(TAG, "onSeek: ");
            mPlayer.setProgress(progress);
        }
    };

    private OnPlayNextListener mOnPlayNextListener = new OnPlayNextListener() {
        @Override
        public void onTimesUp() {
            doPlayNext();
        }

        @Override
        public void onThumbClicked() {
            doPlayNext();
        }

        private void doPlayNext() {
            hidePlayNextDialog();
            mPlayer.prepareNextFile();
        }
    };

    private static class HideControlWidgetTimer extends Handler {

        private static final int HIDE_WIDGET_DELAY = 3000;
        private VideoPlayer mVideoPlayer;

        HideControlWidgetTimer(VideoPlayer videoPlayer) {
            mVideoPlayer = videoPlayer;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mVideoPlayer.hideAllControls();
        }

        void start() {
            removeMessages(0);
            sendEmptyMessageDelayed(0, HIDE_WIDGET_DELAY);
        }

        public void cancel() {
            removeMessages(0);
        }
    }

    public void show(final FadeableObject.FadeInCallback fadeInCallback) {
        if (!mVideoPlayerActive) {
            mPlayer.fadeIn(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mVideoPlayerActive = true;
                    showAllControls();
                    if (fadeInCallback != null) {
                        fadeInCallback.onFadeIn();
                    }
                }
            });
            mWidgetsContainer.addChildObject(mOverlayTitle);
        }
    }

    public void hide() {
        hide(null);
    }

    public void hide(final FadeableObject.FadeOutCallback fadeOutCallback) {
        if (mVideoPlayerActive) {
            hidePlayNextDialog();
            hideAllControls();
            mPlayer.stop();
            mPlayer.fadeOut(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mVideoPlayerActive = false;
                    if (fadeOutCallback != null) {
                        fadeOutCallback.onFadeOut();
                    }
                }
            });
            mWidgetsContainer.removeChildObject(mOverlayTitle);
        }
    }


    public GVRSceneObject getWidgetsContainer() {
        return mWidgetsContainer;
    }

    public void reposition(float[] newModelMatrix) {
        mPlayer.reposition(newModelMatrix);

        GVRTransform ownerTrans = mWidgetsContainer.getTransform();

        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();

        ownerTrans.setModelMatrix(newModelMatrix);
        ownerTrans.setScale(scaleX, scaleY, scaleZ);

        ownerTrans.setPosition(newModelMatrix[8] * -8.05f, newModelMatrix[9] * -8.05f, newModelMatrix[10] * -8.05f);
    }

    public static class ConfigureVideoPlayer{

        static final float SCALE_FACTOR = .4f;

    }

}
