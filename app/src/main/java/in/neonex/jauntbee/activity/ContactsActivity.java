package in.neonex.jauntbee.activity;

import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.CONTACT_PREF;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.JAUNT_BEE_PREF;
import in.neonex.jauntbee.R;
import in.neonex.jauntbee.utility.PhoneUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ContactsActivity extends Activity {
    private ListView myList;
    private MyAdapter myAdapter;
    private ArrayList<ListItem> myItems = new ArrayList<ListItem>();
    private ImageView arrowImageView;
    private EditText currentEditText;
    private Button saveButton;
    private SharedPreferences sharedPreferences;
    private LinkedHashSet<String> contactSet = new LinkedHashSet<String>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.contacts);
mAuth = FirebaseAuth.getInstance();
	mAuthStateListner = new FirebaseAuth.AuthStateListener() {
		@Override
		public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
	if(firebaseAuth == null){
		Intent intent  = new Intent(ContactsActivity.this,LoginActivity.class);
		startActivity(intent);
		finish();
	}
		}
	};

	sharedPreferences = getSharedPreferences(JAUNT_BEE_PREF, Context.MODE_PRIVATE);

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

	myList = (ListView) findViewById(R.id.MyList);

	myList.setItemsCanFocus(true);
	myAdapter = new MyAdapter();

	ListItem listItem = new ListItem();

	myList.setAdapter(myAdapter);
	myAdapter.notifyDataSetChanged();

	if (sharedPreferences.contains(CONTACT_PREF)) {
	    String[] emergencyArray = sharedPreferences.getString(CONTACT_PREF, "").split(";");

	    for (String temp : emergencyArray) {
		listItem = new ListItem();
		String number = temp.split(":")[0];
		String name = temp.split(":")[1];
		contactSet.add(temp);
		listItem.name = name;
		listItem.number = number;
		myItems.add(listItem);
	    }

	} else {
	    listItem.name = "";
	    listItem.number ="";
	    myItems.add(listItem);
	}

	saveButton = (Button) findViewById(R.id.saveButton);
	saveButton.setOnClickListener(new Button.OnClickListener() {

	    @Override
	    public void onClick(View arg0) {

			Intent intent  = new Intent(ContactsActivity.this,DestinationActivity.class);
			startActivity(intent);

		boolean validationFlag = true;
		ArrayList<String> allEmergencyContacts = new ArrayList<String>();

		for (String contactString : contactSet) {

		    allEmergencyContacts.add(contactString);
		}

		if (allEmergencyContacts.isEmpty()) {
		    validationFlag = false;
		    Toast.makeText(ContactsActivity.this, "Add atleast one emergency contact", Toast.LENGTH_SHORT).show();
		}

		if (validationFlag) {
		    StringBuffer emergencyBuffer = new StringBuffer();
		    for (String temp : allEmergencyContacts) {
			emergencyBuffer.append(temp + ";");
		    }

		    SharedPreferences.Editor editor = sharedPreferences.edit();
		    editor.putString(CONTACT_PREF, emergencyBuffer.toString());
		    editor.commit();

		    Toast.makeText(ContactsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
		    finish();
		}

	    }
	});

    }

	@Override
	protected void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthStateListner);
	}

	class MyAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	public MyAdapter() {
	    mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount() {
	    return myItems.size();
	}

	public ListItem getItem(int position) {
	    return myItems.get(position);
	}

	public long getItemId(int position) {
	    return position;
	}

	@SuppressLint("InflateParams")
	public View getView(final int position, View convertView, ViewGroup parent) {
	    final ViewHolder holder;
	    if (convertView == null) {
		holder = new ViewHolder();
		convertView = mInflater.inflate(R.layout.contact_single_item, null);
		holder.captionEditText = (EditText) convertView.findViewById(R.id.ItemCaption);

		holder.addContactButton = (Button) convertView.findViewById(R.id.contactAdd);
		holder.addOrDeleteButton = (Button) convertView.findViewById(R.id.buttonAdd);

		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }

	    holder.captionEditText.setTag(position);

	    String text = getItem(position).name;
	    String number = getItem(position).number;

	    if (!"".equals(text)) {
		String heading = text;
		float fontSize = 1.125f;

		String delimiter = "\n";
		String screenSize = PhoneUtil.getScreenSizeName(ContactsActivity.this);
		if ("large".equals(screenSize) || "xlarge".equals(screenSize)) {
		    fontSize = 1.25f;
		}
		text += delimiter + number;
		SpannableString spannableString = new SpannableString(text);
		spannableString.setSpan(new RelativeSizeSpan(fontSize), 0, heading.length(), 0);
		spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, heading.length(), 0);
		holder.captionEditText.setText(spannableString);
	    } else {
		holder.captionEditText.setText(text);
	    }

	    holder.captionEditText.setEnabled(false);

	    holder.addContactButton.setTag(position);
	    holder.addOrDeleteButton.setTag(position);

	    holder.captionEditText.setFocusable(true);
	    if (position == myItems.size() - 1) {
		holder.captionEditText.requestFocus();
	    }

	    holder.addContactButton.setOnClickListener(new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
		    currentEditText = holder.captionEditText;
		    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		    startActivityForResult(intent, 1);

		}
	    });

	    holder.addOrDeleteButton.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View view) {
		    int tag = (Integer) view.getTag();
		    if (tag != (myItems.size() - 1)) {
			ListItem item = myItems.get(tag);
			contactSet.remove(item.number + ":" + item.name);
			myItems.remove(tag);
			myAdapter.notifyDataSetChanged();

		    } else if (myItems.size() != 5) {
			if (holder.captionEditText.getText().toString().length() > 0) {
			    ListItem listItem = new ListItem();
			    listItem.name = "";
			    listItem.number = "";
			    myItems.add(listItem);
			    myAdapter.notifyDataSetChanged();
			} else {
			    Toast.makeText(ContactsActivity.this, "Add a contact to this field first", Toast.LENGTH_SHORT).show();
			}

		    } else if (myItems.size() == 5) {
			ListItem item = myItems.get(tag);
			contactSet.remove(item.name + ":" + item.number);
			myItems.remove(tag);
			myAdapter.notifyDataSetChanged();
		    }
		}
	    });

	    holder.captionEditText.addTextChangedListener(new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		// after contact has been added add it to ListItem
		public void afterTextChanged(Editable s) {
		    String text = holder.captionEditText.getText().toString();
		    int position = (Integer) holder.captionEditText.getTag();
		    if (!"".equals(text)) {
			myItems.get(position).name = text.split("\n")[0];
			myItems.get(position).number = text.split("\n")[1];
		    }

		}
	    });

	    // all items except last on stack
	    if (position != (myItems.size() - 1)) {
		holder.addOrDeleteButton.setCompoundDrawablesWithIntrinsicBounds(null,
			getResources().getDrawable(R.drawable.ic_action_cancel), null, null);
	    } else {
		// if five items are already added
		if (position == 4) {
		    holder.addOrDeleteButton.setCompoundDrawablesWithIntrinsicBounds(null,
			    getResources().getDrawable(R.drawable.ic_action_cancel), null, null);
		} else {
		    holder.addOrDeleteButton.setCompoundDrawablesWithIntrinsicBounds(null,
			    getResources().getDrawable(R.drawable.ic_action_new), null, null);
		}
	    }

	    myList.setSelection(myItems.size() - 1);

	    return convertView;
	}
    }

    class ViewHolder {

	EditText captionEditText;
	Button addContactButton;
	Button addOrDeleteButton;
    }

    class ListItem {
	String name;
	String number;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == 1) {
	    if (resultCode == RESULT_OK) {
		Uri contactData = data.getData();
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contactData, null, null, null, null);
		cursor.moveToFirst();

		// remove existing contact from set
		if (!"".equals(currentEditText.getText().toString())) {
		    String number = currentEditText.getText().toString().split("\n")[1];
		    String name = currentEditText.getText().toString().split("\n")[0];
		    contactSet.remove(number + ":" + name);
		}

		// add to selected contact to set
		String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
		String text = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		contactSet.add(number + ":" + text);

		currentEditText.setEnabled(false);

		String heading = text;
		float fontSize = 1.125f;

		String delimiter = "\n";
		String screenSize = PhoneUtil.getScreenSizeName(ContactsActivity.this);
		if ("large".equals(screenSize) || "xlarge".equals(screenSize)) {
		    fontSize = 1.25f;
		}
		text += delimiter + number;
		SpannableString spannableString = new SpannableString(text);
		spannableString.setSpan(new RelativeSizeSpan(fontSize), 0, heading.length(), 0);
		spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, heading.length(), 0);
		currentEditText.setText(spannableString);

	    }
	}
    }
}
