package sk.bak.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import sk.bak.fragmenty.HomeFragmentKurzy;
import sk.bak.fragmenty.HomeFragmentRychlyPrehlad;

/**
 *
 * Trieda adapteru pre page switcher z home fragmnetu
 *
 */
public class HomeFragmentViewPagerAdapter extends FragmentStateAdapter {


    public HomeFragmentViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment selectedFragment = null;

        switch (position) {
            case 0:
                selectedFragment = new HomeFragmentRychlyPrehlad();
                break;
            case 1:
                selectedFragment = new HomeFragmentKurzy();
                break;
        }

        return selectedFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
