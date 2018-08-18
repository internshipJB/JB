package in.neonex.jauntbee.adapter;

import in.neonex.jauntbee.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final static String[] text = { "Emergency Contacts", /*"Settings",*/ "Help", "About","Sign Out" };
    private final Integer[] image = { R.drawable.contacts,/* R.drawable.settings,*/ R.drawable.help, R.drawable.about ,R.drawable.ic_exit_to_app_black_24dp};

    public NavigationAdapter(Activity context) {
	super(context, R.layout.navigation_item, text);
	this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
	LayoutInflater inflater = context.getLayoutInflater();
	if (position == 0) {
	    IconHolder holder;
	    if (view == null) {
		holder = new IconHolder();
		view = inflater.inflate(R.layout.drawer_icon_item, parent, false);
		holder.iconImageView = (ImageView) view.findViewById(R.id.nav_icon_img);
		view.setTag(holder);
		view.setEnabled(false);
	    } else {
		holder = (IconHolder) view.getTag();
	    }
	    holder.iconImageView.setImageResource(R.drawable.nav_icon);
	} else {
	    MenuHolder holder;
	    if (view == null) {
		holder = new MenuHolder();
		view = inflater.inflate(R.layout.navigation_item, parent, false);
		holder.imageView = (ImageView) view.findViewById(R.id.nav_img);
		holder.textView = (TextView) view.findViewById(R.id.nav_txt);
		view.setTag(holder);
	    } else {
		holder = (MenuHolder) view.getTag();
	    }
	    holder.textView.setText(text[position - 1]);
	    holder.imageView.setImageResource(image[position - 1]);
	}
	return view;
    }

    @Override
    public int getViewTypeCount() {
	return 2;
    }

    @Override
    public int getItemViewType(int position) {
	if (position == 0) {
	    return 0;
	} else {
	    return 1;
	}
    }

    @Override
    public int getCount() {
	return image.length + 1;
    }

    private static class MenuHolder {

	private TextView textView;
	private ImageView imageView;

    }

    private static class IconHolder {

	private ImageView iconImageView;

    }
}
