package bstmap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparator<K>, V> implements Map61B<K, V> {
    Node root = null;

    // nested class Node presenting the node in the BST
    public class Node<K,V> {
        private K key;
        private V value;
        private Node left;
        private Node right;

        public  Node(K k,V v,Node left,Node right){
            key = k;
            value = v;
            this.left = left;
            this.right = right;
        }

    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
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
