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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.util.Set;

public abstract class FadeableSceneObject extends GVRSceneObject {

    private Set<GVRSceneObject> mRenderables = new ArraySet<>();
    private FadeAnimationHandler mFadeHandler = FadeAnimationHandler.INSTANCE;

    public FadeableSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        findRenderable(this);
    }

    public FadeableSceneObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
        findRenderable(this);
    }

    public void fadeIn() {
        this.fadeIn(null);
    }

    public void fadeIn(final OnFadeFinish onFadeFinish) {
        if (!isEnabled()) {
            setEnable(true);
            if (!mRenderables.isEmpty()) {
                mFadeHandler.fadeObjects(getGVRContext(), mRenderables, FadeType.FADE_IN, new OnFadeFinish() {
                    @Override
                    public void onFadeFinished() {
                        if (onFadeFinish != null) {
                            onFadeFinish.onFadeFinished();
                        }
                    }
                });
            } else {
                if (onFadeFinish != null) {
                    onFadeFinish.onFadeFinished();
                }
            }
        }
    }

    public void fadeOut() {
        fadeOut(null);
    }

    public void fadeOut(final OnFadeFinish onFadeFinish) {
        if (isEnabled()) {
            if (!mRenderables.isEmpty()) {
                mFadeHandler.fadeObjects(getGVRContext(), mRenderables, FadeType.FADE_OUT, new OnFadeFinish() {
                    @Override
                    public void onFadeFinished() {
                        setEnable(false);
                        if (onFadeFinish != null) {
                            onFadeFinish.onFadeFinished();
                        }
                    }
                });
            } else {
                setEnable(false);
                if (onFadeFinish != null) {
                    onFadeFinish.onFadeFinished();
                }
            }
        }
    }

    private void findRenderable(@NonNull GVRSceneObject object) {
        if (object.getRenderData() != null) {
            mRenderables.add(object);
        } else {
            for (GVRSceneObject child : object.getChildren()) {
                findRenderable(child);
            }
        }
    }

    @Override
    public boolean addChildObject(GVRSceneObject child) {
        try {
            return super.addChildObject(child);
        } finally {
            findRenderable(child);
        }
    }
}
