package net.donky.geo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.List;

public class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

    private final List<PagerItem> mPagerItems;
    private SparseArray<WeakReference<Fragment>> mRegisteredFragments = new SparseArray<>();

    public TabsFragmentPagerAdapter(FragmentManager fm, List<PagerItem> pagerItems) {
        super(fm);
        mPagerItems = pagerItems;
    }

    @Override
    public Fragment getItem(int position) {
        try {
            PagerItem pagerItem = mPagerItems.get(position);
            Fragment fragment = pagerItem.clazz.newInstance();
            fragment.setArguments(pagerItem.args);
            return fragment;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Fragment getRegisteredFragment(int position) {
        WeakReference<Fragment> fragmentWeakReference = mRegisteredFragments.get(position);
        return fragmentWeakReference == null ? null : fragmentWeakReference.get();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPagerItems.get(position).pageTitle;
    }

    @Override
    public int getCount() {
        return mPagerItems.size();
    }

    public static class PagerItem{
        public final CharSequence pageTitle;
        public final Class<? extends Fragment> clazz;
        public final Bundle args;

        public PagerItem(Class<? extends Fragment> clazz, CharSequence pageTitle) {
            this(clazz, pageTitle, null);
        }

        public PagerItem(Class<? extends Fragment> clazz, CharSequence pageTitle, Bundle args) {
            this.clazz = clazz;
            this.pageTitle = pageTitle;
            this.args = args;
        }
    }
}
