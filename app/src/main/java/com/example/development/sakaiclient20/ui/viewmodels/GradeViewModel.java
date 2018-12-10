package com.example.development.sakaiclient20.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.development.sakaiclient20.persistence.entities.Grade;
import com.example.development.sakaiclient20.repositories.CourseRepository;
import com.example.development.sakaiclient20.repositories.GradeRepository;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GradeViewModel extends BaseViewModel {

    private GradeRepository gradeRepository;
    private HashMap<String, MutableLiveData<List<Grade>>> siteIdToGrades;

    /**
     * Grades view model constructor
     *
     * @param courseRepository course repository dependency needed for superclass
     * @param gradeRepository grades repository dependency needed to refresh and get grades
     */
    @Inject
    GradeViewModel(CourseRepository courseRepository, GradeRepository gradeRepository) {
        super(courseRepository);
        this.gradeRepository = gradeRepository;
        this.siteIdToGrades = new HashMap<>();
    }

    /**
     * Called by UI controller to get grades for a site
     * if hashmap already has the grades, return that, otherwise
     * refresh the grades and put into hashmap
     *
     * @param siteId site to get grades for
     * @return live data containing grades list
     */
    public LiveData<List<Grade>> getGradesForSite(String siteId) {

        if (!this.siteIdToGrades.containsKey(siteId)) {
            this.siteIdToGrades.put(siteId, new MutableLiveData<>());
            refreshSiteData(siteId);
        }
        return this.siteIdToGrades.get(siteId);
    }


    /**
     * Loads grades for a site from the grades repository into
     * a hashmap mapping from the siteId to the grades list
     *
     * @param siteId site to load the grades for
     */
    public void loadSiteGrades(String siteId) {
        this.compositeDisposable.add(
                this.gradeRepository.getGradesForSite(siteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this.siteIdToGrades.get(siteId)::setValue,
                                Throwable::printStackTrace
                        )
        );
    }


    /**
     * Refreshes all grades by telling the grades repository to make
     * a network request and then persist them in the database
     * <p>
     * Then it calls load courses (now that the new grades are in the database)
     */
    @Override
    public void refreshAllData() {
        this.compositeDisposable.add(
                this.gradeRepository.refreshAllGrades()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::loadCourses,
                                Throwable::printStackTrace
                        )
        );
    }

    /**
     * Refreshes the grades for a given site
     * <p>
     * Loads the site grades given that the grades for that site are updated in the database
     *
     * @param siteId
     */
    @Override
    public void refreshSiteData(String siteId) {
        this.compositeDisposable.add(
                this.gradeRepository.refreshSiteGrades(siteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> loadSiteGrades(siteId),
                                Throwable::printStackTrace
                        )
        );
    }


}