package org.gearvrf.videoplayer.component.video.loading;

import android.util.Log;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.IViewEvents;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.custom.FadeableSceneObject;

public class LoadingAsset extends FadeableSceneObject implements IViewEvents {

    private static final String TAG = LoadingAsset.class.getSimpleName();
    private GVRViewSceneObject mLoadingObject;

    public LoadingAsset(GVRContext gvrContext) {
        super(gvrContext);
        mLoadingObject = new GVRViewSceneObject(gvrContext, R.layout.layout_loading, this);
        mLoadingObject.waitFor();
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(mLoadingObject);
        GVRAnimation animation = new GVRRotationByAxisWithPivotAnimation(this, 2, -360f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f).start(getGVRContext().getAnimationEngine());
        animation.setRepeatMode(1);
        animation.setRepeatCount(-1);
        Log.d(TAG, "Animation Loading ");
    }
}
