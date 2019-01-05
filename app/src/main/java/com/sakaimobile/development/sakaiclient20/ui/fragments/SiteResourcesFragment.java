package com.sakaimobile.development.sakaiclient20.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakaimobile.development.sakaiclient20.R;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Resource;
import com.sakaimobile.development.sakaiclient20.ui.viewholders.ResourceDirectoryViewHolder;
import com.sakaimobile.development.sakaiclient20.ui.viewholders.ResourceItemViewHolder;
import com.sakaimobile.development.sakaiclient20.ui.viewmodels.ResourceViewModel;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class SiteResourcesFragment extends Fragment {

    @Inject
    ResourceViewModel resourceViewModel;

    private String currentSiteId;

    private AndroidTreeView resourcesTreeView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bun = getArguments();
        currentSiteId = bun.getString(getString(R.string.siteid_tag));

    }


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_site_resources, null);

        // get the parent view container
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setRefreshing(true);

        // setup the treeview
        final TreeNode root = TreeNode.root();
        resourcesTreeView = new AndroidTreeView(getActivity(), root);
        resourcesTreeView.setDefaultAnimation(true);
        swipeRefreshLayout.addView(resourcesTreeView.getView());

        // request the resources for the site
        LiveData<List<Resource>> resourceLiveData =
                resourceViewModel.getResourcesForSite(currentSiteId);

//        beingObserved.add(resourceLiveData);

        // observe on the resources data
        resourceLiveData.observe(this, resources -> {

//            setupToolbar(resources);

            // update the resources tree view
            updateResourcesTreeView(root, resources);

            // if this change was detected because of a refresh, just stop refreshing
            swipeRefreshLayout.setRefreshing(false);
        });

        // set refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
                    resourceViewModel.refreshSiteResources(currentSiteId);
                }
        );

        return view;
    }

    /**
     * Updates the resources tree view by removing old nodes and adding new nodes
     * from the resources list
     *
     * @param root          root of the tree (Root is never changed)
     * @param flatResources new list of resources
     */
    private void updateResourcesTreeView(TreeNode root, List<Resource> flatResources) {

        // remove the old children of the root, so we can build a new treeview
        removeChildren(root);

        List<TreeNode> children = getChildren(flatResources, 1, flatResources.size());

        for (TreeNode n : children) {
            resourcesTreeView.addNode(root, n);
        }
    }


    /**
     * Recursively gets the children for a "node" given the list of resources
     *
     * @param resources list, must be in a proper traversal format, otherwise algorithm won't work
     *                  vist, then go to all children, recursively
     * @param start     start of the list to get children of
     * @param end       end of the part of the list representing the descendants
     * @return a list of nodes which are the children of the node at resources[start]
     */
    private List<TreeNode> getChildren(List<Resource> resources, int start, int end) {

        List<TreeNode> children = new ArrayList<>();
        for (int i = start; i < end; i++) {

            Resource resource = resources.get(i);
            if (resource.isDirectory) {

                // build directory node
                TreeNode dirNode = buildResourceDirNode(resource);

                // add the directory as the child of the parent node
                children.add(dirNode);

                // recursively get the children of this directory, and add them
                List<TreeNode> dirChildren = getChildren(resources, i + 1, i + 1 + resource.numDescendants);
                dirNode.addChildren(dirChildren);
                // move forward index
                i += resource.numDescendants;
            } else {

                // add this file node as a child of the parent node
                children.add(buildResourceFileNode(resource));
            }
        }

        return children;
    }


    /**
     * Build a node for a resource file item
     *
     * @param resource item
     * @return treenode to add
     */
    private TreeNode buildResourceFileNode(Resource resource) {
        ResourceItemViewHolder.ResourceFileItem fileItem =
                new ResourceItemViewHolder.ResourceFileItem(resource.title, resource.url);

        TreeNode fileNode = new TreeNode(fileItem).setViewHolder(new ResourceItemViewHolder(getActivity()));

        fileNode.setClickListener((node, value) -> {

            if (value instanceof ResourceItemViewHolder.ResourceFileItem)
                downloadFile((ResourceItemViewHolder.ResourceFileItem) value);
        });


        return fileNode;
    }

    /**
     * Build a node for a resource directory, only using the resource title
     *
     * @param resource item
     * @return treenode to add
     */
    private TreeNode buildResourceDirNode(Resource resource) {
        ResourceDirectoryViewHolder.ResourceDirectoryItem dirItem =
                new ResourceDirectoryViewHolder.ResourceDirectoryItem(resource.title);

        return new TreeNode(dirItem).setViewHolder(new ResourceDirectoryViewHolder(getActivity()));
    }


    /**
     * Downloads a file inside a given resource file item node
     * <p>
     * loads up a web fragment to download the file
     *
     * @param item resource file item node
     */
    private void downloadFile(ResourceItemViewHolder.ResourceFileItem item) {
        WebFragment fragment = WebFragment.newInstance(item.url);

        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    /**
     * This may look weird at first glance, but do not fret
     * originally, I was doing a simple foreach loop and calling node.deleteChild(), but for
     * some reason this does not update the view, from what I found
     * then I tried a simple foreach loop and deleting the node using the reference to the treeview
     * itself, but I started getting concurrent modification exceptions, which makes sense since I was
     * iterating through the list and deleting the list at the same time (with node.getChildren())
     * <p>
     * **IMPORTANT**, the list returned by getChildren() is the same list internally used
     * so changing this list will also change the internal representation
     * <p>
     * After this, I tried a regular for loop and removed the child at index i
     * but since the getChildren() is returning the same list, the list is getting shorter
     * each time I remove a child, so the index i isn't valid
     * <p>
     * To fix this, I would continue until the list of children was empty and keep deleting
     * the first child. this seemed to work
     *
     * @param node node to remove children of
     */
    private void removeChildren(TreeNode node) {

        List<TreeNode> children = node.getChildren();
        while (children.size() > 0) {
            TreeNode child = children.get(0);
            resourcesTreeView.removeNode(child);
        }
    }



}