package th.co.banana.scan.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

import th.co.banana.scan.R;
import th.co.banana.scan.tag.Action;

public class MainActivity extends AppCompatActivity {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(Action.TAG_MAIN, "OpenCV Loaded Success");
        } else {
            Log.d(Action.TAG_MAIN, "OpenCV Loaded didn't Success");
        }
    }

    private Button take,sel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        take = (Button) findViewById(R.id.take_picture);
        sel = (Button) findViewById(R.id.select_picture);

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(i);
            }
        });
        sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ProcessActivity.class);
                i.putExtra("From","Gallery");
                startActivity(i);
            }
        });
    }
}
