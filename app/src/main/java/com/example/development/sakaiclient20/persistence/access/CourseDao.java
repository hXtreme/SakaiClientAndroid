package com.example.development.sakaiclient20.persistence.access;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.example.development.sakaiclient20.persistence.composites.CourseWithAllData;
import com.example.development.sakaiclient20.persistence.entities.Course;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Development on 8/11/18.
 */
@Dao
public abstract class CourseDao implements BaseDao<Course> {

    @Transaction
    @Query("SELECT * FROM courses")
    public abstract Flowable<List<CourseWithAllData>> getAllCourses();

    @Transaction
    @Query("SELECT * FROM courses WHERE siteId = :siteId LIMIT 1")
    public abstract Flowable<CourseWithAllData> getCourse(String siteId);

}