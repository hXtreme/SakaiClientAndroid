package com.sakaimobile.development.sakaiclient20.ui.fragments.assignments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.sakaimobile.development.sakaiclient20.R;
import com.sakaimobile.development.sakaiclient20.networking.utilities.SharedPrefsUtil;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Assignment;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Course;
import com.sakaimobile.development.sakaiclient20.ui.helpers.AssignmentSortingUtils;
import com.sakaimobile.development.sakaiclient20.ui.helpers.RutgersSubjectCodes;
import com.sakaimobile.development.sakaiclient20.ui.listeners.TreeViewItemClickListener;
import com.sakaimobile.development.sakaiclient20.ui.viewholders.AssignmentCourseViewHolder;
import com.sakaimobile.development.sakaiclient20.ui.viewholders.AssignmentTermHeaderViewHolder;
import com.sakaimobile.development.sakaiclient20.ui.viewholders.TermHeaderViewHolder;
import com.sakaimobile.development.sakaiclient20.ui.viewmodels.AssignmentViewModel;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;


/**
 * Created by Shoumyo Chakravorti.
 *
 * The main tab ({@link android.support.v4.app.Fragment}) that shows the user's
 * assignments. Assignments are always found within their respective term. If assignments
 * are sorted by date, they are not under any particular course header, but if
 * assignments are sorted by courses then they show up under their respective
 * courses (which, in turn, are visible under their respective terms).
 */

public class AssignmentsFragment extends Fragment {

    /**
     * The {@link AndroidTreeView} that is represented by this
     *  {@link android.support.v4.app.Fragment}.
     */
    private AndroidTreeView treeView;

    /**
     * The parent layout for the assignments TreeView.
     */
    private FrameLayout treeContainer;
    private ProgressBar progressBar;

    /**
     * If the {@link android.support.v4.app.Fragment} is specified to show assignments
     * sorted by their courses, this is the non-null list of courses sorted by their terms.
     */
    private List<List<Course>> courses;

    /**
     * If the {@link android.support.v4.app.Fragment} is specified to show assignments
     * sorted by date, this is the list of assignments sorted by date within their repsective
     * terms.
     */
    private List<List<Assignment>> assignments;

    @Inject
    AssignmentViewModel assignmentViewModel;

    /**
     * Whether the assignments should be shown as being sorted by course. {@code False} if
     * assignments should be sorted by term instead.
     */
    private boolean sortedByCourses = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This fragment provides the option to sort assignments by course or date.
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_assignments, container, false);

        // Set up refresh layout to make a new network request and re-instantiate the
        // assignments fragment
        this.treeContainer = view.findViewById(R.id.assignments_container);
        this.progressBar = view.findViewById(R.id.assignments_progressbar);
        this.progressBar.setIndeterminate(true);

        // View to ultimately be added to the screen
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.assignmentViewModel
            .getCoursesByTerm(true)
            .observe(this, courses -> {
                this.courses = courses;
                if(this.sortedByCourses)
                    AssignmentSortingUtils.sortCourseAssignments(courses);
                else
                    this.assignments = AssignmentSortingUtils.sortAssignmentsByTerm(courses);

                // Construct the tree view based on th
                // Make the TreeView visible inside the parent layout
                if(this.treeView == null) {
                    this.treeView = new AndroidTreeView(getContext());
                    this.treeView.setDefaultAnimation(true);
                }

                this.renderTree();
                this.progressBar.setVisibility(View.GONE);
            }
        );
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    /**
     * On top of performing regular functions of {@link Fragment#onDetach()},
     * saves the expanded state of the tree view so that returning to this tab
     * restores the same state.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        this.saveTreeState();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.assignments_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_by_date: {
                // Nothing to do if tree view is already sorted by date
                if(!sortedByCourses) return true;
                break;
            }
            case R.id.action_sort_by_course: {
                if(sortedByCourses) return true;
                break;
            }
            case R.id.action_refresh: {
                this.progressBar.setVisibility(View.VISIBLE);
                this.saveTreeState();
                this.assignmentViewModel.refreshAllData();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

        this.saveTreeState();
        sortedByCourses = !sortedByCourses;
        this.renderTree();
        return true;
    }

    /**
     * Constructs the {@link AndroidTreeView} with a Term -> Course -> Assignment hierarchy
     * if the {@link Fragment} is specified to sort by courses. Since the root node is handled
     * by reference, nothing needs to be returned.
     *
     */
    private TreeNode createTreeFromCourses() {
        TreeNode root =  TreeNode.root();
        Context currContext = getActivity();

        // The courses as returned by the DataHandler are already sorted by term,
        // so we just need to loop through them to create the terms with all
        // courses and their assignments
        for(List<Course> courseList : courses) {
            // If there are no courses in the term, skip it (the Sakai
            // API shouldn't return a term if there are no courses for it,
            // so nothing special needs to be done here)
            if(courseList.size() == 0)
                continue;

            // Get the term name
            String termName = courseList.get(0).term.toString();

            // Create a term header item, and make a tree node using it
            TermHeaderViewHolder.TermHeaderItem termHeaderItem =
                    new TermHeaderViewHolder.TermHeaderItem(termName);
            TreeNode termNode = new TreeNode(termHeaderItem);
            // Set the term header view holder to inflate the appropriate view
            termNode.setViewHolder(new TermHeaderViewHolder(currContext));

            // For each course, create the course node with its respective
            // assignments
            for(Course course : courseList) {
                //SKIP THE COURSE IF IT DOESN'T HAVE ANY ASSIGNMENTS
                if(course.assignments.size() == 0)
                    continue;

                // Create a course header item, and make a tree node using it
                String courseIconCode =
                        RutgersSubjectCodes.mapCourseCodeToIcon.get(course.subjectCode);
                AssignmentCourseViewHolder.CourseHeaderItem courseHeaderItem =
                        new AssignmentCourseViewHolder.CourseHeaderItem(
                                course.title,
                                courseIconCode,
                                course.assignments
                        );

                TreeNode courseNode = new TreeNode(courseHeaderItem);
                // Set the course header view holder to inflate the appropriate view
                courseNode.setViewHolder(new AssignmentCourseViewHolder(currContext));

                // Add the course to the term
                termNode.addChild(courseNode);
            }

            // Add the term to the root node
            if(termNode.getChildren().size() > 0)
                root.addChild(termNode);
        }

        return root;
    }

    /**
     * Constructs the {@link AndroidTreeView} for assignments sorted within their terms
     * by placing assignments under their respective term header. Similar to
     * {@code createTreeFromCourses}, since the root node is handled by reference,
     * nothing needs to be returned.
     */
    private TreeNode createTreeFromAssignments() {
        if(this.assignments == null)
            this.assignments = AssignmentSortingUtils.sortAssignmentsByTerm(this.courses);

        TreeNode root =  TreeNode.root();
        Context currContext = getActivity();

        // The courses as returned by the DataHandler are already sorted by term,
        // so we just need to loop through them to create the terms with all
        // courses and their assignments
        for(List<Assignment> termAssignments : assignments) {
            // If no assignments, ignore this term/semester
            if(termAssignments.size() == 0)
                continue;

            // Get the term name
            String termName = termAssignments.get(0).term.toString();

            // Create a term header item, and make a tree node using it
            AssignmentTermHeaderViewHolder.TermHeaderItem termHeaderItem =
                    new AssignmentTermHeaderViewHolder.TermHeaderItem(termName, termAssignments);
            TreeNode termNode = new TreeNode(termHeaderItem);
            // Set the term header view holder to inflate the appropriate view
            termNode.setViewHolder(new AssignmentTermHeaderViewHolder(currContext));

            // Add the term to the root node
            root.addChild(termNode);
        }

        return root;
    }

    private void renderTree() {
        TreeNode root = this.sortedByCourses
                ? createTreeFromCourses()
                : createTreeFromAssignments();

        this.treeContainer.removeAllViews();
        this.treeView.setRoot(root);

        TreeViewItemClickListener nodeClickListener = new TreeViewItemClickListener(this.treeView, root);
        this.treeView.setDefaultNodeClickListener(nodeClickListener);

        String state = this.getTreeState();
        this.treeView.restoreState(state);

        this.treeContainer.addView(this.treeView.getView());
    }

    private void saveTreeState() {
        SharedPrefsUtil.saveTreeState(
                getContext(),
                this.treeView,
                sortedByCourses
                        ? SharedPrefsUtil.ASSIGNMENTS_BY_COURSES_TREE_TYPE
                        : SharedPrefsUtil.ASSIGNMENTS_BY_TERM_TREE_TYPE
        );
    }

    private String getTreeState() {
        return SharedPrefsUtil.getTreeState(
                getContext(),
                sortedByCourses
                        ? SharedPrefsUtil.ASSIGNMENTS_BY_COURSES_TREE_TYPE
                        : SharedPrefsUtil.ASSIGNMENTS_BY_TERM_TREE_TYPE
        );
    }
}
