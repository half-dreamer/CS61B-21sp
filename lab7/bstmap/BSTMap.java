package bstmap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    Node root = null;

    // nested class Node presenting the node in the BST
    public class Node {
        public K key;
        public V value;
        public Node left;
        public Node right;

        public  Node(K key,V value,Node left,Node right){
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public void put(K key, V value) {
            root = put(key,value,root);
    }
    //this recursive function return a tree(or node) which has been put in the key(value)
    public Node put (K key,V value,Node node) {
        if (node == null) {
            return  new Node(key,value,null,null);
        } else {
            int cmp = key.compareTo(node.key);
            if (cmp <= 0) {
                node.left = put(key,value,node.left);
            } else {
                node.right = put(key,value,node.right);
            }
            return node;
        }
    }

    @Override
    public boolean containsKey(K key) {
            return containsKey(key,root);
    }

    public boolean containsKey(K key ,Node node) {
        if (node == null ){
            return false;
        }
        if (node.key.compareTo(key) ==0) {
            return true;
        } else if (isLeaf(node) && node.key.compareTo(key) != 0) {
            return false;
        } else {
            return containsKey(key,node.left)||containsKey(key,node.right);
        }
    }

    @Override
    public V get(K key) {
        return get(key,root);
    }
// assume the node(a.k.a the tree) have the key and according value
    public V get(K key,Node node) {
        if (node==null){
            return null;
        }
        if (key.compareTo(node.key) ==0 ) {
            return node.value;
        } else {
            if (key.compareTo(node.key) < 0) {
                return get(key,node.left);
            } else {
                return get(key,node.right);
            }
        }
    }

    public  boolean isLeaf(Node node) {
        return node.left == null && node.right == null;
    }

    @Override
    public int size() {
        return size(root);
    }

    public int size(Node node) {
        if (node == null){
            return 0;
        } else {
            return 1+size(node.left)+size(node.right);
        }
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        } else {
            Node curNode = findNode(key,root);
            if (isLeaf(curNode) ) {

            }
        }
    }

   public  Node findRemovedNode(K key, Node node) {

   }

    public Node findNode(K key, Node node) {
        if (node.key == key) {
            return  node;
        } else {
            cmp = key.compareTo(node.key);
            if (cmp <= 0) {
                return findNode(key,node.left);
            } else {
                return findNode(key,node.right);
            }
        }
    }
    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        throw new UnsupportedOperationException();
    }


}
