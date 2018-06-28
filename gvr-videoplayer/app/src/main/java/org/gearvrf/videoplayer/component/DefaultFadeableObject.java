package org.gearvrf.videoplayer.component;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.videoplayer.component.custom.FadeableSceneObject;

public class DefaultFadeableObject extends FadeableSceneObject {

    public DefaultFadeableObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public DefaultFadeableObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
    }
}
