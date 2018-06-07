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
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SampleActivity extends GVRActivity {
    private static final String TAG = "GVR_ARCORE_AUGMENTEDIMAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        SampleMain mMain = new SampleMain();
        setMain(mMain, "gvr.xml");

        Bitmap image = loadImageBitmap();
        ArrayList<Bitmap> imagesList = new ArrayList<>();
        if (image != null) {
            imagesList.add(image);
            mMain.setAugmentedImages(imagesList);
        }
    }

    private Bitmap loadImageBitmap() {
        try (InputStream is = getAssets().open("default.jpg")) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }
}
