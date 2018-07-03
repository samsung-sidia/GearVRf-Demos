/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.videoplayer.component.custom;

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.util.Set;

public abstract class FadeableSceneObject extends GVRSceneObject {

    private FadeAnimationHandler mFadeHandler = FadeAnimationHandler.INSTANCE;

    public FadeableSceneObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public FadeableSceneObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
    }

    public void fadeIn() {
        this.fadeIn(null);
    }

    public void fadeIn(final OnFadeFinish onFadeFinish) {
        if (!isEnabled()) {
            setEnable(true);
            mFadeHandler.fadeObjects(getGVRContext(), this, FadeType.FADE_IN, new OnFadeFinish() {
                @Override
                public void onFadeFinished(GVRSceneObject obj) {
                    if (onFadeFinish != null) {
                        onFadeFinish.onFadeFinished(obj);
                    }
                }
            });
        }
    }

    public void fadeOut() {
        this.fadeOut(null);
    }

    public void fadeOut(final OnFadeFinish onFadeFinish) {
        if (isEnabled()) {
            mFadeHandler.fadeObjects(getGVRContext(), this, FadeType.FADE_OUT, new OnFadeFinish() {
                @Override
                public void onFadeFinished(GVRSceneObject obj) {
                    obj.setEnable(false);
                    Log.d("@#@", getName() + ": " + isEnabled());
                    if (onFadeFinish != null) {
                        onFadeFinish.onFadeFinished(obj);
                    }
                }
            });
        }
    }
}
