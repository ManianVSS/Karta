package org.mvss.karta.server.utils;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
@JsonIgnoreProperties("root")
public class TreeNode<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T value;

    @JsonIgnore
    private transient TreeNode<T> parent;

    private ArrayList<TreeNode<T>> children;

    public void normalize() {
        if (children != null) {
            for (TreeNode<T> child : children) {
                child.parent = this;
                child.normalize();
            }
        }
    }

    public void setChildren(ArrayList<TreeNode<T>> children) {
        this.children = children;
        normalize();
    }

    public void addChild(TreeNode<T> child) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(child);
        child.parent = this;
    }

    public TreeNode<T> createChild(T childValue) {
        TreeNode<T> child = new TreeNode<>();
        child.setValue(childValue);
        addChild(child);
        return child;
    }

    public void cloneAsChild(TreeNode<T> parentTree) {
        parentTree.addChild(this.toBuilder().build());
    }

    public TreeNode<T> getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }
}
