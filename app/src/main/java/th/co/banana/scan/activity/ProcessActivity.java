package th.co.banana.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import th.co.banana.scan.R;
import th.co.banana.scan.adapter.ProcessAdapter;
import th.co.banana.scan.manager.ScanManager;
import th.co.banana.scan.model.CustomGallery;
import th.co.banana.scan.model.SheetDateModel;
import th.co.banana.scan.tag.Action;
import th.co.banana.scan.util.CameraCheckAndFileUtil;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class ProcessActivity extends AppCompatActivity {

    private ImageLoader imageLoader;
    private String from;

    private ArrayList<CustomGallery> dataT = new ArrayList<>();
    private TextView test;

    private String answers;
    private ProcessAdapter adapter;

    private boolean process = false;
    private boolean started = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        from = getIntent().getStringExtra("From");

        test = (TextView) findViewById(R.id.test);

        adapter = new ProcessAdapter();

        RecyclerView recycleView = (RecyclerView) findViewById(R.id.recyclerView);
        recycleView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recycleView.setHasFixedSize(true);
        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(adapter);

        initImageLoader();

        init();
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {
        if (from.equals("Gallery")) {
            Intent i = new Intent();
            i.setAction(Action.ACTION_MULTIPLE_PICK);
            startActivityForResult(i, 200);
            started = true;
        } else {
            dataT = new ArrayList<>();

            dataT = getIntent().getParcelableArrayListExtra("DataT");

            started = false;
        }

        ScanManager.getInstance().setDateModels(new ArrayList<SheetDateModel>());
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (dataT.size() != 0) {

            process = false;

            CustomGallery gallery = new CustomGallery(dataT.get(0).isSeleted, dataT.get(0).sdcardPath);

            dataT.remove(0);

            Intent i = new Intent(ProcessActivity.this, CropActivity.class);

            i.putExtra("CustomGallery", gallery);

            startActivity(i);
        } else if (!started) {
            process = true;
        }

        if (process) {
            test.setText("size : " + ScanManager.getInstance().getDateModels().size());

            for (SheetDateModel model :
                    ScanManager.getInstance().getDateModels()) {
                Bitmap bm = getBitmapPerspective(model);

                model.setBm(bm);

                model.setAnswers(answers);

                File pictureFile = CameraCheckAndFileUtil.getOutputMediaFile(MEDIA_TYPE_IMAGE);

                try {
                    FileOutputStream out = new FileOutputStream(pictureFile);
                    model.getBm().compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.d("Exception", e.getMessage());
                }

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(pictureFile);
                mediaScanIntent.setData(contentUri);
                ProcessActivity.this.sendBroadcast(mediaScanIntent);
            }

            adapter.setDateModels(ScanManager.getInstance().getDateModels());
            adapter.notifyDataSetChanged();

            ScanManager.getInstance().setDateModels(null);
        }
    }

    @NonNull
    private Bitmap getBitmapPerspective(SheetDateModel model) {
        Mat mBm = new Mat(model.getBm().getHeight(), model.getBm().getWidth(), CvType.CV_8UC4);

        Utils.bitmapToMat(model.getBm(), mBm);

        List<Point> order = model.getOrderTemp();
        List<Point> orderDst = new ArrayList<>();

        orderDst.add(new Point(0, 0));
        orderDst.add(new Point(model.getBm().getWidth(), 0));
        orderDst.add(new Point(model.getBm().getWidth(), model.getBm().getHeight()));
        orderDst.add(new Point(0, model.getBm().getHeight()));

        Mat srcP = Converters.vector_Point2f_to_Mat(order);
        Mat desP = Converters.vector_Point2f_to_Mat(orderDst);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcP, desP);
        Imgproc.warpPerspective(mBm.clone(), mBm, perspectiveTransform, mBm.size());
        //////////////////////////////////////////////////////////////////////////////
        Core.flip(mBm.t(), mBm, 0);

        Imgproc.cvtColor(mBm.clone(), mBm, Imgproc.COLOR_BGR2GRAY);

//        Core.inRange(mBm.clone(), new Scalar(0, 0, 0), new Scalar(80, 80, 80), mBm);

        Imgproc.adaptiveThreshold(mBm.clone(), mBm, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 19, 25);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> contoursNew = new ArrayList<>();
        List<MatOfPoint> contoursNew2 = new ArrayList<>();
        List<MatOfPoint> contoursNew3 = new ArrayList<>();

//        Bitmap bm = Bitmap.createBitmap(mBm.cols(), mBm.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mBm, bm);

        Mat temp = mBm.clone();

        Imgproc.morphologyEx(temp.clone(), temp, Imgproc.MORPH_OPEN,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        Imgproc.morphologyEx(mBm.clone(), mBm, Imgproc.MORPH_OPEN,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

//        Imgproc.morphologyEx(mBm.clone(), mBm, Imgproc.MORPH_OPEN,
//                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));


//        Bitmap bm = Bitmap.createBitmap(mBm.cols(), mBm.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mBm, bm);

//        Mat t = mBm.clone();

//        Imgproc.morphologyEx(t.clone(), t, Imgproc.MORPH_OPEN,
//                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
//
//        Bitmap bm = Bitmap.createBitmap(t.cols(), t.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(t, bm);

        Mat hierarchy = new Mat();

        Imgproc.findContours(mBm.clone(),
                contours,
                hierarchy,
                Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint m :
                contours) {
            Rect rect = Imgproc.boundingRect(m);

            if (rect.y >= mBm.rows() - (25 * 2) && rect.y <= mBm.rows() - (5 * 2)
                    && rect.x <= mBm.cols() - (25 * 2) && rect.x >= (15 * 2)) {
                contoursNew.add(m);
            }
        }

//        Mat mRotate = mBm.clone();
//
//        Imgproc.cvtColor(mRotate.clone(), mRotate, Imgproc.COLOR_GRAY2BGR);
//
//        for (int i = 0; i < contoursNew.size(); i++) {
//            Imgproc.drawContours(mRotate, contoursNew, i, new Scalar(255, 0, 0), 2, 8, hierarchy, 0, new Point());
//        }
//
//        Bitmap bm = Bitmap.createBitmap(mRotate.cols(), mRotate.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRotate, bm);

        Collections.sort(contoursNew, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                Rect rect1 = Imgproc.boundingRect(o1);
                Rect rect2 = Imgproc.boundingRect(o2);
                double a = rect1.x;
                double b = rect2.x;
                return Double.compare(a, b);
            }
        });

        Rect rect1 = Imgproc.boundingRect(contoursNew.get(0));
        Rect rect2 = Imgproc.boundingRect(contoursNew.get(contoursNew.size() - 1));

        double y = rect1.y - rect2.y;
        double x = Math.abs(rect1.x - rect2.x);

        Point center = new Point(0, 0);
        double angle;
        if (y < 0) {
            angle = (Math.asin(Math.abs(y) / x) * 180.00) / Math.PI;
        } else {
            angle = -(Math.asin(Math.abs(y) / x) * 180.00) / Math.PI;
        }
        double scale = 1;

        Mat mapMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
        Imgproc.warpAffine(mBm.clone(), mBm, mapMatrix, new Size(mBm.cols(), mBm.rows()), Imgproc.INTER_LINEAR);

        Imgproc.warpAffine(temp.clone(), temp, mapMatrix, new Size(temp.cols(), temp.rows()), Imgproc.INTER_LINEAR);


        contours = new ArrayList<>();
        contoursNew = new ArrayList<>();
        contoursNew2 = new ArrayList<>();
        contoursNew3 = new ArrayList<>();

        Imgproc.findContours(mBm.clone(),
                contours,
                hierarchy,
                Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint m :
                contours) {
            Rect rect = Imgproc.boundingRect(m);

            double v = Imgproc.contourArea(m);

            if (rect.y >= mBm.rows() - (25 * 2) && rect.y <= mBm.rows() - (5 * 2)
                    && rect.x <= mBm.cols() - (20 * 2) && rect.x >= (10 * 2)
                    && v > 0) {
                contoursNew.add(m);
            } else if (rect.x >= mBm.cols() - (25 * 2) && rect.x <= mBm.cols() - (5 * 2)
                    && rect.y <= mBm.rows() - (25 * 2) && rect.y >= (20 * 2)
                    && v > 0) {
                contoursNew2.add(m);
            } else {
                if (v < 125 && v > 25
                        && rect.x >= (10 * 2) && rect.x <= mBm.cols() - (25 * 2)
                        && rect.y >= (25 * 2) && rect.y <= mBm.rows() - (25 * 2)) {
//                    contoursNew3.add(m);
                }
            }
        }

//        Mat mRotate = mBm.clone();
//
//        Imgproc.cvtColor(mRotate.clone(), mRotate, Imgproc.COLOR_GRAY2BGR);
//
//        for (int i = 0; i < contoursNew.size(); i++) {
//            Imgproc.drawContours(mRotate, contoursNew, i, new Scalar(255, 0, 0), 2, 8, hierarchy, 0, new Point());
//        }
//
//        for (int i = 0; i < contoursNew2.size(); i++) {
//            Imgproc.drawContours(mRotate, contoursNew2, i, new Scalar(0, 0, 255), 2, 8, hierarchy, 0, new Point());
//        }
//
////        for (int i = 0; i < contoursNew3.size(); i++) {
////            Imgproc.drawContours(mRotate, contoursNew3, i, new Scalar(0, 255, 0), 2, 8, hierarchy, 0, new Point());
////        }
//
//        Bitmap bm = Bitmap.createBitmap(mRotate.cols(), mRotate.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRotate, bm);
//
//        mRotate.release();
//

//        Bitmap bm = Bitmap.createBitmap(mBm.cols(), mBm.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mBm, bm);

//        Bitmap bm = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(temp, bm);

        contours = new ArrayList<>();

        Imgproc.findContours(temp,
                contours,
                hierarchy,
                Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        temp.release();

        for (MatOfPoint m :
                contours) {
            Rect rect = Imgproc.boundingRect(m);

            double v = Imgproc.contourArea(m);

            if (rect.y >= mBm.rows() - (25 * 2) && rect.y <= mBm.rows() - (5 * 2)
                    && rect.x <= mBm.cols() - (25 * 2) && rect.x >= (10 * 2)
                    && v > 15) {
//                contoursNew.add(m);
            } else if (rect.x >= mBm.cols() - (25 * 2) && rect.x <= mBm.cols() - (5 * 2)
                    && rect.y <= mBm.rows() - (25 * 2) && rect.y >= (20 * 2)
                    && v > 15) {
//                contoursNew2.add(m);
            } else {
                if (v < 125 && v > 15
                        && rect.x >= (10 * 2) && rect.x <= mBm.cols() - (25 * 2)
                        && rect.y >= (25 * 2) && rect.y <= mBm.rows() - (25 * 2)) {
                    contoursNew3.add(m);
                }
            }
        }

        Mat mRotate = mBm.clone();

        Imgproc.cvtColor(mRotate.clone(), mRotate, Imgproc.COLOR_GRAY2BGR);

        for (int i = 0; i < contoursNew.size(); i++) {
            Imgproc.drawContours(mRotate, contoursNew, i, new Scalar(255, 0, 0), 2, 8, hierarchy, 0, new Point());
        }

        for (int i = 0; i < contoursNew2.size(); i++) {
            Imgproc.drawContours(mRotate, contoursNew2, i, new Scalar(0, 0, 255), 2, 8, hierarchy, 0, new Point());
        }

        for (int i = 0; i < contoursNew3.size(); i++) {
            Imgproc.drawContours(mRotate, contoursNew3, i, new Scalar(0, 255, 0), 2, 8, hierarchy, 0, new Point());
        }

        Bitmap bm = Bitmap.createBitmap(mRotate.cols(), mRotate.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRotate, bm);

        mRotate.release();

        Collections.sort(contoursNew, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                Rect rect1 = Imgproc.boundingRect(o1);
                Rect rect2 = Imgproc.boundingRect(o2);
                double a = rect1.x;
                double b = rect2.x;
                return Double.compare(a, b);
            }
        });


        Collections.sort(contoursNew2, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                Rect rect1 = Imgproc.boundingRect(o1);
                Rect rect2 = Imgproc.boundingRect(o2);
                double a = rect1.y;
                double b = rect2.y;
                return Double.compare(b, a);
            }
        });

        String[] table = new String[120];
        String[] tableId = new String[11];
        for (int i = 0; i < table.length; i++) {
            table[i] = "";
            if (i < 11) {
                tableId[i] = "";
            }
        }

        answers = contoursNew.size() + " , " + contoursNew2.size() + " , " + contoursNew3.size() + "\n";
        int count = -1;
        for (MatOfPoint m :
                contoursNew3) {
            count++;
            int row = 0;
            int col = 0;
            Rect rectNow = Imgproc.boundingRect(m);
            for (int i = 0; i < contoursNew.size(); i++) {
                Rect rectRow = Imgproc.boundingRect(contoursNew.get(i));
                Rect rectRow2;
                Rect rectRow3;
                if (i == 0) {

                    rectRow2 = Imgproc.boundingRect(contoursNew.get(i + 1));

                    if ((rectNow.x + ((double) rectNow.width / 2.0)) >= rectRow.x &&
                            (rectNow.x + ((double) rectNow.width / 2.0)) <= ((rectRow.x + rectRow.width) + rectRow2.x) / 2) {
                        row = i;
                        break;
                    }
                } else if (i == contoursNew.size() - 1) {

                    rectRow3 = Imgproc.boundingRect(contoursNew.get(i - 1));

                    if ((rectNow.x + ((double) rectNow.width / 2.0)) >= ((rectRow3.x + rectRow3.width) + rectRow.x) / 2 &&
                            (rectNow.x + ((double) rectNow.width / 2.0)) <= rectRow.x + rectRow.width) {
                        row = i;
                        break;
                    }

                } else {
                    rectRow2 = Imgproc.boundingRect(contoursNew.get(i + 1));
                    rectRow3 = Imgproc.boundingRect(contoursNew.get(i - 1));

                    if ((rectNow.x + ((double) rectNow.width / 2.0)) >= ((rectRow3.x + rectRow3.width) + rectRow.x) / 2 &&
                            (rectNow.x + ((double) rectNow.width / 2.0)) <= ((rectRow.x + rectRow.width) + rectRow2.x) / 2) {
                        row = i;
                        break;
                    }
                }
            }
            for (int i = contoursNew2.size() - 1; i >= 0; i--) {

                Rect rectCol = Imgproc.boundingRect(contoursNew2.get(i));
                Rect rectCol2;
                Rect rectCol3;

                if (i == contoursNew2.size() - 1) {

                    rectCol2 = Imgproc.boundingRect(contoursNew2.get(i - 1));

                    if ((rectNow.y + ((double) rectNow.height / 2.0)) >= rectCol.y &&
                            (rectNow.y + ((double) rectNow.height / 2.0)) <= ((rectCol.y + rectCol.height) + rectCol2.y) / 2) {
                        col = i;
                        break;
                    }
                } else if (i == 0) {
                    rectCol3 = Imgproc.boundingRect(contoursNew2.get(i + 1));

                    if ((rectNow.y + ((double) rectNow.height / 2.0)) >= ((rectCol3.y + rectCol3.height) + rectCol.y) / 2 &&
                            (rectNow.y + ((double) rectNow.height / 2.0)) <= rectCol.y + rectCol.height) {
                        col = i;
                        break;
                    }
                } else {

                    rectCol2 = Imgproc.boundingRect(contoursNew2.get(i - 1));
                    rectCol3 = Imgproc.boundingRect(contoursNew2.get(i + 1));

                    if ((rectNow.y + ((double) rectNow.height / 2.0)) >= ((rectCol3.y + rectCol3.height) + rectCol.y) / 2 &&
                            (rectNow.y + ((double) rectNow.height / 2.0)) <= ((rectCol.y + rectCol.height) + rectCol2.y) / 2) {
                        col = i;
                        break;
                    }
                }
            }

            if (row > 12) {
                int answer = col % 6;
                int set = col / 6;
                int choice = row - 13;
                if (answer != 0 && answer < 6) {

                    Mat mCrop = new Mat(mBm.rows(), mBm.cols(), CvType.CV_8UC1, new Scalar(0));

                    Imgproc.drawContours(mCrop, contoursNew3, count, new Scalar(255), -1);

                    mBm.copyTo(mCrop, mCrop.clone());

                    int pi = Core.countNonZero(mCrop);
                    double v = Imgproc.contourArea(m);

                    if (pi <= 150 && pi >= 10) {
                        table[(choice) + (30 * set)] += (answer + " = " + pi + ",  " + v + ",  ");
                    }

                    mCrop.release();
                }
            } else {
                int n = row - 1;
                int p = col - 12;
                if (p > 5) {
                    p = p - 1;
                }
                if (n < 10 && p >= 0 && p < 11 && p != 6) {
                    tableId[p] += String.valueOf(n);
                }
            }
        }

        answers += tableId[0] + tableId[1] + tableId[2] + tableId[3] + tableId[4] + tableId[5]
                + tableId[6] + tableId[7] + tableId[8] + tableId[9] + tableId[10] + "\n";

        for (int i = 0; i < table.length; i++) {
            answers += (i + 1) + ")." + table[i] + "\n";
        }
        hierarchy.release();
        mBm.release();
        ////////////////////////////////////////////////////////////////////////////
        return bm;
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            dataT = new ArrayList<>();

            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;

                dataT.add(item);
            }

            started = false;
        }
    }
}
