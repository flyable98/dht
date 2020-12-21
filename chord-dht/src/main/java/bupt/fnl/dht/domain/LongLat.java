package bupt.fnl.dht.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * author:oldmanw
 * create at:2020-10-26 16:45
 */
public class LongLat {
    public String city[] = {"北京", "南京", "武汉","重庆"};
    public double WeiDu[] = { 40.22, 31.33, 30.58, 29.40};
    public double JingDu[] = { 116.23, 118.89, 114.03, 106.54};
    private static final Map<Double, Double> JingWei;
    static
    {
        JingWei = new HashMap<Double, Double>();
        JingWei.put(40.22,  116.23);
        JingWei.put(31.33, 118.89);
        JingWei.put( 30.58,  114.03);
        JingWei.put(29.40, 106.54);
    }

    public LongLat() {
    }

    public LongLat(String[] city, double[] weiDu, double[] jingDu) {
        this.city = city;
        WeiDu = weiDu;
        JingDu = jingDu;
    }

    public String[] getCity() {
        return city;
    }

    public void setCity(String[] city) {
        this.city = city;
    }

    public double[] getWeiDu() {
        return WeiDu;
    }

    public void setWeiDu(double[] weiDu) {
        WeiDu = weiDu;
    }

    public double[] getJingDu() {
        return JingDu;
    }

    public void setJingDu(double[] jingDu) {
        JingDu = jingDu;
    }

    public static Map<Double, Double> getJingWei() {
        return JingWei;
    }
}
