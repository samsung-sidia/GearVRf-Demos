/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arcore.augmentedimage;

import android.graphics.Bitmap;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRAugmentedImage;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAugmentedImageEventsListener;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;

public class SampleMain extends GVRMain {
    private static String TAG = "GVR_ARCORE_AUGMENTEDIMAGE";

    private GVRContext mGVRContext;
    private GVRScene mainScene;

    private GVRSceneObject mUpperLeft;
    private GVRSceneObject mUpperRight;
    private GVRSceneObject mLowerLeft;
    private GVRSceneObject mLowerRight;
    private GVRAnchor mAnchor;

    private GVRMixedReality mixedReality;

    private ArrayList<Bitmap> mImagesList;
    private ArrayList<GVRAugmentedImage> mAugmentedImages;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();

        mixedReality = new GVRMixedReality(gvrContext, mainScene);
        mixedReality.registerAugmentedImageListener(new IAugmentedImageEventsListener() {
            @Override
            public void onAugmentedImageDetection(GVRAugmentedImage gvrAugmentedImage) {
                mAugmentedImages.add(gvrAugmentedImage);
            }

            @Override
            public void onAugmentedImageStateChange(GVRAugmentedImage gvrAugmentedImage, GVRTrackingState gvrTrackingState) {
                Log.d(TAG, gvrTrackingState + "");
            }
        });
        mixedReality.resume();
        mixedReality.setAugmentedImages(mImagesList);

        mAugmentedImages = new ArrayList<>();

        load3DModels(gvrContext);
    }

    private void load3DModels(GVRContext gvrContext) {
        //Load the 3D models which will be shown when the image is detected
        //They are simple objects and they will be put in each corner of the detected image
        try {
            mUpperLeft = mGVRContext.getAssetLoader().loadModel("objects/frame_upper_left.obj");
            mUpperRight = mGVRContext.getAssetLoader().loadModel("objects/frame_upper_right.obj");
            mLowerLeft = mGVRContext.getAssetLoader().loadModel("objects/frame_lower_left.obj");
            mLowerRight = mGVRContext.getAssetLoader().loadModel("objects/frame_lower_right.obj");

            float[] matrix = new float[16];
            mAnchor = mixedReality.createAnchor(matrix);
            mAnchor.attachSceneObject(mUpperLeft);
            mAnchor.attachSceneObject(mUpperRight);
            mAnchor.attachSceneObject(mLowerLeft);
            mAnchor.attachSceneObject(mLowerRight);

            mainScene.addSceneObject(mAnchor);
            mAnchor.setEnable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStep() {
        super.onStep();

        for (GVRAugmentedImage augmentedImage : mAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    mAnchor.setEnable(false);
                    break;
                case TRACKING:
                    if (!mAnchor.isEnabled()) {
                        mAnchor.setEnable(true);
                    }
                    mixedReality.updateAnchorPose(mAnchor, augmentedImage.getCenterPose());
                    float imageExtentX = augmentedImage.getExtentX();
                    float imageExtentZ = augmentedImage.getExtentZ();
                    //Just update the 3D objects according to the detected image center and its size
                    mUpperLeft.getTransform().setPosition(-0.5f * imageExtentX, 0.0f, -0.5f * imageExtentZ);
                    mUpperRight.getTransform().setPosition(0.5f * imageExtentX, 0.0f, -0.5f * imageExtentZ);
                    mLowerLeft.getTransform().setPosition(-0.5f * imageExtentX, 0.0f, 0.5f * imageExtentZ);
                    mLowerRight.getTransform().setPosition(0.5f * imageExtentX, 0.0f, 0.5f * imageExtentZ);
                    break;
                case STOPPED:
                    mAnchor.setEnable(false);
                    break;
                default:
                    break;
            }
        }
    }

    public void setAugmentedImages(ArrayList<Bitmap> imagesList) { mImagesList = imagesList;}
}