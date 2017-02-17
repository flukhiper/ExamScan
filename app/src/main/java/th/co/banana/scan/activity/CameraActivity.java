package th.co.banana.scan.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import th.co.banana.scan.R;
import th.co.banana.scan.custom.CameraPreview;
import th.co.banana.scan.model.CustomGallery;
import th.co.banana.scan.util.CameraCheckAndFileUtil;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    private ImageView pPreview;
    private RelativeLayout pbPreview;
    private ImageButton cSnap, cDone, cOkay, cCan;

    private Bitmap bitmap, bitmapR;

    private ArrayList<CustomGallery> dataT = new ArrayList<>();
    private float widthF, heightF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initLayout();

        initCamera();
    }

    private void initLayout() {
        pPreview = (ImageView) findViewById(R.id.picture_preview);
        pbPreview = (RelativeLayout) findViewById(R.id.picture_preview_background);

        cSnap = (ImageButton) findViewById(R.id.click_snap);
        cDone = (ImageButton) findViewById(R.id.click_done);

        cOkay = (ImageButton) findViewById(R.id.click_okay);
        cCan = (ImageButton) findViewById(R.id.click_cancel);

        cSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbPreview.animate().alpha(1.0f).setDuration(200)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                pbPreview.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                Matrix matrixR = new Matrix();

                matrixR.postRotate(90);

                bitmapR = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrixR, true);

                Matrix matrixS = new Matrix();
                if (widthF < heightF) {
                    matrixS.postScale(widthF, widthF);
                } else {
                    matrixS.postScale(heightF, heightF);
                }

                Bitmap scaledBitmap = Bitmap.createBitmap(bitmapR, 0, 0, bitmapR.getWidth(), bitmapR.getHeight(), matrixS, false);

                pPreview.setImageBitmap(scaledBitmap);

            }
        });

        cDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CameraActivity.this, ProcessActivity.class);
                i.putExtra("From", "Camera");
                i.putParcelableArrayListExtra("DataT", dataT);
                startActivity(i);
                finish();
            }
        });

        cOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbPreview.animate().alpha(0.0f).setDuration(1000)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                pbPreview.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                File pictureFile = CameraCheckAndFileUtil.getOutputMediaFile(MEDIA_TYPE_IMAGE);

                try {
                    FileOutputStream out = new FileOutputStream(pictureFile);
                    bitmapR.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.d("Exception", e.getMessage());
                }

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(pictureFile);
                mediaScanIntent.setData(contentUri);
                CameraActivity.this.sendBroadcast(mediaScanIntent);

                CustomGallery item = new CustomGallery();
                item.sdcardPath = pictureFile.getAbsolutePath();

                dataT.add(item);

            }
        });

        cCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbPreview.animate().alpha(0.0f).setDuration(1000)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                pbPreview.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
            }
        });
    }

    private void initCamera() {
        // Create an instance of Camera
        if (checkCameraHardware(this)) {
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            mPreview.setPreviewSize(640, 480);
            mPreview.setPreviewCallback(previewCallback);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

            ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
//
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            widthF = ((float) displaymetrics.widthPixels) / 480.0f;

            heightF = ((float) displaymetrics.heightPixels) / 640.0f;
//
            float factor = 0;

            if (widthF < heightF) {
                factor = widthF;
            } else {
                factor = heightF;
            }

            layoutParams.height = (int) (640.0f * factor);
            layoutParams.width = (int) (640.0f * factor);

            preview.addView(mPreview);
        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();  // release the camera immediately on pause event
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), parameters.getJpegQuality(), out);

            byte[] bytes = out.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    };

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            mPreview.setmCamera(null);
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
