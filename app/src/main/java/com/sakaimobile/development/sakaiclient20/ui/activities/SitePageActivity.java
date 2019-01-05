package com.sakaimobile.development.sakaiclient20.ui.activities;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sakaimobile.development.sakaiclient20.R;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Course;
import com.sakaimobile.development.sakaiclient20.persistence.entities.SitePage;
import com.sakaimobile.development.sakaiclient20.ui.fragments.AnnouncementsFragment;
import com.sakaimobile.development.sakaiclient20.ui.fragments.SiteGradesFragment;
import com.sakaimobile.development.sakaiclient20.ui.fragments.SiteResourcesFragment;
import com.sakaimobile.development.sakaiclient20.ui.fragments.WebFragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class SitePageActivity extends AppCompatActivity {


    protected Set<LiveData> beingObserved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_page);

        beingObserved = new HashSet<>();

        Intent i = getIntent();
        String siteType = i.getStringExtra(getString(R.string.site_type_tag));
        Course course = (Course) i.getSerializableExtra(getString(R.string.course_tag));

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> this.onBackPressed());


        // set the toolbar title as siteType + coursename
        getSupportActionBar().setTitle(String.format("%s: %s", siteType, course.title));


        // load the appropriate fragment for the site type
        if (siteType.equals(getString(R.string.gradebook))) {
            startSiteGradesFragment(course);
        } else if (siteType.equals(getString(R.string.announcements_site))) {
            startSiteAnnouncementsFragment(course);
        } else if (siteType.equals(getString(R.string.resources_site))) {
            startSiteResourcesFragment(course);
        } else {
            startWebViewFragment(siteType, course);
        }
    }


    private void startSiteGradesFragment(Course course) {
        Bundle bun = new Bundle();
        bun.putString(getString(R.string.siteid_tag), course.siteId);

        SiteGradesFragment fragment = new SiteGradesFragment();
        fragment.setArguments(bun);

        addFragment(fragment);
    }

    private void startSiteAnnouncementsFragment(Course course) {

        HashMap<String, Course> siteIdToCourse = new HashMap<>();
        siteIdToCourse.put(course.siteId, course);

        Bundle bun = new Bundle();
        bun.putString(getString(R.string.siteid_tag), course.siteId);
        bun.putSerializable(getString(R.string.siteid_to_course_map), siteIdToCourse);

        AnnouncementsFragment fragment = new AnnouncementsFragment();
        fragment.setArguments(bun);

        addFragment(fragment);
    }

    private void startSiteResourcesFragment(Course course) {
        Bundle bun = new Bundle();
        bun.putString(getString(R.string.siteid_tag), course.siteId);

        SiteResourcesFragment fragment = new SiteResourcesFragment();
        fragment.setArguments(bun);

        addFragment(fragment);
    }

    private void startWebViewFragment(String siteName, Course course) {

        String url = null;
        for (SitePage page : course.sitePages) {
            if (page.title.equals(siteName))
                url = page.url;
        }

        // url will never be null because we only displayed
        // site pages with valid urls, so the user can never click
        // on a site page with an invalid url
        WebFragment fragment = WebFragment.newInstance(url);
        addFragment(fragment);

    }


    private void addFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeObservations();
    }

    protected void removeObservations() {
        for (LiveData liveData : beingObserved) {
            liveData.removeObservers(this);
        }
        beingObserved.clear();
    }


}