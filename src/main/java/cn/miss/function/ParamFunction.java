package cn.miss.function;

/**
 * @Author MissNull
 * @Description:  多参数函数
 * @Date: Created in 2017/8/29.
 */

@FunctionalInterface
public interface ParamFunction<T, F> {
    void accept( F port,T...param);

    default ParamFunction<T, F> andThen(ParamFunction<? super T, ? super F> consumer) {
        return (F port,T...param) -> {
            accept(port,param);
            consumer.accept(port,param);
        };
    }
}
