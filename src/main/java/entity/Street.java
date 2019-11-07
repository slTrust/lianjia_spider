package entity;

public class Street {
    private int id;
    private String code;
    private String name;
    private String link;
    private Area area;

    public Street(){

    }

    public Street(String code, String name, String link) {
        this.code = code;
        this.name = name;
        this.link = link;
    }

    public Street(String code, String name, String link, Area area) {
        this.code = code;
        this.name = name;
        this.link = link;
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Street{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", link='" + link + '\'' +
                ", area=" + area +
                '}';
    }
}
