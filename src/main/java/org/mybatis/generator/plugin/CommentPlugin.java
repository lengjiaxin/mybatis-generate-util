package org.mybatis.generator.plugin;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.internal.JDBCConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentPlugin extends PluginAdapter {

    private static final String AUTHOR = "admin";

    public CommentPlugin() {
    }

    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String remark = introspectedColumn.getRemarks();
        field.addJavaDocLine("/** " + remark + " */");
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addModelClassComment(topLevelClass, introspectedTable);
        return true;
    }

    private void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String remarks = "";
        String author = this.getProperties().getProperty("admin");
        if (null == author || "".equals(author)) {
            author = System.getProperty("user.name");
        }

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();

        try {
            JDBCConnectionConfiguration jdbcConnectionConfiguration = this.context.getJdbcConnectionConfiguration();
            Connection connection = new JDBCConnectionFactory(jdbcConnectionConfiguration).getConnection();
            ResultSet rs = connection.getMetaData().getTables(table.getIntrospectedCatalog(), table.getIntrospectedSchema(), table.getIntrospectedTableName(), (String[]) null);
            if (null != rs && rs.next()) {
                remarks = rs.getString("REMARKS");
            }

            this.closeConnection(connection, rs);
        } catch (SQLException var8) {
            ;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * " + remarks);
        topLevelClass.addJavaDocLine(" *");
        topLevelClass.addJavaDocLine(" * @author " + author);
        topLevelClass.addJavaDocLine(" * @date " + format.format(new Date()));
        topLevelClass.addJavaDocLine(" *");
        topLevelClass.addJavaDocLine(" */");
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addModelClassComment(topLevelClass, introspectedTable);
        return true;
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    private void closeConnection(Connection connection, ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException var5) {
                ;
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException var4) {
                ;
            }
        }

    }
}
