package com.example.development.sakaiclientandroid.api_models.all_sites;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AllSites implements Serializable
{

    @SerializedName("entityPrefix")
    @Expose
    private String entityPrefix;
    @SerializedName("site_collection")
    @Expose
    private List<SiteCollection> siteCollection = new ArrayList<SiteCollection>();
    private final static long serialVersionUID = 6697315883558393879L;

    public String getEntityPrefix() {
        return entityPrefix;
    }

    public void setEntityPrefix(String entityPrefix) {
        this.entityPrefix = entityPrefix;
    }

    public List<SiteCollection> getSiteCollection() {
        return siteCollection;
    }

    public void setSiteCollection(List<SiteCollection> siteCollection) {
        this.siteCollection = siteCollection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("entityPrefix", entityPrefix).append("siteCollection", siteCollection).toString();
    }

}