package com.example.development.sakaiclientandroid.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import com.example.development.sakaiclientandroid.NavActivity;
import com.example.development.sakaiclientandroid.R;
import com.example.development.sakaiclientandroid.utils.DataHandler;
import com.example.development.sakaiclientandroid.utils.custom.GradebookTermsExpListAdapter;
import com.example.development.sakaiclientandroid.utils.requests.RequestCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllGradesFragment extends BaseFragment {

    private List<String> termHeaders;
    private HashMap<String, List<String>> termToCourseTitles;
    private HashMap<String, List<Integer>> termToCourseSubjectCodes;
    private HashMap<String, List<String>> termToCourseIds;
    SwipeRefreshLayout swipeRefreshLayout;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //reset header
        ((NavActivity)getActivity()).setActionBarTitle(getString(R.string.app_name));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_grades, null);

        this.termHeaders = new ArrayList<>();
        this.termToCourseTitles = new HashMap<>();
        this.termToCourseSubjectCodes = new HashMap<>();
        this.termToCourseIds = new HashMap<>();


        final ExpandableListView expandableListView = view.findViewById(R.id.all_grades_listview);
        final ProgressBar spinner = getActivity().findViewById(R.id.nav_activity_progressbar);


        //if we already have grades for all sites cached, then no need to make another request
        if(DataHandler.gradesRequestedForAllSites()) {

            //prepare the headers and children
            DataHandler.prepareHeadersAndChildrenWithGrades(
                    this.termHeaders,
                    this.termToCourseTitles,
                    this.termToCourseSubjectCodes,
                    this.termToCourseIds
            );

            spinner.setVisibility(View.GONE);
            GradebookTermsExpListAdapter adapter = new GradebookTermsExpListAdapter(mContext, termHeaders, termToCourseTitles, termToCourseIds, termToCourseSubjectCodes);
            expandableListView.setAdapter(adapter);
        }
        //if we have not, then must make request
        else {

            //start spinner
            spinner.setVisibility(View.VISIBLE);

            DataHandler.requestAllGrades(new RequestCallback() {

                @Override
                public void onAllGradesSuccess() {

                    //now prepare headers and children
                    DataHandler.prepareHeadersAndChildrenWithGrades(
                            termHeaders,
                            termToCourseTitles,
                            termToCourseSubjectCodes,
                            termToCourseIds
                    );

                    GradebookTermsExpListAdapter adapter = new GradebookTermsExpListAdapter(mContext, termHeaders, termToCourseTitles, termToCourseIds, termToCourseSubjectCodes);

                    spinner.setVisibility(View.GONE);
                    expandableListView.setAdapter(adapter);
                }


                @Override
                public void onAllGradesFailure(Throwable throwable) {

                    //TODO failure message

                }

            });
        }



        this.swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                DataHandler.requestAllGrades(new RequestCallback() {

                    @Override
                    public void onAllGradesSuccess() {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onAllGradesFailure(Throwable throwable) {
                        //TODO error
                    }
                });
            }
        });

        return view;
    }




}
