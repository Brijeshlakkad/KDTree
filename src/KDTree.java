import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class KDTree<T> {
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
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
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

    public KDTree(Class<?> classType, int bucketSize, int attrIndexSeed) {
        attributes = Arrays.stream(classType.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
        this.bucketSize = bucketSize;
        root = new Node(true, attrIndexSeed);
    }

    private Object getAttributeValue(T dataPoint, int attrIndex) throws NoSuchFieldException, IllegalAccessException {
        return dataPoint.getClass().getDeclaredField(attributes[attrIndex]).get(dataPoint);
    }

    public void add(T newPoint) throws NoSuchFieldException, IllegalAccessException {
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

    public Node addToBucket(Node currentNode, T newPoint) throws NoSuchFieldException, IllegalAccessException {
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

    private void display(Node node) {
        // base case
        if (node == null) {
            return;
        }

        if (node.leaf) {
            for (int i = 0; i < node.bucket.size(); i++) {
                System.out.println(" | " + node.bucket.get(i) + " | ");
            }
        }

        display(node.left);
        display(node.right);
    }

    public void display() {
        display(root);
    }

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
            for (int j = 0; j < line.size(); j++) {

                String f = line.get(j);
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

//
//    private boolean isNull(Node<T> node) {
//        return node == null || node.dataPoint == null;
//    }
//
//    public T getLeftMostMinDataPoint(Node<T> currentNode, int attrIndex, int currentAttrIndex) throws NoSuchFieldException, IllegalAccessException {
//        if (isNull(currentNode)) {
//            return null;
//        }
//
//        int nextAttrIndex = ++currentAttrIndex % attributes.length;
//
//        if (currentAttrIndex == attrIndex) {
//            if (isNull(currentNode.left)) return currentNode.dataPoint;
//            return getLeftMostMinDataPoint(currentNode.left, attrIndex, nextAttrIndex);
//        }
//
//        // Find left most minimum in the left and right subtree.
//        T minDataPoint = currentNode.dataPoint;
//        int min = (int) getAttributeValue(minDataPoint, currentAttrIndex);
//
//        if (!isNull(currentNode.left)) {
//            T leftMinDataPoint = getLeftMostMinDataPoint(currentNode.left, attrIndex, nextAttrIndex);
//            int leftMin = (int) getAttributeValue(leftMinDataPoint, currentAttrIndex);
//            if (leftMin < min) {
//                min = leftMin;
//                minDataPoint = leftMinDataPoint;
//            }
//        }
//
//        if (!isNull(currentNode.right)) {
//            T rightMinDataPoint = getLeftMostMinDataPoint(currentNode.right, attrIndex, nextAttrIndex);
//            int rightMin = (int) getAttributeValue(rightMinDataPoint, currentAttrIndex);
//            if (min > rightMin) {
//                min = rightMin;
//                minDataPoint = rightMinDataPoint;
//            }
//        }
//
//        return minDataPoint;
//    }
//
//    private Node<T> deleteRecord(Node<T> currentNode, T newPoint, int attrIndexSeed) throws NoSuchFieldException, IllegalAccessException {
//        int currentAttrIndex = attrIndexSeed;
//
//        while (!isNull(currentNode)) {
//            if (currentNode.dataPoint.equals(newPoint)) {
//                int nextAttrIndex = ++currentAttrIndex % attributes.length;
//                if (!isNull(currentNode.right)) {
//                    currentNode.dataPoint = getLeftMostMinDataPoint(currentNode.right, currentAttrIndex, nextAttrIndex);
//                    return deleteRecord(currentNode.left, currentNode.dataPoint, nextAttrIndex);
//                } else if (!isNull(currentNode.left)) {
//                    currentNode.dataPoint = getLeftMostMinDataPoint(currentNode.left, currentAttrIndex, nextAttrIndex);
//                    return deleteRecord(currentNode.right, currentNode.dataPoint, nextAttrIndex);
//                }
//                currentNode.dataPoint = null;
//                return null;
//            }
//
//            int newValue = (int) getAttributeValue(newPoint, currentAttrIndex);
//            int currentValue = (int) getAttributeValue(currentNode.dataPoint, currentAttrIndex);
//
//
//            if (newValue < currentValue) {
//                if (isNull(currentNode.left)) {
//                    return null;
//                } else {
//                    currentNode = currentNode.left;
//                }
//            } else {
//                if (isNull(currentNode.right)) {
//                    return null;
//                } else {
//                    currentNode = currentNode.right;
//                }
//            }
//
//            currentAttrIndex = ++currentAttrIndex % attributes.length;
//        }
//
//        return null;
//    }
//
//    public void deleteRecord(T newPoint) throws NoSuchFieldException, IllegalAccessException {
//        deleteRecord(root, newPoint, attrIndexSeed);
//    }
}
