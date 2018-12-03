package com.example.development.sakaiclient20.repositories;

import android.os.AsyncTask;

import com.example.development.sakaiclient20.models.sakai.assignments.AssignmentsResponse;
import com.example.development.sakaiclient20.networking.services.AssignmentsService;
import com.example.development.sakaiclient20.persistence.access.AssignmentDao;
import com.example.development.sakaiclient20.persistence.access.AttachmentDao;
import com.example.development.sakaiclient20.persistence.composites.AssignmentWithAttachments;
import com.example.development.sakaiclient20.persistence.entities.Assignment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Development on 8/8/18.
 */

public class AssignmentRepository {

    private AssignmentDao assignmentDao;
    private AttachmentDao attachmentDao;
    private AssignmentsService assignmentsService;

    public AssignmentRepository(
            AssignmentDao assignmentDao,
            AttachmentDao attachmentDao,
            AssignmentsService service
    ) {
        this.assignmentDao = assignmentDao;
        this.attachmentDao = attachmentDao;
        this.assignmentsService = service;
    }

    public Single<List<Assignment>> getSiteAssignments(String siteId) {
        return assignmentDao
                .getSiteAssignments(siteId)
                .firstOrError()
                .map(AssignmentRepository::flattenCompositesToEntities);
    }

    public Single<List<Assignment>> getAllAssignments() {
        return assignmentDao
                .getAllAssignments()
                .firstOrError()
                .map(AssignmentRepository::flattenCompositesToEntities);
    }

    public Completable refreshAllAssignments() {
        return assignmentsService
                .getAllAssignments()
                .map(AssignmentsResponse::getAssignments)
                .map(this::persistAssignments)
                .ignoreElement();
    }

    public Completable refreshSiteAssignments(String siteId) {
        return assignmentsService
                .getSiteAssignments(siteId)
                .map(AssignmentsResponse::getAssignments)
                .map(this::persistAssignments)
                .ignoreElement();
    }

    private List<Assignment> persistAssignments(List<Assignment> assignments) {
        InsertAssignmentsTask task = new InsertAssignmentsTask(assignmentDao, attachmentDao);

        // Using generic varargs can supposedly pollute the heap,
        // so convert to array before passing as task argument
        task.execute(assignments.toArray(new Assignment[assignments.size()]));
        return assignments;
    }

    static List<Assignment> flattenCompositesToEntities(List<AssignmentWithAttachments> assignmentComposites) {
        List<Assignment> assignmentEntities = new ArrayList<>(assignmentComposites.size());

        for(AssignmentWithAttachments composite : assignmentComposites) {
            Assignment assignment = composite.assignment;
            assignment.attachments = composite.attachments;
            assignmentEntities.add(assignment);
        }

        return assignmentEntities;
    }

    private static class InsertAssignmentsTask extends AsyncTask<Assignment, Void, Void> {

        private WeakReference<AssignmentDao> assignmentDao;
        private WeakReference<AttachmentDao> attachmentDao;

        private InsertAssignmentsTask(AssignmentDao assignmentDao, AttachmentDao attachmentDao) {
            this.assignmentDao = new WeakReference<>(assignmentDao);
            this.attachmentDao = new WeakReference<>(attachmentDao);
        }

        @Override
        protected Void doInBackground(Assignment... assignments) {
            if(assignmentDao == null || assignmentDao.get() == null)
                return null;
            if(attachmentDao == null || attachmentDao.get() == null)
                return null;

            assignmentDao.get().insert(assignments);
            for(Assignment assignment : assignments)
                attachmentDao.get().insert(assignment.attachments);

            return null;
        }
    }

}
