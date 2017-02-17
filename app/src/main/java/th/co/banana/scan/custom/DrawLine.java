package th.co.banana.scan.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by MSILeopardPro on 8/2/2560.
 */

public class DrawLine extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    public DrawLine(Context context) {
        super(context);
    }

    public DrawLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(3);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    public void StartDrawPath(Point tl, Point tr, Point bl, Point br){
        canvasBitmap.eraseColor(Color.TRANSPARENT);
        drawPath.reset();
        drawPath.moveTo(tl.x, tl.y);
        drawPath.lineTo(tr.x, tr.y);
        drawPath.lineTo(br.x, br.y);
        drawPath.lineTo(bl.x, bl.y);
        drawPath.lineTo(tl.x, tl.y);
        invalidate();
    }

    public void DrawPath(Point tl, Point tr, Point bl, Point br) {
        drawPath.reset();
        drawPath.moveTo(tl.x, tl.y);
        drawPath.lineTo(tr.x, tr.y);
        drawPath.lineTo(br.x, br.y);
        drawPath.lineTo(bl.x, bl.y);
        drawPath.lineTo(tl.x, tl.y);
        invalidate();
    }

    public void DrawReset() {
        drawCanvas.drawPath(drawPath, drawPaint);
        invalidate();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float touchX = event.getX();
//        float touchY = event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                drawPath.moveTo(touchX, touchY);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                drawPath.lineTo(touchX, touchY);
//                break;
//            case MotionEvent.ACTION_UP:
//                drawCanvas.drawPath(drawPath, drawPaint);
//                drawPath.reset();
//                break;
//            default:
//                return false;
//        }
//        invalidate();
//        return true;
//    }
}
