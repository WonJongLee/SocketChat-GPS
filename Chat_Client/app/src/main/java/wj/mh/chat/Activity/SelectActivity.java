package wj.mh.chat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import wj.mh.chat.Activity.ChatActivity;
import wj.mh.chat.R;

public class SelectActivity extends Activity {
    ImageView maleBtn;
    ImageView femaleBtn;
    Intent intent;

    float scale;
    String sex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        maleBtn = (ImageView)findViewById(R.id.maleButton);
        femaleBtn = (ImageView)findViewById(R.id.femaleButton);

        maleBtn.setOnTouchListener(sexTouch);
        femaleBtn.setOnTouchListener(sexTouch);


        scale = getResources().getDisplayMetrics().density; // 화면의 밀도를 구한다.

    }


    public void sexOnClick(View v) {
        switch (v.getId()) {
            case R.id.maleButton:
                sex = "male";
                intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("sex", sex);
                startActivity(intent);
                finish();
                break;
            case R.id.femaleButton:
                sex = "female";
                intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("sex", sex);
                startActivity(intent);
                finish();
                break;
        }

    }

    private View.OnTouchListener sexTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                v.setBackgroundResource(R.drawable.button_background);

                float dip = 10.0f;  // 변환하고자하는 dip 치수
                int pixel = (int) (dip  * scale + 0.5f); // 변환하는데 0.5 는 반올림을 위하여 붙여줌.
                v.setPadding(pixel,pixel,pixel,pixel);
            } else {
                v.setBackgroundResource(R.drawable.edit_bg);

                float dip = 10.0f;  // 변환하고자하는 dip 치수
                int pixel = (int) (dip  * scale + 0.5f); // 변환하는데 0.5 는 반올림을 위하여 붙여줌.
                v.setPadding(pixel,pixel,pixel,pixel);
                

            }

            return false;
        }
    };

}
