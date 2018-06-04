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

import android.os.Message;
import android.util.Log;
import android.os.Handler;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.ICloudAnchorListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class SampleMain extends GVRMain {
    private static String TAG = "GVR_ARCORE";
    private final int MAX_VIRTUAL_OBJECTS = 20;

    private GVRContext mGVRContext;
    private GVRScene mainScene;

    private GVRMixedReality mixedReality;
    private SampleHelper mHelper;

    private List<GVRAnchor> mVirtualObjects;
    private int mVirtObjCount = 0;

    private BluetoothService mBluetoothService;
    private BluetoothSetup mBluetoothSetup;
    private boolean mCloudAnchorEnabled;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mHelper = new SampleHelper();
        mVirtualObjects = new ArrayList<>();
        mVirtObjCount = 0;
        mCloudAnchorEnabled = false;
        TouchHandler touchHandler = new TouchHandler();

        mHelper.initCursorController(gvrContext, touchHandler);

        mBluetoothService = new BluetoothService(new MessageHandler(this));
        mBluetoothSetup = new BluetoothSetup(gvrContext, mBluetoothService);

        mixedReality = new GVRMixedReality(gvrContext, true, mainScene);
        mixedReality.registerPlaneListener(planeEventsListener);
        mixedReality.registerAnchorListener(anchorEventsListener);
        mixedReality.resume();

    }

    @Override
    public void onStep() {
        super.onStep();
        for (GVRAnchor anchor: mVirtualObjects) {
            for (GVRSceneObject obj: anchor.getChildren()) {
                ((VirtualObject) obj).reactToLightEnvironment(
                        mixedReality.getLightEstimate().getPixelIntensity());
            }
        }
    }

    void resolveAnchor(String cloudId) {
        mixedReality.resolveCloudAnchor(cloudId, cloudAnchorReceiveListener);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<SampleMain> mMainWeakReference;

        MessageHandler(SampleMain main) {
            mMainWeakReference = new WeakReference<>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BluetoothService.MessageConstants.MESSAGE_READ:
                    String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
                    mMainWeakReference.get().resolveAnchor(readMessage);
                    break;
                case BluetoothService.MessageConstants.CONNECTED:
                    mMainWeakReference.get().mCloudAnchorEnabled = true;
                    mMainWeakReference.get().mBluetoothSetup.removeButtons();
                    break;
            }
        }
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener() {
        @Override
        public void onPlaneDetection(GVRPlane gvrPlane) {
            gvrPlane.setSceneObject(mHelper.createQuadPlane(getGVRContext()));
            mainScene.addSceneObject(gvrPlane);
        }

        @Override
        public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState gvrTrackingState) {
            if (gvrTrackingState != GVRTrackingState.TRACKING) {
                gvrPlane.setEnable(false);
            } else {
                gvrPlane.setEnable(true);
            }
        }

        @Override
        public void onPlaneMerging(GVRPlane gvrPlane, GVRPlane gvrPlane1) {
        }
    };

    private IAnchorEventsListener anchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {
            if (gvrTrackingState != GVRTrackingState.TRACKING) {
                gvrAnchor.setEnable(false);
            } else {
                gvrAnchor.setEnable(true);
            }
        }
    };

    private ICloudAnchorListener cloudAnchorListener = new ICloudAnchorListener() {
        @Override
        public void onTaskComplete(GVRAnchor anchor) {
            mBluetoothService.write(anchor.getCloudAnchorId().getBytes(Charset.defaultCharset()));
        }
    };

    private ICloudAnchorListener cloudAnchorReceiveListener = new ICloudAnchorListener() {
        @Override
        public void onTaskComplete(GVRAnchor gvrAnchor) {
            VirtualObject andy = new VirtualObject(mGVRContext);

            addVirtualObjectFromCloud(gvrAnchor, andy);
        }
    };


    public class TouchHandler extends GVREventListeners.TouchEvents {
        private GVRSceneObject mDraggingObject = null;


        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onEnter(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject() || mDraggingObject != null ||
                    sceneObj.getTag() == "GVRButton" || sceneObj.getTag() == "GVRListView") {
                return;
            }

            ((VirtualObject) sceneObj).onPickEnter();
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject() ||
                    sceneObj.getTag() == "GVRButton" || sceneObj.getTag() == "GVRListView") {

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

            if (sceneObj == mixedReality.getPassThroughObject() ||
                    sceneObj.getTag() == "GVRButton" || sceneObj.getTag() == "GVRListView") {

                return;
            }

            if (mDraggingObject == null) {
                mDraggingObject = sceneObj;
                Log.d(TAG, "onStartDragging");
                ((VirtualObject) sceneObj).onTouchStart();
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
                    ((VirtualObject) mDraggingObject).onTouchEnd();
                }
                mDraggingObject = null;
            } else if (sceneObj == mixedReality.getPassThroughObject()) {
                onSingleTap(sceneObj, pickInfo);
            } else if (sceneObj.getTag() == "GVRButton") {
                ((BluetoothSetup.GVRButton) sceneObj).getOnClickListener().onClick();
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
                    mixedReality.updateAnchorPose((GVRAnchor) mDraggingObject.getParent(),
                            gvrHitResult.getPose());
                }
            }
        }

        private GVRPicker.GVRPickedObject pickSceneObject(GVRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            mHelper.getCursorController().getPicker().getWorldPickRay(origin, direction);

            return GVRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
        }

        private void onSingleTap(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            GVRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            VirtualObject andy = new VirtualObject(mGVRContext);

            if (gvrHitResult == null) {
                return;
            }

            addVirtualObject(gvrHitResult.getPose(), andy);
        }
    }

    private void addVirtualObject(float[] pose, VirtualObject andy) {
        GVRAnchor anchor;

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS) {
            anchor = mixedReality.createAnchor(pose, andy);
            if (mCloudAnchorEnabled) {
                mixedReality.hostAnchor(anchor, cloudAnchorListener);
            }

            mainScene.addSceneObject(anchor);
            mVirtualObjects.add(anchor);
        } else {
            anchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
            mixedReality.updateAnchorPose(anchor, pose);
        }

        anchor.setName("id: " + mVirtObjCount);
        Log.d(TAG, "New virtual object " + anchor.getName());

        mVirtObjCount++;
    }

    private void addVirtualObjectFromCloud(GVRAnchor cloudAnchor, VirtualObject andy) {
        cloudAnchor.attachSceneObject(andy);

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS) {
            mainScene.addSceneObject(cloudAnchor);
            mVirtualObjects.add(cloudAnchor);
        } else {
            GVRAnchor oldAnchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
            mVirtualObjects.add(mVirtObjCount % mVirtualObjects.size(), cloudAnchor);
            mainScene.addSceneObject(cloudAnchor);

            mixedReality.removeAnchor(oldAnchor);
        }

        cloudAnchor.setName("id: " + mVirtObjCount);
        Log.d(TAG, "New virtual object " + cloudAnchor.getName());

        mVirtObjCount++;
    }
}