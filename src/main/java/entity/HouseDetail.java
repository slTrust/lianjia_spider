package entity;

import java.util.Date;

public class HouseDetail {
    private int id;
//    房屋户型
    private String house_type;
//--     建筑面积
    private double build_area;
//--     计租面积
    private double rental_area;
//--     套内面积
    private double inner_area;
//--     房屋朝向
    private String aspect;
//--     装修情况
    private String decorated;
// --     供暖方式
    private String heating_mode;
// --     产权年限
    private String property;
// --     用电类型
    private String electricity_type;
// --     所在楼层
    private String floor;
// --     户型结构
    private String house_struct;
// --     建筑类型
    private String build_type;
// --     建筑结构
    private String build_struct;
// --     梯户比例
    private String ladder_house_ratio;
// --     配备电梯
    private String elevator;
// --     用水类型
    private String water_type;
// --     燃气价格
    private Double gas_price;

// --     # 交易属性
// --     挂牌时间
    private long listing_date;
// --     上次交易
    private long last_trade;
// --     房屋年限
    private String house_years;
// --     抵押信息
    private String mortgage;
// --     交易权属
    private String trade_right;
// --     房屋用途
    private String house_use;
// --     产权所属
    private String property_owner;
// --     房本备件
    private String spare_parts;

    public HouseDetail() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHouse_type() {
        return house_type;
    }

    public void setHouse_type(String house_type) {
        this.house_type = house_type;
    }

    public double getBuild_area() {
        return build_area;
    }

    public void setBuild_area(double build_area) {
        this.build_area = build_area;
    }

    public double getRental_area() {
        return rental_area;
    }

    public void setRental_area(double rental_area) {
        this.rental_area = rental_area;
    }

    public double getInner_area() {
        return inner_area;
    }

    public void setInner_area(double inner_area) {
        this.inner_area = inner_area;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getDecorated() {
        return decorated;
    }

    public void setDecorated(String decorated) {
        this.decorated = decorated;
    }

    public String getHeating_mode() {
        return heating_mode;
    }

    public void setHeating_mode(String heating_mode) {
        this.heating_mode = heating_mode;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getElectricity_type() {
        return electricity_type;
    }

    public void setElectricity_type(String electricity_type) {
        this.electricity_type = electricity_type;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getHouse_struct() {
        return house_struct;
    }

    public void setHouse_struct(String house_struct) {
        this.house_struct = house_struct;
    }

    public String getBuild_type() {
        return build_type;
    }

    public void setBuild_type(String build_type) {
        this.build_type = build_type;
    }

    public String getBuild_struct() {
        return build_struct;
    }

    public void setBuild_struct(String build_struct) {
        this.build_struct = build_struct;
    }

    public String getLadder_house_ratio() {
        return ladder_house_ratio;
    }

    public void setLadder_house_ratio(String ladder_house_ratio) {
        this.ladder_house_ratio = ladder_house_ratio;
    }

    public String getElevator() {
        return elevator;
    }

    public void setElevator(String elevator) {
        this.elevator = elevator;
    }

    public String getWater_type() {
        return water_type;
    }

    public void setWater_type(String water_type) {
        this.water_type = water_type;
    }

    public Double getGas_price() {
        return gas_price;
    }

    public void setGas_price(Double gas_price) {
        this.gas_price = gas_price;
    }

    public long getListing_date() {
        return listing_date;
    }

    public void setListing_date(long listing_date) {
        this.listing_date = listing_date;
    }

    public long getLast_trade() {
        return last_trade;
    }

    public void setLast_trade(long last_trade) {
        this.last_trade = last_trade;
    }

    public String getHouse_years() {
        return house_years;
    }

    public void setHouse_years(String house_years) {
        this.house_years = house_years;
    }

    public String getMortgage() {
        return mortgage;
    }

    public void setMortgage(String mortgage) {
        this.mortgage = mortgage;
    }

    public String getTrade_right() {
        return trade_right;
    }

    public void setTrade_right(String trade_right) {
        this.trade_right = trade_right;
    }

    public String getHouse_use() {
        return house_use;
    }

    public void setHouse_use(String house_use) {
        this.house_use = house_use;
    }

    public String getProperty_owner() {
        return property_owner;
    }

    public void setProperty_owner(String property_owner) {
        this.property_owner = property_owner;
    }

    public String getSpare_parts() {
        return spare_parts;
    }

    public void setSpare_parts(String spare_parts) {
        this.spare_parts = spare_parts;
    }

    @Override
    public String toString() {
        return "HouseDetail{" +
                "id=" + id +
                ", house_type='" + house_type + '\'' +
                ", build_area=" + build_area +
                ", rental_area=" + rental_area +
                ", inner_area=" + inner_area +
                ", aspect='" + aspect + '\'' +
                ", decorated='" + decorated + '\'' +
                ", heating_mode='" + heating_mode + '\'' +
                ", property='" + property + '\'' +
                ", electricity_type='" + electricity_type + '\'' +
                ", floor='" + floor + '\'' +
                ", house_struct='" + house_struct + '\'' +
                ", build_type='" + build_type + '\'' +
                ", build_struct='" + build_struct + '\'' +
                ", ladder_house_ratio='" + ladder_house_ratio + '\'' +
                ", elevator='" + elevator + '\'' +
                ", water_type='" + water_type + '\'' +
                ", gas_price=" + gas_price +
                ", listing_date=" + listing_date +
                ", last_trade=" + last_trade +
                ", house_years='" + house_years + '\'' +
                ", mortgage='" + mortgage + '\'' +
                ", trade_right='" + trade_right + '\'' +
                ", house_use='" + house_use + '\'' +
                ", property_owner='" + property_owner + '\'' +
                ", spare_parts='" + spare_parts + '\'' +
                '}';
    }
}
