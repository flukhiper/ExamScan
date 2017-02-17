package th.co.banana.scan.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import th.co.banana.scan.model.SheetDateModel;

/**
 * Created by nuuneoi on 11/16/2014.
 */
public class ScanManager {

    public interface ScanListener {
        void onFinished();

        void onNewProcess();
    }

    private static ScanManager instance;

    private List<SheetDateModel> dateModelsFinished = new ArrayList<>();
    private List<SheetDateModel> dateModels = new ArrayList<>();

    private boolean started = false;
    private boolean finished = false;

    private AsyncTask<Void, Void, String> task;

    private ScanListener listener;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setListener(ScanListener listener) {
        this.listener = listener;
    }

    public List<SheetDateModel> getDateModels() {
//        return dateModelsFinished;
        return dateModels;
    }

    public void setDateModels(List<SheetDateModel> dateModels) {
        this.dateModels = dateModels;
    }

    public void addDateModels(SheetDateModel model) {
        this.dateModels.add(model);
    }

    public static ScanManager getInstance() {
        if (instance == null)
            instance = new ScanManager();
        return instance;
    }

    private Context mContext;

    private ScanManager() {
        mContext = Contextor.getInstance().getContext();
    }

    public void startExecute() {
        started = true;

        task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                while (started) {
                    if (dateModels.size() != 0) {
                        if (finished) {
                            listener.onNewProcess();
                        }
                        finished = false;

                        SheetDateModel model = dateModels.get(0);
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

                        Imgproc.cvtColor(mBm.clone(), mBm, Imgproc.COLOR_BGR2GRAY);

                        Bitmap bm = Bitmap.createBitmap(mBm.cols(), mBm.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mBm, bm);

                        mBm.release();

                        SheetDateModel modelF = new SheetDateModel();
                        modelF.setBm(bm);

                        dateModelsFinished.add(modelF);
                        dateModels.remove(model);
                    } else {
                        started = false;

                        if (!finished) {
                            finished = true;

                            listener.onFinished();
                            //TODO:say finished all
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

}
