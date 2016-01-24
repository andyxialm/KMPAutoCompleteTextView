package cn.refactor.kmpautocompletetextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.kmpautotextview.KMPAutoComplTextView;


/**
 * 作者 : andy
 * 日期 : 15/10/26 21:01
 * 邮箱 : andyxialm@gmail.com
 * 描述 : 测试界面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> data = new ArrayList<String>();
        data.add("Red roses for wedding");
        data.add("Bouquet with red roses");
        data.add("Single red rose flower");

        KMPAutoComplTextView complTextView = (KMPAutoComplTextView) findViewById(R.id.tvAutoCompl);
        complTextView.setDatas(data);
        complTextView.setOnPopupItemClickListener(new KMPAutoComplTextView.OnPopupItemClickListener() {
            @Override
            public void onPopupItemClick(CharSequence charSequence) {
                Toast.makeText(MainActivity.this, charSequence.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
