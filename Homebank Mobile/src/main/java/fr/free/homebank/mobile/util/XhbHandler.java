package fr.free.homebank.mobile.util;

import android.content.ContentValues;
import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

public class XhbHandler {
	public static final String KEY_VERSION = "version";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CAR = "car";
	public static final String KEY_DAYS = "days";

	private static final String TAG_FILE_START = "homebank";
	private static final String ATTR_START_VERSION = "v";

	private static final String TAG_PROPERTIES = "properties";
	private static final String ATTR_PROPERTIES_TITLE = "title";
    private static final String ATTR_PROPERTIES_CAR = "auto_smode";
    private static final String ATTR_PROPERTIES_DAYS = "auto_weekday";
//	private static final String ATTR_PROPERTIES_CAR = "car_category";
//	private static final String ATTR_PROPERTIES_DAYS = "auto_nbdays";

	private static final String TAG_ACCOUNT = "account";
	private static final String ATTR_ACCOUNT_NAME = "name";
	private static final String ATTR_ACCOUNT_KEY = "key";
	private static final String ATTR_ACCOUNT_FLAGS = "flags";
	private static final String ATTR_ACCOUNT_POS = "pos";
	private static final String ATTR_ACCOUNT_TYPE = "type";
	private static final String ATTR_ACCOUNT_NUMBER = "number";
	private static final String ATTR_ACCOUNT_INITIAL = "initial";
	private static final String ATTR_ACCOUNT_MINIMUM = "minimum";
	private static final String ATTR_ACCOUNT_CHEQUE1 = "cheque1";
	private static final String ATTR_ACCOUNT_CHEQUE2 = "cheque2";
	private static final String ATTR_ACCOUNT_BANKNAME = "bankname";

	private static final String TAG_PAYEE = "pay";
	private static final String ATTR_PAYEE_NAME = "name";
	private static final String ATTR_PAYEE_KEY = "key";

	private static final String TAG_CATEGORY = "cat";
	private static final String ATTR_CATEGORY_NAME = "name";
	private static final String ATTR_CATEGORY_KEY = "key";
	private static final String ATTR_CATEGORY_PARENT = "parent";
	private static final String ATTR_CATEGORY_FLAGS = "flags";

	private static final String TAG_TRANS = "ope";
	private static final String ATTR_TRANS_ACCOUNT = "account";
	private static final String ATTR_TRANS_DST_ACCOUNT = "dst_account";
	private static final String ATTR_TRANS_PAYMODE = "paymode";
	private static final String ATTR_TRANS_FLAGS = "flags";
	private static final String ATTR_TRANS_INFO = "info";
	private static final String ATTR_TRANS_TAGS = "tags";
	private static final String ATTR_TRANS_DATE = "date";
	private static final String ATTR_TRANS_AMOUNT = "amount";
	private static final String ATTR_TRANS_CATEGORY = "category";
	private static final String ATTR_TRANS_WORDING = "wording";
	private static final String ATTR_TRANS_PAYEE = "payee";
	private static final String ATTR_TRANS_KXFER = "kxfer";

	private static final String TAG_FAV = "fav";
	private static final String ATTR_FAV_ACCOUNT = "account";
	private static final String ATTR_FAV_DST_ACCOUNT = "dst_account";
	private static final String ATTR_FAV_PAYMODE = "paymode";
	private static final String ATTR_FAV_FLAGS = "flags";
	private static final String ATTR_FAV_NEXT = "nextdate";
	private static final String ATTR_FAV_EVERY = "every";
	private static final String ATTR_FAV_UNIT = "unit";
	private static final String ATTR_FAV_LIMIT = "limit";
	private static final String ATTR_FAV_AMOUNT = "amount";
	private static final String ATTR_FAV_CATEGORY = "category";
	private static final String ATTR_FAV_WORDING = "wording";
	private static final String ATTR_FAV_PAYEE = "payee";

	private static final String TAG_ASG = "asg";
	private static final String ATTR_ASG_KEY = "key";
	private static final String ATTR_ASG_NAME = "name";
	private static final String ATTR_ASG_EXACT = "exact";
	private static final String ATTR_ASG_PAYEE = "payee";
	private static final String ATTR_ASG_CATEGORY = "category";

	// We don't use namespaces
	private static final String NAMESPACE = null;
	
	private static final String XML_REMOVE = " encoding='UTF-8'";

	private XmlPullParser mParser;
	private InputStream mIn;

	private String mPath;

	public XhbHandler(String path) {
		mPath = path;
		mParser = Xml.newPullParser();
		try {
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	public XhbHandler(String path, Object object) {
		mPath = path;
	}

	private void resetParser() {
		try {
			mIn = new BufferedInputStream(new FileInputStream(mPath));
			mParser.setInput(mIn, null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPath(String path) {
		mPath = path;
	}

	public ContentValues getInfos() throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);

		ContentValues infos = new ContentValues();
		infos.put(KEY_VERSION,
				mParser.getAttributeValue(NAMESPACE, ATTR_START_VERSION));

		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_PROPERTIES)) {
				infos.put(KEY_TITLE, mParser.getAttributeValue(NAMESPACE,
						ATTR_PROPERTIES_TITLE));
				infos.put(KEY_CAR, mParser.getAttributeValue(NAMESPACE,
						ATTR_PROPERTIES_CAR));
				infos.put(KEY_DAYS, mParser.getAttributeValue(NAMESPACE,
						ATTR_PROPERTIES_DAYS));
				return infos;
			} else {
				skip();
			}
		}
		return null;
	}

	public ArrayList<ContentValues> getAccounts()
			throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> accounts = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_ACCOUNT)) {
				ContentValues account = new ContentValues();
				account.put(DbAdapter.KEY_ROWID,
						mParser.getAttributeValue(NAMESPACE, ATTR_ACCOUNT_KEY));
				account.put(DbAdapter.KEY_NAME,
						mParser.getAttributeValue(NAMESPACE, ATTR_ACCOUNT_NAME));
				account.put(DbAdapter.KEY_FLAGS, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_FLAGS));
				account.put(DbAdapter.KEY_POS,
						mParser.getAttributeValue(NAMESPACE, ATTR_ACCOUNT_POS));
				account.put(DbAdapter.KEY_TYPE,
						mParser.getAttributeValue(NAMESPACE, ATTR_ACCOUNT_TYPE));
				account.put(DbAdapter.KEY_NO, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_NUMBER));
				account.put(DbAdapter.KEY_INITIAL, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_INITIAL));
				account.put(DbAdapter.KEY_MIN, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_MINIMUM));
				account.put(DbAdapter.KEY_CHEQUE1, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_CHEQUE1));
				account.put(DbAdapter.KEY_CHEQUE2, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_CHEQUE2));
				account.put(DbAdapter.KEY_BANKNAME, mParser.getAttributeValue(
						NAMESPACE, ATTR_ACCOUNT_BANKNAME));
				accounts.add(account);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();
		return accounts;
	}

	public ArrayList<ContentValues> getCategories()
			throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> categories = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_CATEGORY)) {
				ContentValues category = new ContentValues();
				category.put(DbAdapter.KEY_ROWID,
						mParser.getAttributeValue(NAMESPACE, ATTR_CATEGORY_KEY));
				category.put(DbAdapter.KEY_NAME, mParser.getAttributeValue(
						NAMESPACE, ATTR_CATEGORY_NAME));
				category.put(DbAdapter.KEY_PARENT, mParser.getAttributeValue(
						NAMESPACE, ATTR_CATEGORY_PARENT) == null ? "0" : mParser.getAttributeValue(
								NAMESPACE, ATTR_CATEGORY_PARENT));
				category.put(DbAdapter.KEY_FLAGS, mParser.getAttributeValue(
						NAMESPACE, ATTR_CATEGORY_FLAGS) == null ? "0" : mParser.getAttributeValue(
								NAMESPACE, ATTR_CATEGORY_FLAGS));
				categories.add(category);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();
		return categories;
	}

	public ArrayList<ContentValues> getPayees() throws XmlPullParserException,
			IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> payees = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_PAYEE)) {
				ContentValues payee = new ContentValues();
				payee.put(DbAdapter.KEY_ROWID,
						mParser.getAttributeValue(NAMESPACE, ATTR_PAYEE_KEY));
				payee.put(DbAdapter.KEY_NAME,
						mParser.getAttributeValue(NAMESPACE, ATTR_PAYEE_NAME));
				payees.add(payee);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();
		return payees;
	}

	public ArrayList<ContentValues> getFavorites()
			throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> favorites = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_FAV)) {
				ContentValues favorite = new ContentValues();
				favorite.put(DbAdapter.KEY_ACC,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_ACCOUNT));
				favorite.put(DbAdapter.KEY_DST_ACC, mParser.getAttributeValue(
						NAMESPACE, ATTR_FAV_DST_ACCOUNT));
				favorite.put(DbAdapter.KEY_PAYMODE,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_PAYMODE));
				favorite.put(DbAdapter.KEY_FLAGS,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_FLAGS));
				favorite.put(DbAdapter.KEY_NEXT_DATE,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_NEXT));
				favorite.put(DbAdapter.KEY_EVERY,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_EVERY));
				favorite.put(DbAdapter.KEY_UNIT,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_UNIT));
				favorite.put(DbAdapter.KEY_LIMIT,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_LIMIT));
				favorite.put(DbAdapter.KEY_AMOUNT,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_AMOUNT));
				favorite.put(DbAdapter.KEY_CATEGORY,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_CATEGORY));
				favorite.put(DbAdapter.KEY_TEXT,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_WORDING));
				favorite.put(DbAdapter.KEY_PAYEE,
						mParser.getAttributeValue(NAMESPACE, ATTR_FAV_PAYEE));
				favorites.add(favorite);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();
		return favorites;
	}

	public ArrayList<ContentValues> getAssignments()
			throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> assignments = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_ASG)) {
				ContentValues assignment = new ContentValues();
				assignment.put(DbAdapter.KEY_ROWID,
						mParser.getAttributeValue(NAMESPACE, ATTR_ASG_KEY));
				assignment.put(DbAdapter.KEY_NAME,
						mParser.getAttributeValue(NAMESPACE, ATTR_ASG_NAME));
				assignment.put(DbAdapter.KEY_EXACT,
						mParser.getAttributeValue(NAMESPACE, ATTR_ASG_EXACT));
				assignment.put(DbAdapter.KEY_PAYEE,
						mParser.getAttributeValue(NAMESPACE, ATTR_ASG_PAYEE));
				assignment
						.put(DbAdapter.KEY_CATEGORY, mParser.getAttributeValue(
								NAMESPACE, ATTR_ASG_CATEGORY));
				assignments.add(assignment);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();
		return assignments;
	}

	public ArrayList<ContentValues> getTransactions()
			throws XmlPullParserException, IOException {
		resetParser();
		mParser.nextTag();

		ArrayList<ContentValues> transactions = new ArrayList<ContentValues>();
		mParser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_FILE_START);

		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = mParser.getName();
			if (name.equals(TAG_TRANS)) {
				ContentValues m = new ContentValues();
				// date, amount, account, category is non-empty
				m.put(DbAdapter.KEY_DATE,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_DATE));
				m.put(DbAdapter.KEY_AMOUNT,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_AMOUNT));
				m.put(DbAdapter.KEY_CATEGORY, mParser.getAttributeValue(
						NAMESPACE, ATTR_TRANS_CATEGORY));
				m.put(DbAdapter.KEY_PAYEE,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_PAYEE) == null ? "0" : mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_PAYEE));
				m.put(DbAdapter.KEY_TEXT, mParser.getAttributeValue(NAMESPACE,
						ATTR_TRANS_WORDING));
				m.put(DbAdapter.KEY_ACC, mParser.getAttributeValue(NAMESPACE,
						ATTR_TRANS_ACCOUNT));
				m.put(DbAdapter.KEY_DST_ACC, mParser.getAttributeValue(
						NAMESPACE, ATTR_TRANS_DST_ACCOUNT));
				m.put(DbAdapter.KEY_PAYMODE, mParser.getAttributeValue(
						NAMESPACE, ATTR_TRANS_PAYMODE));
				m.put(DbAdapter.KEY_FLAGS,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_FLAGS));
				m.put(DbAdapter.KEY_INFO,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_INFO));
				m.put(DbAdapter.KEY_TAGS,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_TAGS));
				m.put(DbAdapter.KEY_KXFER,
						mParser.getAttributeValue(NAMESPACE, ATTR_TRANS_KXFER));
				transactions.add(m);
				while (mParser.next() != XmlPullParser.END_TAG) {
				}
			} else {
				skip();
			}
		}
		mIn.close();

		return transactions;
	}

	// Skips tags the parser isn't interested in. Uses depth to handle nested
	// tags. i.e., if the next tag after a START_TAG isn't a matching END_TAG,
	// it keeps
	// going until it finds the matching END_TAG (as indicated by the value of
	// "depth" being 0).
	private void skip() throws XmlPullParserException, IOException {
		if (mParser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (mParser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	public void writeFile(Context context, ContentValues infos,
			ArrayList<ContentValues> accounts, ArrayList<ContentValues> payees,
			ArrayList<ContentValues> categories,
			ArrayList<ContentValues> assignments,
			ArrayList<ContentValues> favorites,
			ArrayList<ContentValues> transactions) {

		try {
//			BufferedOutputStream out = new BufferedOutputStream(
//					context.openFileOutput("test.xml", Context.MODE_PRIVATE));

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(mPath));

			XmlSerializer serializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(NAMESPACE, TAG_FILE_START);
			serializer.attribute(NAMESPACE, ATTR_START_VERSION,
					infos.getAsString(KEY_VERSION));

			serializer.startTag(NAMESPACE, TAG_PROPERTIES);
			serializer.attribute(NAMESPACE, ATTR_PROPERTIES_TITLE,
					infos.getAsString(KEY_TITLE));
			serializer.attribute(NAMESPACE, ATTR_PROPERTIES_CAR,
					infos.getAsString(KEY_CAR));
			serializer.attribute(NAMESPACE, ATTR_PROPERTIES_DAYS,
					infos.getAsString(KEY_DAYS));
			serializer.endTag(NAMESPACE, TAG_PROPERTIES);

			for (ContentValues account : accounts) {
				serializer.startTag(NAMESPACE, TAG_ACCOUNT);
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_KEY,
						account.getAsString(DbAdapter.KEY_ROWID));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_FLAGS,
						account.getAsString(DbAdapter.KEY_FLAGS) == null ? "" : account.getAsString(DbAdapter.KEY_FLAGS));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_POS,
						account.getAsString(DbAdapter.KEY_POS) == null ? "" : account.getAsString(DbAdapter.KEY_POS));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_TYPE,
						account.getAsString(DbAdapter.KEY_TYPE) == null ? "" : account.getAsString(DbAdapter.KEY_TYPE));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_NAME,
						account.getAsString(DbAdapter.KEY_NAME));
				if (account.getAsString(DbAdapter.KEY_NO) != null)
					serializer.attribute(NAMESPACE, ATTR_ACCOUNT_NUMBER,
							account.getAsString(DbAdapter.KEY_NO));
				if (account.getAsString(DbAdapter.KEY_BANKNAME) != null)
					serializer.attribute(NAMESPACE, ATTR_ACCOUNT_BANKNAME,
							account.getAsString(DbAdapter.KEY_BANKNAME));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_INITIAL,
						account.getAsString(DbAdapter.KEY_INITIAL));
				serializer.attribute(NAMESPACE, ATTR_ACCOUNT_MINIMUM,
						account.getAsString(DbAdapter.KEY_MIN));
				if (account.getAsString(DbAdapter.KEY_CHEQUE1) != null)
					serializer.attribute(NAMESPACE, ATTR_ACCOUNT_CHEQUE1,
							account.getAsString(DbAdapter.KEY_CHEQUE1));
				if (account.getAsString(DbAdapter.KEY_CHEQUE2) != null)
					serializer.attribute(NAMESPACE, ATTR_ACCOUNT_CHEQUE2,
							account.getAsString(DbAdapter.KEY_CHEQUE2));
				serializer.endTag(NAMESPACE, TAG_ACCOUNT);
			}

			for (ContentValues payee : payees) {
				serializer.startTag(NAMESPACE, TAG_PAYEE);
				serializer.attribute(NAMESPACE, ATTR_PAYEE_KEY,
						payee.getAsString(DbAdapter.KEY_ROWID));
				serializer.attribute(NAMESPACE, ATTR_PAYEE_NAME,
						payee.getAsString(DbAdapter.KEY_NAME));
				serializer.endTag(NAMESPACE, TAG_PAYEE);
			}

			for (ContentValues category : categories) {
				serializer.startTag(NAMESPACE, TAG_CATEGORY);
				serializer.attribute(NAMESPACE, ATTR_CATEGORY_KEY,
						category.getAsString(DbAdapter.KEY_ROWID));
				if (category.getAsLong(DbAdapter.KEY_PARENT) != 0)
					serializer.attribute(NAMESPACE, ATTR_CATEGORY_PARENT,
							category.getAsString(DbAdapter.KEY_PARENT));
				if (category.getAsLong(DbAdapter.KEY_FLAGS) != 0)
					serializer.attribute(NAMESPACE, ATTR_CATEGORY_FLAGS,
							category.getAsString(DbAdapter.KEY_FLAGS));
				serializer.attribute(NAMESPACE, ATTR_CATEGORY_NAME,
						category.getAsString(DbAdapter.KEY_NAME));
				serializer.endTag(NAMESPACE, TAG_CATEGORY);
			}

			for (ContentValues assignment : assignments) {
				serializer.startTag(NAMESPACE, TAG_ASG);
				serializer.attribute(NAMESPACE, ATTR_ASG_KEY,
						assignment.getAsString(DbAdapter.KEY_ROWID));
				serializer.attribute(NAMESPACE, ATTR_ASG_NAME,
						assignment.getAsString(DbAdapter.KEY_NAME));
				serializer.attribute(NAMESPACE, ATTR_ASG_EXACT,
						assignment.getAsString(DbAdapter.KEY_EXACT));
				serializer.attribute(NAMESPACE, ATTR_ASG_PAYEE,
						assignment.getAsString(DbAdapter.KEY_PAYEE));
				serializer.attribute(NAMESPACE, ATTR_ASG_CATEGORY,
						assignment.getAsString(DbAdapter.KEY_CATEGORY));
				serializer.endTag(NAMESPACE, TAG_ASG);
			}

			for (ContentValues favorite : favorites) {
				serializer.startTag(NAMESPACE, TAG_FAV);
				serializer.attribute(NAMESPACE, ATTR_FAV_AMOUNT,
						favorite.getAsString(DbAdapter.KEY_AMOUNT));//
				//if (favorite.getAsLong(DbAdapter.KEY_ACC) != 0)
				if (favorite.getAsString(DbAdapter.KEY_ACC) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_ACCOUNT,
							favorite.getAsString(DbAdapter.KEY_ACC));
				if (favorite.getAsString(DbAdapter.KEY_DST_ACC) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_DST_ACCOUNT,
							favorite.getAsString(DbAdapter.KEY_DST_ACC));
				if (favorite.getAsString(DbAdapter.KEY_PAYMODE) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_PAYMODE,
							favorite.getAsString(DbAdapter.KEY_PAYMODE));
				if (favorite.getAsString(DbAdapter.KEY_FLAGS) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_FLAGS,
							favorite.getAsString(DbAdapter.KEY_FLAGS));
				if (favorite.getAsString(DbAdapter.KEY_PAYEE) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_PAYEE,
							favorite.getAsString(DbAdapter.KEY_PAYEE));
				if (favorite.getAsString(DbAdapter.KEY_CATEGORY) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_CATEGORY,
							favorite.getAsString(DbAdapter.KEY_CATEGORY));
				serializer.attribute(NAMESPACE, ATTR_FAV_WORDING,
						favorite.getAsString(DbAdapter.KEY_TEXT));//
				serializer.attribute(NAMESPACE, ATTR_FAV_NEXT,
						favorite.getAsString(DbAdapter.KEY_NEXT_DATE));//
				serializer.attribute(NAMESPACE, ATTR_FAV_EVERY,
						favorite.getAsString(DbAdapter.KEY_EVERY));//
				if (favorite.getAsString(DbAdapter.KEY_UNIT) != null)
					serializer.attribute(NAMESPACE, ATTR_FAV_UNIT,
							favorite.getAsString(DbAdapter.KEY_UNIT));
				serializer.attribute(NAMESPACE, ATTR_FAV_LIMIT,
						favorite.getAsString(DbAdapter.KEY_LIMIT));//
				serializer.endTag(NAMESPACE, TAG_FAV);
			}

			for (ContentValues transaction : transactions) {
				serializer.startTag(NAMESPACE, TAG_TRANS);
				serializer.attribute(NAMESPACE, ATTR_TRANS_DATE,
						transaction.getAsString(DbAdapter.KEY_DATE));
				serializer.attribute(NAMESPACE, ATTR_TRANS_AMOUNT,
						transaction.getAsString(DbAdapter.KEY_AMOUNT));
				serializer.attribute(NAMESPACE, ATTR_TRANS_ACCOUNT,
						transaction.getAsString(DbAdapter.KEY_ACC));
				if (transaction.getAsString(DbAdapter.KEY_DST_ACC) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_DST_ACCOUNT,
							transaction.getAsString(DbAdapter.KEY_DST_ACC));
				if (transaction.getAsString(DbAdapter.KEY_PAYMODE) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_PAYMODE,
							transaction.getAsString(DbAdapter.KEY_PAYMODE));
				if (transaction.getAsString(DbAdapter.KEY_FLAGS) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_FLAGS,
							transaction.getAsString(DbAdapter.KEY_FLAGS));
				serializer.attribute(NAMESPACE, ATTR_TRANS_PAYEE,
						transaction.getAsString(DbAdapter.KEY_PAYEE));
				if (transaction.getAsString(DbAdapter.KEY_CATEGORY) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_CATEGORY,
							transaction.getAsString(DbAdapter.KEY_CATEGORY));
				if (transaction.getAsString(DbAdapter.KEY_TEXT) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_WORDING,
							transaction.getAsString(DbAdapter.KEY_TEXT));
				if (transaction.getAsString(DbAdapter.KEY_INFO) != null)
					serializer.attribute(NAMESPACE, ATTR_TRANS_INFO,
							transaction.getAsString(DbAdapter.KEY_INFO));
				serializer.attribute(NAMESPACE, ATTR_TRANS_TAGS,
						transaction.getAsString(DbAdapter.KEY_TAGS) == null ? "" : transaction.getAsString(DbAdapter.KEY_TAGS));
				serializer.attribute(NAMESPACE, ATTR_TRANS_KXFER,
						transaction.getAsString(DbAdapter.KEY_KXFER) == null ? "" : transaction.getAsString(DbAdapter.KEY_KXFER));
				serializer.endTag(NAMESPACE, TAG_TRANS);
			}

			serializer.endTag(NAMESPACE, TAG_FILE_START);
			serializer.endDocument();

			out.write(writer.toString().replace(XML_REMOVE, "").getBytes());
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}