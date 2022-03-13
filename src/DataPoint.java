import java.util.Objects;

public class DataPoint {
    int salary;
    int age;

    public DataPoint(int salary, int age) {
        this.salary = salary;
        this.age = age;
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