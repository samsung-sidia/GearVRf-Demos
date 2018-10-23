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

package org.gearvrf.arpet.mode;

import android.support.annotation.IntDef;
import android.view.View;
import android.view.ViewGroup;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ShareAnchorView2 extends BasePetView implements IViewEvents {

    private ViewGroup mMainView;

    @IntDef({
            R.layout.view_lets_start,
            R.layout.view_waiting_for_guests,
            R.layout.view_waiting_for_host,
            R.layout.view_connection_found,
            R.layout.view_no_connection_found,
            R.layout.view_waiting_message,
            R.layout.view_anchor_shared,
            R.layout.view_sharing_error,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewOption {
    }

    private @UserType
    int userType;

    @IntDef({UserType.GUEST, UserType.HOST})
    public @interface UserType {
        int GUEST = 0;
        int HOST = 1;
    }

    public ShareAnchorView2(PetContext petContext) {
        super(petContext);
        GVRViewSceneObject viewObject = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.view_sharing_anchor_main, this);
        viewObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        getTransform().setPosition(0.0f, 0.0f, -0.9f);
    }

    public void showView(@ViewOption int view) {
        mPetContext.getActivity().runOnUiThread(() -> {
            mMainView.removeAllViews();
            View child = View.inflate(mPetContext.getGVRContext().getContext(), view, mMainView);
        });
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mMainView = (ViewGroup) view;
    }

    @Override
    public void onStartRendering(GVRViewSceneObject viewObject, View view) {
        viewObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(viewObject);
        showView(R.layout.view_waiting_for_guests);
    }
}
