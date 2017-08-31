package cn.miss.function;

/**
 * @Author MissNull
 * @Description: 无参数函数
 * @Date: Created in 2017/8/30.
 */
@FunctionalInterface
public interface NoParamFunction {
    void accept();

    default NoParamFunction andThen(NoParamFunction after) {
        return () -> {
            accept();
            after.accept();
        };
    }
}
