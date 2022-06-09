package org.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class InsertBatchPlugin extends PluginAdapter {
    List<String> skipColumnList = new ArrayList<>();

    public InsertBatchPlugin() {
        skipColumnList.add("id");
        skipColumnList.add("is_delete");
        skipColumnList.add("create_time");
        skipColumnList.add("update_time");
//        skipColumnList.add("state");
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addMethod(this.generateInsertBatch(method, introspectedTable));
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addMethod(this.generateInsertBatch(method, introspectedTable));
        return true;
    }

//    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
//        interfaze.addMethod(this.generateInsertBatch(method, introspectedTable));
//        return true;
//    }
//
//    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
//        interfaze.addMethod(this.generateInsertBatch(method, introspectedTable));
//        return true;
//    }
//
//    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        topLevelClass.addMethod(this.generateInsertBatch(method, introspectedTable));
//        return true;
//    }
//
//    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        topLevelClass.addMethod(this.generateInsertBatch(method, introspectedTable));
//        return true;
//    }

    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "insertBatch"));
        insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        StringBuffer tableColumn = new StringBuffer();
        StringBuffer javaType = new StringBuffer();

        int columnCount = 0;
        for (int i = 0; i < columns.size(); ++i) {
            IntrospectedColumn column = columns.get(i);

            String actualColumnName = column.getActualColumnName();
            if (skipColumnList.contains(actualColumnName)) {
                continue;
            }

            if (columnCount != 0) {
                tableColumn.append(",");
                javaType.append(",");
            }
            columnCount++;

            tableColumn.append("`" + column.getActualColumnName() + "`");
            javaType.append("#{item." + column.getJavaProperty() + ",jdbcType=" + column.getJdbcTypeName() + "}");
        }

        insertBatchElement.addElement(new TextElement("insert into " + tableName + " (" + tableColumn.toString() + ") values  <foreach collection=\"records\" item=\"item\" index=\"index\" separator=\",\"> ( " + javaType.toString() + " )  </foreach>"));
        parentElement.addElement(insertBatchElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private Method generateInsertBatch(Method method, IntrospectedTable introspectedTable) {
        Method method1 = new Method("insertBatch");
        method1.setVisibility(method.getVisibility());
        method1.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method1.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<" + introspectedTable.getBaseRecordType() + ">"), "records", "@Param(\"records\")"));
        this.context.getCommentGenerator().addGeneralMethodComment(method1, introspectedTable);
        return method1;
    }
}
