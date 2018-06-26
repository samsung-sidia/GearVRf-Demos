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

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

import java.util.Set;

public enum FadeHandler {

    INSTANCE;

    private static final float FADE_DURATION = .2F;

    private OnFadeFinish onFinish;
    private int counter;
    private int opacity;

    public void fadeObjects(@NonNull Set<GVRSceneObject> objects,
                            @FadeType int fadeType,
                            @NonNull OnFadeFinish onFadeFinish) {

        if (objects.isEmpty()) {
            return;
        }

        this.opacity = fadeType == FadeType.FADE_IN ? 1 : 0;
        this.onFinish = onFadeFinish;

        synchronized (FadeHandler.this) {
            counter = objects.size();
        }

        for (GVRSceneObject object : objects) {
            fadeObject(object);
        }
    }

    private void fadeObject(GVRSceneObject object) {
        GVROpacityAnimation animation = new GVROpacityAnimation(object, FADE_DURATION, opacity);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                synchronized (FadeHandler.this) {
                    counter--;
                    if (counter == 0) {
                        onFinish.onFadeFinished();
                    }
                }
            }
        });
        animation.start(object.getGVRContext().getAnimationEngine());
    }
}
