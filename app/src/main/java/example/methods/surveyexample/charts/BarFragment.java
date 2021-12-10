package example.methods.surveyexample.charts;

import static example.methods.surveyexample.model.CloudDBZoneWrapper.no;
import static example.methods.surveyexample.model.CloudDBZoneWrapper.yes;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import example.methods.surveyexample.R;

public class BarFragment extends Fragment {
    public BarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_bar, container, false);
        BarChart barChart = (BarChart) view.findViewById(R.id.barChart);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);

        ArrayList<BarEntry> visitors = new ArrayList<>();
        visitors.add(new BarEntry(2, Float.parseFloat(String.valueOf(yes))));
        visitors.add(new BarEntry(1, Float.parseFloat(String.valueOf(no))));


        BarDataSet barDataSet = new BarDataSet(visitors, "Visitors");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);

        String[] answer = new String[] {"evet","hayır"};
        XAxis xAxis=barChart.getXAxis();
       // xAxis.setValueFormatter(new MyXAxisValueFormatter(answer));
        //xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);



        return view;
    }
    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;
        public MyXAxisValueFormatter(String[] values) {
            this.mValues=values;
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int)value];
        }
    }


}