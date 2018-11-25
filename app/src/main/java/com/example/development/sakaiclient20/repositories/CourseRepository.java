package com.example.development.sakaiclient20.repositories;

import android.os.AsyncTask;

import com.example.development.sakaiclient20.models.Term;
import com.example.development.sakaiclient20.models.sakai.courses.CoursesResponse;
import com.example.development.sakaiclient20.networking.services.CoursesService;
import com.example.development.sakaiclient20.persistence.access.AssignmentDao;
import com.example.development.sakaiclient20.persistence.access.AttachmentDao;
import com.example.development.sakaiclient20.persistence.access.CourseDao;
import com.example.development.sakaiclient20.persistence.access.SitePageDao;
import com.example.development.sakaiclient20.persistence.composites.AssignmentWithAttachments;
import com.example.development.sakaiclient20.persistence.composites.CourseWithAllData;
import com.example.development.sakaiclient20.persistence.entities.Assignment;
import com.example.development.sakaiclient20.persistence.entities.Attachment;
import com.example.development.sakaiclient20.persistence.entities.Course;
import com.example.development.sakaiclient20.persistence.entities.SitePage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.TreeMap;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CourseRepository {

    private CourseDao courseDao;
    private SitePageDao sitePageDao;
    private CoursesService coursesService;

    public CourseRepository(
            CourseDao courseDao,
            SitePageDao sitePageDao,
            CoursesService coursesService
    ) {
        this.courseDao = courseDao;
        this.sitePageDao = sitePageDao;
        this.coursesService = coursesService;
    }

    public Single<Course> getCourse(String siteId) {
        return courseDao.getCourse(siteId)
                .map(this::flattenCompositeToEntity);
    }

    public Single<List<List<Course>>> getCoursesSortedByTerm() {
        return courseDao.getAllCourses()
                .toObservable()
                .flatMapIterable(courses -> courses)
                .map(this::flattenCompositeToEntity)
                .toList()
                .map(this::sortCoursesByTerm);
    }

    public Completable refreshAllCourses() {
        return coursesService.getAllSites()
                .map(CoursesResponse::getCourses)
                .map(this::persistCourses)
                .toCompletable();
    }

    private List<Course> persistCourses(List<Course> courses) {
        InsertCoursesTask task = new InsertCoursesTask(courseDao, sitePageDao);
        task.execute(courses.toArray(new Course[courses.size()]));
        return courses;
    }

    private Course flattenCompositeToEntity(CourseWithAllData courseWithAllData) {
        Course entity = courseWithAllData.course;
        entity.sitePages = courseWithAllData.sitePages;
        entity.grades = courseWithAllData.grades;
        entity.assignments =
            AssignmentRepository.flattenCompositesToEntities(courseWithAllData.assignments);
        return entity;
    }

    private List<List<Course>> sortCoursesByTerm(List<Course> courses) {
        TreeMap<Term, List<Course>> coursesByTerm = new TreeMap<>();

        for(Course course : courses) {
            Term term = course.term;
            if(coursesByTerm.containsKey(term)) {
                coursesByTerm.get(term).add(course);
            } else {
                List<Course> coursesForTerm = new ArrayList<>();
                coursesForTerm.add(course);
                coursesByTerm.put(term, coursesForTerm);
            }
        }

        List<List<Course>> coursesSortedByTerm = new ArrayList<>(coursesByTerm.size());
        // We need to go through the keySet in descending order since the
        // more recent terms have larger values (year + starting month)
        for(Term term : coursesByTerm.descendingKeySet()) {
            coursesSortedByTerm.add(coursesByTerm.get(term));
        }

        return coursesSortedByTerm;
    }

    private static class InsertCoursesTask extends AsyncTask<Course, Void, Void> {

        private WeakReference<CourseDao> courseDao;
        private WeakReference<SitePageDao> sitePageDao;

        private InsertCoursesTask(CourseDao courseDao, SitePageDao sitePageDao) {
            this.courseDao = new WeakReference<>(courseDao);
            this.sitePageDao = new WeakReference<>(sitePageDao);
        }

        @Override
        protected Void doInBackground(Course... courses) {
            if(courseDao == null || courseDao.get() == null)
                return null;

            courseDao.get().insert(courses);
            for(Course course : courses)
                sitePageDao.get().insert(course.sitePages);

            return null;
        }
    }
}
