package entity;

public class House {
    private int id;
    private String title;
    private String link;
    private Double total_price;
    private Double square_metre_price;
    private HouseDetail houseDetail;

    public House() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(Double total_price) {
        this.total_price = total_price;
    }

    public Double getSquare_metre_price() {
        return square_metre_price;
    }

    public void setSquare_metre_price(Double square_metre_price) {
        this.square_metre_price = square_metre_price;
    }

    public HouseDetail getHouseDetail() {
        return houseDetail;
    }

    public void setHouseDetail(HouseDetail houseDetail) {
        this.houseDetail = houseDetail;
    }

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", total_price=" + total_price +
                ", square_metre_price=" + square_metre_price +
                ", houseDetail=" + houseDetail +
                '}';
    }
}
