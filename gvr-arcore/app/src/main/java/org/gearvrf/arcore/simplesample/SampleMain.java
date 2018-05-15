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
import android.util.Log;

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
import org.joml.Vector3f;

import java.util.EnumSet;

public class SampleMain extends GVRMain implements IPlaneEventsListener, IAnchorEventsListener {
    private static String TAG = "GVR_ARCORE";
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
            gvrAnchor.setEnable(false);
        }
        else {
            gvrAnchor.setEnable(true);
        }
    }


    private class TouchHandler extends GVREventListeners.TouchEvents {
        private GVRSceneObject mDraggingObject = null;


        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onEnter(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject() || mDraggingObject != null) {
                return;
            }

            ((VirtualObject)sceneObj).onPickEnter();
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                if (mDraggingObject != null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                    mDraggingObject = null;
                }
                return;
            }

            if (mDraggingObject == null) {
                ((VirtualObject) sceneObj).onPickExit();
            }
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchStart(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                return;
            }

            if (mDraggingObject == null) {
                mDraggingObject = sceneObj;
                Log.d(TAG, "onStartDragging");
                ((VirtualObject)sceneObj).onTouchStart();
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchEnd(sceneObj, pickInfo);


            if (mDraggingObject != null) {
                Log.d(TAG, "onStopDragging");

                if (pickSceneObject(mDraggingObject) == null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                } else {
                    ((VirtualObject)mDraggingObject).onTouchEnd();
                }
                mDraggingObject = null;
            } else if (sceneObj == mixedReality.getPassThroughObject()) {
                onSingleTap(sceneObj, pickInfo);
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onInside(sceneObj, pickInfo);

            if (mDraggingObject == null)
                return;

            pickInfo = pickSceneObject(mixedReality.getPassThroughObject());
            if (pickInfo != null) {
                GVRHitResult gvrHitResult = mixedReality.hitTest(
                        mixedReality.getPassThroughObject(), pickInfo);

                if (gvrHitResult != null) {
                    mixedReality.updateAnchorPose((GVRAnchor)mDraggingObject.getParent(),
                            gvrHitResult.getPose());
                }
            }
        }

        private GVRPicker.GVRPickedObject pickSceneObject(GVRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            mCursorController.getPicker().getWorldPickRay(origin, direction);

            return GVRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
        }

        private void onSingleTap(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            GVRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            VirtualObject andy = new VirtualObject(mGVRContext);

            if (gvrHitResult == null) {
                return;
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