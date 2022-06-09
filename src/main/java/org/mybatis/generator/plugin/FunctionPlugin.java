package org.mybatis.generator.plugin;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class FunctionPlugin extends PluginAdapter {

    public FunctionPlugin() {
    }

    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addField(topLevelClass, introspectedTable, "functionColumn");
        this.addField(topLevelClass, introspectedTable, "function");
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addMethod(this.generateFunction(method, introspectedTable));
        return true;
    }

    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addMethod(this.generateFunction(method, introspectedTable));
        return true;
    }

    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addMethod(this.generateFunction(method, introspectedTable));
        return true;
    }

    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addMethod(this.generateFunction(method, introspectedTable));
        return true;
    }

    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement sumElement = new XmlElement("select");
        sumElement.addAttribute(new Attribute("id", "functionByExample"));
        sumElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        sumElement.addAttribute(new Attribute("resultType", "java.lang.Object"));
        sumElement.addElement(new TextElement("select ${function}(${functionColumn}) from " + tableName + " <if test=\"_parameter != null\" > <include refid=\"Example_Where_Clause\" /> </if>"));
        parentElement.addElement(sumElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private Method generateFunction(Method method, IntrospectedTable introspectedTable) {
        Method m = new Method("functionByExample");
        m.setVisibility(method.getVisibility());
        m.setReturnType(FullyQualifiedJavaType.getObjectInstance());
        m.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example"));
        this.context.getCommentGenerator().addGeneralMethodComment(m, introspectedTable);
        return m;
    }

    private void addField(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
        CommentGenerator commentGenerator = this.context.getCommentGenerator();
        Field field = new Field();
        field.setVisibility(JavaVisibility.PROTECTED);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName(name);
        field.setInitializationString("null");
        commentGenerator.addFieldComment(field, introspectedTable);
        topLevelClass.addField(field);
        char c = name.charAt(0);
        String camel = Character.toUpperCase(c) + name.substring(1);
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("set" + camel);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), name));
        method.addBodyLine("this." + name + "=" + name + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("get" + camel);
        method.addBodyLine("return " + name + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
    }
}
