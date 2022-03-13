import java.lang.reflect.Field;
import java.util.Arrays;

class Node<T> {
    T dataPoint;

    Node<T> left;
    Node<T> right;

    public Node(T dataPoint) {
        this.dataPoint = dataPoint;
    }
}

public class KDTree<T> {
    private Node<T> root;
    private final String[] attributes;
    private int attrIndexSeed;

    public KDTree(Class<?> classType) {
        attributes = Arrays.stream(classType.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
    }

    public KDTree(Class<?> classType, int attrIndexSeed) {
        this(classType);
        this.attrIndexSeed = attrIndexSeed;
    }

    private Object getAttributeValue(T dataPoint, int attrIndex) throws NoSuchFieldException, IllegalAccessException {
        return dataPoint.getClass().getDeclaredField(attributes[attrIndex]).get(dataPoint);
    }

    public void add(T newPoint) throws NoSuchFieldException, IllegalAccessException {
        Node<T> currentNode = root;
        int currentAttrIndex = attrIndexSeed;

        if (root == null || root.dataPoint == null) {
            root = new Node<>(newPoint);
        }

        while (!isNull(currentNode)) {
            int newValue = (int) getAttributeValue(newPoint, currentAttrIndex);
            int currentValue = (int) getAttributeValue(currentNode.dataPoint, currentAttrIndex);

            if (newValue < currentValue) {
                if (currentNode.left == null) {
                    currentNode.left = new Node<>(newPoint);
                    break;
                } else {
                    currentNode = currentNode.left;
                }
            } else {
                if (currentNode.right == null) {
                    currentNode.right = new Node<>(newPoint);
                    break;
                } else {
                    currentNode = currentNode.right;
                }
            }

            currentAttrIndex = ++currentAttrIndex % attributes.length;
        }
    }

    private boolean isNull(Node<T> node) {
        return node == null || node.dataPoint == null;
    }

    public T getLeftMostMinDataPoint(Node<T> currentNode, int attrIndex, int currentAttrIndex) throws NoSuchFieldException, IllegalAccessException {
        if (isNull(currentNode)) {
            return null;
        }

        int nextAttrIndex = ++currentAttrIndex % attributes.length;

        if (currentAttrIndex == attrIndex) {
            if (isNull(currentNode.left)) return currentNode.dataPoint;
            return getLeftMostMinDataPoint(currentNode.left, attrIndex, nextAttrIndex);
        }

        // Find left most minimum in the left and right subtree.
        T minDataPoint = currentNode.dataPoint;
        int min = (int) getAttributeValue(minDataPoint, currentAttrIndex);

        if (!isNull(currentNode.left)) {
            T leftMinDataPoint = getLeftMostMinDataPoint(currentNode.left, attrIndex, nextAttrIndex);
            int leftMin = (int) getAttributeValue(leftMinDataPoint, currentAttrIndex);
            if (leftMin < min) {
                min = leftMin;
                minDataPoint = leftMinDataPoint;
            }
        }

        if (!isNull(currentNode.right)) {
            T rightMinDataPoint = getLeftMostMinDataPoint(currentNode.right, attrIndex, nextAttrIndex);
            int rightMin = (int) getAttributeValue(rightMinDataPoint, currentAttrIndex);
            if (min > rightMin) {
                min = rightMin;
                minDataPoint = rightMinDataPoint;
            }
        }

        return minDataPoint;
    }

    private Node<T> deleteRecord(Node<T> currentNode, T newPoint, int attrIndexSeed) throws NoSuchFieldException, IllegalAccessException {
        int currentAttrIndex = attrIndexSeed;

        while (!isNull(currentNode)) {
            if (currentNode.dataPoint.equals(newPoint)) {
                int nextAttrIndex = ++currentAttrIndex % attributes.length;
                if (!isNull(currentNode.right)) {
                    currentNode.dataPoint = getLeftMostMinDataPoint(currentNode.right, currentAttrIndex, nextAttrIndex);
                    return deleteRecord(currentNode.left, currentNode.dataPoint, nextAttrIndex);
                } else if (!isNull(currentNode.left)) {
                    currentNode.dataPoint = getLeftMostMinDataPoint(currentNode.left, currentAttrIndex, nextAttrIndex);
                    return deleteRecord(currentNode.right, currentNode.dataPoint, nextAttrIndex);
                }
                currentNode.dataPoint = null;
                return null;
            }

            int newValue = (int) getAttributeValue(newPoint, currentAttrIndex);
            int currentValue = (int) getAttributeValue(currentNode.dataPoint, currentAttrIndex);


            if (newValue < currentValue) {
                if (isNull(currentNode.left)) {
                    return null;
                } else {
                    currentNode = currentNode.left;
                }
            }  else {
                if (isNull(currentNode.right)) {
                    return null;
                } else {
                    currentNode = currentNode.right;
                }
            }

            currentAttrIndex = ++currentAttrIndex % attributes.length;
        }

        return null;
    }

    public void deleteRecord(T newPoint) throws NoSuchFieldException, IllegalAccessException {
        deleteRecord(root, newPoint, attrIndexSeed);
    }
}
