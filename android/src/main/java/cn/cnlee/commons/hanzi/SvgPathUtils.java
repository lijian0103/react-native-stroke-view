package cn.cnlee.commons.hanzi;

import android.graphics.Path;
import android.graphics.PointF;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SvgPathUtils {

    private final static String TAG = SvgPathUtils.class.getSimpleName();
    private String[] svgPath;
    private List<Integer> cmdPosList = new ArrayList<>();
    private Float ratio;
    private Float offset;

    public SvgPathUtils(Float ratio, Float offset) {
        this.ratio = ratio;
        this.offset = offset;
    }

    /***
     * M = moveto(M X,Y) ：将画笔移动到指定的坐标位置
     * L = lineto(L X,Y) ：画直线到指定的坐标位置
     * H = horizontal lineto(H X)：画水平线到指定的X坐标位置
     * V = vertical lineto(V Y)：画垂直线到指定的Y坐标位置
     * C = curveto(C X1,Y1,X2,Y2,ENDX,ENDY)：三次贝赛曲线
     * S = smooth curveto(S X2,Y2,ENDX,ENDY)：平滑曲率
     * Q = quadratic Belzier curve(Q X,Y,ENDX,ENDY)：二次贝赛曲线
     * T = smooth quadratic Belzier curveto(T ENDX,ENDY)：映射
     * A = elliptical Arc(A RX,RY,XROTATION,FLAG1,FLAG2,X,Y)：弧线
     * Z = closepath()：关闭路径
     * @param svgPathStr
     * @return
     */
    public Path parse(String svgPathStr) {
//        Log.e(TAG, svgPathStr);
        if (TextUtils.isEmpty(svgPathStr)) {
            return null;
        }
        this.svgPath = svgPathStr.trim().split(" ");
        Path path = new Path();
        path.setFillType(Path.FillType.WINDING);
        extractCommand();
        //记录最后一个操作点
        PointF lastPoint = new PointF();
        int size = cmdPosList.size();
        for (int i = 0; i < size; i++) {
            Integer index = cmdPosList.get(i);
            String ps[] = getCoordinateDataByPos(i);
            switch (this.svgPath[index].charAt(0)) {
                case 'm':
                case 'M': {
                    lastPoint.set(Float.parseFloat(ps[0]) * ratio, offset - Float.parseFloat(ps[1]) * ratio);
                    path.moveTo(lastPoint.x, lastPoint.y);
                }
                break;
                case 'l':
                case 'L': {
                    lastPoint.set(Float.parseFloat(ps[0]) * ratio, offset - Float.parseFloat(ps[1]) * ratio);
                    path.lineTo(lastPoint.x, lastPoint.y);
                }
                break;
                case 'h':
                case 'H': {//基于上个坐标在水平方向上划线，因此y轴不变
                    lastPoint.set(Float.parseFloat(ps[0]), lastPoint.y);
                    path.lineTo(lastPoint.x, lastPoint.y);
                }
                break;
                case 'v':
                case 'V': {//基于上个坐标在水平方向上划线，因此x轴不变
                    lastPoint.set(lastPoint.x, offset - Float.parseFloat(ps[0]));
                    path.lineTo(lastPoint.x, lastPoint.y);
                }
                break;
                case 'c':
                case 'C': {//3次贝塞尔曲线
                    lastPoint.set(Float.parseFloat(ps[4]) * ratio, offset - Float.parseFloat(ps[5]) * ratio);
                    path.cubicTo(Float.parseFloat(ps[0]) * ratio, offset - Float.parseFloat(ps[1]) * ratio, Float.parseFloat(ps[2]) * ratio, offset - Float.parseFloat(ps[3]) * ratio, Float.parseFloat(ps[4]) * ratio, offset - Float.parseFloat(ps[5]) * ratio);
                }
                break;
                case 's':
                case 'S': {//一般S会跟在C或是S命令后面使用，用前一个点做起始控制点
                    path.cubicTo(lastPoint.x,lastPoint.y, Float.parseFloat(ps[0]), Float.parseFloat(ps[1]), Float.parseFloat(ps[2]), Float.parseFloat(ps[3]));
                    lastPoint.set(Float.parseFloat(ps[2]), Float.parseFloat(ps[3]));
                }
                break;
                case 'q':
                case 'Q': {//二次贝塞尔曲线
                    lastPoint.set(Float.parseFloat(ps[2]) * ratio, offset - Float.parseFloat(ps[3]) * ratio);
                    path.quadTo(Float.parseFloat(ps[0]) * ratio,  offset - Float.parseFloat(ps[1]) * ratio, Float.parseFloat(ps[2]) * ratio, offset -Float.parseFloat(ps[3]) * ratio);
                }
                break;
                case 't':
                case 'T': {//T命令会跟在Q后面使用，用Q的结束点做起始点
                    path.quadTo(lastPoint.x,lastPoint.y, Float.parseFloat(ps[0]), Float.parseFloat(ps[1]));
                    lastPoint.set(Float.parseFloat(ps[0]), Float.parseFloat(ps[1]));
                }
                break;
                case 'a':
                case 'A':{//画弧
                }
                break;
                case 'z':
                case 'Z': {//结束
                    path.close();
                }
                break;
            }
        }
        return path;
    }

    /***
     * 根据命令字母的位置获取相应的坐标数据
     * @param pos
     * @return
     */
    private String[] getCoordinateDataByPos(int pos) {
        int cmdIndex = cmdPosList.get(pos);
        if (pos == cmdPosList.size() - 1) {
            return null;
        }
        int size = cmdPosList.get(pos + 1) - cmdIndex - 1;
        String[] arr =  Arrays.copyOfRange(svgPath, cmdIndex + 1, cmdIndex + size + 1);
        return arr;
    }

    /***
     * 抽取path中命令字母位置
     */
    private void extractCommand() {
        cmdPosList.clear();
        int mIndex = 0;
        while (mIndex < this.svgPath.length) {
            char c = this.svgPath[mIndex].charAt(0);
            if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                cmdPosList.add(mIndex);
            }
            ++mIndex;
        }
    }

}
