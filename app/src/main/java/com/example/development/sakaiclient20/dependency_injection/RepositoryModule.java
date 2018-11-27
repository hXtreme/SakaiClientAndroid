package com.example.development.sakaiclient20.dependency_injection;

import com.example.development.sakaiclient20.networking.services.CoursesService;
import com.example.development.sakaiclient20.persistence.access.CourseDao;
import com.example.development.sakaiclient20.persistence.access.SitePageDao;
import com.example.development.sakaiclient20.repositories.CourseRepository;

import dagger.Module;
import dagger.Provides;

@Module(includes = { DaoModule.class, ServiceModule.class })
class RepositoryModule {

    @Provides static CourseRepository provideCourseRepository(
            CourseDao courseDao,
            SitePageDao sitePageDao,
            CoursesService coursesService
    ) {
        return new CourseRepository(courseDao, sitePageDao, coursesService);
    }

}
