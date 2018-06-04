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


import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.util.ArrayList;
import java.util.Set;

class BluetoothSetup {
    private static String TAG = "GVR_ARCORE";

    private GVRContext mGVRContext;
    private BluetoothService mBTHelper;

    private GVRButton mHostButton;
    private GVRButton mResolveButton;
    private GVRButton mSkipButton;
    private GVRButton mCancelButton;
    private GVRButton mHostingView;
    private GVRViewSceneObject mDeviceListView;

    BluetoothSetup(GVRContext gvrContext, BluetoothService btHelper) {
        mBTHelper = btHelper;
        mGVRContext = gvrContext;

        init();
        showButtons();
    }

    private void init() {
        mHostButton = createHostButton();
        mResolveButton = createResolveButton();
        mSkipButton = createSkipButton();
        mCancelButton = createCancelButton();
        mHostingView = createHostingView();
        mDeviceListView = createListOfPairedDevices(mBTHelper.getPairedDevices());
    }

    private void showButtons() {
        mHostButton.setEnable(true);
        mResolveButton.setEnable(true);
        mSkipButton.setEnable(true);
    }
    public void removeButtons() {
        mGVRContext.getMainScene().removeSceneObject(mHostButton);
        mGVRContext.getMainScene().removeSceneObject(mResolveButton);
        mGVRContext.getMainScene().removeSceneObject(mSkipButton);
        mGVRContext.getMainScene().removeSceneObject(mHostingView);
        mGVRContext.getMainScene().removeSceneObject(mDeviceListView);
        mGVRContext.getMainScene().removeSceneObject(mCancelButton);
    }

    private GVRButton createHostButton() {
        GVRButton hostButton = new GVRButton(mGVRContext, 2f, 1f, "HOST");
        hostButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                mBTHelper.startAccpetingRequests();
                mHostButton.setEnable(false);
                mResolveButton.setEnable(false);
                mSkipButton.setEnable(false);
                mHostingView.setEnable(true);
            }
        });

        hostButton.getTransform().setPosition(-2.5f, 0f, -10f);
        mGVRContext.getMainScene().addSceneObject(hostButton);

        return hostButton;
    }


    private GVRButton createResolveButton() {
        GVRButton resolveButton = new GVRButton(mGVRContext, 2f, 1f, "RESOLVE");
        resolveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                mDeviceListView.setEnable(true);
                mCancelButton.setEnable(true);
            }
        });

        resolveButton.getTransform().setPosition(0f, 0f, -10f);
        mGVRContext.getMainScene().addSceneObject(resolveButton);

        return resolveButton;
    }

    private GVRButton createSkipButton() {
        GVRButton skipButton = new GVRButton(mGVRContext, 2f, 1f, "SKIP");
        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                removeButtons();
            }
        });

        skipButton.getTransform().setPosition(2.5f, 0f, -10f);
        mGVRContext.getMainScene().addSceneObject(skipButton);

        return skipButton;
    }

    private GVRButton createCancelButton() {
        GVRButton cancelButton = new GVRButton(mGVRContext, 2f, 1f, "CANCEL");
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                mDeviceListView.setEnable(false);
                mCancelButton.setEnable(false);
            }
        });

        cancelButton.getTransform().setPosition(3f, -3f, -10f);
        mGVRContext.getMainScene().addSceneObject(cancelButton);

        return cancelButton;
    }

    private GVRButton createHostingView() {
        GVRButton hostingView = new GVRButton(mGVRContext, 2f, 1f, "HOSTING");

        hostingView.getTransform().setPosition(0f, 0f, -10f);
        mGVRContext.getMainScene().addSceneObject(hostingView);

        return hostingView;
    }

    private GVRViewSceneObject createListOfPairedDevices(final Set<BluetoothDevice> devicesList) {
        IViewEvents viewEvents = new IViewEvents() {
            @Override
            public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
                ArrayList<String> devicesName = new ArrayList<>();
                final ArrayList<BluetoothDevice> btDevices = new ArrayList<>();

                for (BluetoothDevice device: devicesList) {
                    btDevices.add(device);
                    devicesName.add(device.getName());
                }

                ListView listView = view.findViewById(R.id.deviceList);
                listView.setBackgroundColor(Color.GRAY);
                listView.setAdapter(new ArrayAdapter<>(mGVRContext.getActivity(),
                        R.layout.device_name, devicesName));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Log.d(TAG, "connect to " + btDevices.get(i).getName());
                        mBTHelper.connectToDevice(btDevices.get(i));
                    }
                });
            }

            @Override
            public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
                gvrViewSceneObject.getTransform().setPosition(0f, 0f, -1f);
                mGVRContext.getMainScene().addSceneObject(gvrViewSceneObject);
                gvrViewSceneObject.setEnable(false);
                gvrViewSceneObject.setTag("GVRListView");
            }
        };

        return new GVRViewSceneObject(mGVRContext, R.layout.layout_view, viewEvents);
    }

    class GVRButton extends GVRTextViewSceneObject {
        private OnClickListener mOnClickListener;

        GVRButton(GVRContext gvrContext, float width, float height, String text) {
            super(gvrContext, width, height, text);

            setTag("GVRButton");
            setTextColor(Color.WHITE);
            setBackgroundColor(Color.GRAY);
            setGravity(Gravity.CENTER);
            setEnable(false);

            attachCollider(new GVRMeshCollider(gvrContext, getRenderData().getMesh()));
        }

        void setOnClickListener(OnClickListener onClickListener) {
            mOnClickListener = onClickListener;
        }

        OnClickListener getOnClickListener() {
            return mOnClickListener;
        }
    }

    interface OnClickListener {
        void onClick();
    }
}
