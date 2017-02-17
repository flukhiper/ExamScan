package th.co.banana.scan.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import th.co.banana.scan.R;
import th.co.banana.scan.custom.DrawLine;
import th.co.banana.scan.model.CustomGallery;
import th.co.banana.scan.model.SheetDateModel;
import th.co.banana.scan.manager.ScanManager;
import th.co.banana.scan.util.BitmapCacheUtil;

public class CropActivity extends AppCompatActivity {

    private ArrayList<Point> orderTemp = new ArrayList<>();
    private ArrayList<Point> Temp = new ArrayList<>();
    private Bitmap bm, bitmap,resizedBitmap;
    private float xS, yS, wS, hS;

    private RelativeLayout observe;
    private ImageButton snap;
    private ImageView bmImage, bitmapImage, tl, tr, bl, br;
    private DrawLine drawView;
    private View temp;

    private Point tlP,trP,blP,brP;
    private float widthF;
    private int top;

    private CustomGallery gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crop);

        initInstance();

        Bitmap bitmapR = initBitmap();

        calCorner(bitmapR);

        setLayout();
    }

    private void initInstance() {
        tl = (ImageView) findViewById(R.id.tl);
        tr = (ImageView) findViewById(R.id.tr);
        bl = (ImageView) findViewById(R.id.bl);
        br = (ImageView) findViewById(R.id.br);
        bmImage = (ImageView) findViewById(R.id.bm);
        bitmapImage = (ImageView) findViewById(R.id.bitmap);
        observe = (RelativeLayout) findViewById(R.id.observe);
        temp = findViewById(R.id.temp);
        snap = (ImageButton) findViewById(R.id.snap);
        drawView = (DrawLine) findViewById(R.id.draw);

    }

    private Bitmap initBitmap() {
        gallery = getIntent().getParcelableExtra("CustomGallery");

        bm = BitmapFactory.decodeFile(gallery.sdcardPath);

        Matrix matrixR = new Matrix();

        matrixR.postRotate(270);

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixR, true);
    }

    private void calCorner(Bitmap bitmapR) {
        Mat mRGB = new Mat(bitmapR.getHeight(),bitmapR.getWidth(), CvType.CV_8UC4);

        Utils.bitmapToMat(bitmapR, mRGB);

        double hFactor = (double) mRGB.cols() / 320.0;
        double wFactor = (double) mRGB.rows() / 240.0;

        Size size = new Size(320, 240);

        Imgproc.resize(mRGB.clone(), mRGB, size);

        Imgproc.cvtColor(mRGB.clone(), mRGB, Imgproc.COLOR_RGB2GRAY);

        Imgproc.GaussianBlur(mRGB.clone(), mRGB, new Size(3, 3), 0);

        Imgproc.Canny(mRGB.clone(), mRGB, 75, 200);

        final List<MatOfPoint> contours = new ArrayList<>();
        final List<MatOfPoint> contoursNew = new ArrayList<>();

        Mat hierarchy = new Mat();

        Imgproc.findContours(mRGB,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f pointMat = new MatOfPoint2f();
        MatOfPoint approx;

        double max = 0;
        double e, area;
        double areaMax = mRGB.cols() * mRGB.rows();

        for (MatOfPoint m :
                contours) {
            e = Imgproc.arcLength(new MatOfPoint2f(m.toArray()), true);

            Imgproc.approxPolyDP(new MatOfPoint2f(m.toArray()), pointMat, 0.02 * e, true);

            if (pointMat.height() == 4) {
                area = Imgproc.contourArea(pointMat);
                if (area > max) {
                    max = area;
                }
                if (area >= areaMax / 2.25 && area <= areaMax / 1.5) {
                    approx = new MatOfPoint();
                    pointMat.convertTo(approx, CvType.CV_32S);
                    contoursNew.add(approx);
                }
            }
        }

        Collections.sort(contoursNew, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                double area1 = Imgproc.contourArea(o1);
                double area2 = Imgproc.contourArea(o2);
                return Double.compare(area1, area2);
            }
        });

        Temp = new ArrayList<>();

        for (int i = 0; i < contoursNew.size(); i++) {
            double x1 = contoursNew.get(i).get(0, 0)[0] * wFactor;
            double y1 = contoursNew.get(i).get(0, 0)[1] * hFactor;
            double x2 = contoursNew.get(i).get(1, 0)[0] * wFactor;
            double y2 = contoursNew.get(i).get(1, 0)[1] * hFactor;
            double x3 = contoursNew.get(i).get(2, 0)[0] * wFactor;
            double y3 = contoursNew.get(i).get(2, 0)[1] * hFactor;
            double x4 = contoursNew.get(i).get(3, 0)[0] * wFactor;
            double y4 = contoursNew.get(i).get(3, 0)[1] * hFactor;

            Temp.add(new Point((int) (x1), (int) (y1)));
            Temp.add(new Point((int) (x2), (int) (y2)));
            Temp.add(new Point((int) (x3), (int) (y3)));
            Temp.add(new Point((int) (x4), (int) (y4)));
            break;
        }

        mRGB.release();
        hierarchy.release();
    }

    private void setLayout() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        widthF = (float) displaymetrics.widthPixels / (float) bm.getWidth();
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(widthF, widthF);

        // "RECREATE" THE NEW BITMAP
        resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);

        bmImage.setImageBitmap(resizedBitmap);

        bmImage.setOnTouchListener(listener);

        bmImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initVariable();

                hS = bmImage.getHeight();
                wS = bmImage.getWidth();
                xS = bmImage.getLeft();
                yS = bmImage.getTop();

                tl.setX(xS + (orderTemp.get(1).x));
                tl.setY(yS + (orderTemp.get(1).y));

                tr.setX(xS + (orderTemp.get(3).x));
                tr.setY(yS + (orderTemp.get(3).y));

                bl.setX(xS + (orderTemp.get(0).x));
                bl.setY(yS + (orderTemp.get(0).y));

                br.setX(xS + (orderTemp.get(2).x));
                br.setY(yS + (orderTemp.get(2).y));

                int tlX = (int) tl.getX() + tl.getWidth() / 2;
                int tlY = (int) tl.getY() + tl.getHeight() / 2;
                tlP = new Point(tlX, tlY);
                int trX = (int) tr.getX() + tl.getWidth() / 2;
                int trY = (int) tr.getY() + tl.getHeight() / 2;
                trP = new Point(trX, trY);
                int blX = (int) bl.getX() + tl.getWidth() / 2;
                int blY = (int) bl.getY() + tl.getHeight() / 2;
                blP = new Point(blX, blY);
                int brX = (int) br.getX() + tl.getWidth() / 2;
                int brY = (int) br.getY() + tl.getHeight() / 2;
                brP = new Point(brX, brY);

                drawView.DrawPath(tlP, trP, blP, brP);
                drawView.DrawReset();

                //don't forget to remove the listener to prevent being called again by future layout events:
                bmImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        temp.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                top = temp.getHeight();

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                lp.setMargins(0, top, 0, 0);

                observe.setLayoutParams(lp);

                temp.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderTemp = new ArrayList<>();

                int tlX = (int) tl.getX() + tl.getWidth() / 2;
                int tlY = (int) tl.getY() + tl.getHeight() / 2;
                tlP = new Point((int) ((tlX - (int) xS) / widthF), (int) ((tlY - (int) yS) / widthF));
                int trX = (int) tr.getX() + tl.getWidth() / 2;
                int trY = (int) tr.getY() + tl.getHeight() / 2;
                trP = new Point((int) ((trX - (int) xS) / widthF), (int) ((trY - (int) yS) / widthF));
                int blX = (int) bl.getX() + tl.getWidth() / 2;
                int blY = (int) bl.getY() + tl.getHeight() / 2;
                blP = new Point((int) ((blX - (int) xS) / widthF), (int) ((blY - (int) yS) / widthF));
                int brX = (int) br.getX() + tl.getWidth() / 2;
                int brY = (int) br.getY() + tl.getHeight() / 2;
                brP = new Point((int) ((brX - (int) xS) / widthF), (int) ((brY - (int) yS) / widthF));

                orderTemp.add(tlP);
                orderTemp.add(trP);
                orderTemp.add(brP);
                orderTemp.add(blP);

                SheetDateModel model = new SheetDateModel(bm, orderTemp);
                ScanManager.getInstance().addDateModels(model);

                finish();
            }
        });
    }

    private void initVariable() {
        if (Temp.size() != 0) {

            Collections.sort(Temp, new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    double a = o1.x - o2.x;
                    double b = o1.y - o2.y;
                    if (b != 0) {
                        if (b > 0) {
                            return -1;
                        } else if (b < 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } else {
                        if (a > 0) {
                            return -1;
                        } else if (a < 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            });

            int wi = tl.getWidth() / 2;
            Point p0 = new Point((int) ((bm.getWidth() - Temp.get(0).y) * widthF) - wi, (int) ((Temp.get(0).x) * widthF) - wi);
            Point p1 = new Point((int) ((bm.getWidth() - Temp.get(1).y) * widthF) - wi, (int) ((Temp.get(1).x) * widthF) - wi);
            if (Temp.get(0).x > Temp.get(1).x) {
                orderTemp.add(p0);
                orderTemp.add(p1);
            } else {
                orderTemp.add(p1);
                orderTemp.add(p0);
            }

            Point p2 = new Point((int) ((bm.getWidth() - Temp.get(2).y) * widthF) - wi, (int) ((Temp.get(2).x) * widthF) - wi);
            Point p3 = new Point((int) ((bm.getWidth() - Temp.get(3).y) * widthF) - wi, (int) ((Temp.get(3).x) * widthF) - wi);
            if (Temp.get(2).x > Temp.get(3).x) {
                orderTemp.add(p2);
                orderTemp.add(p3);
            } else {
                orderTemp.add(p3);
                orderTemp.add(p2);
            }

        } else {

            orderTemp.add(new Point(0, (int) (bm.getHeight() * widthF) - tl.getWidth()));
            orderTemp.add(new Point(0, 0));
            orderTemp.add(new Point((int) (bm.getWidth() * widthF) - tl.getWidth(), (int) (bm.getHeight() * widthF) - tl.getWidth()));
            orderTemp.add(new Point((int) (bm.getWidth() * widthF) - tl.getWidth(), 0));
        }
    }

    View.OnTouchListener listener = new View.OnTouchListener() {
        public ImageView view;
        float dY;
        float dX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int dimension = tl.getWidth();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    view = findNearView(event);

                    setMiniCrop(dimension);

                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    drawView.StartDrawPath(tlP, trP, blP, brP);
                    observe.setVisibility(View.VISIBLE);
                    break;

                case MotionEvent.ACTION_MOVE:

                    setMiniCrop(dimension);

                    float x = event.getRawX() + dX;
                    float y = event.getRawY() + dY;

                    if (x >= xS && x <= xS + wS - dimension
                            && y >= yS && y <= yS + hS - dimension) {
                        view.animate()
                                .x(x)
                                .y(y)
                                .setDuration(0)
                                .start();
                    }
                    drawView.DrawPath(tlP, trP, blP, brP);
                    break;
                case MotionEvent.ACTION_UP:
                    drawView.DrawReset();
                    observe.setVisibility(View.GONE);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void setMiniCrop( int dimension) {
            int tlX = (int) tl.getX() + dimension / 2;
            int tlY = (int) tl.getY() + dimension / 2;
            tlP = new Point(tlX, tlY);
            int trX = (int) tr.getX() + dimension / 2;
            int trY = (int) tr.getY() + dimension / 2;
            trP = new Point(trX, trY);
            int blX = (int) bl.getX() + dimension / 2;
            int blY = (int) bl.getY() + dimension / 2;
            blP = new Point(blX, blY);
            int brX = (int) br.getX() + dimension / 2;
            int brY = (int) br.getY() + dimension / 2;
            brP = new Point(brX, brY);
//            bitmap = ThumbnailUtils.extractThumbnail(bm, dimension, dimension);
            if (view.getId() == tl.getId()) {
                bitmap = Bitmap.createBitmap(resizedBitmap, (int) (tl.getX() - xS), (int) (tl.getY() - yS), dimension, dimension);
            } else if (view.getId() == tr.getId()) {
                bitmap = Bitmap.createBitmap(resizedBitmap, (int) (tr.getX() - xS), (int) (tr.getY() - yS), dimension, dimension);
            } else if (view.getId() == br.getId()) {
                bitmap = Bitmap.createBitmap(resizedBitmap, (int) (br.getX() - xS), (int) (br.getY() - yS), dimension, dimension);
            } else if (view.getId() == bl.getId()) {
                bitmap = Bitmap.createBitmap(resizedBitmap, (int) (bl.getX() - xS), (int) (bl.getY() - yS), dimension, dimension);
            }

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (view.getX() <= wS / 2) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }

            lp.setMargins(0, top, 0, 0);

            observe.setLayoutParams(lp);

            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(3, 3);

            // "RECREATE" THE NEW BITMAP
            Bitmap resized = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            bitmapImage.setImageBitmap(resized);
        }
    };

    private ImageView findNearView(MotionEvent event) {
        float tlD = Math.abs(tlP.x - event.getRawX()) + Math.abs(tlP.y - event.getRawY());
        float trD = Math.abs(trP.x - event.getRawX()) + Math.abs(trP.y - event.getRawY());
        float blD = Math.abs(blP.x - event.getRawX()) + Math.abs(blP.y - event.getRawY());
        float brD = Math.abs(brP.x - event.getRawX()) + Math.abs(brP.y - event.getRawY());

        float minT = Math.min(tlD, trD);
        float minB = Math.min(blD, brD);
        float min = Math.min(minT, minB);

        if (min == tlD) {
            return tl;
        } else if (min == trD) {
            return tr;
        } else if (min == blD) {
            return bl;
        } else {
            return br;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
