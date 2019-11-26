

```
# 本地安装 docker 安装好 mysql 8 
# 找一个数据持久化目录
# 然后运行下面这句
docker run -v "$PWD/data":/var/lib/mysql --name my-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=xdml -p 3306:3306 -d mysql
```

```
mvn initialize
```