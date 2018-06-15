package org.gearvrf.videoplayer.component.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.model.GalleryItem;
import org.gearvrf.videoplayer.model.HomeItem;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.AlbumAsyncTask;
import org.gearvrf.videoplayer.provider.asyntask.GetDataCallback;
import org.gearvrf.videoplayer.provider.asyntask.VideoAsyncTask;

import java.util.LinkedList;
import java.util.List;

public class Gallery extends FadeableObject implements OnItemsSelectionListener {

    private static final String TAG = Gallery.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private GVRViewSceneObject mObjectViewGallery;
    private List<GalleryItem> mItemList = new LinkedList<>();
    private Breadcrumb mBreadcrumb;
    private OnGalleryEventListener mOnGalleryEventListener;

    @SuppressLint("InflateParams")
    public Gallery(GVRContext gvrContext) {
        super(gvrContext);

        View mainView = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.gallery_layout, null);
        mRecyclerView = mainView.findViewById(R.id.recycler_view);

        GalleryItemAdapter adapterGallery = new GalleryItemAdapter<>(mItemList);
        adapterGallery.setOnItemSelectionListener(this);
        mRecyclerView.setAdapter(adapterGallery);
        mRecyclerView.setLayoutManager(new GridLayoutManager(gvrContext.getContext(), 3));

        mObjectViewGallery = new GVRViewSceneObject(gvrContext, mainView, 1, 1);
        mObjectViewGallery.getTransform().setScale(6, 6, 1);
        addChildObject(mObjectViewGallery);

        mBreadcrumb = new Breadcrumb(mainView.findViewById(R.id.breadcrumb));
        mBreadcrumb.showHome();
        mBreadcrumb.setOnBreadcrumbListener(new OnBreadcrumbListener() {
            @Override
            public void onHomeClicked() {
                loadHome();
            }

            @Override
            public void onSourceClicked(@SourceType int sourceType) {
                if (sourceType == SourceType.LOCAL) {
                    loadAlbums();
                } else if (sourceType == SourceType.EXTERNAL) {
                    // loadExternal();
                }
            }
        });

        loadHome();
    }

    private void loadHome() {
        mItemList.clear();
        mItemList.addAll(createHomeItems());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void loadSources() {

    }

    private void loadAlbums() {
        new AlbumAsyncTask(new GetDataCallback<List<Album>>() {
            @Override
            public void onResult(List<Album> data) {
                mItemList.clear();
                mItemList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    private void loadVideos(String albumTitle) {
        new VideoAsyncTask(albumTitle, new GetDataCallback<List<Video>>() {
            @Override
            public void onResult(List<Video> data) {
                mItemList.clear();
                mItemList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    @Override
    public void onItemSelected(List<? extends GalleryItem> itemList) {

        GalleryItem item = itemList.get(0);

        switch (item.getType()) {
            case GalleryItem.Type.TYPE_HOME:
                //TODO loadSources();
                break;
            case GalleryItem.Type.TYPE_ALBUM:
                loadVideos(((Album) item).getTitle());
                mBreadcrumb.showAlbum(((Album) item).getTitle());
                break;
            case GalleryItem.Type.TYPE_VIDEO:
                if (mOnGalleryEventListener != null) {
                    mOnGalleryEventListener.onVideosSelected((List<Video>) itemList);
                }
                break;
            default:
                Log.d(TAG, "Unknown type: " + item.getType());
        }
    }

    public void setOnGalleryEventListener(OnGalleryEventListener listener) {
        this.mOnGalleryEventListener = listener;
    }

    private List<HomeItem> createHomeItems() {
        Context context = getGVRContext().getContext();
        List<HomeItem> homeItems = new LinkedList<>();
        homeItems.add(new HomeItem(context.getString(R.string.gallery_source_type_local), R.drawable.ic_folder_local_videos));
        homeItems.add(new HomeItem(context.getString(R.string.gallery_source_type_external), R.drawable.ic_folder_external_videos));
        return homeItems;
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mObjectViewGallery;
    }
}

