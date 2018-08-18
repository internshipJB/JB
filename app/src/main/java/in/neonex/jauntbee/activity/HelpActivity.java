package in.neonex.jauntbee.activity;

import in.neonex.jauntbee.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends Activity {

    private ImageView arrowImageView;
    private TextView helpTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.help);

	helpTextView = (TextView) (findViewById(R.id.helpText));

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

	helpTextView.setMovementMethod(new ScrollingMovementMethod());
	//helpTextView
	//	.setText(Html
	//		.fromHtml("<h3></h3><b>JauntBee</b> is created for your safe and secure travel.<br/><br/>Use these features of JauntBee in case of any emergency �<br/><br/><ol><li> <b>Call Police</b> � This will call the nearest police station or the emergency hotline number of your country.<br/><br/></li><li> <b>S.O.S.</b> �  This alerts and send your current location to your emergency contacts.<br/><br/></li><li> <b>Call Friends</b> � This will call your emergency contacts.</li></ol><br/>Other salient features of JauntBee �<br/><br/><ol><li><i>Heuristically</i> determines and warns you in case there is deviation in your route.</li><li> Notifies you when you are <i>about to reach</i> your destination.</li></ol><br/>For more information visit - <a href = 'http://www.jauntbee.xyz'>jauntbee.xyz</a><br/>"));

   helpTextView.setText("JauntBee");
    }

}
