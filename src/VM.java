/**
 * Virtual Machine creates kd-tree object and invokes its methods.
 */
public class VM {
    public static void main(String[] args) {
        KDTree<DataPoint> kdTree = new KDTree<>(DataPoint.class, 2, 1);
        try {
//            kdTree.add(new DataPoint(3, 6));
//            kdTree.add(new DataPoint(17, 15));
//            kdTree.add(new DataPoint(13, 15));
//            kdTree.add(new DataPoint(6, 12));
//            kdTree.add(new DataPoint(9, 1));
//            kdTree.add(new DataPoint(2, 7));
//            kdTree.add(new DataPoint(10, 19));

            kdTree.add(new DataPoint(25, 60));
            kdTree.add(new DataPoint(50, 120));
            kdTree.add(new DataPoint(25, 400));
            kdTree.add(new DataPoint(45, 60));
            kdTree.add(new DataPoint(70, 110));
            kdTree.add(new DataPoint(45, 350));
            kdTree.add(new DataPoint(50, 75));
            kdTree.add(new DataPoint(30, 260));
            kdTree.add(new DataPoint(60, 260));
            kdTree.add(new DataPoint(60, 270));
            kdTree.add(new DataPoint(20, 200));
            kdTree.add(new DataPoint(100, 500));
            kdTree.add(new DataPoint(65, 380));

            kdTree.delete(new DataPoint(60, 270));
            kdTree.delete(new DataPoint(60, 260));

//            kdTree.delete(new DataPoint(60, 270));
//            kdTree.delete(new DataPoint(60, 260));
//            kdTree.delete(new DataPoint(45, 350));
//            kdTree.delete(new DataPoint(30, 260));
//            kdTree.delete(new DataPoint(25, 400));

            kdTree.print();

        } catch (NotFound e) {
            System.out.println("Something went wrong!");
        }
    }
}
