package uk.ac.ebi.eva.dbsnpimporter.models;


import java.util.Map;

public class Sample {
    private String id;

    private Character sex;

    private String father;

    private String mother;

    private Map<String, String> cohorts;

    public Sample(String id, Character sex, String father, String mother,
                  Map<String, String> cohorts) {
        this.id = id;
        this.sex = sex;
        this.father = father;
        this.mother = mother;
        this.cohorts = cohorts;
    }

    public String getId() {
        return id;
    }

    public Character getSex() {
        return sex;
    }

    public String getFather() {
        return father;
    }

    public String getMother() {
        return mother;
    }

    public Map<String, String> getCohorts() {
        return cohorts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (!id.equals(sample.id)) return false;
        if (sex != null ? !sex.equals(sample.sex) : sample.sex != null) return false;
        if (father != null ? !father.equals(sample.father) : sample.father != null) return false;
        if (mother != null ? !mother.equals(sample.mother) : sample.mother != null) return false;
        return cohorts != null ? cohorts.equals(sample.cohorts) : sample.cohorts == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (father != null ? father.hashCode() : 0);
        result = 31 * result + (mother != null ? mother.hashCode() : 0);
        result = 31 * result + (cohorts != null ? cohorts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id='" + id + '\'' +
                ", sex=" + sex +
                ", father='" + father + '\'' +
                ", mother='" + mother + '\'' +
                ", cohorts=" + cohorts +
                '}';
    }
}
