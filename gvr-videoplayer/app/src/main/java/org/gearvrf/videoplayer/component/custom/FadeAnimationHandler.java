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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;

import java.util.Collection;

public enum FadeAnimationHandler {

    INSTANCE;

    private static final float FADE_DURATION = .2F;

    private GVRContext mContext;
    private OnFadeFinish onFinish;
    private int opacity;

    public void fadeObjects(@NonNull GVRContext context,
                            @NonNull Collection<GVRSceneObject> objects,
                            @FadeType int fadeType,
                            @NonNull OnFadeFinish onFadeFinish) {

        if (objects.isEmpty()) {
            return;
        }

        this.mContext = context;
        this.opacity = fadeType == FadeType.FADE_IN ? 1 : 0;
        this.onFinish = onFadeFinish;

        fadeObjects(objects);
    }

    private void fadeObjects(Collection<GVRSceneObject> objects) {
        CollectionSceneObject collectionSceneObject = new CollectionSceneObject(mContext, objects);
        CollectionOpacityAnimation animation = new CollectionOpacityAnimation(collectionSceneObject, FADE_DURATION, opacity);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                onFinish.onFadeFinished();
            }
        });
        animation.start(mContext.getAnimationEngine());
    }
}
