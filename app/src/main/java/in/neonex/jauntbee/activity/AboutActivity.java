package in.neonex.jauntbee.activity;

import in.neonex.jauntbee.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class AboutActivity extends Activity {

    private ImageView arrowImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.about);

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	    Window window = this.getWindow();
	    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	    this.getWindow().setStatusBarColor(0XFF9e9e9e);
	}

	arrowImageView = (ImageView) findViewById(R.id.blackArrowImage);
	arrowImageView.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		finish();
	    }
	});

    }
}
