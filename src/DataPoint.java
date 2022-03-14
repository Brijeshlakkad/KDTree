import java.util.Objects;

/**
 * Datapoint class with salary and age as its member fields.
 */
public class DataPoint {
    int age;
    int salary;

    public DataPoint(int age, int salary) {
        this.age = age;
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPoint dataPoint = (DataPoint) o;
        return salary == dataPoint.salary && age == dataPoint.age;
    }

    @Override
    public int hashCode() {
        return Objects.hash(salary, age);
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "salary=" + salary +
                ", age=" + age +
                '}';
    }
}