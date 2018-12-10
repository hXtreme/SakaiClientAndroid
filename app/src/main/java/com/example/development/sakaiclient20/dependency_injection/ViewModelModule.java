package com.example.development.sakaiclient20.dependency_injection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.development.sakaiclient20.ui.viewmodels.CourseViewModel;
import com.example.development.sakaiclient20.ui.viewmodels.GradeViewModel;
import com.example.development.sakaiclient20.ui.viewmodels.ViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module(includes = { RepositoryModule.class })
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModel.class)
    abstract ViewModel bindCourseViewModel(CourseViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(GradeViewModel.class)
    abstract ViewModel bindGradeViewModel(GradeViewModel gradeViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

}