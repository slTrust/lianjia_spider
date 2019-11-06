#### docker常用命令

- 安装docker客户端 自行搜索即可
- 设置一个镜像以便快速的下载

```
# 删除容器
docker rm id 
# 强制删除容器
docker rm -f id

# 删除 image
docker rmi id 

# 停止某运行的进程
docker stop id 

# 查看所有进程
docker ps -a 

# 查看所有镜像
docker images 
```

#### docker 安装mysql

```
docker安装mysql镜像

# 这样不指定tag 代表安装最新版
docker pull mysql
# 也可以这样
docker pull mysql:8.0
```


#### mysql持久化数据

```$xslt
cd ~/Desktop/
mkdir docker_mysql
# 进入到持久化保存数据的目录
cd docker_mysql
```

- 挂载一个本地目录保存数据 `docker run -v "$PWD/data":/var/lib/mysql --name my-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=xdml -p 3306:3306 -d mysql`
- 提交数据`docker commit my-mysql`
- 持久化数据
   - 然后下次当你删了容器的时候 下次在初始化的时候 数据都存在
   - `docker run -v "$PWD/data":/var/lib/mysql --name my-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=xdml -p 3306:3306 -d mysql`

```
http://note.youdao.com/noteshare?id=9c912476ba523db7a32358203c46a72b
``` 