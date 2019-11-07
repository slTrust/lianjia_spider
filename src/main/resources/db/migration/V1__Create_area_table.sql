SET FOREIGN_KEY_CHECKS=0;
drop table if exists house_images;
drop table if exists house_base_detail;
drop table if exists house;
drop table if exists street;
drop table if exists area;
SET FOREIGN_KEY_CHECKS=1;


create table area(
  id bigint primary key auto_increment,
  name varchar(20),
  code varchar(30)
);


create table street (
    id bigint primary key auto_increment,
    name varchar(20),
    code varchar(30),
    area_id bigint,
    foreign key(area_id) references area(id)
);

create table house (
    id bigint primary key auto_increment,
    title varchar(20),
    link varchar(50),
    total_price decimal(12,2),
    square_metre_price decimal(12,2),
    street_id bigint,
    foreign key(street_id) references street(id)
);

create table house_images(
  id bigint primary key auto_increment,
  link varchar(200),
  house_id bigint,
  foreign key(house_id) references house(id)
);

create table house_base_detail(
    id bigint primary key auto_increment,
--  房屋户型
    house_type varchar(20),
--     建筑面积
    build_area decimal(6,2),
--     套内面积
    inner_area decimal(6,2),
--     房屋朝向
    aspect varchar(30),
--     装修情况
    decorated varchar(30),
--     供暖方式
    heatingMode varchar(30),
--     产权年限
    property varchar(30),
--     用电类型
    electricity_type varchar(30),
--     所在楼层
    floor varchar(30),
--     户型结构
    house_struct varchar (30),
--     建筑类型
    build_type varchar(30),
--     建筑结构
    build_struct varchar(30),
--     梯户比例
    ladder_house_ratio varchar(30),
--     配备电梯
    elevator varchar(30),
--     用水类型
    water_type varchar(30),
--     燃气价格
    gas_price decimal(4,2),

--     # 交易属性
--     挂牌时间
    listing_date date,
--     上次交易
    last_trade date,
--     房屋年限
    house_years varchar (10),
--     抵押信息
    mortgage varchar (30),
--     交易权属
    trade_right varchar(30),
--     房屋用途
    house_use varchar (30),
--     产权所属
    propertyOwner varchar(30),
--     房本备件
    spare_parts varchar(30),
--     hid fk 房源id
    house_id bigint,
    foreign key(house_id) references house(id)
);
