package in.neonex.jauntbee.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogManager {
	Context context;

	public AlertDialogManager(Context context) {
		this.context = context;
	}

	public void showAlertDialog(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}

}
