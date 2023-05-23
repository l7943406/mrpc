package cn.muchen7.utils;

public class CompressibilityUtil extends Number {

    private static volatile double originalDataSize = 0;
    private  static volatile double dataSizeAfterCompress = 0;
    private static volatile double compressibility = 0;

    public synchronized void add(double  originalDataSize,double dataSizeAfterCompress) {
        this.originalDataSize += originalDataSize;
        this.dataSizeAfterCompress += dataSizeAfterCompress;
    }


    @Override
    public synchronized int intValue() {
        return (int) this.doubleValue();
    }

    @Override
    public synchronized long longValue() {
        return (long) this.doubleValue();
    }

    @Override
    public synchronized float floatValue() {
        return (float) this.doubleValue();
    }

    @Override
    public synchronized double doubleValue() {
        if (this.dataSizeAfterCompress <= 0 || this.originalDataSize <= 0){
            return this.compressibility;
        }

        compressibility = this.dataSizeAfterCompress / this.originalDataSize;
        return compressibility;
    }
}
