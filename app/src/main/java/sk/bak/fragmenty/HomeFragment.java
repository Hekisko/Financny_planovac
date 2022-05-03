package sk.bak.fragmenty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import sk.bak.R;
import sk.bak.adapters.HomeFragmentViewPagerAdapter;

public class HomeFragment extends Fragment {

    private View currentView;

    private ViewPager2 viewPager2;
    private FragmentStateAdapter viewPagerAdapter;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.home_fragment, container, false);

        tabLayout = currentView.findViewById(R.id.home_fragment_tab_layout);
        viewPager2 = currentView.findViewById(R.id.home_fragment_view_pager);
        viewPagerAdapter = new HomeFragmentViewPagerAdapter(getActivity());

        viewPager2.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Rýchly prehľad hlavného účtu");
                        break;
                    case 1:
                        tab.setText("Aktuálne kurzy");
                        break;
                }
            }
        }).attach();






        return currentView;
    }
}
