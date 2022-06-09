/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class InsertBatchSelectivePlugin extends PluginAdapter {
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
        addBatchInsertSelectiveXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    public void addBatchInsertSelectiveXml(Document document, IntrospectedTable introspectedTable) {
        XmlElement rootElement = document.getRootElement();

        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        String incrementField = introspectedTable.getTableConfiguration().getProperties().getProperty("incrementField");
        if (incrementField != null) {
            incrementField = incrementField.toUpperCase();
        }

        XmlElement insertBatchElement = new XmlElement("insert");
        insertBatchElement.addAttribute(new Attribute("id", "insertBatchSelective"));
        insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        XmlElement javaPropertyAndDbType = new XmlElement("trim");
        javaPropertyAndDbType.addAttribute(new Attribute("prefix", " ("));
        javaPropertyAndDbType.addAttribute(new Attribute("suffix", ")"));
        javaPropertyAndDbType.addAttribute(new Attribute("suffixOverrides", ","));

        XmlElement trim1Element = new XmlElement("trim");
        trim1Element.addAttribute(new Attribute("prefix", "("));
        trim1Element.addAttribute(new Attribute("suffix", ")"));
        trim1Element.addAttribute(new Attribute("suffixOverrides", ","));

        for (IntrospectedColumn introspectedColumn : columns) {
            String columnName = introspectedColumn.getActualColumnName();
            if (!columnName.toUpperCase().equals(incrementField)) {
                XmlElement iftest = new XmlElement("if");
                iftest.addAttribute(new Attribute("test", "item." + introspectedColumn.getJavaProperty() + " != null"));
                iftest.addElement(new TextElement("`" + columnName + "`,"));
                trim1Element.addElement(iftest);

                XmlElement trimiftest = new XmlElement("if");
                trimiftest.addAttribute(new Attribute("test", "item."
                        + introspectedColumn.getJavaProperty() + " != null"));
                trimiftest.addElement(new TextElement("#{item."
                        + introspectedColumn.getJavaProperty() + ",jdbcType="
                        + introspectedColumn.getJdbcTypeName() + "},"));
                javaPropertyAndDbType.addElement(trimiftest);
            }
        }

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "records"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));

        foreachElement.addElement(new TextElement("insert into "
                + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        foreachElement.addElement(trim1Element);
        foreachElement.addElement(new TextElement(" values "));

        foreachElement.addElement(javaPropertyAndDbType);
        insertBatchElement.addElement(foreachElement);

        rootElement.addElement(insertBatchElement);
    }

    private Method generateInsertBatch(Method method, IntrospectedTable introspectedTable) {
        Method batchSelective = new Method("insertBatchSelective");
        batchSelective.setVisibility(batchSelective.getVisibility());
        batchSelective.setReturnType(FullyQualifiedJavaType.getIntInstance());
        batchSelective.addParameter(new Parameter(
                new FullyQualifiedJavaType("java.util.List<" + introspectedTable.getBaseRecordType() + ">"),
                "records", "@Param(\"records\")"));
        this.context.getCommentGenerator().addGeneralMethodComment(batchSelective, introspectedTable);
        return batchSelective;
    }

}
