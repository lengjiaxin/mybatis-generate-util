import club.coolcat.common.dao.MybatisGeneratorUtil;

public class Run extends MybatisGeneratorUtil {
    public static void main(String[] args) {
        MybatisGeneratorUtil mybatisGeneratorUtil = new MybatisGeneratorUtil();
        try {
            mybatisGeneratorUtil.generateAll(Run.class);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
