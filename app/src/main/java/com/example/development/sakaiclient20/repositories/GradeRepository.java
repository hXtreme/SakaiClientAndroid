package com.example.development.sakaiclient20.repositories;

import android.os.AsyncTask;

import com.example.development.sakaiclient20.models.sakai.gradebook.GradesResponse;
import com.example.development.sakaiclient20.models.sakai.gradebook.SiteGrades;
import com.example.development.sakaiclient20.networking.services.GradeService;
import com.example.development.sakaiclient20.persistence.access.GradeDao;
import com.example.development.sakaiclient20.persistence.entities.Grade;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class GradeRepository {

    private GradeDao gradeDao;
    private GradeService gradeService;

    public GradeRepository(GradeDao gradeDao, GradeService service) {
        this.gradeDao = gradeDao;
        this.gradeService = service;
    }

    public Single<List<Grade>> getGradesForSite(String siteId) {
        return gradeDao
                .getGradesForSite(siteId)
                .firstOrError();
    }

    public Completable refreshSiteGrades(String siteId) {
        return this.gradeService
                .getGradeForSite(siteId)
                .map(SiteGrades::getGradesList)
                .map(this::persistGrades)
                .toObservable()
                .ignoreElements();
    }

    public Completable refreshAllGrades() {
        // get the list of site grades
        // for each sitegrades obj, get its list of grades
        // persist each list of grades
        // collect the results, then mark as complete
        return this.gradeService
                .getAllGrades()
                .map(GradesResponse::getSiteGrades)
                .toObservable()
                .flatMapIterable(siteGrades -> siteGrades)
                .map(SiteGrades::getGradesList)
                .map(this::persistGrades)
                .ignoreElements();
    }

    private List<Grade> persistGrades(List<Grade> grades) {

        if(grades.size() == 0)
            return grades;

        // all of the grades in the given list are of the same course (Same siteId)
        String siteId = grades.get(0).siteId;
        // insert the grades
        gradeDao.insertGradesForSite(siteId, (Grade[]) grades.toArray());

        return grades;
    }

    private static class InsertGradesTask extends AsyncTask<Grade, Void, Void> {

        private WeakReference<GradeDao> gradeDao;
        private String siteId;

        InsertGradesTask(GradeDao dao, String siteId) {
            this.gradeDao = new WeakReference<>(dao);
            this.siteId = siteId;
        }

        @Override
        protected Void doInBackground(Grade... grades) {

            if (gradeDao == null || gradeDao.get() == null)
                return null;

            gradeDao.get().insertGradesForSite(siteId, grades);
            return null;
        }
    }

}