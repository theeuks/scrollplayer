package hk.hku.cs.c3330.scrollplayer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ScrollplayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollplayer);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scrollplayer, menu);
        return true;
    }
    
}
