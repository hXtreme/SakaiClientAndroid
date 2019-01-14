package com.sakaimobile.development.sakaiclient20.ui.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sakaimobile.development.sakaiclient20.R;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Announcement;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Course;
import com.sakaimobile.development.sakaiclient20.ui.fragments.AnnouncementsFragment;
import com.sakaimobile.development.sakaiclient20.ui.helpers.RutgersSubjectCodes;
import com.sakaimobile.development.sakaiclient20.ui.listeners.OnAnnouncementSelected;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by atharva on 7/8/18
 */
public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementItemViewHolder> {


    private int announcementType;

    // list of announcements to display
    private List<Announcement> announcements;
    // mapping siteIdToCourse, needed to get subject code and course title
    private Map<String, Course> siteIdToCourse;
    // click listener for each announcement card
    private OnAnnouncementSelected announcementclickListener;

    private LinearLayoutManager manager;

    public AnnouncementsAdapter(List<Announcement> announcements,
                                Map<String, Course> siteIdToCourse,
                                RecyclerView recyclerView,
                                int type,
                                View scrollToTopFab) {

        this.announcements = announcements;
        this.announcementType = type;
        this.siteIdToCourse = siteIdToCourse;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // if the first item is visible then make the FAB disappear
                if(manager.findFirstCompletelyVisibleItemPosition() == 0)
                    scrollToTopFab.setVisibility(View.GONE);
                // otherwise make the FAB reappear
                else
                    scrollToTopFab.setVisibility(View.VISIBLE);
            }
        });
    }

    public int getCurScrollPos() {
        return manager.findFirstCompletelyVisibleItemPosition();
    }


    /**
     * View holder for each announcement card
     */
    class AnnouncementItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView courseIcon;
        TextView authorTxt;
        TextView date;
        TextView cardHeading2;
        TextView cardHeading3;


        AnnouncementItemViewHolder(View cardView) {
            //give this card view to the reycler view's view holder
            super(cardView);

            //save all of the views we will need to change
            authorTxt = cardView.findViewById(R.id.author_name);
            courseIcon = cardView.findViewById(R.id.course_icon);
            cardHeading3 = cardView.findViewById(R.id.title_txt);
            date = cardView.findViewById(R.id.date_text);
            cardHeading2 = cardView.findViewById(R.id.course_name);

            cardView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Announcement announcementToExpand = announcements.get(pos);
            announcementclickListener.onAnnouncementSelected(announcementToExpand, siteIdToCourse);
        }
    }


    @Override
    public AnnouncementItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_announcements, parent, false);
        return new AnnouncementItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(AnnouncementItemViewHolder holder, int position) {

        //set the data inside the card

        Announcement currAnnouncement = announcements.get(position);

        AnnouncementItemViewHolder announcementHolder = (AnnouncementItemViewHolder) holder;

        announcementHolder.authorTxt.setText(currAnnouncement.createdBy);

        int subjCode = siteIdToCourse.get(currAnnouncement.siteId).subjectCode;
        announcementHolder.courseIcon.setText(RutgersSubjectCodes.mapCourseCodeToIcon.get(subjCode));

        announcementHolder.date.setText(currAnnouncement.getShortFormattedDate());

        //check to see the announcement type
        if (announcementType == AnnouncementsFragment.ALL_ANNOUNCEMENTS) {

            // if all announcements, show course title, then announcement title
            announcementHolder.cardHeading2.setText(siteIdToCourse.get(currAnnouncement.siteId).title);
            announcementHolder.cardHeading3.setText(currAnnouncement.title);

        } else if (announcementType == AnnouncementsFragment.SITE_ANNOUNCEMENTS) {

            //if site announcements, show announcement title, then announcement body
            announcementHolder.cardHeading2.setText(currAnnouncement.title);

            try {
                //if its after android N, use this method for setting the html
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    announcementHolder.cardHeading3.setText(Html.fromHtml(currAnnouncement.body, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    announcementHolder.cardHeading3.setText(Html.fromHtml(currAnnouncement.body));
                }
            } catch (RuntimeException e) {
//                    java.lang.RuntimeException: PARAGRAPH span must start at paragraph boundary (832 follows  )
                announcementHolder.cardHeading3.setText("");
                e.printStackTrace();
            }

        }


    }


    @Override
    public int getItemCount() {
        return announcements == null ? 0 : announcements.size();
    }


    public void setClickListener(OnAnnouncementSelected announcementclickListener) {
        this.announcementclickListener = announcementclickListener;
    }

}
