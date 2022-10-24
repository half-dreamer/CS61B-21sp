package bstmap;

import java.util.Comparator;
import java.util.HashSet;
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

    public Node getToNode (K key,Node curNode) {
        if (curNode.key.compareTo(key) == 0) {
            return curNode;
        } else if  (curNode.key.compareTo(key) > 0 ) {
            return getToNode(key,curNode.left);
        } else {
            return getToNode(key,curNode.right);
        }

    }

    @Override
    public V get(K key) {
        if (! containsKey(key)) {
            return null;
        } else {
            return getToNode(key,root).value;
        }
    }
// assume the node(a.k.a the tree) have the key and according value


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
        V value = get(key);
        root = remove(key,root);
        return  value;
    }

    @Override
    public V remove(K key, V value) {
        return remove(key);
   }

   /* this method get a curNode (tree) and return a node(tree) which have complete the remove function */
   public Node remove(K key,Node curNode) {
        if (curNode == null) {
            return null;
        } else if (curNode.key.compareTo(key) == 0){
           // this is the base case and we find the target node
               if (isLeaf(curNode)) {
                   return null;
                   //no child case
               } else if (curNode.left == null) {
                   return curNode.right;
               } else if (curNode.right == null ) {
                   return curNode.left;
               } else {
                   Node substituter = findMinNode(curNode.right);
                   curNode.key = substituter.key;
                   curNode.value = substituter.value;
                   curNode.right = remove(substituter.key,curNode.right);
                   return curNode;
           }
       }
        else {
           if (curNode.key.compareTo(key) > 0) {
               curNode.left = remove(key, curNode.left);
           } else if (curNode.key.compareTo(key) < 0) {
               curNode.right = remove(key, curNode.right);
           }
       }
        return curNode;
   }

   public Node findMinNode (Node node) {
       if (node.left == null) {
           return node;
       } else {
           return findMinNode(node.left);
       }
   }


    @Override
    public Iterator iterator() {
        return keySet().iterator();
    }

    @Override
    public Set keySet() {
        Set keySet = new HashSet();
        addKey(root,keySet);
        return keySet;
    }

    public void addKey (Node n,Set keySet) {
        if (n == null) {
        } else {
            keySet.add(n.key);
            addKey(n.left,keySet);
            addKey(n.right,keySet);
        }
    }


}
