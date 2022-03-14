import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * KD Tree is a binary search tree where data node is a K-dimensional point in space.
 * <p>
 * {@see https://en.wikipedia.org/wiki/K-d_tree}
 *
 * @param <T>
 */
public class KDTree<T> {
    /**
     * Node representing a data point in K-dimensional space.
     */
    class Node {
        int value;
        int attrIndex;

        Node left;
        Node right;
        boolean leaf = false;
        List<T> bucket = new ArrayList<>();
        Comparator<T> comparator = (a, b) -> {
            try {
                int aV = ((int) getAttributeValue(a, attrIndex));
                int bV = ((int) getAttributeValue(b, attrIndex));
                return aV - bV;
            } catch (NotFound ignored) {
            }
            return 0;
        };

        public Node(boolean leaf, int attrIndex) {
            this.leaf = leaf;
            this.attrIndex = attrIndex;
        }

        public Node(int value, int attrIndex) {
            this.value = value;
            this.attrIndex = attrIndex;
        }

        /**
         * Adds the point to the bucket associated with this node.
         *
         * @param dataPoint Data point.
         */
        public void add(T dataPoint) {
            // Insertion sort
            bucket.add(dataPoint);
            bucket.sort(comparator);
            leaf = true;
        }

        public int size() {
            return bucket.size();
        }
    }

    private final String[] attributes;
    private Node root;
    private final int bucketSize;

    /**
     * Creates the root node and the list of attributes (dimensions).
     *
     * @param classType     Type of class for data point.
     * @param bucketSize    Leaf node bucket size.
     * @param attrIndexSeed The root attribute index.
     */
    public KDTree(Class<?> classType, int bucketSize, int attrIndexSeed) {
        attributes = Arrays.stream(classType.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
        this.bucketSize = bucketSize;
        root = new Node(true, attrIndexSeed);
    }

    /**
     * Gets the attribute value of datapoint using the Java Reflections.
     *
     * @param dataPoint Data point.
     * @param attrIndex Attribute (dimension) index.
     * @return Value of the datapoint for the particular attribute.
     * @throws NotFound If the attribute not found in the provided object.
     */
    private Object getAttributeValue(T dataPoint, int attrIndex) throws NotFound {
        try {
            return dataPoint.getClass().getDeclaredField(attributes[attrIndex]).get(dataPoint);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new NotFound();
        }
    }

    /**
     * Adds the datapoint at the appropriate leaf node by searching through the tree using binary search.
     *
     * @param newPoint New point.
     * @throws NotFound If the attribute not found in the provided object.
     */
    public void add(T newPoint) throws NotFound {
        if (root.leaf) {
            root = addToBucket(root, newPoint);
            return;
        }

        Node currentNode = root;

        while (!currentNode.leaf) {
            int newValue = (int) getAttributeValue(newPoint, currentNode.attrIndex);
            int currentValue = currentNode.value;

            if (newValue < currentValue) {
                if (currentNode.left.leaf) {
                    currentNode.left = addToBucket(currentNode.left, newPoint);
                    break;
                } else {
                    currentNode = currentNode.left;
                }
            } else {
                if (currentNode.right.leaf) {
                    currentNode.right = addToBucket(currentNode.right, newPoint);
                    break;
                } else {
                    currentNode = currentNode.right;
                }
            }
        }
    }

    /**
     * Adds the node to bucket of leaf node. If the bucket is full, we will split the bucket by median value.
     * The split buckets will be attached to new node which will be returned as a current node.
     * <p>
     * If the bucket is not full, we will simply add the new node to the current node's bucket.
     *
     * @param currentNode Current node to be modified.
     * @param newPoint    New point to be added.
     * @return Modified current node or new node containing the leaves.
     * @throws NotFound If the attribute not found in the provided object.
     */
    public Node addToBucket(Node currentNode, T newPoint) throws NotFound {
        if (currentNode.size() < bucketSize) {
            currentNode.add(newPoint);
        }
        // Split bucket and create a new node.
        else {
            int nextAttrIndex = ((currentNode.attrIndex + 1) % attributes.length);

            List<T> bucket = currentNode.bucket;
            currentNode.add(newPoint);

            T lowerD = currentNode.bucket.get(0);
            T upperD = currentNode.bucket.get(currentNode.bucket.size() - 1);

            int median = (int) getAttributeValue(lowerD, currentNode.attrIndex) + (int) getAttributeValue(upperD, currentNode.attrIndex);
            median /= 2;

            Node newNode = new Node(median, currentNode.attrIndex);
            newNode.left = new Node(true, nextAttrIndex);
            newNode.right = new Node(true, nextAttrIndex);

            for (T t : bucket) {
                if (median > (int) getAttributeValue(t, currentNode.attrIndex)) {
                    newNode.left.add(t);
                } else {
                    newNode.right.add(t);
                }
            }
//            newNode.left.bucket = new ArrayList<>(currentNode.bucket.subList(0, medianIndex));
//            newNode.right.bucket = new ArrayList<>(currentNode.bucket.subList(medianIndex, currentNode.bucket.size()));
            currentNode = newNode;
        }
        return currentNode;
    }

    /**
     * Deletes the datapoint on root node.
     *
     * @param dataPoint Data point.
     */
    public void delete(T dataPoint) {
        try {
            root = delete(root, dataPoint);
        } catch (NotFound e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes the datapoint recursively using binary search.
     *
     * @param currentNode Current node.
     * @param dataPoint   Data point to be deleted.
     * @return Modified current node.
     * @throws NotFound If the attribute not found in the data point object.
     */
    private Node delete(Node currentNode, T dataPoint) throws NotFound {
        if (currentNode.leaf) {
            return null;
        }

        int newValue = (int) getAttributeValue(dataPoint, currentNode.attrIndex);
        int currentValue = currentNode.value;

        if (newValue < currentValue) {
            if (currentNode.left.leaf) {
                if (currentNode.left.bucket.contains(dataPoint)) {
                    currentNode = removeFromBucket(currentNode, dataPoint, true);
                } else {
                    throw new NotFound();
                }
            } else {
                currentNode.left = delete(currentNode.left, dataPoint);
            }
        } else {
            if (currentNode.right.leaf) {
                if (currentNode.right.bucket.contains(dataPoint)) {
                    currentNode = removeFromBucket(currentNode, dataPoint, false);
                } else {
                    throw new NotFound();
                }
            } else {
                currentNode.right = delete(currentNode.right, dataPoint);
            }
        }
        return currentNode;
    }

    /**
     * When we reach to the leave node, we will remove the node if the bucket contains it; Otherwise we will throw an exception.
     * After removing, we will merge two leave buckets if the total size of these bucket is less than the threshold.
     * If both the leaves are imbalanced, we will make these buckets balanced by adding some nodes from one leaf bucket to another.
     *
     * @param currentNode Current node.
     * @param dataPoint   Data point.
     * @param isLeft      If the current node's left leaf has the data point or the right bucket.
     * @return Returns the current node.
     * @throws NotFound If the attribute not found in the data point object.
     */
    private Node removeFromBucket(Node currentNode, T dataPoint, boolean isLeft) throws NotFound {
        int nextAttrIndex = ((currentNode.attrIndex + 1) % attributes.length);

        if (isLeft) {
            currentNode.left.bucket.remove(dataPoint);
        } else {
            currentNode.right.bucket.remove(dataPoint);
        }

        if (currentNode.left.leaf && currentNode.right.leaf) {
            if (currentNode.left.bucket.size() + currentNode.right.bucket.size() < (bucketSize / 2)) {
                // Merge two nodes.
                Node newNode = new Node(true, currentNode.attrIndex);
                newNode.bucket = new ArrayList<>(currentNode.left.bucket);
                newNode.bucket.addAll(currentNode.right.bucket);
                currentNode = newNode;
            } else if (Math.abs(currentNode.left.bucket.size() - currentNode.right.bucket.size()) > 2) {
                // Balance these two leaf buckets.
                List<T> bucket = new ArrayList<>(currentNode.left.bucket);
                bucket.addAll(currentNode.right.bucket);
                int attrIndex = currentNode.attrIndex;
                bucket.sort((a, b) -> {
                    try {
                        int aV = ((int) getAttributeValue(a, attrIndex));
                        int bV = ((int) getAttributeValue(b, attrIndex));
                        return aV - bV;
                    } catch (NotFound ignored) {
                    }
                    return 0;
                });

                T lowerD = bucket.get(0);
                T upperD = bucket.get(bucket.size() - 1);

                int median = (int) getAttributeValue(lowerD, currentNode.attrIndex) + (int) getAttributeValue(upperD, currentNode.attrIndex);
                median /= 2;

                Node newNode = new Node(median, currentNode.attrIndex);
                newNode.left = new Node(true, nextAttrIndex);
                newNode.right = new Node(true, nextAttrIndex);

                for (T t : bucket) {
                    if (median > (int) getAttributeValue(t, currentNode.attrIndex)) {
                        newNode.left.add(t);
                    } else {
                        newNode.right.add(t);
                    }
                }
                currentNode = newNode;
            }
        }
        return currentNode;
    }

    /**
     * Prints the kd-tree.
     */
    public void print() {
        List<List<String>> lines = new ArrayList<List<String>>();

        List<Node> level = new ArrayList<>();
        List<Node> next = new ArrayList<>();

        level.add(root);
        int nn = 1;

        int widest = 0;

        while (nn != 0) {
            List<String> line = new ArrayList<>();

            nn = 0;

            for (Node n : level) {
                if (n == null) {
                    line.add(null);

                    next.add(null);
                    next.add(null);
                } else {
                    if (n.leaf) {
                        StringBuilder leafLine = new StringBuilder();
                        for (T node : n.bucket) {
                            leafLine.append(node);
                        }
                        line.add("||" + leafLine + "||");
                    } else {
                        String aa = n.value + "";
                        line.add(aa);
                        if (aa.length() > widest) widest = aa.length();

                        next.add(n.left);
                        next.add(n.right);

                        if (n.left != null) nn++;
                        if (n.right != null) nn++;
                    }
                }
            }

            if (widest % 2 == 1) widest++;

            lines.add(line);

            List<Node> tmp = level;
            level = next;
            next = tmp;
            next.clear();
        }

        int perpiece = lines.get(lines.size() - 1).size() * (widest + 4);
        for (int i = 0; i < lines.size(); i++) {
            List<String> line = lines.get(i);
            int hpw = (int) Math.floor(perpiece / 2f) - 1;

            if (i > 0) {
                for (int j = 0; j < line.size(); j++) {

                    // split node
                    char c = ' ';
                    if (j % 2 == 1) {
                        if (line.get(j - 1) != null) {
                            c = (line.get(j) != null) ? '┴' : '┘';
                        } else {
                            if (j < line.size() && line.get(j) != null) c = '└';
                        }
                    }
                    System.out.print(c);

                    // lines and spaces
                    if (line.get(j) == null) {
                        for (int k = 0; k < perpiece - 1; k++) {
                            System.out.print(" ");
                        }
                    } else {

                        for (int k = 0; k < hpw; k++) {
                            System.out.print(j % 2 == 0 ? " " : "─");
                        }
                        System.out.print(j % 2 == 0 ? "┌" : "┐");
                        for (int k = 0; k < hpw; k++) {
                            System.out.print(j % 2 == 0 ? "─" : " ");
                        }
                    }
                }
                System.out.println();
            }

            // print line of numbers
            for (String f : line) {

                if (f == null) f = "";
                int gap1 = (int) Math.ceil(perpiece / 2f - f.length() / 2f);
                int gap2 = (int) Math.floor(perpiece / 2f - f.length() / 2f);

                // a number
                for (int k = 0; k < gap1; k++) {
                    System.out.print(" ");
                }
                System.out.print(f);
                for (int k = 0; k < gap2; k++) {
                    System.out.print(" ");
                }
            }
            System.out.println();

            perpiece /= 2;
        }
    }
}
