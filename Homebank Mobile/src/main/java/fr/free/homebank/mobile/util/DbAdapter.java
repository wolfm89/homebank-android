package fr.free.homebank.mobile.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;

public class DbAdapter {
	public static final String TABLE_TRANSACTION = "trans";
	public static final String TABLE_FAVORITE = "favorite";
	public static final String TABLE_ACCOUNT = "account";
	public static final String TABLE_CATEGORY = "category";
	public static final String TABLE_PAYEE = "payee";
	public static final String TABLE_ASSIGNMENT = "assignment";
	public static final String KEY_DATE = "date";
	public static final String KEY_NEXT_DATE = "nextdate";
	public static final String KEY_PAYEE = "payee";
	public static final String KEY_AMOUNT = "amount";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_TEXT = "wording";
	public static final String KEY_NAME = "name";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ACC = "acc";
	public static final String KEY_DST_ACC = "dst_account";
	public static final String KEY_PARENT = "parent";
	public static final String KEY_PAYMODE = "paymode";
	public static final String KEY_FLAGS = "flags";
	public static final String KEY_INFO = "info";
	public static final String KEY_TAGS = "tags";
	public static final String KEY_KXFER = "kxfer";
	public static final String KEY_EVERY = "every";
	public static final String KEY_UNIT = "unit";
	public static final String KEY_LIMIT = "lim";
	public static final String KEY_EXACT = "exact";
	public static final String KEY_POS = "pos";
	public static final String KEY_TYPE = "type";
	public static final String KEY_NO = "number";
	public static final String KEY_BANKNAME = "bankname";
	public static final String KEY_INITIAL = "initial";
	public static final String KEY_MIN = "minimum";
	public static final String KEY_CHEQUE1 = "cheque1";
	public static final String KEY_CHEQUE2 = "cheque2";
	
	public static final String CATEGORY_ID = "parent_id";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String CREATE_ACCOUNT = "CREATE TABLE "+TABLE_ACCOUNT+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "+KEY_FLAGS+" INTEGER, "+KEY_POS+" INTEGER, "+KEY_TYPE+" INTEGER, "
	+KEY_NO+" TEXT, "+KEY_BANKNAME+" TEXT, "+KEY_INITIAL+" DOUBLE, "+KEY_MIN+" DOUBLE, "+KEY_CHEQUE1+" INTEGER, "+KEY_CHEQUE2+" INTEGER, "+KEY_NAME+" TEXT NOT NULL);";
	private static final String CREATE_PAYEE = "CREATE TABLE "+TABLE_PAYEE+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "+KEY_NAME+" TEXT NOT NULL);";
	private static final String CREATE_CATEGORY = "CREATE TABLE "+TABLE_CATEGORY+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "+KEY_PARENT+" INTEGER, "+KEY_FLAGS+" INTEGER, "+KEY_NAME+" TEXT NOT NULL, "//+KEY_AMOUNT+" DOUBLE NOT NULL, "
			+ "FOREIGN KEY("+KEY_PARENT+") REFERENCES "+TABLE_CATEGORY+"("+KEY_ROWID+"));";
	private static final String CREATE_TRANSACTION = "CREATE TABLE "+TABLE_TRANSACTION+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "
			+ KEY_DATE+" INTEGER NOT NULL, "+KEY_PAYEE+" INTEGER, "+KEY_AMOUNT+" DOUBLE NOT NULL, "
			+ KEY_CATEGORY+" INTEGER, "+KEY_TEXT+" TEXT, "+KEY_ACC+" INTEGER NOT NULL, "+KEY_DST_ACC+" INTEGER, "+KEY_PAYMODE+" INTEGER, "+KEY_FLAGS+" INTEGER, "+KEY_INFO+" TEXT, "+KEY_TAGS+" TEXT, "+KEY_KXFER+" INTEGER, "
			+ "FOREIGN KEY("+KEY_ACC+") REFERENCES "+TABLE_ACCOUNT+"("+KEY_ROWID+"), "
			+ "FOREIGN KEY("+KEY_CATEGORY+") REFERENCES "+TABLE_CATEGORY+"("+KEY_ROWID+"), "
			+ "FOREIGN KEY("+KEY_PAYEE+") REFERENCES "+TABLE_PAYEE+"("+KEY_ROWID+")"
			+ ");";
	private static final String CREATE_FAVORITE = "CREATE TABLE "+TABLE_FAVORITE+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "
			+ KEY_NEXT_DATE+" INTEGER NOT NULL, "+KEY_PAYEE+" INTEGER, "+KEY_AMOUNT+" DOUBLE NOT NULL, "
			+ KEY_CATEGORY+" INTEGER, "+KEY_TEXT+" TEXT, "+KEY_ACC+" INTEGER, "+KEY_DST_ACC+" INTEGER, "+KEY_PAYMODE+" INTEGER, "+KEY_FLAGS+" INTEGER, "+KEY_EVERY+" INTEGER, "+KEY_UNIT+" INTEGER, "+KEY_LIMIT+" INTEGER, "
			+ "FOREIGN KEY("+KEY_ACC+") REFERENCES "+TABLE_ACCOUNT+"("+KEY_ROWID+"), "
			+ "FOREIGN KEY("+KEY_CATEGORY+") REFERENCES "+TABLE_CATEGORY+"("+KEY_ROWID+"), "
			+ "FOREIGN KEY("+KEY_PAYEE+") REFERENCES "+TABLE_PAYEE+"("+KEY_ROWID+")"
			+ ");";
	public static final String CREATE_ASSIGNMENT = "CREATE TABLE "+TABLE_ASSIGNMENT+" ("+KEY_ROWID+" INTEGER PRIMARY KEY autoincrement, "
			+KEY_NAME+" TEXT NOT NULL, "+KEY_PAYEE+" INTEGER, "+KEY_CATEGORY+" INTEGER, "+KEY_EXACT+" INTEGER, "
			+ "FOREIGN KEY("+KEY_CATEGORY+") REFERENCES "+TABLE_CATEGORY+"("+KEY_ROWID+"), "
			+ "FOREIGN KEY("+KEY_PAYEE+") REFERENCES "+TABLE_PAYEE+"("+KEY_ROWID+")"
			+");";
	
//    private static final String ALTER_1 = "alter table trans add acc integer not null";

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;

	private final Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_ACCOUNT);
			db.execSQL(CREATE_CATEGORY);
			db.execSQL(CREATE_PAYEE);
			db.execSQL(CREATE_TRANSACTION);
			db.execSQL(CREATE_FAVORITE);
			db.execSQL(CREATE_ASSIGNMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			if (oldVersion == 1) {
//        		db.execSQL(ALTER_1);
//        	}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public DbAdapter(Context context) {
		this.context = context;
	}

	/**
	 * Open the database.
	 * 
	 * @return
	 * @throws android.database.SQLException
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(context);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database.
	 */
	public void close() {
		if (mDbHelper != null)
			mDbHelper.close();
	}

	public long insertAccount(ContentValues args) {
		return mDb.insert(TABLE_ACCOUNT, null, args);
	}

	public long insertCategory(ContentValues args) {
        //args.put(KEY_AMOUNT, "0");
		return mDb.insert(TABLE_CATEGORY, null, args);
	}

	public long insertPayee(ContentValues args) {
		return mDb.insert(TABLE_PAYEE, null, args);
	}

	public long insertTransaction(ContentValues args) {
        //Log.i("TRANSACTION", args.getAsString(KEY_TEXT) + " " + args.getAsString(KEY_CATEGORY) + " " + args.getAsString(KEY_AMOUNT));
        //if  (args.getAsInteger(KEY_CATEGORY) != null && args.getAsDouble(KEY_AMOUNT) != 0)
        //    addCategoryExpenseByTransaction(args.getAsInteger(KEY_CATEGORY), args.getAsDouble(KEY_AMOUNT));
		return mDb.insert(TABLE_TRANSACTION, null, args);
	}

	public long insertFavorite(ContentValues args) {
		return mDb.insert(TABLE_FAVORITE, null, args);
	}

	public long insertAssignment(ContentValues args) {
		return mDb.insert(TABLE_ASSIGNMENT, null, args);
	}

	public Boolean insertFromXhb(String file) throws XmlPullParserException, IOException {
		file = context.getFileStreamPath(file).getPath();
		if (file != null) {
			deleteData();
			XhbHandler parser = new XhbHandler(file);

			saveInfos(parser.getInfos(), context);


			ArrayList<ContentValues> accounts = parser.getAccounts();
			for (Iterator<ContentValues> i = accounts.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertAccount(cv);
			}
			ArrayList<ContentValues> categories = parser.getCategories();
			for (Iterator<ContentValues> i = categories.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertCategory(cv);
			}

			// dumb element
			ContentValues category = new ContentValues();
			category.put(KEY_ROWID, "0");
			category.put(KEY_NAME, "");
			category.put(KEY_PARENT, "0");
			category.put(KEY_FLAGS, "0");
			insertCategory(category);

			ArrayList<ContentValues> payees = parser.getPayees();
			for (Iterator<ContentValues> i = payees.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertPayee(cv);
			}
			ArrayList<ContentValues> transactions = parser.getTransactions();
			for (Iterator<ContentValues> i = transactions.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertTransaction(cv);
			}
			ArrayList<ContentValues> favorites = parser.getFavorites();
			for (Iterator<ContentValues> i = favorites.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertFavorite(cv);
			}
			ArrayList<ContentValues> assignments = parser.getAssignments();
			for (Iterator<ContentValues> i = assignments.iterator(); i.hasNext();) {
				ContentValues cv = i.next();
				insertAssignment(cv);
			}

			return true;
		} else
			return false;
	}

    private void addCategoryExpenseByTransaction(int catId, double amount) {
        ContentValues cat = fetchCategoryParentAmount(catId);
        ContentValues updVal = new ContentValues();
        updVal.put(KEY_AMOUNT, cat.getAsDouble(KEY_AMOUNT) + amount);
        updateCategory(catId, updVal);
        if (cat.getAsInteger(KEY_PARENT) != 0)
            addCategoryExpenseByTransaction(cat.getAsInteger(KEY_PARENT), amount);
    }

    public void removeExpense(Long rowId) {
        ContentValues row = fetchTransactionCategoryAmount(rowId);
        removeExpenseFromCategories(row.getAsInteger(KEY_CATEGORY), row.getAsDouble(KEY_AMOUNT));
    }

    private void removeExpenseFromCategories(int catId, double amount) {
        ContentValues cat = fetchCategoryParentAmount(catId);
        ContentValues updVal = new ContentValues();
        updVal.put(KEY_AMOUNT, cat.getAsDouble(KEY_AMOUNT) - amount);
        updateCategory(catId, updVal);
        if (cat.getAsInteger(KEY_PARENT) != 0)
            removeExpenseFromCategories(cat.getAsInteger(KEY_PARENT), amount);
    }

    public static void saveInfos(ContentValues infos, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Util.INFOS_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(XhbHandler.KEY_VERSION, infos.getAsString(XhbHandler.KEY_VERSION));
        edit.putString(XhbHandler.KEY_TITLE, infos.getAsString(XhbHandler.KEY_TITLE));
        edit.putString(XhbHandler.KEY_CAR, infos.getAsString(XhbHandler.KEY_CAR));
        edit.putString(XhbHandler.KEY_DAYS, infos.getAsString(XhbHandler.KEY_DAYS));
        edit.commit();
    }

    public static ContentValues getInfos(Context context) {
        ContentValues infos = new ContentValues();
        SharedPreferences prefs = context.getSharedPreferences(Util.INFOS_PREFS_NAME, 0);
        infos.put(XhbHandler.KEY_VERSION, prefs.getString(XhbHandler.KEY_VERSION, ""));
        infos.put(XhbHandler.KEY_TITLE, prefs.getString(XhbHandler.KEY_TITLE, ""));
        infos.put(XhbHandler.KEY_CAR, prefs.getString(XhbHandler.KEY_CAR, ""));
        infos.put(XhbHandler.KEY_DAYS, prefs.getString(XhbHandler.KEY_DAYS, ""));
        return infos;
    }

	public void writeToXhb(String file) {
		ArrayList<ContentValues> accounts = getAllFromTable(TABLE_ACCOUNT);
		ArrayList<ContentValues> payees = getAllFromTable(TABLE_PAYEE);
		ArrayList<ContentValues> categories = getAllFromTable(TABLE_CATEGORY);
		ArrayList<ContentValues> assignments = getAllFromTable(TABLE_ASSIGNMENT);
		ArrayList<ContentValues> favorites = getAllFromTable(TABLE_FAVORITE);
		ArrayList<ContentValues> transactions = getAllFromTable(TABLE_TRANSACTION);
		ContentValues infos = getInfos(context);

		file = context.getFileStreamPath(file).getPath();
		if (file != null) {
			XhbHandler parser = new XhbHandler(file, null);
			parser.writeFile(context, infos, accounts, payees, categories, assignments, favorites, transactions);
		}
	}

	public void deleteData() {
		deleteAllAccounts();
		deleteAllCategories();
		deleteAllTransactions();
		deleteAllPayees();
		deleteAllFavorites();
		deleteAllAssignments();
	}

	public boolean deleteTransaction(long rowId) {
        //removeExpense(rowId);
		return mDb.delete(TABLE_TRANSACTION, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllTransactions() {
		return mDb.delete(TABLE_TRANSACTION, null, null) > 0;
	}

	public boolean deleteAllAccounts() {
		return mDb.delete(TABLE_ACCOUNT, null, null) > 0;
	}

	public boolean deleteAllCategories() {
		return mDb.delete(TABLE_CATEGORY, null, null) > 0;
	}

	public boolean deleteAllPayees() {
		return mDb.delete(TABLE_PAYEE, null, null) > 0;
	}

	public boolean deleteAllFavorites() {
		return mDb.delete(TABLE_FAVORITE, null, null) > 0;
	}

	public boolean deleteAllAssignments() {
		return mDb.delete(TABLE_ASSIGNMENT, null, null) > 0;
	}

	public Cursor fetchAllTransactions(long accId, long fromDate, long toDate) {
		String query = "SELECT t."+KEY_ROWID+", t."+KEY_DATE+", t."+KEY_AMOUNT+", t."+KEY_TEXT+", "+TABLE_CATEGORY+"."+KEY_NAME+" AS "+KEY_CATEGORY+", "+TABLE_PAYEE+"."+KEY_NAME+" AS "+KEY_PAYEE+" " +
				"FROM "+TABLE_TRANSACTION+" t " +
				"JOIN "+TABLE_CATEGORY+" ON "+TABLE_CATEGORY+"."+KEY_ROWID+" = t."+KEY_CATEGORY+" " +
				"LEFT OUTER JOIN "+TABLE_PAYEE+" ON t."+KEY_PAYEE+" = "+TABLE_PAYEE+"."+KEY_ROWID+" " +
				"WHERE t."+KEY_ACC+" = ? " +
				"AND t."+KEY_DATE+" BETWEEN ? AND ? " +
				"ORDER BY t."+KEY_DATE+" desc";
//		Log.i("accId", String.valueOf(accId));
//		Log.i("query", query);
		return mDb.rawQuery(query, new String[] { String.valueOf(accId), String.valueOf(fromDate), String.valueOf(toDate) } );
	}

	public Cursor fetchAllAccounts() {
		return mDb.query(TABLE_ACCOUNT, new String[] { KEY_ROWID, KEY_NAME },
				null, null, null, null, KEY_ROWID + " asc");
	}

	public ArrayList<ContentValues> getAllFromTable(String table) {
		ArrayList<ContentValues> data = new ArrayList<ContentValues>();
		Cursor c = mDb.query(table, null, null, null, null, null, KEY_ROWID + " asc");
		if (c.moveToFirst()) {
			do {
				ContentValues map = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(c, map);
				if (map.getAsLong(KEY_ROWID) == null || map.getAsLong(KEY_PARENT) == null) {
					data.add(map);
				}
				else if (map.getAsLong(KEY_ROWID) != 0 || map.getAsLong(KEY_PARENT) != 0) {
					data.add(map);
				}
			} while (c.moveToNext());
		}
		c.close();
		return data;
	}

	/**
	 * Fetch all transactions starting at dateTime.
	 *
	 * @param dateTime
	 * @return
	 */
	public Cursor fetchStarting(Long dateTime) {
		return mDb.query(TABLE_TRANSACTION, new String[] { KEY_ROWID, KEY_DATE,
				KEY_PAYEE, KEY_AMOUNT, KEY_CATEGORY, KEY_TEXT, KEY_ACC }, KEY_DATE
				+ " >= ?", new String[] { dateTime.toString() }, null, null,
				KEY_DATE + " desc");
	}

	/**
	 * Fetch one transaction from the database.
	 *
	 * @param rowId
	 * @return
	 * @throws android.database.SQLException
	 */
	public Cursor fetchTransaction(long rowId) throws SQLException {
		String query = "SELECT DISTINCT t."+KEY_ROWID+", t."+KEY_DATE+", t."+KEY_AMOUNT+", t."+KEY_TEXT+", "+"c1."+KEY_NAME+" AS "+KEY_CATEGORY+", "+"c2."+KEY_NAME+" AS "+KEY_PARENT+", t."+KEY_CATEGORY+" AS "+CATEGORY_ID+", "+TABLE_PAYEE+"."+KEY_NAME+" AS "+KEY_PAYEE+" " +
				"FROM "+TABLE_TRANSACTION+" t, "+TABLE_CATEGORY+" c1, " +TABLE_CATEGORY+" c2 " +
				"JOIN "+TABLE_CATEGORY+" ON t."+KEY_CATEGORY+" = c1."+KEY_ROWID+" " +
				"JOIN "+TABLE_CATEGORY+" ON c2."+KEY_ROWID+" = c1."+KEY_PARENT+" " +
				"LEFT OUTER JOIN "+TABLE_PAYEE+" ON "+TABLE_PAYEE+"."+KEY_ROWID+" = t."+KEY_PAYEE+" " +
				"WHERE t."+KEY_ROWID+" = ? ";
		// Log.i("rowId", String.valueOf(rowId));
		// Log.i("query", query);
		Cursor cursor = mDb.rawQuery(query, new String[] { String.valueOf(rowId) } );
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

    public ContentValues fetchPayee(long rowId) {
        ContentValues cv = new ContentValues();
        Cursor cursor = mDb.query(true, TABLE_PAYEE,
                new String[] { KEY_ROWID, KEY_NAME }, KEY_ROWID + " = ?", new String[] { String.valueOf(rowId) }, null, null, null, null);
        if (cursor.moveToFirst()) {
            cv.put(KEY_ROWID, cursor.getString(cursor.getColumnIndex(KEY_ROWID)));
            cv.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
        }
        else {
            cursor.close();
            return null;
        }
        cursor.close();
        return cv;
    }

    public ContentValues fetchTransactionCategoryAmount(long rowId) {
        Cursor c = mDb.query(true, TABLE_TRANSACTION,
                new String[] { KEY_ROWID, KEY_CATEGORY, KEY_AMOUNT }, KEY_ROWID + " = ?", new String[] { String.valueOf(rowId) }, null, null, null, null);
        ContentValues cv = new ContentValues();
        if (c.moveToFirst()) {
            cv.put(KEY_CATEGORY, c.getInt(c.getColumnIndex(KEY_CATEGORY)));
            cv.put(KEY_AMOUNT, c.getString(c.getColumnIndex(KEY_AMOUNT)));
        }
        else {
            c.close();
            return null;
        }
        c.close();
        return cv;
    }

    public ContentValues fetchCategoryParentAmount(int rowId) {
        Cursor c = mDb.query(true, TABLE_CATEGORY,
                new String[] { KEY_ROWID, KEY_PARENT, KEY_AMOUNT }, KEY_ROWID + " = ?", new String[] { String.valueOf(rowId) }, null, null, null, null);
        ContentValues cv = new ContentValues();
        if (c.moveToFirst()) {
            cv.put(KEY_ROWID, c.getInt(c.getColumnIndex(KEY_ROWID)));
            cv.put(KEY_PARENT, c.getInt(c.getColumnIndex(KEY_PARENT)));
            cv.put(KEY_AMOUNT, c.getString(c.getColumnIndex(KEY_AMOUNT)));
        }
        else {
            c.close();
            return null;
        }
        c.close();
        return cv;
    }

	/**
	 * Fetch all values in a given column from the database.
	 *
	 * @param column
	 * @param table
	 * @return
	 * @throws android.database.SQLException
	 */
	public Cursor fetch(String column, String table) throws SQLException {
		Cursor cursor = mDb.query(true, table,
				new String[] { column }, null, null, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}
	
	public long payeeExists(String name) throws SQLException {
		Cursor cursor = mDb.query(true, TABLE_PAYEE,
				new String[] { KEY_ROWID }, KEY_NAME + " = ?", new String[] { name }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			return cursor.getCount() == 0 ? 0 : Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROWID)));
		}
		else
			return -1;
	}
	
	public long categoryExists(String name, long parent) {
		Cursor cursor = mDb.query(true, TABLE_CATEGORY,
				new String[] { KEY_ROWID }, KEY_NAME + " = ? AND " + KEY_PARENT + " = ?", new String[] { name, String.valueOf(parent) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			return cursor.getCount() == 0 ? 0 : Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROWID)));
		}
		else
			return -1;
	}
	
//	public long categoryExists(String name) throws SQLException {
//		Cursor cursor = mDb.query(true, TABLE_PAYEE,
//				new String[] { KEY_ROWID }, KEY_NAME + " = ?", new String[] { name }, null, null, null, null);
//		if (cursor != null) {
//			cursor.moveToFirst();
//			return cursor.getCount() == 0 ? 0 : Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROWID)));
//		}
//		else
//			return -1;
//	}
	
	
	public boolean updateTransaction(long rowId, ContentValues args) {
        //removeExpense(rowId);
        //addCategoryExpenseByTransaction(args.getAsInteger(KEY_CATEGORY), args.getAsDouble(KEY_AMOUNT));
		return mDb.update(TABLE_TRANSACTION, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

    public boolean updateCategory(int rowId, ContentValues args) {
        return mDb.update(TABLE_CATEGORY, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

	public ArrayList<ContentValues> getCategoryList(
			ArrayList<ContentValues> cats, long id) {
		ContentValues c = new ContentValues();
		Cursor cursor = mDb.query(true, TABLE_CATEGORY,
				new String[] { KEY_ROWID, KEY_PARENT, KEY_NAME }, KEY_ROWID + " = ?", new String[] { String.valueOf(id) }, null, null, null, null);
		cursor.moveToFirst();
		
		c.put(KEY_ROWID, cursor.getString(cursor.getColumnIndex(KEY_ROWID)));
		c.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
		cats.add(c);
		
		if (cursor.getString(cursor.getColumnIndex(KEY_PARENT)).equals("0"))
			return cats;
		return getCategoryList(cats, Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_PARENT))));
	}

	public double getTotal(long mAccount) {
		Cursor cursor = mDb.query(true, TABLE_TRANSACTION,
				new String[] { "SUM(" + KEY_AMOUNT + ") AS " + KEY_AMOUNT }, KEY_ACC + " = ?", new String[] { String.valueOf(mAccount) }, null, null, null, null);
		cursor.moveToFirst();
		double total = cursor.getDouble(cursor.getColumnIndex(KEY_AMOUNT));
		
		cursor = mDb.query(true, TABLE_ACCOUNT,
				new String[] { KEY_INITIAL }, KEY_ROWID + " = ?", new String[] { String.valueOf(mAccount) }, null, null, null, null);
		cursor.moveToFirst();
		total += cursor.getDouble(cursor.getColumnIndex(KEY_INITIAL));
		cursor.close();
		return total;
	}

    private ArrayList<ContentValues> getRootCategories () {
        Cursor c = mDb.query(true, TABLE_CATEGORY,
                new String[] { KEY_ROWID, KEY_PARENT, KEY_NAME }, KEY_PARENT + " = 0", null, null, null, null, null);
        ArrayList<ContentValues> rootCategories = new ArrayList<ContentValues>();
        if (c.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                cv.put(KEY_ROWID, c.getInt(c.getColumnIndex(KEY_ROWID)));
                cv.put(KEY_NAME, c.getString(c.getColumnIndex(KEY_NAME)));
                rootCategories.add(cv);
            } while (c.moveToNext());
        }
        else {
            c.close();
            return null;
        }
        c.close();
        return rootCategories;
    }

    private int getRootCategory(Integer category) {
        Cursor cursor = mDb.query(true, TABLE_CATEGORY,
                new String[] { KEY_ROWID, KEY_PARENT }, KEY_ROWID + " = ?", new String[] { String.valueOf(category) }, null, null, null, null);
        cursor.moveToFirst();

        int parent = cursor.getInt(cursor.getColumnIndex(KEY_PARENT));
        int id = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));

        cursor.close();

        if (parent == 0)
            return id;
        return getRootCategory(parent);
    }

    public ArrayList<ContentValues> getExpensesByRootCategory() {
        ArrayList<ContentValues> transactions = getAllFromTable(TABLE_TRANSACTION);
        ArrayList<ContentValues> rootCategories = getRootCategories();

        for (ContentValues transaction : transactions) {
            int rootCategory = getRootCategory(transaction.getAsInteger(KEY_CATEGORY));
            for (ContentValues cat : rootCategories)
                if (rootCategory == cat.getAsInteger(KEY_ROWID)) {
                    if (cat.getAsDouble(KEY_AMOUNT) == null)
                        cat.put(KEY_AMOUNT, transaction.getAsDouble(KEY_AMOUNT));
                    else {
                        cat.put(KEY_AMOUNT, cat.getAsDouble(KEY_AMOUNT) + transaction.getAsDouble(KEY_AMOUNT));
                    }
                    break;
                }
        }

        for (int i = 0; i < rootCategories.size(); i++)
            if (rootCategories.get(i).getAsDouble(KEY_AMOUNT) == null) {
                rootCategories.remove(i);
                i--;
            }

        return rootCategories;
    }

    public ArrayList<ContentValues> getCategoryExpensesAsc(int limit) {
        Cursor c = mDb.query(true, TABLE_CATEGORY,
                new String[] { KEY_ROWID, KEY_NAME, KEY_AMOUNT, KEY_PARENT }, KEY_PARENT + " = 0", null, null, null, KEY_AMOUNT + " asc", String.valueOf(limit));
        ArrayList<ContentValues> categories = new ArrayList<ContentValues>();
        if (c.moveToFirst()) {
            do {
                ContentValues cv = new ContentValues();
                cv.put(KEY_ROWID, c.getInt(c.getColumnIndex(KEY_ROWID)));
                cv.put(KEY_NAME, c.getString(c.getColumnIndex(KEY_NAME)));
                cv.put(KEY_AMOUNT, c.getDouble(c.getColumnIndex(KEY_AMOUNT)));
                categories.add(cv);
            } while (c.moveToNext());
        }
        else {
            c.close();
            return null;
        }
        c.close();
        return categories;
    }

    public double getTotalExpenses() {
        Cursor c = mDb.query(true, TABLE_CATEGORY,
                new String[] { "SUM(" + KEY_AMOUNT + ") AS " + KEY_AMOUNT }, KEY_AMOUNT + " < 0", null, null, null, null, null);
        if (c.moveToFirst()) {
            double total = c.getDouble(c.getColumnIndex(KEY_AMOUNT));
            c.close();
            return total;
        }
        else {
            c.close();
            return 0;
        }
    }

}
