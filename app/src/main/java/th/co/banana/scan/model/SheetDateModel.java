package th.co.banana.scan.model;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MSILeopardPro on 9/2/2560.
 */

public class SheetDateModel {
    private Bitmap bm;
    private List<Point> orderTemp = new ArrayList<>();
    private String answers = "";

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public List<org.opencv.core.Point> getOrderTemp() {
        List<org.opencv.core.Point> order = new ArrayList<>();
        order.add(new org.opencv.core.Point(orderTemp.get(0).x,orderTemp.get(0).y));
        order.add(new org.opencv.core.Point(orderTemp.get(1).x,orderTemp.get(1).y));
        order.add(new org.opencv.core.Point(orderTemp.get(2).x,orderTemp.get(2).y));
        order.add(new org.opencv.core.Point(orderTemp.get(3).x,orderTemp.get(3).y));
        return order;
    }

    public void setOrderTemp(List<Point> orderTemp) {
        this.orderTemp = orderTemp;
    }

    public SheetDateModel() {
    }

    public SheetDateModel(Bitmap bm, ArrayList<Point> orderTemp) {
        this.bm = bm;
        this.orderTemp = orderTemp;
    }
    
}
