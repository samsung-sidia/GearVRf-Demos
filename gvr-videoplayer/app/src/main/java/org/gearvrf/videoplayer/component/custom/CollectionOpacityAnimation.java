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

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;

import java.util.HashMap;
import java.util.Map;

public final class CollectionOpacityAnimation extends GVRAnimation {

    private Map<GVRSceneObject, AnimationConfiguration> mConfiguration = new HashMap<>();

    CollectionOpacityAnimation(CollectionSceneObject target, float duration, float opacity) {
        super(target, duration);
        for (GVRSceneObject sceneObject : target.getCollection()) {
            if (!mConfiguration.containsKey(sceneObject)) {
                mConfiguration.put(sceneObject, new AnimationConfiguration(
                        sceneObject.getRenderData().getMaterial(), opacity));
            }
        }
    }

    @Override
    protected final void animate(GVRHybridObject target, float ratio) {

        for (GVRSceneObject sceneObject : ((CollectionSceneObject) target).getCollection()) {

            AnimationConfiguration configuration = mConfiguration.get(sceneObject);
            float opacity = configuration.mDeltaOpacity * ratio;

            if (configuration.mInitialColor != null) {
                configuration.mMaterial.setVec4(
                        "diffuse_color",
                        configuration.mInitialColor[0],
                        configuration.mInitialColor[1],
                        configuration.mInitialColor[2],
                        configuration.mInitialColor[3] + opacity);
            } else {
                configuration.mMaterial.setOpacity(configuration.mInitialOpacity + opacity);
            }
        }
    }

    private static class AnimationConfiguration {

        GVRMaterial mMaterial;
        float mInitialOpacity;
        float mDeltaOpacity;
        float[] mInitialColor;

        AnimationConfiguration(GVRMaterial mMaterial, float opacity) {

            this.mMaterial = mMaterial;

            if (mMaterial.hasUniform("u_opacity")) {
                this.mInitialOpacity = mMaterial.getOpacity();
                this.mDeltaOpacity = opacity - mInitialOpacity;
                this.mInitialColor = null;
            } else if (mMaterial.hasUniform("diffuse_color")) {
                this.mInitialOpacity = 1.0f;
                this.mInitialColor = mMaterial.getVec4("diffuse_color");
                this.mDeltaOpacity = opacity - mInitialColor[3];
            } else {
                throw new UnsupportedOperationException("Material must have u_opacity or diffuse_color to animate opacity");
            }
        }
    }
}


