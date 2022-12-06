package  hashmap;
import org.w3c.dom.Node;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author HalfDream
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int bucketsSize = 16;
    private double loadFactor = 0.75;
    private int itemSize = 0;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(bucketsSize);
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
    }


    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        bucketsSize = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(initialSize); }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private int getHashIndex(K key) {
        return Math.floorMod(key.hashCode(),11);
    }
    private Node createNode(K key, V value) {
        Node insertNode = new Node(key,value);
        // note : we need to override the add method  in subclass
        // to consider a situation when insertNode has been in the bucket
        buckets[getHashIndex(key)].add(insertNode);
        itemSize ++;
        return insertNode;
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] backTable = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++ ) {
            backTable[i] = createBucket();
        }
        return backTable;
    }

    // TODO: ImplemeRt the methods of the Map61B Interface below
    // Your code won't compile until you do so!


    @Override
    public boolean containsKey(K key) {
        int Hash = getHashIndex(key) ;
        for (Node n: buckets[Hash]) {
            if (n.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        buckets = createTable(bucketsSize);
        itemSize = 0;
    }


    public Node getNode (K key) {
        int Hash = getHashIndex(key);
        for (Node n: buckets[Hash]) {
            if (n.key.equals(key)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        if (node == null ) {
            return null;
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return itemSize;
    }

    @Override
    public void put(K key, V value) {
        int Hash = getHashIndex(key);
        int flag = 0;
        Collection<Node> bucket = buckets[Hash];
        for (Node n:bucket) {
            if (n.key == key) {
                n.value = value;
                flag = 1 ;
                break;
            }
        }
        if (flag == 0 ) {
            bucket.add(new Node(key, value));
            itemSize ++;
        }

    }

    private void resize() {
        if (itemSize / bucketsSize >= loadFactor) {
            bucketsSize = bucketsSize * 4;
            Collection<Node>[] newBuckets = createTable(bucketsSize);
            for (Collection<Node> bucket: buckets) {
                 for (Node n : bucket) {
                      int Hash = getHashIndex(n.key);
                      newBuckets[Hash].add(n);
                 }
            }
            buckets = newBuckets;
        }
    }


    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                keySet.add(node.key) ;
            }
        }
        return keySet;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove(K key, V value) {
        return remove(key);
    }

    @Override
    public V remove(K key) {
        int Hash = getHashIndex(key);
         Node removedNode = getNode(key);
         buckets[Hash].remove(removedNode);
         return removedNode.value;
    }
}
