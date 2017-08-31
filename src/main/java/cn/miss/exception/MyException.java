package cn.miss.exception;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/31.
 */
public class MyException extends Exception {
    public MyException() {
    }

    public MyException(String message) {
        super(message);
    }

    public MyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyException(Throwable cause) {
        super(cause);
    }

    public MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
