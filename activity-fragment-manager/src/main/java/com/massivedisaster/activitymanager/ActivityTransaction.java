/*
 * ActivityFragmentManager - A library to help android developer working easily with activities and fragments.
 *
 * Copyright (c) 2017 ActivityFragmentManager
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.massivedisaster.activitymanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.View;

import com.massivedisaster.activitymanager.activity.AbstractFragmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * ActivityTransaction
 * The Transaction to open a new activity and inject a specified fragment.
 */
public class ActivityTransaction implements ITransaction<ActivityTransaction> {

    public static final String ACTIVITY_MANAGER_FRAGMENT = "activity_manager_fragment";
    public static final String ACTIVITY_MANAGER_FRAGMENT_TAG = "activity_manager_fragment_tag";
    public static final String ACTIVITY_MANAGER_FRAGMENT_SHARED_ELEMENTS = "activity_manager_fragment_shared_elements";
    private final Class<? extends AbstractFragmentActivity> mAbstractBaseActivity;
    private final Intent mIntent;
    private ActivityOptionsCompat mActivityOptions;
    private List<Pair<View, String>> mSharedElements;
    private Activity mActivityBase;
    private Fragment mFragment;
    private Context mContext;
    private Integer mRequestCode;

    /**
     * ActivityTransaction constructor, created to be used by an activity.
     *
     * @param activity                  The activity to be used to start the new activity.
     * @param abstractBaseActivityClass The AbstractFragmentActivity class.
     * @param fragmentClass             The Fragment to be injected in the activityClass.
     */
    ActivityTransaction(Activity activity, Class<? extends AbstractFragmentActivity> abstractBaseActivityClass,
                        Class<? extends Fragment> fragmentClass) {
        mActivityBase = activity;
        mAbstractBaseActivity = abstractBaseActivityClass;

        mIntent = new Intent(mActivityBase, mAbstractBaseActivity);
        mIntent.putExtra(ACTIVITY_MANAGER_FRAGMENT, fragmentClass.getCanonicalName());
    }

    /**
     * ActivityTransaction constructor, created to be used by a fragment.
     *
     * @param fragment                  The fragment to be used to start the new activity.
     * @param abstractBaseActivityClass The AbstractFragmentActivity class.
     * @param fragmentClass             The Fragment to be injected in the activityClass.
     */
    ActivityTransaction(Fragment fragment, Class<? extends AbstractFragmentActivity> abstractBaseActivityClass,
                        Class<? extends Fragment> fragmentClass) {
        mFragment = fragment;
        mAbstractBaseActivity = abstractBaseActivityClass;

        mIntent = new Intent(mFragment.getContext(), mAbstractBaseActivity);
        mIntent.putExtra(ACTIVITY_MANAGER_FRAGMENT, fragmentClass.getCanonicalName());
    }

    /**
     * ActivityTransaction constructor, created to be used by a fragment.
     *
     * @param context                   The context to be used to start the new activity.
     * @param abstractBaseActivityClass The AbstractFragmentActivity class.
     * @param fragmentClass             The Fragment to be injected in the activityClass.
     */
    ActivityTransaction(Context context, Class<? extends AbstractFragmentActivity> abstractBaseActivityClass,
                        Class<? extends Fragment> fragmentClass) {
        mContext = context;
        mAbstractBaseActivity = abstractBaseActivityClass;

        mIntent = new Intent(mContext, mAbstractBaseActivity);
        mIntent.putExtra(ACTIVITY_MANAGER_FRAGMENT, fragmentClass.getCanonicalName());
    }


    @Override
    public ActivityTransaction setTag(String tag) {
        mIntent.putExtra(ACTIVITY_MANAGER_FRAGMENT_TAG, tag);
        return this;
    }

    @Override
    public ActivityTransaction setBundle(Bundle bundle) {
        mIntent.putExtras(bundle);
        return this;
    }

    @Override
    public ActivityTransaction addSharedElement(View view, String transactionName) {
        mIntent.putExtra(ACTIVITY_MANAGER_FRAGMENT_SHARED_ELEMENTS, true);

        if (mSharedElements == null) {
            mSharedElements = new ArrayList<>();
        }

        mSharedElements.add(new Pair<>(view, transactionName));

        // Create the new activity options
        Pair[] sharedElements = mSharedElements.toArray(new Pair[mSharedElements.size()]);

        mActivityOptions = ActivityOptionsCompat.
                makeSceneTransitionAnimation(mActivityBase == null ? mFragment.getActivity() : mActivityBase, sharedElements);

        return this;
    }

    /**
     * Set the Bundle to be passed to the new Fragment.
     *
     * @param requestCode The code to be used in the {@link Activity#startActivityForResult}.
     * @return Return the Transaction instance.
     */
    public ActivityTransaction setRequestCode(int requestCode) {
        mRequestCode = requestCode;
        return this;
    }

    /**
     * Retrieve the activity options.
     *
     * @return The activity options instance.
     */
    public ActivityOptionsCompat getActivityOptions() {
        return mActivityOptions;
    }

    /**
     * Get the intent created with the configurations to used.
     *
     * @return The Intent to the transaction.
     */
    public Intent getIntent() {
        return mIntent;
    }

    @Override
    public void commit() {
        Intent intent = getIntent();

        Bundle bundleOptions = mActivityOptions != null ? mActivityOptions.toBundle() : null;

        if (mRequestCode == null) {
            if (mActivityBase != null) {
                ContextCompat.startActivity(mActivityBase, intent, bundleOptions);
            } else if (mFragment != null) {
                mFragment.startActivity(intent, bundleOptions);
            } else {
                mContext.startActivity(intent);
            }
        } else {
            if (mActivityBase != null) {
                ActivityCompat.startActivityForResult(mActivityBase, intent, mRequestCode, bundleOptions);
            } else if (mFragment != null) {
                mFragment.startActivityForResult(intent, mRequestCode, bundleOptions);
            } else {
                mContext.startActivity(intent);
            }
        }
    }
}
