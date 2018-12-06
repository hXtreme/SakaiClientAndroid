package com.example.development.sakaiclient20.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.development.sakaiclient20.persistence.access.AnnouncementDao;
import com.example.development.sakaiclient20.persistence.access.AssignmentDao;
import com.example.development.sakaiclient20.persistence.access.AttachmentDao;
import com.example.development.sakaiclient20.persistence.access.CourseDao;
import com.example.development.sakaiclient20.persistence.access.GradeDao;
import com.example.development.sakaiclient20.persistence.access.SitePageDao;
import com.example.development.sakaiclient20.persistence.entities.Announcement;
import com.example.development.sakaiclient20.persistence.entities.Assignment;
import com.example.development.sakaiclient20.persistence.entities.Attachment;
import com.example.development.sakaiclient20.persistence.entities.Course;
import com.example.development.sakaiclient20.persistence.entities.Grade;
import com.example.development.sakaiclient20.persistence.entities.SitePage;
import com.example.development.sakaiclient20.persistence.typeconverters.DateConverter;
import com.example.development.sakaiclient20.persistence.typeconverters.TermConverter;

/**
 * Created by Development on 8/5/18.
 */

@Database(entities = {
        Course.class,
        SitePage.class,
        Grade.class,
        Assignment.class,
        Attachment.class,
        Announcement.class
}, version = 3)
@TypeConverters({DateConverter.class, TermConverter.class})
public abstract class SakaiDatabase extends RoomDatabase {

    private static final Object lock = new Object();
    private static volatile SakaiDatabase mInstance;
    private static final String DB_NAME = "Sakai.db";

    public abstract CourseDao getCourseDao();
    public abstract SitePageDao getSitePageDao();
    public abstract AssignmentDao getAssignmentDao();
    public abstract AttachmentDao getAttachmentDao();
    public abstract GradeDao getGradeDao();
    public abstract AnnouncementDao getAnnouncementDao();


    public static SakaiDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (lock) {
                if (mInstance == null) {
                    mInstance = Room
                            .databaseBuilder(context, SakaiDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return mInstance;
    }
}
