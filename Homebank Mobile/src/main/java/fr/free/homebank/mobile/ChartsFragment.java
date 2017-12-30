package fr.free.homebank.mobile;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;

import fr.free.homebank.mobile.util.DbAdapter;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 2/5/14.
 */
public class ChartsFragment extends Fragment {
    private static int[] COLORS = new int[] { Color.rgb(72, 118, 176),
            Color.rgb(180, 198, 230),
            Color.rgb(227, 126, 35),
            Color.rgb(238, 186, 123),
            Color.rgb(97, 158, 58),
            Color.rgb(175, 222, 142) };
    private static int PIES = 5;
    private static int CAT_EXP_VIEW_ID = 1234;

    private DbAdapter dbHelper;

    private GraphicalView chart;
    private CategorySeries series;
    private DefaultRenderer renderer;
    private String RENDERER = "renderer";
    private String SERIES = "series";


    public static ChartsFragment newInstance() {
        ChartsFragment fragment = new ChartsFragment();
        return fragment;
    }

    public ChartsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(Util.CHARTS_FRAGMENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
//            Log.i("RESTORE", "savedInstanceState NOT null");
            series = (CategorySeries) savedInstanceState.getSerializable(SERIES);
            renderer = (DefaultRenderer) savedInstanceState.getSerializable(RENDERER);
        }
//        if (renderer == null)
//            Log.i("RESTORE", "renderer null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.i("RESTORE", "onResume");
        if(!((NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer)).isDrawerOpen()) {
//            Log.i("RESTORE", "drawerClosed");
            LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
            if (chart == null) {
//                Log.i("RESTORE", "chart null");
                if (renderer != null) {
//                    Log.i("RESTORE", "renderer");
                    chart = ChartFactory.getPieChartView(getActivity(), series, renderer);
                    chart.setId(CAT_EXP_VIEW_ID);
                    layout.findViewById(R.id.chartsProgressBar).setVisibility(View.GONE);
                    layout.addView(chart);
                }
                else {
                    initChart();
                    //addData();
                    new CalculateChartValues().execute();
    //                chart = ChartFactory.getPieChartView(getActivity(), series, renderer);
    //                chart.setId(CAT_EXP_VIEW_ID);
    //                layout.addView(chart);
                }
            } else {
                chart.repaint();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RENDERER, renderer);
        outState.putSerializable(SERIES, series);
    }

    private void initChart() {
        series = new CategorySeries("CategoryExpenses");

        renderer = new DefaultRenderer();
        renderer.setApplyBackgroundColor(false);
        renderer.setChartTitle(getString(R.string.expenses_by_category));
        renderer.setChartTitleTextSize(50);
        renderer.setLabelsTextSize(30);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setDisplayValues(true);
        renderer.setPanEnabled(false);
        renderer.setZoomButtonsVisible(false);
        renderer.setStartAngle(90);
    }

    public void refresh() {
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
        layout.findViewById(R.id.chartsProgressBar).setVisibility(View.VISIBLE);

        layout.removeView(layout.findViewById(CAT_EXP_VIEW_ID));
        initChart();
        //addData();
        new CalculateChartValues().execute();
    }

    private class CalculateChartValues extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            dbHelper.open();
            //ArrayList<ContentValues> categories = dbHelper.getCategoryExpensesAsc(PIES);
            //double total = dbHelper.getTotalExpenses();
            //Log.i("EXPENSES", "total = " + String.valueOf(total));
            ArrayList<ContentValues> categories = dbHelper.getExpensesByRootCategory();
            dbHelper.close();

            Collections.sort(categories, new Util.CategoryExpenseCVComparable());

            double first = 0;

            SimpleSeriesRenderer rdr;

            // NUMBER OF CATEGORIES COULD BE LESS THAN PIES !!!

            BigDecimal bd;

            for (int i = 0; i < PIES; i++) {
                bd = new BigDecimal(categories.get(i).getAsDouble(DbAdapter.KEY_AMOUNT));
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                series.add(categories.get(i).getAsString(DbAdapter.KEY_NAME), bd.doubleValue());
                first += bd.doubleValue();
                rdr = new SimpleSeriesRenderer();
                rdr.setColor(COLORS[i]);
                renderer.addSeriesRenderer(rdr);
            }

            //Log.i("EXPENSES", "first = " + String.valueOf(first));
            //Log.i("EXPENSES", "categories = " + categories.toString());

            double total = 0;
            for (ContentValues cv : categories)
                if (cv.getAsDouble(DbAdapter.KEY_AMOUNT) < 0)
                    total += cv.getAsDouble(DbAdapter.KEY_AMOUNT);

            bd = new BigDecimal(total - first);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            //Log.i("EXPENSES", "total = " +String.valueOf(total));
            //Log.i("EXPENSES", "others = " + String.valueOf(bd.doubleValue()));

            series.add(getString(R.string.others), bd.doubleValue());
            rdr = new SimpleSeriesRenderer();
            rdr.setColor(COLORS[PIES]);
            renderer.addSeriesRenderer(rdr);

            return true;
        }

        protected void onPostExecute(Boolean result) {
            LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
            chart = ChartFactory.getPieChartView(getActivity(), series, renderer);
            chart.setId(CAT_EXP_VIEW_ID);
            layout.findViewById(R.id.chartsProgressBar).setVisibility(View.GONE);
            layout.addView(chart);
        }
    }
}
