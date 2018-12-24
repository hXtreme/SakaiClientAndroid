package com.sakaimobile.development.sakaiclient20;

import android.util.Log;

import com.sakaimobile.development.sakaiclient20.networking.services.ResourcesService;
import com.sakaimobile.development.sakaiclient20.persistence.entities.Resource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ResourceTest {

    public static void test(ResourcesService service) {
        service
                .getSiteResources("cbc83f22-e436-4e54-b88a-14e6e4dd621b")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    List<Resource> resources = response.getResources();

                    Node root = new Node(resources.get(0));
                    root.children = getChildren(resources, 1, resources.size());

                    int i = 0;

                });
    }

    private static List<Node> getChildren(List<Resource> resources, int start, int end) {

        List<Node> children = new ArrayList<>();
        for (int i = start; i < end; i++) {

            Resource resource = resources.get(i);
            if (resource.isDirectory) {

                Node dirNode = new Node(resource);
                dirNode.children = getChildren(resources, i + 1, i + 1 + resource.numDescendants);

                children.add(dirNode);

                // move forward index
                i += resource.numDescendants;
            } else {
                children.add(new Node(resource));
            }

        }

        return children;
    }
}
