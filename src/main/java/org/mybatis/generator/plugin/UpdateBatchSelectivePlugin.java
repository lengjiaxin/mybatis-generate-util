package org.mybatis.generator.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class UpdateBatchSelectivePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addMethod(this.generateUpdateBatchByPrimaryKey(method, introspectedTable));
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addMethod(this.generateUpdateBatchByPrimaryKey(method, introspectedTable));
        return true;
    }

    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addUpdateBatchSelectiveXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    public void addUpdateBatchSelectiveXml(Document document, IntrospectedTable introspectedTable) {
        XmlElement rootElement = document.getRootElement();

        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        String incrementField = introspectedTable.getTableConfiguration().getProperties().getProperty("incrementField");
        if (incrementField != null) {
            incrementField = incrementField.toUpperCase();
        }

        XmlElement updateBatchElement = new XmlElement("update");
        updateBatchElement.addAttribute(new Attribute("id", "updateBatchByPrimaryKeySelective"));
        updateBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        XmlElement setValueXml = new XmlElement("set");

        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();

            // 去除主键修改
            boolean isPrimaryKey = false;
            List<IntrospectedColumn> primaryKeyColumns = introspectedColumn.getIntrospectedTable().getPrimaryKeyColumns();
            for (int i = 0; i < primaryKeyColumns.size(); i++) {
                IntrospectedColumn introspectedColumn1 = primaryKeyColumns.get(i);
                String primaryKey = introspectedColumn1.getActualColumnName();
                if (columnName.equals(primaryKey)) {
                    isPrimaryKey = true;
                }
            }

            if (!isPrimaryKey && !columnName.toUpperCase().equals(incrementField)) {
                String javaProperty = introspectedColumn.getJavaProperty();
                XmlElement trimiftest = new XmlElement("if");
                trimiftest.addAttribute(new Attribute("test", "item." + javaProperty + " != null"));
                trimiftest.addElement(new TextElement("`" + columnName + "` = #{item."
                        + javaProperty + ",jdbcType=" + introspectedColumn.getJdbcTypeName() + "},"));
                setValueXml.addElement(trimiftest);
            }
        }

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "records"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));

        foreachElement.addElement(new TextElement(" update "
                + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        foreachElement.addElement(setValueXml);
        foreachElement.addElement(new TextElement(" where id = #{item.id,jdbcType=BIGINT}"));

        updateBatchElement.addElement(foreachElement);

        rootElement.addElement(updateBatchElement);
    }

    private Method generateUpdateBatchByPrimaryKey(Method method, IntrospectedTable introspectedTable) {
        Method batchSelective = new Method("updateBatchByPrimaryKeySelective");
        batchSelective.setVisibility(batchSelective.getVisibility());
        batchSelective.setReturnType(FullyQualifiedJavaType.getIntInstance());
        batchSelective.addParameter(new Parameter(
                new FullyQualifiedJavaType("java.util.List<" + introspectedTable.getBaseRecordType() + ">"),
                "records", "@Param(\"records\")"));
        this.context.getCommentGenerator().addGeneralMethodComment(batchSelective, introspectedTable);
        return batchSelective;
    }
}
