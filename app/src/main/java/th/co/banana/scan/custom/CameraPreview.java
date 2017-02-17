package th.co.banana.scan.custom;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import th.co.banana.scan.tag.Action;

/**
 * Created by MSILeopardPro on 14/2/2560.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int PreviewSizeHeight,PreviewSizeWidth;
    private Camera.PreviewCallback previewCallback;

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    public void setPreviewSize(int previewSizeHeight, int previewSizeWidth) {
        PreviewSizeHeight = previewSizeHeight;
        PreviewSizeWidth = previewSizeWidth;
    }

    public SurfaceHolder getmHolder() {
        return mHolder;
    }

    public void setmCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        if(mCamera!=null){
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(Action.TAG_CAMERA, "Error setting camera preview: " + e.getMessage());
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters;

        parameters = mCamera.getParameters();
        if (PreviewSizeWidth != 0) {
            // Set the camera preview size
            parameters.setPreviewSize(PreviewSizeHeight, PreviewSizeWidth);
            // Set the take picture size, you can set the large size of the camera supported.
            parameters.setPictureSize(PreviewSizeHeight, PreviewSizeWidth);
        }
        // Set the auto-focus.
        String NowFocusMode = parameters.getFocusMode();
        if (NowFocusMode != null)
            parameters.setFocusMode("auto");

        mCamera.setParameters(parameters);

        mCamera.setDisplayOrientation(90);

        mCamera.setPreviewCallback(previewCallback);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(Action.TAG_CAMERA, "Error starting camera preview: " + e.getMessage());
        }
    }
}
