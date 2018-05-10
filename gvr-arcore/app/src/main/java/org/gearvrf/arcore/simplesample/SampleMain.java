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

package org.gearvrf.arcore.simplesample;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.io.IOException;
import java.util.EnumSet;

public class SampleMain extends GVRMain implements IPlaneEventsListener, IAnchorEventsListener {
    GVRMixedReality mixedReality;
    GVRContext mGVRContext;
    GVRScene mainScene;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;
    private TouchHandler mTouchHandler;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mTouchHandler = new TouchHandler();
        initCursorController(gvrContext);


        mixedReality = new GVRMixedReality(gvrContext, mainScene);
        mixedReality.registerPlaneListener(this);
        mixedReality.registerAnchorListener(this);
    }

    @Override
    public void onPlaneDetection(GVRPlane gvrPlane) {
        gvrPlane.setSceneObject(createQuadPlane(getGVRContext()));
    }

    @Override
    public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState gvrTrackingState) {
        if (gvrTrackingState != GVRTrackingState.TRACKING) {
            gvrPlane.getSceneObject().setEnable(false);
        }
        else {
            gvrPlane.getSceneObject().setEnable(true);
        }
    }

    @Override
    public void onPlaneMerging(GVRPlane childPlane, GVRPlane parentPlane) {
    }

    @Override
    public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {
        if (gvrTrackingState != GVRTrackingState.TRACKING) {
            gvrAnchor.getSceneObject().setEnable(false);
        }
        else {
            gvrAnchor.getSceneObject().setEnable(true);
        }
    }


    private class TouchHandler extends GVREventListeners.TouchEvents {
        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);

            GVRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            GVRSceneObject andy = null;

            if (gvrHitResult == null) {
                return;
            }

            try {
                andy = mGVRContext.getAssetLoader().loadModel("objects/andy.obj");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mixedReality.createAnchor(gvrHitResult.getPose(), andy);
        }
    }


    private void initCursorController(GVRContext gvrContext) {
        mainScene.getEventReceiver().addListener(mTouchHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * 100,
                        0.2f * 100),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-100f);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }


    private int hsvHUE = 0;
    private GVRSceneObject createQuadPlane(GVRContext gvrContext) {
        GVRMesh mesh = GVRMesh.createQuad(gvrContext,
                "float3 a_position", 1.0f, 1.0f);

        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);

        GVRSceneObject polygonObject = new GVRSceneObject(gvrContext, mesh, mat);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f; hsv[2] = 1f;

        int c =  Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f,Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.2f);

        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);

        return polygonObject;
    }
}