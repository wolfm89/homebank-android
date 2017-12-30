package fr.free.homebank.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import fr.free.homebank.mobile.util.DbAdapter;
import fr.free.homebank.mobile.util.Util;

public class EditActivity extends ActionBarActivity implements
		DatePickerDialog.OnDateSetListener,
        TemplateChooser.TemplateChooserListener {
	public static final String TEMP_TRANS = "TEMP_TRANS";

    public static final String CAT_SIZE = "cat_size";
    public static final String FOCUS = "focus";

	// Android widgets
	private Button dateButton;
	private EditText payeeText;
	private EditText amountText;
	private EditText textText;
	private ArrayList<AutoCompleteTextView> categories;
	private LinearLayout categoryLayout;

	// Member variables
	private Long rowId;
	private Long account;
	private Calendar date;
	private DbAdapter dbHelper = new DbAdapter(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        Log.i("LIFECYCLE", "onCreate");
		setContentView(R.layout.activity_edit);
		if (Build.VERSION.SDK_INT >= 11)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		dateButton = (Button) findViewById(R.id.date_button);
		payeeText = (EditText) findViewById(R.id.payee_autocomplete);
		amountText = (EditText) findViewById(R.id.amount_text_view);
		textText = (EditText) findViewById(R.id.text_autocomplete);
		categoryLayout = (LinearLayout) findViewById(R.id.category_layout);

		categories = new ArrayList<AutoCompleteTextView>();

		autoCompleteField(R.id.payee_autocomplete, R.layout.payees,
				DbAdapter.KEY_NAME, DbAdapter.TABLE_PAYEE);
		autoCompleteField(R.id.text_autocomplete, R.layout.texts,
				DbAdapter.KEY_TEXT, DbAdapter.TABLE_TRANSACTION);

        if (savedInstanceState == null) {
            date = Calendar.getInstance();
            Util.setDateButtonText(this, dateButton, date);
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_favorite:
                TemplateChooser templateChooser = new TemplateChooser();
                templateChooser.show(getSupportFragmentManager(), "templates");
                return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onDateButtonClick(View v) {
        DialogFragment dateDialogFragment = Util.DatePickerDialogFragment.
                newInstance(this, this, date.get(Calendar.DATE), date.get(Calendar.MONTH), date.get(Calendar.YEAR));
        dateDialogFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		date.set(year, monthOfYear, dayOfMonth);
		Util.setDateButtonText(this, dateButton, date);
	}

	public void onConfirmButtonClick(View v) {
		saveToDb();
		setResult(RESULT_OK);
		finish();
	}

	public void onCancelButtonClick(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void onRemoveCategoryButtonClick(View v) {
		if (categories.size() > 1) {
			categoryLayout.removeView(categories.get(categories.size() - 1));
			categories.remove(categories.size() - 1);
		}
	}

	public void onAddCategoryButtonClick(View v) {
		AutoCompleteTextView temp = new AutoCompleteTextView(this);
		temp.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		temp.setText("");
		temp.setId(categories.size() + 1);
		temp.setTextColor(Color.BLACK);

		categories.add(temp);
		categoryLayout.addView(temp);

		autoCompleteField(temp.getId(), R.layout.categories,
				DbAdapter.KEY_NAME, DbAdapter.TABLE_CATEGORY);
		temp.setThreshold(1);
		temp.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		temp.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		temp.requestFocus();
	}

	private void populate() {
		SharedPreferences settings = getSharedPreferences(TEMP_TRANS, MODE_PRIVATE);
		Long tempId = settings.getLong(DbAdapter.KEY_ROWID, -1);

		Bundle extras = getIntent().getExtras();
		rowId = extras != null ? extras.getLong(DbAdapter.KEY_ROWID) : null;
		account = extras != null ? extras.getLong(DbAdapter.KEY_ACC) : null;

        if (tempId >= 0) {
			// load from properties
            date = Calendar.getInstance();
			date.setTime(Util.julianToDate(settings.getLong(DbAdapter.KEY_DATE, -1)));
			Util.setDateButtonText(this, dateButton, date);
			payeeText.setText(settings.getString(DbAdapter.KEY_PAYEE, ""));
			amountText.setText(settings.getString(DbAdapter.KEY_AMOUNT, ""));
            if (categories.isEmpty())
                for (int i = 0; i < settings.getInt(CAT_SIZE, 0); i++)
                    onAddCategoryButtonClick(null);
            for (AutoCompleteTextView cat : categories)
                cat.setText(settings.getString(DbAdapter.KEY_CATEGORY + String.valueOf(cat.getId()), ""));
			textText.setText(settings.getString(DbAdapter.KEY_TEXT, ""));
            if (settings.getInt(FOCUS, 0) != 0)
                findViewById(settings.getInt(FOCUS, 0)).requestFocus();
        } else if (tempId == -1) {
            // nothing is saved
            if (rowId != 0 && rowId != null) {
                // load existing from db
                dbHelper.open();
                Cursor transCursor = dbHelper.fetchTransaction(rowId);
                startManagingCursor(transCursor);

                Date date = Util.julianToDate(transCursor.getLong(transCursor.getColumnIndexOrThrow(DbAdapter.KEY_DATE)));
                this.date = Calendar.getInstance();
                this.date.setTime(date);
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                String text = dateFormat.format(date);

                dateButton.setText(text);
                payeeText.setText(transCursor.getString(transCursor.getColumnIndexOrThrow(DbAdapter.KEY_PAYEE)));
                amountText.setText(transCursor.getString(transCursor.getColumnIndexOrThrow(DbAdapter.KEY_AMOUNT)));
                textText.setText(transCursor.getString(transCursor.getColumnIndexOrThrow(DbAdapter.KEY_TEXT)));

                ArrayList<ContentValues> cats = new ArrayList<ContentValues>();
                cats = dbHelper.getCategoryList(cats, Long.parseLong(transCursor.
                        getString(transCursor.getColumnIndexOrThrow(DbAdapter.CATEGORY_ID))));

                populateCategories(cats);

                stopManagingCursor(transCursor);
                dbHelper.close();
            }
            else {
                // create new entry
                onAddCategoryButtonClick(null);
                payeeText.requestFocus();
            }
		}
	}

    private void populateCategories(ArrayList<ContentValues> cats) {
        while (categories.size() > 0) {
            categoryLayout.removeView(categories.get(categories.size() - 1));
            categories.remove(categories.size() - 1);
        }

        int i = cats.size();
        ListIterator<ContentValues> it = cats.listIterator(cats.size());
        while (it.hasPrevious()) {
            ContentValues c = it.previous();
            AutoCompleteTextView temp = new AutoCompleteTextView(this);
            temp.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            temp.setText(c.getAsString(DbAdapter.KEY_NAME));
            temp.setId(cats.size()-i+1);
            temp.setTextColor(Color.BLACK);

            categories.add(temp);
            categoryLayout.addView(temp);

            autoCompleteField(temp.getId(), R.layout.categories,
                    DbAdapter.KEY_NAME, DbAdapter.TABLE_CATEGORY);

            temp.setThreshold(1);
            temp.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            temp.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            i--;
        }
    }

	private void autoCompleteField(int autoCompleteViewId, int editViewId,
			String dbField, String table) {
		dbHelper.open();
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(autoCompleteViewId);
		Cursor cursor = dbHelper.fetch(dbField, table);
		if (cursor != null && cursor.getCount() > 0) {
			startManagingCursor(cursor);
			int index = cursor.getColumnIndexOrThrow(dbField);
			List<String> field = new ArrayList<String>();
			boolean last = false;
			while (!last) {
                if (cursor.getString(index) != null)
				    field.add(cursor.getString(index));
				last = cursor.isLast();
				cursor.moveToNext();
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					editViewId, field);
			textView.setAdapter(adapter);
			stopManagingCursor(cursor);
		}
		dbHelper.close();
	}

	private void saveToDb() { // acc,amount,date not null
		if (amountText.getText() != null && categoriesValid()) {
			dbHelper.open();

			ContentValues values = new ContentValues();
			values.put(DbAdapter.KEY_DATE, Util.dateToJulian(date.getTime()));
			values.put(DbAdapter.KEY_TEXT,
					textText.getText() != null ? textText.getText()
							.toString() : null);

			values.put(DbAdapter.KEY_AMOUNT, amountText.getText().toString());

			if (!payeeText.getText().toString().isEmpty()) {
				long id = dbHelper.payeeExists(payeeText.getText().toString()
						.trim());
				if (id > 0) {
					values.put(DbAdapter.KEY_PAYEE, String.valueOf(id));
				} else {
					ContentValues p = new ContentValues();
					p.put(DbAdapter.KEY_NAME, payeeText.getText().toString()
							.trim());
					long newId = dbHelper.insertPayee(p);
					values.put(DbAdapter.KEY_PAYEE, String.valueOf(newId));
				}
			} else
				values.put(DbAdapter.KEY_PAYEE, String.valueOf(0));

			long parent = 0;
			long id = 0;
			for (AutoCompleteTextView cat : categories) {
				id = dbHelper.categoryExists(cat.getText().toString().trim(), id);
				if (id <= 0) {
					ContentValues c = new ContentValues();
					c.put(DbAdapter.KEY_NAME, cat.getText().toString().trim());
					c.put(DbAdapter.KEY_PARENT, parent);
                    c.put(DbAdapter.KEY_AMOUNT, 0);
					
					// Default values
					if (parent != 0)
						c.put(DbAdapter.KEY_FLAGS, "1");
					else
						c.put(DbAdapter.KEY_FLAGS, "0");
					
					
					id = dbHelper.insertCategory(c);
				}
				parent = id;
			}

			values.put(DbAdapter.KEY_CATEGORY, String.valueOf(id));

			if (rowId == 0) {
				values.put(DbAdapter.KEY_ACC, String.valueOf(account));
				
				// Default values
				values.put(DbAdapter.KEY_DST_ACC, "0");
				values.put(DbAdapter.KEY_PAYMODE, "3");
				values.put(DbAdapter.KEY_FLAGS, "1");
				values.put(DbAdapter.KEY_INFO, "");
				values.put(DbAdapter.KEY_TAGS, "");
				values.put(DbAdapter.KEY_KXFER, "0");
				
				id = dbHelper.insertTransaction(values);
				if (id > 0)
					rowId = id;
			} else
				dbHelper.updateTransaction(rowId, values);

			dbHelper.close();
			
			
		} else
			Log.i("Info", "Dialog");
		// Dialog!

//		if (Utils.validate(values)) {
//			dbHelper.open();
//			if (rowId == null) {
//				long id = dbHelper.create(values);
//				if (id > 0) {
//					rowId = id;
//				}
//			} else {
//				dbHelper.update(rowId, values);
//			}
//			dbHelper.close();
//		}
	}

	private boolean categoriesValid() {
		for (AutoCompleteTextView cat : categories)
			if (cat.getText().toString().isEmpty())
				return false;
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
//        Log.i("LIFECYCLE", "onPause");
		saveState();
	}

	private void saveState() {
//        Log.i("LIFECYCLE", "saveState");
		SharedPreferences settings = getSharedPreferences(TEMP_TRANS, 0);
		Editor editor = settings.edit();
		editor.putLong(DbAdapter.KEY_ROWID, rowId);
		editor.putLong(DbAdapter.KEY_DATE, Util.dateToJulian(date.getTime()));
        editor.putString(DbAdapter.KEY_PAYEE, payeeText.getText().toString());
        editor.putString(DbAdapter.KEY_AMOUNT, amountText.getText().toString());
        for (AutoCompleteTextView cat : categories)
            editor.putString(DbAdapter.KEY_CATEGORY + String.valueOf(cat.getId()), cat.getText().toString());
        editor.putInt(CAT_SIZE, categories.size());
		editor.putString(DbAdapter.KEY_TEXT, textText.getText().toString());
        editor.putInt(FOCUS, getCurrentFocus() != null ? getCurrentFocus().getId() : 0);
		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
//        Log.i("LIFECYCLE", "onResume");
		populate();
	}

    @Override
    public void onTemplateClick(ContentValues template) {
        dbHelper.open();

        account = template.getAsLong(DbAdapter.KEY_ACC);

        ContentValues payee = dbHelper.fetchPayee(template.getAsLong(DbAdapter.KEY_PAYEE));
        if (payee != null)
            payeeText.setText(payee.getAsString(DbAdapter.KEY_NAME));
        amountText.setText(template.getAsString(DbAdapter.KEY_AMOUNT));

        ArrayList<ContentValues> cats = new ArrayList<ContentValues>();
        cats = dbHelper.getCategoryList(cats, template.getAsLong(DbAdapter.KEY_CATEGORY));
        populateCategories(cats);

        textText.setText(template.getAsString(DbAdapter.KEY_TEXT));

        dbHelper.close();
    }

    // /**
	// * When in landscape, make the AutoComplete Next/Done buttons work.
	// */
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// Log.i("KeyCode onKeyUp", String.valueOf(keyCode));
	// if (keyCode == KeyEvent.KEYCODE_ENTER) {
	// if (payeeText.hasFocus()) {
	// // sends focus to amountText (user pressed "Next")
	// payeeText.clearFocus();
	// amountText.requestFocus();
	// return true;
	// } else if (mCategoryText.hasFocus()) {
	// // sends focus to mParentCategoryText (user pressed "Next")
	// mCategoryText.clearFocus();
	// mParentCategoryText.requestFocus();
	// return true;
	// } else if (mParentCategoryText.hasFocus()) {
	// // sends focus to mCategoryText (user pressed "Next")
	// mParentCategoryText.clearFocus();
	// mCategoryText.requestFocus();
	// return true;
	// } else if (mCategoryText.hasFocus()) {
	// // sends focus to textText (user pressed "Next")
	// mCategoryText.clearFocus();
	// textText.requestFocus();
	// return true;
	// } else if (textText.hasFocus()) {
	// // closes soft keyboard (user pressed "Done")
	// textText.clearFocus();
	// InputMethodManager inputManager = (InputMethodManager)
	// getSystemService(Context.INPUT_METHOD_SERVICE);
	// inputManager.hideSoftInputFromWindow(
	// textText.getWindowToken(),
	// InputMethodManager.HIDE_NOT_ALWAYS);
	// return true;
	// }
	// }
	// return false;
	// }

}

