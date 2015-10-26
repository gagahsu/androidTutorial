package net.macdidi.androidtutorial;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void clickOk(View view) {
        // 呼叫這個方法結束Activity元件
        finish();
    }
}
