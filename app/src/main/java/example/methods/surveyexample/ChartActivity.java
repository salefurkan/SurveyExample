package example.methods.surveyexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import example.methods.surveyexample.charts.VPAdapter;
import example.methods.surveyexample.databinding.ActivityChartBinding;

public class ChartActivity extends AppCompatActivity {

    ActivityChartBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private VPAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("TURKEY PULL");
        
        tabLayout = binding.tablayout;
        viewPager = binding.viewpager;

        FragmentManager fm = getSupportFragmentManager();
        adapter=new VPAdapter(fm,getLifecycle());

        viewPager.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("PIE"));
        tabLayout.addTab(tabLayout.newTab().setText("BAR"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }
}