package example.methods.surveyexample.charts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class VPAdapter extends FragmentStateAdapter {
    public VPAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PieFragment();
            case 1:
                return new BarFragment();
            default:
                return new PieFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
