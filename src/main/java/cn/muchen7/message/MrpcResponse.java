package cn.muchen7.message;

import java.io.Serializable;

/**
 * rpc 接受返回结果对象
 *
 * @author muchen
 */
public class MrpcResponse implements Serializable {
    private static final long serialVersionUID = 5286256552793952140L;

    /**
     * 错误
     */
    private Throwable error;

    /**
     * 结果
     */
    private Object result;

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
