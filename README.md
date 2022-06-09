# 用法
```
引入包后,
1.在目标目录的src/test/java中, 建立普通JAVA文件, 内容如下
public static void main(String[] args) {
    MybatisGeneratorUtil mybatisGeneratorUtil = new MybatisGeneratorUtil();
    try {
        mybatisGeneratorUtil.generateAll(Run.class);
    } catch (Throwable throwable) {
        throwable.printStackTrace();
    }
}

  也可以复制救命中的文件
2.复制resource目录下的示例文件,
3.修改src/reources/mybatis.properties中的配置, 按需修改
4.src/reources/中的xml, 建议一类DAO生一个文件或一张表生成一个文件
  请参照示例xml复制并添加表
  xml中, 除了添加表以外, 还需要修改包空间, 建立一类DAO一个空间,
  需要修改的包空间为:
   <javaModelGenerator targetPackage="${package.name}.dao.template.bean.nee"  中的nee
   <sqlMapGenerator targetPackage="mappers.template.nee" 中的nee
   <javaClientGenerator targetPackage="${package.name}.dao.template.mapper.nee" 中的nee
5.注: 除表以及上面3个表空间以外, 其它信息可不用修改
6.运行src/test/java/run.java 即可生成所有实例
7.如果某一个实例需要重新生成, 删除src/test/resources/xml_file_snapshot.txt 中对应的xml,
  再运行src/test/java/run.java 即可
  如果需要全部生成, 删除此文件(xml_file_snapshot.txt)即可

```

## 批量插入/更新使用方法
InsertBatch除了id,create_time,update_time,is_delete这几个字段不在SQL新增中, 其它所有字段都需要处理, 包含有默认值的字段
```
SQL格式:
insert tableName (cloumns) values (values),(values),(values),(values);
```
InsertBatchSelective使用的不是单条SQL语句, 效率较低, 建议少使用;
```
SQL格式:
insert tableName (cloumns) values (values);insert tableName (cloumns) values (values)
```
