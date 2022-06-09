package org.mybatis.generator.plugin;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ExtJsonMysqlPlugin extends PluginAdapter {

    public ExtJsonMysqlPlugin() {
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addExtJson(topLevelClass, introspectedTable, "extJsonSingleSql");
        this.addExtJson(topLevelClass, introspectedTable, "extJsonSql");
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        element.getElements().stream().filter(e -> {
            XmlElement emlE = (XmlElement) e;
            return emlE.getName().equals("where");
        }).findFirst().ifPresent(e -> {
            final String[] param = {"extJsonSingleSql", "extJsonSql"};
            XmlElement where = (XmlElement) e;
            Iterator<Element> iterator = where.getElements().iterator();
            XmlElement trimElement = null;
            while (iterator.hasNext()) {
                XmlElement emlE = (XmlElement) iterator.next();
                if (emlE.getName().equals("foreach")) {
                    if (emlE.getAttributes().stream()
                            .anyMatch(attr -> Objects.equals(attr.getValue(), "example.oredCriteria"))) {
                        param[0] = "example.extJsonSingleSql";
                        param[1] = "example.extJsonSql";
                    }
                    trimElement = new XmlElement("trim");
                    trimElement.addAttribute(new Attribute("prefix", "("));
                    trimElement.addAttribute(new Attribute("suffix", ")"));
                    trimElement.addElement(emlE);
                    iterator.remove();
                }
            }
            if (trimElement != null) {
                where.addElement(trimElement);
            }
            XmlElement extJsonSingleSqlElement = new XmlElement("if");
            extJsonSingleSqlElement.addAttribute(new Attribute("test", param[0] + " != null"));
            extJsonSingleSqlElement.addElement(new TextElement(" ${" + param[0] + "}"));
            where.addElement(extJsonSingleSqlElement);
            XmlElement extJsonSqlElement = new XmlElement("if");
            extJsonSqlElement.addAttribute(new Attribute("test", param[1] + " != null"));
            extJsonSqlElement.addElement(new TextElement(" ${" + param[1] + "}"));
            where.addElement(extJsonSqlElement);
        });
        return super.sqlMapExampleWhereClauseElementGenerated(element, introspectedTable);
    }

    private void addExtJson(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
        CommentGenerator commentGenerator = this.context.getCommentGenerator();
        Field field = new Field();
        field.setVisibility(JavaVisibility.PROTECTED);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName(name);
//        field.setInitializationString("-1");
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

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
