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

package org.gearvrf.arpet;

import android.support.annotation.NonNull;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;

/**
 * Class representing a {@link GVRSceneObject} with an {@link GVRAnchor} and its AR pose matrix
 */
public abstract class AnchoredObject extends GVRSceneObject {

    final String TAG = getClass().getSimpleName();

    private GVRAnchor mAnchor;
    private float[] mPoseMatrix;
    private GVRMixedReality mMixedReality;

    public AnchoredObject(@NonNull GVRContext context, @NonNull GVRMixedReality mixedReality, @NonNull float[] poseMatrix) {
        super(context);
        this.mMixedReality = mixedReality;
        this.mPoseMatrix = poseMatrix;
        this.mAnchor = mixedReality.createAnchor(poseMatrix, this);
    }

    public void updatePose(float[] poseMatrix) {
        mPoseMatrix = poseMatrix;
        mMixedReality.updateAnchorPose(mAnchor, poseMatrix);
    }

    public GVRAnchor getAnchor() {
        return mAnchor;
    }

    public float[] getPoseMatrix() {
        return mPoseMatrix;
    }

    public GVRMixedReality getMixedReality() {
        return mMixedReality;
    }
}
