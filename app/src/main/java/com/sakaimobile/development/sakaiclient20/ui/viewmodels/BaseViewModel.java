package com.sakaimobile.development.sakaiclient20.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.sakaimobile.development.sakaiclient20.persistence.entities.Course;
import com.sakaimobile.development.sakaiclient20.repositories.CourseRepository;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

abstract class BaseViewModel extends ViewModel {
    CourseRepository courseRepository;
    private MutableLiveData<List<List<Course>>> coursesByTerm;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    BaseViewModel(CourseRepository repo) {
        this.courseRepository = repo;
    }

    abstract void refreshAllData();
    abstract void refreshSiteData(String siteId);

    public LiveData<List<List<Course>>> getCoursesByTerm(boolean refresh) {
        if(this.coursesByTerm == null) {
            this.coursesByTerm = new MutableLiveData<>();
        }

        if(refresh)
            refreshAllData();
        else
            loadCourses();

        return this.coursesByTerm;
    }

    void loadCourses() {
        this.compositeDisposable.add(
            this.courseRepository.getCoursesSortedByTerm()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    coursesByTerm::setValue,
                    Throwable::printStackTrace
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}