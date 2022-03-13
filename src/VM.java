public class VM {
    public static void main(String[] args) {
        KDTree<DataPoint> kdTree = new KDTree<>(DataPoint.class);
        try {
            kdTree.add(new DataPoint(3, 6));
            kdTree.add(new DataPoint(17, 15));
            kdTree.add(new DataPoint(13, 15));
            kdTree.add(new DataPoint(6, 12));
            kdTree.add(new DataPoint(9, 1));
            kdTree.add(new DataPoint(2, 7));
            kdTree.add(new DataPoint(10, 19));


//            kdTree.deleteRecord(new DataPoint(10, 19));
            kdTree.deleteRecord(new DataPoint(17, 15));

            System.out.println();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
